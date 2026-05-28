package gnu.project.pbl2.fridge.dto.response;

import java.util.List;

/**
 * FastAPI YOLO 서버(/detect-ingredients)로부터 받는 응답 DTO.
 * FastAPI가 반환하는 JSON 구조: {"ingredients": ["토마토", "양파", ...]}
 */
public record YoloApiResponse(
    // YOLO 모델이 감지한 재료 클래스명 목록
    List<String> ingredients
) {
}
