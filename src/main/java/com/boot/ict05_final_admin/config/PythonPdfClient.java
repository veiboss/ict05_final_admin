package com.boot.ict05_final_admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;

/**
 * FastAPI 기반 PDF 생성 서버와 통신하여 PDF 바이트를 받아오는 클라이언트.
 * 오류시 더이상 0바이트를 "조용히" 반환하지 않도록 예외를 전파한다.
 */
@Component
@Slf4j
public class PythonPdfClient {

    private final WebClient pdfWebClient;

    @Autowired
    public PythonPdfClient(@Value("${pdf.python.base-url}") String baseUrl) {
        this.pdfWebClient = WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(c -> c.defaultCodecs().maxInMemorySize(64 * 1024 * 1024)) // ✅ 64MB
                                .build()
                )
                .build();
    }

    public byte[] generateContractPdf(Object payload) {
        return postPdfOrThrow("/pdf/contract", payload);
    }

    public byte[] generateOrdersReportPdf(Object payload) {
        return postPdfOrThrow("/pdf/orders", payload);
    }

    public byte[] generateTimeReportPdf(Object payload) {
        return postPdfOrThrow("/pdf/time", payload);
    }
    public byte[] generateKpiReportPdf(Object payload) {
        return postPdfOrThrow("/pdf/kpi-report", payload);
    }

    /**
     * 공통 POST 호출 (에러는 예외 전파)
     */
    private byte[] postPdfOrThrow(String path, Object body) {
        byte[] bytes = pdfWebClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PDF)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(msg -> {
                                    log.error("PDF server error {} {}: {}", resp.statusCode().value(), path, msg);
                                    return Mono.error(new IllegalStateException(
                                            "PDF server error %s: %s".formatted(resp.statusCode(), msg)));
                                })
                )
                .bodyToMono(byte[].class)
                .block();

        int len = (bytes == null ? 0 : bytes.length);
        log.info("PDF fetched from {} bytes={}", path, len);

        if (len == 0) {
            // 0바이트는 실패로 간주해 상위로 명확히 전달
            throw new IllegalStateException("Empty PDF from " + path);
        }
        return bytes;
    }

}
