package gnu.project.pbl2.Fridge.controller;

import gnu.project.pbl2.Fridge.dto.request.FridgeCreateRequest;
import gnu.project.pbl2.Fridge.dto.request.FridgeUpdateRequest;
import gnu.project.pbl2.Fridge.dto.response.FridgeResponse;
import gnu.project.pbl2.Fridge.service.FridgeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fridge")
/**
 * 냉장고 재료 관련 HTTP 요청을 받는 컨트롤러.
 * 클라이언트 요청을 서비스 계층으로 전달하고 결과를 HTTP 응답으로 반환한다.
 */
public class FridgeController {

    // 냉장고 재료 조회/등록/수정/삭제 비즈니스 로직을 담당하는 서비스
    private final FridgeService fridgeService;

    // 특정 회원이 보유한 냉장고 재료 목록을 조회한다.
    @GetMapping("/{memberId}")
    public ResponseEntity<List<FridgeResponse>> getFridge(@PathVariable final Long memberId) {
        return ResponseEntity.ok(fridgeService.getFridgeByMemberId(memberId));
    }

    // 냉장고에 새로운 재료를 추가한다.
    @PostMapping
    public ResponseEntity<FridgeResponse> addIngredient(
        @RequestBody final FridgeCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(fridgeService.addIngredient(request));
    }

    // 기존 냉장고 재료의 수량, 단위, 유통기한을 수정한다.
    @PatchMapping("/{fridgeId}")
    public ResponseEntity<FridgeResponse> updateIngredient(
        @PathVariable final Long fridgeId,
        @RequestBody final FridgeUpdateRequest request
    ) {
        return ResponseEntity.ok(fridgeService.updateIngredient(fridgeId, request));
    }

    // 냉장고 재료 식별자(fridgeId)로 특정 재료를 삭제한다.
    @DeleteMapping("/{fridgeId}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable final Long fridgeId) {
        fridgeService.deleteIngredient(fridgeId);
        return ResponseEntity.noContent().build();
    }
}
