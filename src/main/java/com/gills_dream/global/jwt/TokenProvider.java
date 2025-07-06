package com.gills_dream.global.jwt;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.redis.RedisService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenProvider implements InitializingBean {

    private Key key;
    private final CustomUserDetailsServiceImpl customUserDetailsService;
    private final MemberRepository memberRepository;
    private final RedisService redisService;

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }
    }

    public String createAccessToken(String email) {
        Date validity = new Date(System.currentTimeMillis() + Duration.ofDays(1).toMillis());
        String role = getUserRoleByUsername(email);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public String createRefreshToken(String email) {
        Date validity = new Date(System.currentTimeMillis() + Duration.ofDays(28).toMillis());

        return Jwts.builder()
                .setSubject(email)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public String getUserRoleByUsername(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        return member.getRoleType().name();
    }

    public Long getExpirationTime(String token) {
        return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration().getTime();
    }

    public Authentication getAuthentication(String token) {
        String email = getUsernameFromToken(token);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);

            if (redisService.checkExistsValue(token)) {
                return false; // Redis에 이 토큰이 블랙리스트(“logout”)로 등록돼 있는지 확인
            }

            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new GeneralException(ErrorStatus.INVALID_SIGNATURE);
        } catch (ExpiredJwtException e) {
            throw new GeneralException(ErrorStatus.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new GeneralException(ErrorStatus.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }
    }

    @Transactional
    public TokenPair reissue(Member member, String refreshToken) {
        String accessToken = createAccessToken(member.getEmail());

        if (getExpirationTime(refreshToken) <= getExpirationTime(accessToken)) {
            refreshToken = createRefreshToken(member.getEmail());
            redisService.setValue(member.getEmail(), refreshToken, Duration.ofDays(28).toMillis());
        }

        return new TokenPair(member, accessToken, refreshToken);
    }
}
