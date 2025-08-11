package com.gills_dream.domain.plan.service;

import com.gills_dream.domain.gpt.service.GptService;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.enumerate.Region;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.place.service.PlaceService;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.entity.PlanPlace;
import com.gills_dream.domain.plan.enumerate.CategoryType;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.domain.plan.repository.PlanRepository;
import com.gills_dream.domain.plan.vo.PlanPeriod;
import com.gills_dream.domain.python.dto.PythonResponseDto;
import com.gills_dream.domain.python.service.PythonService;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
    public PlanResponseDto.TemplateResultDto saveCompanion(CustomUserDetails userDetails, Long planId, PlanRequestDto.CompanionDto request) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        plan.updateCompanion(request.companion(), request.party());

        return new PlanResponseDto.TemplateResultDto(plan.getId());
    }

    @Override
    @Transactional
    public PlanResponseDto.TemplateResultDto saveDestination(CustomUserDetails userDetails, Long planId, PlanRequestDto.DestinationDto request) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        List<PlanPlace> planPlaceList = placeService.createList(plan, request.stayPlaceDtoList(), request.travelPlaceDtoList());
        plan.getPlanPlaces().addAll(planPlaceList);

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

        plan.updateRegion(Region.fromDescription(request.getRegion()));

        PythonResponseDto.ProcessResultDto processDto = pythonService.processVideo(request);
        plan.getYoutubeIds().addAll(processDto.getVideoResultDtoList().stream().map(PythonResponseDto.VideoResultDto::getVideoId).toList());
        PlacePair placePair = gptService.filterPlace(processDto, request.getRegion());
        List<PlanPlace> createdPlanPlaceList = placeService.createList(plan, placePair);
        plan.getPlanPlaces().addAll(createdPlanPlaceList);

        return new PlanResponseDto.VideoResultDto(plan.getId(), createdPlanPlaceList.stream().map(i -> i.getPlace().getName()).toList());
    }

    @Override
    @Transactional
    public PlanResponseDto.GenerateResultDto generate(CustomUserDetails userDetails, Long planId) {
        Plan plan = checkMemberAndPlan(userDetails, planId);

        PlanResponseDto.PlanDraftDto dto = gptService.generatePlan(plan);

        if (plan.getTransports().isEmpty()) throw new GeneralException(ErrorStatus.TRANSPORT_NOT_FOUND);
        if (plan.getTransports().getFirst() == TransportType.WALK) generateWalk(plan, dto);
        else if (new HashSet<>(plan.getTransports()).containsAll(Arrays.asList(TransportType.BUS, TransportType.SUBWAY))) generateTransit(plan, dto);
        else if (plan.getTransports().getFirst() == TransportType.BICYCLE) generateBicycle(plan, dto);
        else if (plan.getTransports().getFirst() == TransportType.CAR) generateCar(plan, dto);

        return null;
    }

    private void generateWalk(Plan plan, PlanResponseDto.PlanDraftDto dto) {

    }

    private void generateCar(Plan plan, PlanResponseDto.PlanDraftDto dto) {
    }

    private void generateBicycle(Plan plan, PlanResponseDto.PlanDraftDto dto) {
    }

    private void generateTransit(Plan plan, PlanResponseDto.PlanDraftDto dto) {
    }



    private Plan checkMemberAndPlan(CustomUserDetails userDetails, Long planId) {
        Member member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        return planRepository.findByIdAndMember(planId, member)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));
    }
}
