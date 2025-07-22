package com.gills_dream.global.kakao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "다중 경유지 길찾기 응답")
public record RouteRes(
        @Schema(description = "경로 요청 ID", example = "0194de353ee97a56a7807b8a505eb4da")
        String trans_id,

        @Schema(description = "경로 정보 리스트, alternatives가 true일 경우 1개 이상 경로 제공")
        List<Route> routes
) {
    @Schema(description = "경로 정보")
    public record Route(
            @Schema(description = "경로 탐색 결과 코드", example = "0")
            int result_code,

            @Schema(description = "경로 탐색 결과 메시지", example = "길찾기 성공")
            String result_msg,

            @Schema(description = "경로 요약 정보")
            Summary summary,

            @Schema(description = "구간별 경로 정보")
            List<Section> sections
    ) {}

    @Schema(description = "경로 요약 정보")
    public record Summary(
            @Schema(description = "출발지 정보")
            Place origin,

            @Schema(description = "목적지 정보")
            Place destination,

            @Schema(description = "경유지 정보 리스트")
            List<Place> waypoints,

            @Schema(description = "경로 탐색 우선순위 옵션", example = "RECOMMEND")
            String priority,

            @Schema(description = "경로를 포함하는 바운딩 박스")
            Bound bound,

            @Schema(description = "요금 정보")
            Fare fare,

            @Schema(description = "전체 거리 (미터)", example = "2193")
            int distance,

            @Schema(description = "전체 소요 시간 (초)", example = "574")
            int duration
    ) {}

    @Schema(description = "출발지, 목적지, 경유지 공통 정보")
    public record Place(
            @Schema(description = "장소 이름", example = "서울역")
            String name,

            @Schema(description = "X 좌표 (경도)", example = "127.11023403583478")
            double x,

            @Schema(description = "Y 좌표 (위도)", example = "37.39434769502827")
            double y
    ) {}

    @Schema(description = "경로 포함 영역 바운딩 박스")
    public record Bound(
            @Schema(description = "최소 X 좌표", example = "127.10873060789754")
            double min_x,

            @Schema(description = "최소 Y 좌표", example = "37.39446252338457")
            double min_y,

            @Schema(description = "최대 X 좌표", example = "127.11400101911747")
            double max_x,

            @Schema(description = "최대 Y 좌표", example = "37.402458630852735")
            double max_y
    ) {}

    @Schema(description = "요금 정보")
    public record Fare(
            @Schema(description = "택시 요금 (원)", example = "6600")
            int taxi,

            @Schema(description = "통행 요금 (원)", example = "0")
            int toll
    ) {}

    @Schema(description = "구간별 경로 정보")
    public record Section(
            @Schema(description = "구간 거리 (미터)", example = "1074")
            int distance,

            @Schema(description = "구간 소요 시간 (초)", example = "280")
            int duration,

            @Schema(description = "구간 포함 영역 바운딩 박스")
            Bound bound,

            @Schema(description = "도로 정보 리스트")
            List<Road> roads,

            @Schema(description = "안내 정보 리스트")
            List<Guide> guides
    ) {}

    @Schema(description = "도로 정보")
    public record Road(
            @Schema(description = "도로명", example = "판교역로")
            String name,

            @Schema(description = "도로 길이 (미터)", example = "364")
            int distance,

            @Schema(description = "예상 이동 시간 (초)", example = "97")
            int duration,

            @Schema(description = "현재 교통 정보 속도 (km/h)", example = "27.0")
            double traffic_speed,

            @Schema(description = "현재 교통 정보 상태", example = "3")
            int traffic_state,

            @Schema(description = "도로 경로 좌표 배열 (X, Y 순서)", example = "[127.10966790676201, 37.394469584427156, 127.10967141980313, 37.39512739646385]")
            List<Double> vertexes
    ) {}

    @Schema(description = "안내 정보")
    public record Guide(
            @Schema(description = "명칭", example = "출발지")
            String name,

            @Schema(description = "X 좌표 (경도)", example = "127.10991634747967")
            double x,

            @Schema(description = "Y 좌표 (위도)", example = "37.39447145478345")
            double y,

            @Schema(description = "이전 안내 지점부터 현재 안내 지점까지 거리 (미터)", example = "0")
            int distance,

            @Schema(description = "이전 안내 지점부터 현재 안내 지점까지 시간 (초)", example = "0")
            int duration,

            @Schema(description = "안내 타입", example = "100")
            int type,

            @Schema(description = "안내 문구", example = "출발지")
            String guidance,

            @Schema(description = "링크된 도로 인덱스", example = "0")
            int road_index
    ) {}
}