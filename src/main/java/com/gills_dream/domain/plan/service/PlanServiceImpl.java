package com.gills_dream.domain.plan.service;

import com.gills_dream.domain.gpt.service.GptService;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.domain.place.dto.PlaceAddedDto;
import com.gills_dream.domain.place.dto.PlaceCacheWithContentId;
import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.enumerate.PlaceType;
import com.gills_dream.domain.place.enumerate.Region;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.place.repository.PlaceRepository;
import com.gills_dream.domain.place.service.PlaceCacheService;
import com.gills_dream.domain.place.service.PlaceService;
import com.gills_dream.domain.place.vo.PlacePeriod;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.entity.PlanPlace;
import com.gills_dream.domain.plan.enumerate.CategoryType;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.domain.plan.repository.PlanRepository;
import com.gills_dream.domain.plan.vo.PlanPeriod;
import com.gills_dream.domain.python.dto.PythonResponseDto;
import com.gills_dream.domain.python.service.PythonService;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.jwt.CustomUserDetails;
import com.gills_dream.global.map.tourapi.TourApiClient;
import com.gills_dream.global.map.tourapi.TourPlaceDetailRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static com.gills_dream.domain.place.enumerate.Platform.TOUR;
import static com.gills_dream.domain.plan.enumerate.TransportType.TRANSPORT_MAP;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {
    private final GptService gptService;
    private final PlanRepository planRepository;
    private final MemberRepository memberRepository;
    private final PythonService pythonService;
    private final PlaceService placeService;
    private final PlaceRepository placeRepository;
    private final TourApiClient tourApiClient;

    @Override
    @Transactional
    public PlanResponseDto.MoodResultDto selectAndSummarizeMood(CustomUserDetails userDetails, PlanRequestDto.MoodTextDto request) {
        Member member = null;
        if (userDetails != null) {
            member = memberRepository.findById(userDetails.getMember().getId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        }
        List<MoodType> moodTypeList = gptService.selectMood(request.inputText());
        String summary = gptService.summarizeInput(request.inputText());

        Plan plan = Plan.builder()
                .member(member)
                .moods(moodTypeList)
                .moodSummary(summary)
                .build();
        planRepository.save(plan);
        if (member != null) member.getPlanList().add(plan);

        return new PlanResponseDto.MoodResultDto(
                plan.getId(),
                summary,
                moodTypeList.stream().toList()
        );
    }

    @Override
    @Transactional
    public PlanResponseDto.TemplateResultDto saveDuration(CustomUserDetails userDetails, Long planId, PlanRequestDto.DurationDto request) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        plan.updatePeriod (PlanPeriod.builder()
                .startedAt(request.startDate())
                .finishedAt(request.finishedDate())
                .duration(request.duration())
                .build());

        return new PlanResponseDto.TemplateResultDto(plan.getId());
    }

    @Override
    public PlanResponseDto.TemplateResultDto saveCompanion(CustomUserDetails userDetails, Long planId, PlanRequestDto.CompanionDto request) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        plan.updateCompanion(request.companion(), request.party());

        return new PlanResponseDto.TemplateResultDto(plan.getId());
    }

    @Override
    @Transactional
    public PlanResponseDto.TemplateResultDto saveDestination(CustomUserDetails userDetails, Long planId, PlanRequestDto.DestinationDto request) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        request.tourDtoList().forEach(tourPlaceDto -> {
            TourPlaceDetailRes detail = tourPlaceDto.platform() == TOUR ?
                    tourApiClient.getPlaceDetail(tourPlaceDto.contentId()) : null;
            Place place = Place.builder()
                    .platform(tourPlaceDto.platform())
                    .contentId(tourPlaceDto.contentId())
                    .name(tourPlaceDto.title() != null ? tourPlaceDto.title() : (detail != null ? detail.title() : null))
                    .detailUrl(tourPlaceDto.detailUrl() != null ? tourPlaceDto.detailUrl() : null)
                    .lat(tourPlaceDto.lat() != null ? tourPlaceDto.lat() : (detail != null ? detail.mapy() : null))
                    .lng(tourPlaceDto.lng() != null ? tourPlaceDto.lng() : (detail != null ? detail.mapx() : null))
                    .placeType(PlaceType.TOUR)
                    .images(detail != null ? List.of(detail.firstimage()) : List.of())
                    .build();
            placeRepository.save(place);
            PlanPlace planPlace = PlanPlace.builder()
                    .plan(plan)
                    .place(place)
                    .period(PlacePeriod.builder()
                            .startedAt(tourPlaceDto.date() != null ? LocalDate.parse(tourPlaceDto.date()) : null)
                            .build())
                    .build();
            plan.getPlanPlaces().add(planPlace);
            place.getPlanPlaces().add(planPlace);
        });

        request.accommodationDtoList().forEach(accommodationDto -> {
            TourPlaceDetailRes detail = accommodationDto.platform() == TOUR ?
                    tourApiClient.getPlaceDetail(accommodationDto.contentId()) : null;
            Place place = Place.builder()
                    .platform(accommodationDto.platform())
                    .contentId(accommodationDto.contentId())
                    .name(accommodationDto.title() != null ? accommodationDto.title() : (detail != null ? detail.title() : null))
                    .detailUrl(accommodationDto.detailUrl() != null ? accommodationDto.detailUrl() : null)
                    .lat(accommodationDto.lat() != null ? accommodationDto.lat() : (detail != null ? detail.mapy() : null))
                    .lng(accommodationDto.lng() != null ? accommodationDto.lng() : (detail != null ? detail.mapx() : null))
                    .placeType(PlaceType.ACCOMMODATION)
                    .images(detail != null ? List.of(detail.firstimage()) : List.of())
                    .build();
            placeRepository.save(place);
            PlanPlace planPlace = PlanPlace.builder()
                    .plan(plan)
                    .place(place)
                    .period(
                            PlacePeriod.builder()
                                    .startedAt(accommodationDto.startDate() != null ? LocalDate.parse(accommodationDto.startDate()) : null)
                                    .finishedAt(accommodationDto.endDate() != null ? LocalDate.parse(accommodationDto.endDate()) : null)
                                    .duration(
                                            accommodationDto.startDate() != null && accommodationDto.endDate() != null
                                                    ? Math.toIntExact(
                                                    LocalDate.parse(accommodationDto.endDate()).toEpochDay()
                                                            - LocalDate.parse(accommodationDto.startDate()).toEpochDay()
                                            )
                                                    : null
                                    )
                                    .build()
                    )
                    .build();
            plan.getPlanPlaces().add(planPlace);
            place.getPlanPlaces().add(planPlace);
        });

        return new PlanResponseDto.TemplateResultDto(plan.getId());
    }

    @Override
    @Transactional
    public PlanResponseDto.TemplateResultDto savStyle(CustomUserDetails userDetails, Long planId, PlanRequestDto.StyleDto request) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        String tType = request.transport();
        plan.getTransports().addAll(TRANSPORT_MAP.get(tType));
        plan.getCategories().addAll(request.categoryTypeList().stream().map(CategoryType::fromDescription).toList());

        return new PlanResponseDto.TemplateResultDto(plan.getId());
    }

    @Override
    @Transactional
    public PlanResponseDto.VideoResultDto saveVideo(CustomUserDetails userDetails, Long planId, PlanRequestDto.VideoUrlDto request) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        plan.updateRegion(request.getRegion());

        // 비디오에서 장소 추출
        PythonResponseDto.ProcessResultDto processDto = pythonService.processVideo(request);
        plan.getVideoIds().addAll(processDto.getVideoResultDtoList().stream().map(PythonResponseDto.VideoResultDto::getVideoId).toList());

        // 비디오에서 추출된 장소 필터링
        PlacePair placePair = gptService.filterPlace(processDto, request.getRegion());

        // 비디오에서 추출된 장소를 tour&kakao api에서 검색
        List<PlaceCacheWithContentId> placeCacheList = placeService.searchPlaces(plan, placePair);

        // 검색 내용 바탕으로 redis에 업로드
        List<PlaceAddedDto> placeAddedDtoList = placeService.addToRedis(plan, placeCacheList);
        plan.getContentIds().addAll(placeAddedDtoList.stream().map(PlaceAddedDto::contentId).toList());

        return new PlanResponseDto.VideoResultDto(plan.getId(), placeAddedDtoList);
    }

//    @Override
//    @Transactional
//    public PlanResponseDto.GenerateResultDto generate(CustomUserDetails userDetails, Long planId) {
//        Plan plan = checkMemberAndPlan(userDetails, planId);
//
//        // candidate1: '어디로?'에서 받아서 db에 저장한 장소 정보 -> 필수로 계획에 포함
//        // candidate2: 비디오에서 추출한 장소 정보
//        // candidate3: 숙소 리스트 (순위 or 하드 코딩)
//        List<Place> candidate1 = placeRepository.findAllByPlanId(plan.getId());
//
//        PlanResponseDto.PlanDraftDto dto = gptService.generatePlan(plan);
//        placeService.addList(plan, dto);
//
//        return null;
//    }


    private Plan checkMemberAndPlan(CustomUserDetails userDetails, Long planId) {
        Member member = null;
        if (userDetails != null) {
            member = memberRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        }

        if (member == null) {
            return planRepository.findById(planId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));
        }

        return planRepository.findByIdAndMember(planId, member)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));
    }
}
