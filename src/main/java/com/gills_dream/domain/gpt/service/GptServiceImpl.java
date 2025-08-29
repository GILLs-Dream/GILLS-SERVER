package com.gills_dream.domain.gpt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.domain.python.dto.PythonResponseDto;
import com.gills_dream.global.common.properties.PromptProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
    public List<String> filterPlace(PythonResponseDto.ProcessResultDto processDto, String region) {
        List<String> filteredPlaceList = new ArrayList<>();
        try {
            for (PythonResponseDto.VideoResultDto dto: processDto.getVideoResultDtoList()) {
                List<String> newPlaceList;
                String prompt = buildPrompt(dto, promptProperties.getFilterPlace(), region);
                String response = getResponse(prompt);
                System.out.println(response);
                newPlaceList = objectMapper.readValue(response, new TypeReference<>() {});
                filteredPlaceList.addAll(newPlaceList);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to process GPT response", e);
        }

        return filteredPlaceList;
    }

    @Override
    public MapRouteReq createRouteRequest(MapRouteReq routeReq, String transportType, Long count) {
        try {
            // 1) 입력을 JSON 문자열로 직렬화
            String inputJson = objectMapper.writeValueAsString(routeReq);

            // 2) 프롬프트 템플릿 파일 읽고 {{input}} 치환
            String prompt = buildPrompt(inputJson, transportType, promptProperties.getCreateRoute());

            // 3) GPT 호출 → JSON 문자열만 오도록 getResponse에서 정제
            String response = getResponse(prompt);

            // 4) 응답 JSON → MapRouteReq 로 역직렬화
            MapRouteReq reordered = objectMapper.readValue(response, new TypeReference<MapRouteReq>() {});

            // 5) 방어 코드: 실패/이상 시 원본 반환
            if (reordered == null || reordered.locations() == null || reordered.locations().size() < 2) {
                return routeReq;
            }

            return reordered;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create route request from GPT response", e);
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
    private String buildPrompt(String input, String transportType, String promptProperties) throws IOException {
        try {
            Resource resource = new FileSystemResource(promptProperties);
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template
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
