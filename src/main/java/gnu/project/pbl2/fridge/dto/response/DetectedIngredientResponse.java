package gnu.project.pbl2.fridge.dto.response;

import java.util.List;

/**
 * YOLO 감지 결과를 클라이언트에 반환하는 응답 DTO.
 * <p>
 * 카메라 사진에서 YOLO가 감지한 재료 중 DB에 등록된 것은 {@code matched}에,
 * DB에 없는 재료명은 {@code unmatched}에 담아 반환한다.
 * 프론트엔드는 matched 목록을 냉장고 등록 화면에 pre-select 할 수 있다.
 */
public record DetectedIngredientResponse(
    // DB의 ingredient 테이블과 매칭된 재료 목록
    List<MatchedIngredient> matched,
    // YOLO가 감지했으나 DB에 없는 재료명 (참고용)
    List<String> unmatched
) {

    /**
     * DB에 존재하는 재료의 ID와 이름.
     * 프론트엔드에서 냉장고 등록 시 ingredientId를 바로 사용할 수 있다.
     */
    public record MatchedIngredient(
        Long ingredientId,
        String name
    ) {
    }
}
