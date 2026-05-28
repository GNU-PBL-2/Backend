package gnu.project.pbl2.fridge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.fridge.dto.request.FridgeCreateRequest;
import gnu.project.pbl2.fridge.dto.request.FridgeUpdateRequest;
import gnu.project.pbl2.fridge.dto.response.DetectedIngredientResponse;
import gnu.project.pbl2.fridge.dto.response.DetectedIngredientResponse.MatchedIngredient;
import gnu.project.pbl2.fridge.dto.response.FridgeResponse;
import gnu.project.pbl2.fridge.service.FridgeService;
import gnu.project.pbl2.fridge.service.YoloService;
import gnu.project.pbl2.support.ControllerTestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

/**
 * FridgeController 슬라이스 테스트.
 * 실제 서비스 빈 없이 MockMvc로 HTTP 요청/응답만 검증한다.
 */
@WebMvcTest(FridgeController.class)
class FridgeControllerTest extends ControllerTestSupport {

    @MockBean private FridgeService fridgeService;
    @MockBean private YoloService yoloService;

    // ── 샘플 응답 ────────────────────────────────────────
    private FridgeResponse sampleFridgeResponse() {
        return new FridgeResponse(
            100L, 10L, "토마토", BigDecimal.valueOf(2), "개", LocalDate.of(2026, 12, 31)
        );
    }

    // ════════════════════════════════════════════════════
    // GET /api/v1/fridge/{memberId}
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("GET /api/v1/fridge/{memberId} — 냉장고 목록 조회")
    class GetFridge {

        @Test
        @DisplayName("냉장고 재료 목록을 200 OK 로 반환한다")
        void 냉장고_목록_조회_200() throws Exception {
            given(fridgeService.getFridgeByMemberId(1L))
                .willReturn(List.of(sampleFridgeResponse()));

            mockMvc.perform(get("/api/v1/fridge/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fridgeId").value(100))
                .andExpect(jsonPath("$[0].ingredientName").value("토마토"))
                .andExpect(jsonPath("$[0].unit").value("개"));
        }

