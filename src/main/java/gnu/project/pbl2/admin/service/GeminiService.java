package gnu.project.pbl2.admin.service;

import gnu.project.pbl2.admin.dto.GeminiRecipeDto;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final WebClient webClient;

    @Value("${fastapi.base-url}")
    private String fastapiBaseUrl;

    public GeminiRecipeDto extractRecipeFromYoutube(String youtubeUrl) {
        log.info("FastAPI 레시피 분석 요청: {}", youtubeUrl);

        Map<String, String> requestBody = Map.of("youtube_url", youtubeUrl);

        try {
            return webClient.post()
                .uri(fastapiBaseUrl + "/analyze-recipe")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .map(body -> new RuntimeException("FastAPI 오류: " + body)))
                .bodyToMono(GeminiRecipeDto.class)
                .block();

        } catch (Exception e) {
            log.error("FastAPI 호출 실패: {}", e.getMessage());
            throw new RuntimeException("레시피 분석 실패: " + e.getMessage());
        }
    }
}