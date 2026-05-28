package gnu.project.pbl2.fridge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.fridge.dto.response.DetectedIngredientResponse;
import gnu.project.pbl2.fridge.entity.Ingredient;
import gnu.project.pbl2.fridge.repository.IngredientRepository;
import java.io.IOException;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * YoloService 단위 테스트.
 *
 * <p>FastAPI 서버를 실제로 실행하지 않고 {@link MockWebServer}로 HTTP 응답을 시뮬레이션한다.
 * 핵심 검증 포인트:
 * <ul>
 *   <li>이미지 Content-Type 검증 (image/* 아닌 경우 거부)</li>
 *   <li>FastAPI 응답을 DB 재료와 매칭하는 로직</li>
 *   <li>FastAPI 서버 오류 시 BusinessException 발환</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class YoloServiceTest {

    private MockWebServer mockWebServer;
    private YoloService yoloService;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientNameMapper ingredientNameMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // MockWebServer 포트로 WebClient 생성 — fastapiBaseUrl 로 주입
        String baseUrl = String.format("http://localhost:%d", mockWebServer.getPort());
        WebClient webClient = WebClient.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();

        yoloService = new YoloService(webClient, ingredientRepository, ingredientNameMapper, baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    // ── 헬퍼 ────────────────────────────────────────────
    private MockMultipartFile jpegImage() {
        return new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[100]);
    }

    private MockMultipartFile pngImage() {
        return new MockMultipartFile("image", "test.png", "image/png", new byte[200]);
    }

    private void enqueueYoloResponse(String... ingredientNames) {
        String body = "[\"" + String.join("\",\"", ingredientNames) + "\"]";
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"ingredients\":" + body + "}")
            .addHeader("Content-Type", "application/json"));
    }

    private void enqueueEmptyYoloResponse() {
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"ingredients\":[]}")
            .addHeader("Content-Type", "application/json"));
    }

    private Ingredient fakeIngredient(Long id, String name) {
        Ingredient ing = mock(Ingredient.class);
        given(ing.getId()).willReturn(id);
        given(ing.getName()).willReturn(name);
        return ing;
    }

    // ════════════════════════════════════════════════════
    // 이미지 유효성 검증
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("이미지 유효성 검증")
    class ValidateImage {

        @Test
        @DisplayName("image/* 가 아닌 Content-Type 이면 YOLO_INVALID_IMAGE 예외가 발생한다")
        void 비이미지_파일_거부() {
            MockMultipartFile textFile = new MockMultipartFile(
                "image", "file.txt", "text/plain", "hello".getBytes()
            );

            assertThatThrownBy(() -> yoloService.detectIngredients(textFile))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.YOLO_INVALID_IMAGE);
        }

        @Test
        @DisplayName("Content-Type 이 null 이면 YOLO_INVALID_IMAGE 예외가 발생한다")
        void null_ContentType_거부() {
            MockMultipartFile noType = new MockMultipartFile(
                "image", "file", null, new byte[10]
            );

            assertThatThrownBy(() -> yoloService.detectIngredients(noType))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.YOLO_INVALID_IMAGE);
        }

        @Test
        @DisplayName("image/jpeg 는 유효한 Content-Type 이다")
        void jpeg_이미지_통과() {
            enqueueEmptyYoloResponse();

            DetectedIngredientResponse result = yoloService.detectIngredients(jpegImage());

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("image/png 는 유효한 Content-Type 이다")
        void png_이미지_통과() {
            enqueueEmptyYoloResponse();

            DetectedIngredientResponse result = yoloService.detectIngredients(pngImage());

            assertThat(result).isNotNull();
        }
    }

    // ════════════════════════════════════════════════════
    // DB 매칭 로직
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("DB 매칭 로직")
    class MatchWithDatabase {

        @Test
        @DisplayName("감지된 재료가 모두 DB에 있으면 matched 목록에 전부 담긴다")
        void 전체_매칭_성공() {
            Ingredient tomato = fakeIngredient(65L, "토마토");
            Ingredient onion  = fakeIngredient(13L, "양파");
            enqueueYoloResponse("tomato", "onion");
            given(ingredientNameMapper.toKorean("tomato")).willReturn(Optional.of("토마토"));
            given(ingredientNameMapper.toKorean("onion")).willReturn(Optional.of("양파"));
            given(ingredientRepository.findByName("토마토")).willReturn(Optional.of(tomato));
            given(ingredientRepository.findByName("양파")).willReturn(Optional.of(onion));

            DetectedIngredientResponse result = yoloService.detectIngredients(jpegImage());

            assertThat(result.matched()).hasSize(2);
            assertThat(result.unmatched()).isEmpty();
            assertThat(result.matched())
                .extracting(DetectedIngredientResponse.MatchedIngredient::name)
                .containsExactlyInAnyOrder("토마토", "양파");
        }

        @Test
        @DisplayName("매퍼에 없는 클래스명은 DB 조회 없이 unmatched에 담긴다")
        void 일부_미매칭() {
            Ingredient tomato = fakeIngredient(65L, "토마토");
            enqueueYoloResponse("tomato", "avocado");
            given(ingredientNameMapper.toKorean("tomato")).willReturn(Optional.of("토마토"));
            // avocado: 매퍼 미등록 → mock 기본값 Optional.empty() → unmatched
            given(ingredientRepository.findByName("토마토")).willReturn(Optional.of(tomato));

            DetectedIngredientResponse result = yoloService.detectIngredients(jpegImage());

            assertThat(result.matched()).hasSize(1);
            assertThat(result.matched().get(0).ingredientId()).isEqualTo(65L);
            assertThat(result.unmatched()).containsExactly("avocado");
        }

        @Test
        @DisplayName("매퍼에 없는 클래스명만 감지되면 matched는 비어있고 unmatched에 모두 담긴다")
        void 전체_미매칭() {
            enqueueYoloResponse("mango", "avocado");
            // 두 클래스 모두 매퍼 미등록 → mock 기본값 Optional.empty() → 전부 unmatched

            DetectedIngredientResponse result = yoloService.detectIngredients(jpegImage());

            assertThat(result.matched()).isEmpty();
            assertThat(result.unmatched()).containsExactlyInAnyOrder("mango", "avocado");
        }

        @Test
        @DisplayName("YOLO 가 아무것도 감지하지 않으면 matched, unmatched 모두 비어있다")
        void YOLO_감지_없음() {
            enqueueEmptyYoloResponse();

            DetectedIngredientResponse result = yoloService.detectIngredients(jpegImage());

            assertThat(result.matched()).isEmpty();
            assertThat(result.unmatched()).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════
    // FastAPI 서버 오류 처리
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("FastAPI 서버 오류 처리")
    class FastApiErrorHandling {

        @Test
        @DisplayName("FastAPI 가 500 에러를 반환하면 YOLO_DETECT_FAILED 예외가 발생한다")
        void FastAPI_500_에러() {
            mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));

            assertThatThrownBy(() -> yoloService.detectIngredients(jpegImage()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.YOLO_DETECT_FAILED);
        }

        @Test
        @DisplayName("FastAPI 가 400 에러를 반환하면 YOLO_DETECT_FAILED 예외가 발생한다")
        void FastAPI_400_에러() {
            mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody("Bad Request"));

            assertThatThrownBy(() -> yoloService.detectIngredients(jpegImage()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.YOLO_DETECT_FAILED);
        }

        @Test
        @DisplayName("FastAPI 에 실제로 multipart/form-data 요청이 전달된다")
        void FastAPI_multipart_요청_전달() throws InterruptedException {
            enqueueEmptyYoloResponse();

            yoloService.detectIngredients(jpegImage());

            RecordedRequest recorded = mockWebServer.takeRequest();
            assertThat(recorded.getPath()).isEqualTo("/detect-ingredients");
            assertThat(recorded.getMethod()).isEqualTo("POST");
            assertThat(recorded.getHeader("Content-Type")).contains("multipart/form-data");
        }
    }
}
