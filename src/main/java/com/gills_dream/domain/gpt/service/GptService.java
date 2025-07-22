package com.gills_dream.domain.gpt.service;

import com.gills_dream.domain.plan.enumerate.MoodType;

import java.util.List;

public interface GptService {
    List<MoodType> selectMood(String input);
}
