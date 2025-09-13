package com.gills_dream.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.redis.RedisService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = resolveToken(servletRequest);

            if (StringUtils.hasText(accessToken)) {
                // 먼저 validateToken
                if (!tokenProvider.validateToken(accessToken)) {
                    throw new GeneralException(ErrorStatus.INVALID_TOKEN);
                }

                // 블랙리스트 확인
                if (redisService.checkExistsValue(accessToken)) {
                    throw new GeneralException(ErrorStatus.EXPIRED_TOKEN);
                }

                // 인증 등록
                Authentication authentication = tokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(servletRequest, servletResponse);
        } catch (JwtException e) {
            log.error("[JwtAuthenticationFilter] " + e.getMessage());
            writeErrorResponse(servletResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - " + e.getMessage());
        } catch (GeneralException e) {
            log.error("[JwtAuthenticationFilter] " + e.getMessage());
            String errorCode = e.getErrorReasonHttpStatus().getCode();

            int httpCode = switch (errorCode) {
                case "INVALID TOKEN", "EXPIRED TOKEN", "UNSUPPORTED TOKEN", "INVALID SIGNATURE" -> 401;
                default -> 400;
            };
            writeErrorResponse(servletResponse, httpCode, e.getErrorReasonHttpStatus().getMessage());
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && StringUtils.startsWithIgnoreCase(bearerToken,
                "Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void writeErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", message);

        objectMapper.writeValue(response.getWriter(), errorBody);
    }
}