        @Test
        @DisplayName("냉장고가 비어 있으면 빈 배열로 200 OK 를 반환한다")
        void 냉장고_비어있으면_빈배열_200() throws Exception {
            given(fridgeService.getFridgeByMemberId(1L)).willReturn(List.of());

            mockMvc.perform(get("/api/v1/fridge/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ════════════════════════════════════════════════════
    // POST /api/v1/fridge
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("POST /api/v1/fridge — 냉장고 재료 추가")
    class AddIngredient {

        @Test
        @DisplayName("정상 요청이면 201 Created 와 FridgeResponse 를 반환한다")
        void 재료_추가_201() throws Exception {
            given(fridgeService.addIngredient(any(FridgeCreateRequest.class)))
                .willReturn(sampleFridgeResponse());

            String body = """
                {
                    "memberId": 1,
                    "ingredientId": 10,
                    "quantity": 2,
                    "unit": "개",
                    "expiryDate": "2026-12-31"
                }
                """;

            mockMvc.perform(post("/api/v1/fridge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fridgeId").value(100))
                .andExpect(jsonPath("$.ingredientName").value("토마토"));
        }

        @Test
        @DisplayName("memberId 가 없으면 400 Bad Request 를 반환한다")
        void memberId_없으면_400() throws Exception {
            String body = """
                {
                    "ingredientId": 10,
                    "quantity": 2,
                    "unit": "개"
                }
                """;

            mockMvc.perform(post("/api/v1/fridge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("수량이 0 이하면 400 Bad Request 를 반환한다")
        void 수량_0이하_400() throws Exception {
            String body = """
                {
                    "memberId": 1,
                    "ingredientId": 10,
                    "quantity": -1,
                    "unit": "개"
                }
                """;

            mockMvc.perform(post("/api/v1/fridge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }
    }

    // ════════════════════════════════════════════════════
    // PATCH /api/v1/fridge/{fridgeId}
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("PATCH /api/v1/fridge/{fridgeId} — 냉장고 재료 수정")
    class UpdateIngredient {

        @Test
        @DisplayName("정상 수정 요청이면 200 OK 와 FridgeResponse 를 반환한다")
        void 재료_수정_200() throws Exception {
            given(fridgeService.updateIngredient(any(Long.class), any(FridgeUpdateRequest.class)))
                .willReturn(sampleFridgeResponse());

            String body = """
                {
                    "quantity": 5,
                    "unit": "kg"
                }
                """;

            mockMvc.perform(patch("/api/v1/fridge/100")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fridgeId").value(100));
        }

        @Test
        @DisplayName("존재하지 않는 fridgeId 면 서비스에서 던진 예외가 404 로 반환된다")
        void 냉장고_없으면_404() throws Exception {
            given(fridgeService.updateIngredient(any(), any()))
                .willThrow(new BusinessException(ErrorCode.FRIDGE_NOT_FOUND));

            mockMvc.perform(patch("/api/v1/fridge/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isNotFound());
        }
    }

    // ════════════════════════════════════════════════════
    // DELETE /api/v1/fridge/{fridgeId}
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("DELETE /api/v1/fridge/{fridgeId} — 냉장고 재료 삭제")
    class DeleteIngredient {

        @Test
        @DisplayName("정상 삭제 요청이면 204 No Content 를 반환한다")
        void 재료_삭제_204() throws Exception {
            willDoNothing().given(fridgeService).deleteIngredient(100L);

            mockMvc.perform(delete("/api/v1/fridge/100"))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("존재하지 않는 fridgeId 면 서비스에서 던진 예외가 404 로 반환된다")
        void 냉장고_없으면_404() throws Exception {
            willThrow(new BusinessException(ErrorCode.FRIDGE_NOT_FOUND))
                .given(fridgeService).deleteIngredient(999L);

            mockMvc.perform(delete("/api/v1/fridge/999"))
                .andExpect(status().isNotFound());
        }
    }

    // ════════════════════════════════════════════════════
    // POST /api/v1/fridge/detect (YOLO)
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("POST /api/v1/fridge/detect — 카메라 이미지 재료 감지")
    class DetectIngredients {

        @Test
        @DisplayName("이미지 업로드 시 감지된 재료 목록을 200 OK 로 반환한다")
        void 재료_감지_200() throws Exception {
            DetectedIngredientResponse mockResponse = new DetectedIngredientResponse(
                List.of(
                    new MatchedIngredient(65L, "토마토"),
                    new MatchedIngredient(13L, "양파")
                ),
                List.of("avocado")
            );
            given(yoloService.detectIngredients(any())).willReturn(mockResponse);

            MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[100]
            );

            mockMvc.perform(multipart("/api/v1/fridge/detect").file(imageFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matched").isArray())
                .andExpect(jsonPath("$.matched.length()").value(2))
                .andExpect(jsonPath("$.matched[0].ingredientId").value(65))
                .andExpect(jsonPath("$.matched[0].name").value("토마토"))
                .andExpect(jsonPath("$.unmatched[0]").value("avocado"));
        }

        @Test
        @DisplayName("YOLO 감지 실패 시 서비스에서 던진 예외가 500 으로 반환된다")
        void YOLO_실패_500() throws Exception {
            given(yoloService.detectIngredients(any()))
                .willThrow(new BusinessException(ErrorCode.YOLO_DETECT_FAILED));

            MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[100]
            );

            mockMvc.perform(multipart("/api/v1/fridge/detect").file(imageFile))
                .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("이미지 파일 없이 요청하면 400 Bad Request 를 반환한다")
        void 이미지_없음_400() throws Exception {
            // multipart 필드 'image' 누락 → MissingServletRequestPartException → 400
            // GlobalExceptionHandler 에서 명시적으로 400 처리
            mockMvc.perform(multipart("/api/v1/fridge/detect"))
                .andExpect(status().isBadRequest());
        }
    }
}
