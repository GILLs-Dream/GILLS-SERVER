package com.gills_dream.global.jwt;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        Member member = memberRepository.findByName(name).orElseThrow(()->new GeneralException(ErrorStatus.USER_NOT_FOUND));
        return new CustomUserDetails(member);
    }

    public Member findUserByUserDetails(UserDetails userDetails) {
        return memberRepository.findByName(userDetails.getUsername())
                .orElseThrow(()->new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }
}
