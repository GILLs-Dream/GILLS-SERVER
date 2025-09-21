package com.gills_dream.domain.plan.service;

import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.repository.PlanRepository;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.jwt.CustomUserDetails;
import com.gills_dream.global.util.HtmlToPdfUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PlanHtmlRenderService {

    private final TemplateEngine templateEngine;
    private final PlanService planService;
    private final HtmlToPdfUtil htmlToPdfUtil;
    private final PlanRepository planRepository;

    public byte[] exportPdf(CustomUserDetails userDetails, Long planId) {

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));
        PlanResponseDto.GenerateResultDto dto = planService.viewPlan(userDetails, planId);
        if (dto == null) throw new IllegalArgumentException("Plan not found: " + planId);

        String html = renderHtml(dto, plan);

        String baseUri;
        try {
            baseUri = new ClassPathResource("static/").getURL().toString();
        } catch (Exception e) {
            baseUri = null;
        }
        return htmlToPdfUtil.convert(html, baseUri);
    }

    public String renderHtml(PlanResponseDto.GenerateResultDto plan, Plan entity) {
        return renderHtml(plan, null, entity);
    }

    public String renderHtml(PlanResponseDto.GenerateResultDto plan, String summaryText, Plan entity) {
        Context ctx = new Context(java.util.Locale.KOREA);
        ctx.setVariable("title", plan.title());
        ctx.setVariable("duration", plan.duration());
        ctx.setVariable("summary", summaryText);

        List<Map<String, Object>> days = new ArrayList<>();
        if (plan.planPerDayDtoList() != null) {
            for (PlanResponseDto.PlanPerDayDto day : plan.planPerDayDtoList()) {
                Map<String, Object> d = new HashMap<>();
                d.put("dayNum", day.dayNum());

                String prevName = null;
                if (day.from() != null) {
                    String fromName = placeNameOnly(day.from());
                    d.put("from", true);
                    d.put("fromText", fromName);
                    d.put("fromImg", safeImg(day.from()));
                    prevName = fromName;
                } else {
                    d.put("from", null);
                }

                List<Map<String, Object>> routes = new ArrayList<>();
                List<Map<String, Object>> places = new ArrayList<>();
                if (day.routeDtoList() != null) {
                    for (PlanResponseDto.RouteDto r : day.routeDtoList()) {
                        String currName = placeNameOnly(r.to());

                        Map<String, Object> row = new HashMap<>();
                        row.put("toText", currName);
                        row.put("infoText", formatDistanceTimeFromMeters(r.distance(), r.time(), entity.getTransportType() == null ? null : entity.getTransportType().getDescription()));
                        routes.add(row);

                        Map<String, Object> p = new HashMap<>();
                        p.put("text", currName);
                        p.put("img", safeImg(r.to()));
                        places.add(p);

                        prevName = currName;
                    }
                }
                d.put("routes", routes.isEmpty() ? null : routes);
                d.put("places", places);
                days.add(d);
            }
        }
        ctx.setVariable("days", days);
        return templateEngine.process("plan", ctx);
    }

    private static String placeNameOnly(PlanResponseDto.PlaceDto p) {
        if (p == null) return "-";
        return (p.name() == null || p.name().isBlank()) ? "-" : p.name();
    }
    private static String safeImg(PlanResponseDto.PlaceDto p) {
        if (p == null) return null;
        String url = p.mainImg();
        return (url == null || url.isBlank()) ? null : url;
    }

    /** 입력: meters, seconds, transport → 출력: "x.x km , y min · 교통수단" */
    private static String formatDistanceTimeFromMeters(Double meters, double seconds, String transport) {
        String dist = (meters == null)
                ? "-"
                : String.format(java.util.Locale.US, "%,.1f km", meters / 1000.0);

        String time = (seconds > 0)
                ? String.format(java.util.Locale.US, "%,d min", Math.round(seconds / 60.0))
                : "-";

        String result;
        if ("-".equals(dist) && "-".equals(time)) {
            result = "-";
        } else if ("-".equals(dist)) {
            result = time;
        } else if ("-".equals(time)) {
            result = dist;
        } else {
            result = dist + " , " + time;
        }

        if (transport != null && !transport.isBlank()) {
            result += " · " + transport;
        }
        return result;
    }
}