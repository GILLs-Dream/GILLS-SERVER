package com.gills_dream.domain.temp.contoller;

import com.gills_dream.global.apiPayload.ApiResponse;
import com.gills_dream.global.apiPayload.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name="임시", description = "임시 테스트 API")
@RestController
@RequiredArgsConstructor
public class TempController {
    @GetMapping("/temp")
    @Operation(summary = "임시 테스트 API")
    public ApiResponse<?> temp() {
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }
}
