package com.boot.ict05_final_admin.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * FastAPI 기반 PDF 생성 서버와 통신하여 PDF 바이트를 받아오는 클라이언트.
 * <p>
 * 내부적으로 {@link WebClient} 를 사용해 JSON 페이로드를 POST로 전송하고
 * 응답을 {@code application/pdf} 바이트 배열로 수신합니다.
 * 네트워크/서버 오류가 발생하면 빈 바이트 배열({@code new byte[0]})을 반환합니다.
 * <br><br>
 * <b>주의</b>
 * <ul>
 *   <li>동기(blocking) 호출을 위해 내부에서 {@link Mono#block()} 을 사용합니다.
 *       (요청 스레드가 블로킹됩니다.)</li>
 *   <li>요청/응답의 직렬화는 스프링의 기본 ObjectMapper 설정(Jackson)에 의존합니다.</li>
 *   <li>{@code pdf.base-url} 프로퍼티에 FastAPI 서버의 베이스 URL을 설정해야 합니다.
 *       예: {@code http://localhost:8000}</li>
 * </ul>
 *
 * <h3>예시</h3>
 * <pre>{@code
 * Map<String, Object> payload = Map.of("storeName", "토스트랩 강남점", "title", "가맹 계약서", ...);
 * byte[] pdf = pythonPdfClient.generateContractPdf(payload);
 * }</pre>
 */
@Component
@RequiredArgsConstructor
public class PythonPdfClient {

    /** 재사용 가능한 스레드-세이프 HTTP 클라이언트 */
    private final WebClient webClient;

    /**
     * 베이스 URL을 주입받아 {@link WebClient} 를 구성합니다.
     *
     * @param baseUrl FastAPI PDF 서버의 베이스 URL (예: {@code http://localhost:8000})
     */
    public PythonPdfClient(@Value("${pdf.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * 계약서 PDF를 생성합니다.
     * <p>엔드포인트: {@code POST /pdf/contract}</p>
     *
     * @param payload 계약서 생성에 필요한 데이터(JSON 직렬화 가능한 객체)
     * @return 생성된 PDF의 바이트 배열. 실패 시 길이 0의 바이트 배열
     */
    public byte[] generateContractPdf(Object payload) {
        return postPdf("/pdf/contract", payload);
    }

    /**
     * (구성에 따라) 통합 판매/통계 리포트 PDF를 생성합니다.
     * <p>엔드포인트: {@code POST /pdf/sales-report}</p>
     *
     * @param payload 리포트 생성에 필요한 데이터(JSON 직렬화 가능한 객체)
     * @return 생성된 PDF의 바이트 배열. 실패 시 길이 0의 바이트 배열
     */
    public byte[] generateSalesReportPdf(Object payload) {
        return postPdf("/pdf/sales-report", payload);
    }

    /**
     * (구성에 따라) 매장 요약 리포트 PDF를 생성합니다.
     * <p>엔드포인트: {@code POST /pdf/store-summary}</p>
     *
     * @param payload 리포트 생성에 필요한 데이터(JSON 직렬화 가능한 객체)
     * @return 생성된 PDF의 바이트 배열. 실패 시 길이 0의 바이트 배열
     */
    public byte[] generateStoreSummaryPdf(Object payload) {
        return postPdf("/pdf/store-summary", payload);
    }

    /**
     * 공통 POST 헬퍼.
     * <ul>
     *   <li>요청 본문: {@code application/json}</li>
     *   <li>수락 헤더: {@code application/pdf}</li>
     *   <li>응답 본문: PDF 바이트 배열</li>
     * </ul>
     * 오류 발생 시 빈 바이트 배열을 반환합니다.
     *
     * @param path 호출할 엔드포인트 경로(베이스 URL 기준)
     * @param body 전송할 페이로드(JSON 직렬화 가능한 객체)
     * @return PDF 바이트 배열 또는 빈 배열
     */
    private byte[] postPdf(String path, Object body) {
        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PDF)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(byte[].class)
                .onErrorResume(e -> Mono.just(new byte[0])) // 실패 시 빈 PDF 바이트
                .block();
    }
}
