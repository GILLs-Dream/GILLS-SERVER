package com.gills_dream.domain.temp.contoller;

import com.gills_dream.domain.member.entity.KakaoProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Tag(name="임시", description = "임시 테스트 API")
@Controller
@RequiredArgsConstructor
public class TempController {
    private final KakaoProperties kakaoApi;

    @GetMapping("/login")
    @Operation(summary = "카카오 로그인 테스트")
    public String loginForm(Model model)
    {
        model.addAttribute("kakaoApiKey", kakaoApi.getApiKey());
        model.addAttribute("kakaoRedirectUri", kakaoApi.getRedirectUri());
        return "login";
    }
}
