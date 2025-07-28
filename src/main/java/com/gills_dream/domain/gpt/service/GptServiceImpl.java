package com.gills_dream.domain.gpt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.global.common.properties.PromptProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            String prompt = buildSelectMoodPrompt(input);
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
    public String summarizeMood(String input) {
        String summary;
        try {
            String prompt = buildSummarizeMoodPrompt(input);
            String response = getResponse(prompt);
            System.out.println(response);
            summary = objectMapper.readValue(response, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to process GPT response", e);
        }

        return summary;
    }

    private String buildSelectMoodPrompt(String input) throws IOException {
        try {
            Resource resource = new ClassPathResource(promptProperties.getSelectMood());
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template
                    .replace("{{input}}", input);
        } catch (IOException e) {
            throw new IOException("Failed to read prompt text", e);
        }
    }

    private String buildSummarizeMoodPrompt(String input) throws IOException {
        try {
            Resource resource = new ClassPathResource(promptProperties.getSummarizeInput());
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template
                    .replace("{{input}}", input);
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
