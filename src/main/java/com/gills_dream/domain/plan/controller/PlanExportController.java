package com.gills_dream.domain.plan.controller;

import com.gills_dream.domain.plan.service.PlanHtmlRenderService;
import com.gills_dream.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "여행 PDF 추출")
@RestController
@RequestMapping("/v1/plan")
@RequiredArgsConstructor
public class PlanExportController {
    private final PlanHtmlRenderService planHtmlRenderService;

    @Operation(summary = "여행 계획 PDF 추출")
    @GetMapping("/{planId}/export/pdf")
    public ResponseEntity<ByteArrayResource> exportPdf(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @PathVariable("planId") Long planId) {
        byte[] pdf = planHtmlRenderService.exportPdf(userDetails, planId);
        String filename = "plan-" + planId + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(pdf.length)
                .body(new ByteArrayResource(pdf));
    }
}