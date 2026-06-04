package gnu.project.pbl2.cart.controller.docs;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.cart.dto.request.CartItemAddBatchRequest;
import gnu.project.pbl2.cart.dto.request.CartItemUpdateRequest;
import gnu.project.pbl2.cart.dto.response.CartItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Cart", description = "장바구니 API")
public interface CartDocs {

    @Operation(summary = "장바구니 목록 조회")
    ResponseEntity<List<CartItemResponse>> getItems(Accessor accessor);

    @Operation(summary = "장바구니 재료 일괄 추가 (같은 이름이면 수량 합산)")
    ResponseEntity<List<CartItemResponse>> addItems(Accessor accessor, CartItemAddBatchRequest request);

    @Operation(summary = "장바구니 항목 수정 (수량 또는 체크 상태)")
    ResponseEntity<CartItemResponse> updateItem(Accessor accessor, Long cartItemId, CartItemUpdateRequest request);

    @Operation(summary = "장바구니 항목 단건 삭제")
    ResponseEntity<Void> deleteItem(Accessor accessor, Long cartItemId);

    @Operation(summary = "장바구니 항목 일괄 삭제")
    ResponseEntity<Void> deleteItems(Accessor accessor, List<Long> cartItemIds);
}
