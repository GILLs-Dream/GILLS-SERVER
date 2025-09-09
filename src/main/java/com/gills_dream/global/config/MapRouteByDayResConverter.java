package com.gills_dream.global.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gills_dream.domain.map.dto.MapRouteByDayRes;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter
public class MapRouteByDayResConverter implements AttributeConverter<MapRouteByDayRes, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(MapRouteByDayRes attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }

    @Override
    public MapRouteByDayRes convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return objectMapper.readValue(dbData, MapRouteByDayRes.class);
        } catch (IOException e) {
            throw new RuntimeException("JSON 역직렬화 실패", e);
        }
    }
}
