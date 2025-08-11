package com.gills_dream.domain.gpt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.entity.Plan;
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

    @Override
    public String summarizePlan(Long duration, List<PlanResponseDto.PlanPerDayDto> planPerDayList) {
        return "";
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
