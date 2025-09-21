package com.gills_dream.global.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class HtmlToPdfUtil {
    public byte[] convert(String html, String baseUri) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            html = normalizeNamedEntities(html);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, baseUri);
            builder.toStream(baos);

            builder.useFont(
                    () -> {
                        try {
                            return new ClassPathResource("fonts/NotoSansKR-Regular.ttf").getInputStream();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to load NotoSansKR-Regular.ttf from classpath:/fonts/", e);
                        }
                    },
                    "Noto Sans KR",
                    400,
                    PdfRendererBuilder.FontStyle.NORMAL,
                    true
            );

            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF 변환 실패: " + e.getMessage(), e);
        }
    }

    private static String normalizeNamedEntities(String s) {
        if (s == null) return null;
        return s.replace("&nbsp;", "&#160;")
                .replace("&ensp;", "&#8194;")
                .replace("&emsp;", "&#8195;");
    }
}