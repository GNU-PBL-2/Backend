package gnu.project.pbl2.admin.service;

import gnu.project.pbl2.admin.dto.FastApiJobResponse;
import gnu.project.pbl2.admin.dto.FastApiJobStatus;
import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class GeminiService {

    private final WebClient webClient;
    private final String fastapiBaseUrl;

    public GeminiService(
        final WebClient webClient,
        @Value("${fastapi.base-url}") final String fastapiBaseUrl
    ) {
        this.webClient = webClient;
        this.fastapiBaseUrl = fastapiBaseUrl;
    }

    public String submitRecipeJob(String youtubeUrl) {
        log.info("FastAPI 레시피 분석 작업 제출: {}", youtubeUrl);

        Map<String, String> requestBody = Map.of("youtube_url", youtubeUrl);

        try {
            FastApiJobResponse response = webClient.post()
                .uri(fastapiBaseUrl + "/analyze-recipe")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    res -> res.bodyToMono(String.class)
                        .map(body -> new BusinessException(ErrorCode.RECIPE_IMPORT_FAILED)))
                .bodyToMono(FastApiJobResponse.class)
                .block();

            if (response == null || response.jobId() == null) {
                throw new BusinessException(ErrorCode.RECIPE_IMPORT_FAILED);
            }
            log.info("분석 작업 생성됨: jobId={}", response.jobId());
            return response.jobId();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("FastAPI 작업 제출 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.RECIPE_IMPORT_FAILED);
        }
    }

    public FastApiJobStatus getJobStatus(String jobId) {
        try {
            FastApiJobStatus status = webClient.get()
                .uri(fastapiBaseUrl + "/analyze-recipe/" + jobId)
                .retrieve()
                .onStatus(s -> s.value() == 404,
                    res -> res.bodyToMono(String.class)
                        .map(body -> new BusinessException(ErrorCode.IMPORT_JOB_NOT_FOUND)))
                .onStatus(s -> s.is5xxServerError(),
                    res -> res.bodyToMono(String.class)
                        .map(body -> new BusinessException(ErrorCode.RECIPE_IMPORT_FAILED)))
                .bodyToMono(FastApiJobStatus.class)
                .block();

            if (status == null) {
                throw new BusinessException(ErrorCode.RECIPE_IMPORT_FAILED);
            }
            return status;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("FastAPI 상태 조회 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.RECIPE_IMPORT_FAILED);
        }
    }
}
