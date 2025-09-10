package com.gills_dream.domain.plan.service;

import com.gills_dream.domain.gpt.service.GptService;
import com.gills_dream.domain.map.dto.MapRouteByDayRes;
import com.gills_dream.domain.map.dto.MapRouteRes;
import com.gills_dream.domain.map.service.RouteService;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.domain.place.dto.PlaceAddedDto;
import com.gills_dream.domain.place.dto.PlaceCacheWithContentId;
import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.place.repository.PlaceRepository;
import com.gills_dream.domain.place.service.PlaceService;
import com.gills_dream.domain.plan.dto.PlanListResponseDto;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.enumerate.CategoryType;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.domain.plan.repository.PlanRepository;
import com.gills_dream.domain.plan.vo.PlanPeriod;
import com.gills_dream.domain.plan.vo.PlanSummary;
import com.gills_dream.domain.python.dto.PythonResponseDto;
import com.gills_dream.domain.python.service.PythonService;
import com.gills_dream.domain.s3.service.S3Service;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PlanServiceImpl implements PlanService {
    private final GptService gptService;
    private final PlanRepository planRepository;
    private final MemberRepository memberRepository;
    private final PythonService pythonService;
    private final PlaceService placeService;
    private final PlaceRepository placeRepository;
    private final RouteService routeService;
    private final S3Service s3Service;

    @Override
    @Transactional
    public PlanResponseDto.MoodResultDto selectAndSummarizeMood(CustomUserDetails userDetails, PlanRequestDto.MoodTextDto request) {
        Member member = memberRepository.findById(userDetails.getMember().getId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<MoodType> moodTypeList = gptService.selectMood(request.inputText());
        String summary = gptService.summarizeInput(request.inputText());

        Plan plan = Plan.builder()
                .member(member)
                .moods(moodTypeList)
                .moodSummary(summary)
                .build();
        planRepository.save(plan);
        member.getPlanList().add(plan);

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
    @Transactional
    public PlanResponseDto.TemplateResultDto saveCompanion(CustomUserDetails userDetails, Long planId, PlanRequestDto.CompanionDto request) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        plan.updateCompanion(request.companion(), request.party());

        return new PlanResponseDto.TemplateResultDto(plan.getId());
    }

    @Override
    @Transactional
    public PlanResponseDto.TemplateResultDto saveDestination(CustomUserDetails userDetails, Long planId, PlanRequestDto.DestinationDto request) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        placeService.createList(plan, request);

        return new PlanResponseDto.TemplateResultDto(plan.getId());
    }

    @Override
    @Transactional
    public PlanResponseDto.TemplateResultDto savStyle(CustomUserDetails userDetails, Long planId, PlanRequestDto.StyleDto request) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        plan.updateTransportType(TransportType.fromDescription(request.transport()));
        plan.getCategories().addAll(request.categoryTypeList().stream().map(CategoryType::fromDescription).toList());

        return new PlanResponseDto.TemplateResultDto(plan.getId());
    }

    @Override
    @Transactional
    public PlanResponseDto.VideoResultDto saveVideo(CustomUserDetails userDetails, Long planId, PlanRequestDto.VideoUrlDto request) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        plan.updateRegion(request.getRegion());

        // 비디오에서 장소 추출
        List<PlaceAddedDto> placeAddedDtoList = new ArrayList<>();
        if (!request.getVideoUrlList().isEmpty()) {
            PythonResponseDto.ProcessResultDto processDto = pythonService.processVideo(request);
            plan.getVideoIds().addAll(processDto.getVideoResultDtoList().stream().map(PythonResponseDto.VideoResultDto::getVideoId).toList());

            // 비디오에서 추출된 장소 필터링
            PlacePair placePair = gptService.filterPlace(processDto, request.getRegion());

            // 비디오에서 추출된 장소를 tour&kakao api에서 검색
            List<PlaceCacheWithContentId> placeCacheList = placeService.searchPlaces(plan, placePair);

            // 검색 내용 바탕으로 redis에 업로드
            placeAddedDtoList = placeService.addToRedis(plan, placeCacheList);
        }

        return new PlanResponseDto.VideoResultDto(plan.getId(), placeAddedDtoList);
    }

    @Override
    @Transactional
    public MapRouteByDayRes generate(CustomUserDetails userDetails, Long planId) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        // 여행 생성 (숙소 없는 경우만 gpt한테 숙소 한곳 추천해달라고 하기)
        MapRouteByDayRes generated = routeService.getRoute(planId, plan.getTransportType());

        plan.updateGeneratedRoute(generated);

        return generated;
    }

    @Override
    public PlanResponseDto.GenerateResultDto viewPlan(CustomUserDetails userDetails, Long planId) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        MapRouteByDayRes generatedRoute = plan.getGeneratedRoute();
        List<PlanResponseDto.PlanPerDayDto> planPerDayDtoList = new ArrayList<>();

        if (generatedRoute == null) throw new GeneralException(ErrorStatus.PLAN_NOT_GENERATED);
        for (int i=0; i<generatedRoute.days().size(); i++) {
            if (generatedRoute.days().get(i).routes().isEmpty()) throw new GeneralException(ErrorStatus.PLAN_NOT_GENERATED);
            MapRouteRes routes = generatedRoute.days().get(i).routes().getFirst();

            if (routes.legs().size() <= 1) throw new GeneralException(ErrorStatus.PLAN_NOT_GENERATED);
            MapRouteRes.Leg firstPlace = routes.legs().getFirst();
            log.info("from place: " + firstPlace.startName());
            Place fromPlace = placeRepository.findByNameAndLatAndLngRounded(firstPlace.startName(), firstPlace.startLat(), firstPlace.startLng())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.CANNOT_FIND_PLACE_FROM_DB));

            PlanResponseDto.PlaceDto from = new PlanResponseDto.PlaceDto(fromPlace.getId(), fromPlace.getName(), fromPlace.getImages().isEmpty() ? "" : fromPlace.getImages().getFirst());

            List<PlanResponseDto.RouteDto> routeDtoList = new ArrayList<>();
            for (int j=1; j < routes.legs().size(); j++) { // 하루의 경로
                MapRouteRes.Leg preLeg = routes.legs().get(j-1);
                MapRouteRes.Leg leg = routes.legs().get(j);
                log.info("to place: " + leg.startName());
                Place toPlace = placeRepository.findByNameAndLatAndLngRounded(leg.startName(), leg.startLat(), leg.startLng())
                        .orElseThrow(() -> new GeneralException(ErrorStatus.CANNOT_FIND_PLACE_FROM_DB));
                PlanResponseDto.PlaceDto to = new PlanResponseDto.PlaceDto(toPlace.getId(), toPlace.getName(), toPlace.getImages().isEmpty() ? "" : toPlace.getImages().getFirst());
                PlanResponseDto.RouteDto routeDto = new PlanResponseDto.RouteDto(j, to, preLeg.distance(), preLeg.duration());
                routeDtoList.add(routeDto);
                if (j == (routes.legs().size() - 1)) { // 하루의 마지막 경로
                    log.info("last place: " + leg.endName());
                    Place lastPlace = placeRepository.findByNameAndLatAndLngRounded(leg.endName(), leg.endLat(), leg.endLng())
                            .orElseThrow(() -> new GeneralException(ErrorStatus.CANNOT_FIND_PLACE_FROM_DB));
                    PlanResponseDto.PlaceDto last = new PlanResponseDto.PlaceDto(lastPlace.getId(), lastPlace.getName(), lastPlace.getImages().isEmpty() ? "" : lastPlace.getImages().getFirst());
                    PlanResponseDto.RouteDto lastRoute = new PlanResponseDto.RouteDto(j+1, last, leg.distance(), leg.duration());
                    routeDtoList.add(lastRoute);
                }
            }

            PlanResponseDto.PlanPerDayDto planPerDayDto = new PlanResponseDto.PlanPerDayDto(i+1, from , routeDtoList);
            planPerDayDtoList.add(planPerDayDto);
        }

        return new PlanResponseDto.GenerateResultDto(
                plan.getMoodSummary(),
                planId,
                plan.getPeriod().getDuration(),
                planPerDayDtoList
        );

    }

    @Override
    @Transactional
    public PlanResponseDto.SummaryDto viewPlanSummary(CustomUserDetails userDetails, Long planId) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        String summary = gptService.summarizePlan(plan);

        PlanSummary planSummary = PlanSummary.builder()
                .summary(summary)
                .build();

        plan.updateSummary(planSummary);

        return new PlanResponseDto.SummaryDto(
                plan.getMoodSummary(),
                planSummary.getSummary()
        );
    }
    @Override
    public PlanListResponseDto getTemplateList(CustomUserDetails userDetails) {
        Member member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<Plan> planList = planRepository.findAllByMember(member);
        return PlanListResponseDto.fromEntityList(planList);
    }

    @Override
    @Transactional
    public PlanResponseDto.TemplateResultDto setTitleAndImg(CustomUserDetails userDetails, Long planId, PlanRequestDto.TitleDto request, MultipartFile image) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        if (plan.getSummary() == null) {
            throw new GeneralException(ErrorStatus.PLAN_SUMMARY_NOT_GENERATED);
        }

        if (request.title() == null || request.title().isEmpty()) plan.getSummary().updateTitle(plan.getMoodSummary());
        else plan.getSummary().updateTitle(request.title());

        if (image != null) plan.getSummary().updateMainImg(s3Service.uploadImg(userDetails.getMember(), image));
        else plan.getSummary().updateMainImg("");

        return new PlanResponseDto.TemplateResultDto(plan.getId());
    }

    private Plan checkMemberAndPlan(CustomUserDetails userDetails, Long planId) {
        Member member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        return planRepository.findByIdAndMember(planId, member)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));
    }
}
