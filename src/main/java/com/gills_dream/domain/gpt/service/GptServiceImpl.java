package com.gills_dream.domain.gpt.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gills_dream.domain.gpt.dto.GptResponseDto;
import com.gills_dream.domain.map.dto.MapRouteByDayRes;
import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.domain.python.dto.PythonResponseDto;
import com.gills_dream.global.common.properties.PromptProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gills_dream.global.apiPayload.code.status.ErrorStatus.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GptServiceImpl implements GptService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final PromptProperties promptProperties;

    @Override
    public List<MoodType> selectMood(String input) {
        List<MoodType> moodTypeList;
        try {
            String prompt = buildPrompt(input, promptProperties.getSelectMood());
            String response = getResponse(prompt);
            System.out.println(response);
            List<String> moodList = objectMapper.readValue(response, new TypeReference<>() {});
            moodTypeList = moodList.stream().map(MoodType::valueOf).toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to process GPT response", e);
        }

        return moodTypeList;
    }

    @Override
    public String summarizeInput(String input) {
        String summary;
        try {
            String prompt = buildPrompt(input, promptProperties.getSummarizeInput());
            String response = getResponse(prompt);
            System.out.println(response);
            summary = objectMapper.readValue(response, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to process GPT response", e);
        }

        return summary;
    }

    @Override
    public String summarizePlan(Plan plan) {
        String summary;
        try {
            String prompt = buildPrompt(plan, promptProperties.getSummarizePlan());
            String response = getResponse(prompt);
            System.out.println(response);
            summary = objectMapper.readValue(response, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to process GPT response", e);
        }
        return summary;
    }

    @Override
    public PlacePair filterPlace(PythonResponseDto.ProcessResultDto processDto, String region) {
        PlacePair placePair = new PlacePair(new ArrayList<>(), new ArrayList<>());
        try {
            for (PythonResponseDto.VideoResultDto dto: processDto.getVideoResultDtoList()) {
                String prompt = buildPrompt(dto, promptProperties.getFilterPlace(), region);
                String response = getResponse(prompt);
                System.out.println(response);
                placePair = objectMapper.readValue(response, new TypeReference<PlacePair>() {});
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to process GPT response", e);
        }

        return placePair;
    }

    private String buildPrompt(Plan plan, String promptProperties) throws IOException {
        try {
            Resource resource = new FileSystemResource(promptProperties);
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template
                    .replace("{{region}}", plan.getRegion())
                    .replace("{{duration}}", plan.getPeriod().getDuration().toString())
                    .replace("{{party}}", plan.getParty().toString())
                    .replace("{{teamName}}", plan.getTeamName())
                    .replace("{{categories}}", plan.getCategories().toString())
                    .replace("{{moods}}", plan.getMoods().toString())
                    .replace("{{moodSummary}}", plan.getMooodSummary())
                    .replace("{{generatedRoute}}", plan.getGeneratedRoute() == null ? "" : plan.getGeneratedRoute().toString());
        } catch (IOException e) {
            throw new IOException("Failed to read prompt text", e);
        }
    }

    @Override
    public MapRouteByDayRes createRouteRequest(MapRouteReq routeReq, String transportType, Long count) {
        try {
            // 1) 입력을 JSON 문자열로 직렬화
            String inputJson = objectMapper.writeValueAsString(routeReq);

            // 2) 프롬프트 템플릿 파일 읽고 {{input}} 치환
            String prompt = buildPrompt(routeReq.region(), String.valueOf(routeReq.planPeriod()), inputJson, transportType, promptProperties.getCreateRoute());

            log.info("build prompt completed");
            // 3) GPT 호출 → JSON 문자열만 오도록 getResponse에서 정제
            String response = getResponse(prompt);

            log.info("GPT 응답: {}", response);

            // 4) 응답 JSON → MapRouteByDayRes 로 역직렬화
            MapRouteByDayRes byDay = objectMapper.readValue(
                    response,
                    new com.fasterxml.jackson.core.type.TypeReference<MapRouteByDayRes>() {}
            );

            // 5) 방어 코드: 실패/이상 시 원본을 INDEXED 모드 1일치로 감싸서 반환
            if (byDay == null || byDay.days() == null || byDay.days().isEmpty()) {
                return fallbackIndexed(routeReq);
            }

            return byDay;

        } catch (IOException e) {
            // 파싱 실패 등도 동일한 폴백 사용
            return fallbackIndexed(routeReq);
        }
    }

    /** 원본을 INDEXED 모드 1일치로 감싸는 폴백 */
    private MapRouteByDayRes fallbackIndexed(MapRouteReq routeReq) {
        return MapRouteByDayRes.builder()
                .mode(MapRouteByDayRes.Mode.INDEXED)
                .days(java.util.List.of(
                        MapRouteByDayRes.Day.builder()
                                .index(1)
                                .date(null)
                                .locations(routeReq.locations())
                                .build()
                ))
                .build();
    }

    private String buildPrompt(String input, String promptProperties) throws IOException {
        try {
            Resource resource = new FileSystemResource(promptProperties);
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template
                    .replace("{{input}}", input);
        } catch (IOException e) {
            throw new IOException("Failed to read prompt text", e);
        }
    }
    private String buildPrompt(String region, String period, String input, String transportType, String promptProperties) throws IOException {
        try {
            Resource resource = new FileSystemResource(promptProperties);
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template
                    .replace("{{region}}", region)
                    .replace("{{period}}", period)
                    .replace("{{input}}", input)
                    .replace("{{transportation}}", transportType);
        } catch (IOException e) {
            throw new IOException("Failed to read prompt text", e);
        }
    }

    private String buildPrompt(PythonResponseDto.VideoResultDto dto, String promptProperties, String region) throws IOException {
        try {
            Resource resource = new FileSystemResource(promptProperties);
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template
                    .replace("{{region}}", region)
                    .replace("{{title}}", dto.getTitle())
                    .replace("{{description}}", dto.getDescription())
                    .replace("{{place-list}}", String.join(", ", dto.getPlaceList()));
        } catch (IOException e) {
            throw new IOException("Failed to read prompt text", e);
        }
    }

    private String getResponse(String prompt) throws IOException {
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 응답 정제
        return response
                .replaceAll("(?s)```json", "")
                .replaceAll("```", "")
                .trim();
    }
}
