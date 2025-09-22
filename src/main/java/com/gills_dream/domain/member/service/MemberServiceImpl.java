package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberRequestDto;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.LoginType;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.entity.RoleType;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.repository.PlanRepository;
import com.gills_dream.domain.s3.service.S3Service;
import com.gills_dream.domain.token.service.CustomTokenService;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.common.properties.AdminProperties;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.global.jwt.CustomUserDetails;
import com.gills_dream.global.jwt.TokenProvider;
import com.gills_dream.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final AdminProperties adminProperties;
    private final PlanRepository planRepository;
    private final S3Service s3Service;
    private final CustomTokenService tokenService;

    @Override
    @Transactional
    public MemberResponseDto.TokenPairWithFlagDto kakaoLogin(String kakaoAccessToken) {
        boolean flag = false;

        // 유저에 대한 정보 받기
        MemberResponseDto.KakaoUserInfoDto kakaoUser = kakaoService.getUserInfo(kakaoAccessToken);

        // 우리 서버의 jwt 토큰 발행
        String email = kakaoUser.getKakaoAccount().getEmail();
        if (email == null) throw new GeneralException(ErrorStatus.EMAIL_IS_REQUIRED);

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() ->
                    memberRepository.save(Member.builder()
                            .email(email)
                            .loginType(LoginType.KAKAO)
                            .roleType(decideRole(email))
                            .build())
                );

        if (member.getNickname() == null) flag = true;

        String accessToken = tokenProvider.createAccessToken(member.getEmail());
        String refreshToken = tokenProvider.createRefreshToken(member.getEmail());

        // Redis에 refresh token 저장
        redisService.setValue(member.getEmail(), refreshToken, Duration.ofDays(28).toMillis());

        return new MemberResponseDto.TokenPairWithFlagDto(
                new TokenPair(member, accessToken, refreshToken),
                flag
        );
    }

    @Override
    @Transactional
    public MemberResponseDto.TokenPairWithFlagDto appleLogin(String identityToken) {
        boolean flag = false;
        MemberResponseDto.AppleUserInfoDto appleUser = appleService.getUserInfo(identityToken);
        String sub = appleUser.getSub();

        Member member;
        Optional<Member> existing = memberRepository.findByAppleSub(sub);

        if (existing.isPresent()) {
            member = existing.get();
        } else {
            String email = appleUser.getEmail();
            if (email == null) throw new GeneralException(ErrorStatus.EMAIL_IS_REQUIRED);

            member = memberRepository.save(Member.builder()
                    .email(email)
                    .sub(sub)
                    .loginType(LoginType.APPLE)
                    .roleType(decideRole(email))
                    .build());
        }

        if (member.getNickname() == null) flag = true;

        String accessToken = tokenProvider.createAccessToken(member.getEmail());
        String refreshToken = tokenProvider.createRefreshToken(member.getEmail());

        redisService.setValue(member.getEmail(), refreshToken, Duration.ofDays(28).toMillis());

        return new MemberResponseDto.TokenPairWithFlagDto(
                new TokenPair(member, accessToken, refreshToken),
                flag
        );
    }

    @Override
    @Transactional
    public void delete(String accessToken, Member memberDet) {
        if (memberDet == null) throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);

        Member member = memberRepository.findById(memberDet.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // refreshToken을 redis에서 삭제하고 accessToken을 redis에 저장
        redisService.deleteValue(member.getEmail()); // 재로그인 방지
        redisService.setValue(accessToken, "logout", tokenProvider.getExpirationTime(accessToken)); // AccessToken을 블랙리스트에 저장

        // DB에서 멤버 삭제
        for (Plan plan : member.getPlanList()) {
            plan.updateMember(null);
        }
        planRepository.flush();
        member.getPlanList().clear();
        memberRepository.delete(member);
    }

    @Override
    public MemberResponseDto.MemberInfoDto info(CustomUserDetails userDetails) {
        Member member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        return new MemberResponseDto.MemberInfoDto(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImg()
        );
    }

    @Override
    @Transactional
    public void logout(String accessToken, Member member) {
        if (member == null) throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);

        // 로그아웃 시 refreshToken을 redis에서 삭제하고 accessToken을 redis에 저장
        redisService.deleteValue(member.getEmail()); // 재로그인 방지
        redisService.setValue(accessToken, "logout", tokenProvider.getExpirationTime(accessToken)); // AccessToken을 블랙리스트에 저장
    }


    @Override
    @Transactional
    public MemberResponseDto.ProfileResultDto setProfile(CustomUserDetails userDetails, MemberRequestDto.ProfileDto request, MultipartFile image) {
        Member member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (request.nickname() == null) throw new GeneralException(ErrorStatus.NICKNAME_IS_NECESSARY);
        member.updateNickname(request.nickname());
        if (image != null) {
            if (member.getProfileImg() != null) {
                s3Service.deleteImg(member.getProfileImg());
            }
            String imageUrl = s3Service.uploadImg(member, image);
            member.updateProfileImg(imageUrl);
        }
        if (request.marketingAgreement() != null) member.updateMarketingAgreement(request.marketingAgreement());

        memberRepository.save(member);
        return new MemberResponseDto.ProfileResultDto(
                member.getId(),
                member.getNickname(),
                member.getProfileImg(),
                member.getMarketingAgreement()
        );
    }

    @Override
    @Transactional
    public MemberResponseDto.ProfileResultDto editProfile(CustomUserDetails userDetails, MemberRequestDto.ProfileDto request, MultipartFile image) {
        Member member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (request.nickname() != null) member.updateNickname(request.nickname());
        if (request.marketingAgreement() != null) member.updateMarketingAgreement(request.marketingAgreement());
        if (image != null) {
            if (member.getProfileImg() != null) {
                s3Service.deleteImg(member.getProfileImg());
            }
            String imageUrl = s3Service.uploadImg(member, image);
            member.updateProfileImg(imageUrl);
        }

        memberRepository.save(member);
        return new MemberResponseDto.ProfileResultDto(
                member.getId(),
                member.getNickname(),
                member.getProfileImg(),
                member.getMarketingAgreement()
        );
    }

    @Override
    @Transactional
    public MemberResponseDto.EmailWithTokenDto editEmail(CustomUserDetails userDetails, MemberRequestDto.EmailDto request) {
        log.info(userDetails.getId() + "");
        Member member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        String oldEmail = member.getEmail();
        String newEmail = request.email();

        TokenPair tokenPair;
        boolean flag = false;
        if (request.email() != null && !member.getEmail().equals(request.email())) {
            if (memberRepository.existsByEmail(newEmail)) throw new GeneralException(ErrorStatus.SAME_EMAIL_EXISTS);

            // Member의 email 수정
            member.updateEmail(newEmail);

            // 새로운 이메일에 대한 토큰 발급
            String accessToken = tokenProvider.createAccessToken(newEmail);
            String refreshToken = tokenProvider.createRefreshToken(newEmail);

            // Redis에 refresh token 저장
            redisService.setValue(newEmail, refreshToken, Duration.ofDays(28).toMillis());

            tokenPair = new TokenPair(member, accessToken, refreshToken);
            flag = true;

            // 기존 이메일에 대한 레디스 토큰 삭제
            redisService.deleteValue(oldEmail);
        } else {
            tokenPair = tokenService.reissue(redisService.getValue(oldEmail));
        }

        return new MemberResponseDto.EmailWithTokenDto(
                member.getId(),
                member.getEmail(),
                tokenPair,
                flag
        );
    }

    @Override
    public MemberResponseDto.CheckDto check(String email) {
        return new MemberResponseDto.CheckDto(
                memberRepository.existsByEmail(email)
        );
    }

    private RoleType decideRole(String email) {
        return adminProperties.getEmails().stream()
                .anyMatch(em -> em.equals(email))
                ? RoleType.ADMIN
                : RoleType.USER;
    }
}
