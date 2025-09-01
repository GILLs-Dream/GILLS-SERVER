package com.gills_dream.domain.plan.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum TransportType {
    BUS("버스"),
    CAR("자동차"),
    WALK("도보"),
    SUBWAY("지하철"),
    BICYCLE("자전거");
    private final String description;
    public static final Map<String, List<TransportType>> TRANSPORT_MAP = Map.of (
            "대중교통", List.of(TransportType.BUS, TransportType.SUBWAY),
            "도보", List.of(TransportType.WALK),
            "자전거", List.of(TransportType.BICYCLE),
            "자동차", List.of(TransportType.CAR)
    );

    public static String toCategory(TransportType type) {
        return switch (type) {
            case BUS, SUBWAY -> "대중교통";
            default -> type.getDescription();
        };
    }
}
