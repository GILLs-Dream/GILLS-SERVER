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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    public Boolean checkRegion(String region) {
        try {
            String prompt = buildRegionPrompt(region, promptProperties.getCheckRegion());
            System.out.println("GPT PROMPT => [" + prompt + "]");

            String response = getResponse(prompt);
            System.out.println("GPT RAW => [" + response + "]");

            Boolean parsed = parseStrictBoolean(response);
            if (parsed != null) return parsed;

            // 형식 위반 시 명시적으로 실패 처리 (상위에서 INVALID_REGION 변환)
            throw new RuntimeException("Non-boolean output: " + String.valueOf(response));
        } catch (IOException e) {
            throw new RuntimeException("Failed to process GPT response", e);
        }
    }

    private static Boolean parseStrictBoolean(String raw) {
        if (raw == null) return null;

        // 1) 1차 정규화
        String s = raw.replace("```", "")      // 코드펜스 제거
                .replace("`", "")        // 단일 백틱 제거
                .replaceAll("[\\[\\]\"]", "") // [, ], " 제거
                .trim()
                .toLowerCase();

        // 2) 여러 줄이면 첫 줄만
        int nl = s.indexOf('\n');
        if (nl >= 0) s = s.substring(0, nl).trim();

        // 3) 완전 일치만 허용
        if ("true".equals(s))  return Boolean.TRUE;
        if ("false".equals(s)) return Boolean.FALSE;

        // 4) 마지막 방어선: 원문에서 true/false '단어'의 마지막 등장 채택
        Matcher m = Pattern.compile("\\b(true|false)\\b", Pattern.CASE_INSENSITIVE).matcher(raw);
        String last = null;
        while (m.find()) last = m.group(1).toLowerCase();
        if ("true".equals(last))  return Boolean.TRUE;
        if ("false".equals(last)) return Boolean.FALSE;

        return null; // 결정 불가
    }

    /**
     * 프롬프트 템플릿의 {{region}} 자리 치환.
     * template이 null이어도 NPE 안 나도록 방어.
     */
    private String buildRegionPrompt(String region, String templatePath) throws IOException {
        try {
            Resource resource = new FileSystemResource(templatePath);
            String templateContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return templateContent.replace("{{region}}", region == null ? "" : region);
        } catch (IOException e) {
            throw new IOException("Failed to read prompt text", e);
        }
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
                    .replace("{{teamName}}", plan.getTeamName()==null ? "" : plan.getTeamName())
                    .replace("{{categories}}", plan.getCategories().toString())
                    .replace("{{moods}}", plan.getMoods().toString())
                    .replace("{{moodSummary}}", plan.getMoodSummary())
                    .replace("{{generatedRoute}}", plan.getGeneratedRoute() == null ? "" : plan.getGeneratedRoute().toString());
        } catch (IOException e) {
            throw new IOException("Failed to read prompt text", e);
        }
    }

    @Override
    public MapRouteByDayRes createRouteRequest(
            MapRouteReq routeReq,
            String transportType,
            Long count,
            List<String> categories,
            String moodSummary
    ) {
        try {
            String inputJson = objectMapper.writeValueAsString(routeReq);

            String prompt = buildPrompt(
                    routeReq.region(),
                    String.valueOf(routeReq.planPeriod()),
                    inputJson,
                    transportType,
                    categories,
                    moodSummary,
                    promptProperties.getCreateRoute()
            );

            log.info("build prompt completed");
            String response = getResponse(prompt);
            log.info("GPT 응답: {}", response);

            // 거부/비JSON 방어
            if (response == null || response.isBlank()
                    || response.toLowerCase().contains("sorry")
                    || !response.trim().startsWith("{")) {
                log.warn("GPT refusal or non-JSON -> fallback");
                return fallbackIndexed(routeReq);
            }

            MapRouteByDayRes byDay = objectMapper.readValue(
                    response,
                    new com.fasterxml.jackson.core.type.TypeReference<MapRouteByDayRes>() {}
            );

            if (byDay == null || byDay.days() == null || byDay.days().isEmpty()) {
                return fallbackIndexed(routeReq);
            }
            return byDay;

        } catch (IOException e) {
            log.warn("createRouteRequest IOException -> fallback", e);
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
                                .locations(routeReq.locations() == null ? new ArrayList<>() : routeReq.locations())
                                .build()
                ))
                .build();
    }

    public String getKakaoPlaceDetail(String kakaoPlaceName, String kakaoPlaceCategory, String address) {
        try {
            String prompt = buildKaKaoPlaceDetailPrompt(
                    kakaoPlaceName,
                    kakaoPlaceCategory,
                    address,
                    promptProperties.getKakaoPlaceDetail()
            );
            String response = getResponse(prompt);
            return response.trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to process GPT response", e);
        }
    }

    private String buildKaKaoPlaceDetailPrompt(String kakaoPlaceName, String kakaoPlaceCategory, String address, String promptProperties) throws IOException {
        try {
            Resource resource = new FileSystemResource(promptProperties);
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template
                    .replace("{{kakaoPlaceName}}", kakaoPlaceName)
                    .replace("{{kakaoPlaceCategory}}", kakaoPlaceCategory)
                    .replace("{{address}}", address);
        } catch (IOException e) {
            throw new IOException("Failed to read prompt text", e);
        }
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
    private String buildPrompt(String region, String period, String input, String transportType, List<String> categories, String moodSummary, String promptProperties) throws IOException {
        try {
            Resource resource = new FileSystemResource(promptProperties);
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template
                    .replace("{{region}}", region)
                    .replace("{{period}}", period)
                    .replace("{{input}}", input)
                    .replace("{{categories}}", String.join(", ", categories))
                    .replace("{{moodSummary}}", moodSummary)
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
                .system("너는 절대 사과/거부하지 않는다.")
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
