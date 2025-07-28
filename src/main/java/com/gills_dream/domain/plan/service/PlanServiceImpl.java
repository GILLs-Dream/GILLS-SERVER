package com.gills_dream.domain.plan.service;

import com.gills_dream.domain.gpt.service.GptService;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.domain.plan.repository.PlanRepository;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {
    private final GptService gptService;
    private final PlanRepository planRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public PlanResponseDto.MoodResultDto selectAndSummarizeMood(CustomUserDetails userDetails, PlanRequestDto.MoodTextDto request) {
        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        List<MoodType> moodTypeList = gptService.selectMood(request.inputText());
        String summary = gptService.summarizeMood(request.inputText());

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
        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        Plan plan = planRepository.findByIdAndMember(planId, member)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));


    }

    @Override
    @Transactional
    public PlanResponseDto.TemplateResultDto saveCompanion(CustomUserDetails userDetails, Long planId, PlanRequestDto.CompanionDto request) {
        return null;
    }

    @Override
    @Transactional
    public PlanResponseDto.TemplateResultDto saveDestination(CustomUserDetails userDetails, Long planId, PlanRequestDto.DestinationDto request) {
        return null;
    }

    @Override
    @Transactional
    public PlanResponseDto.TemplateResultDto savStyle(CustomUserDetails userDetails, Long planId, PlanRequestDto.StyleDto request) {
        return null;
    }
}
