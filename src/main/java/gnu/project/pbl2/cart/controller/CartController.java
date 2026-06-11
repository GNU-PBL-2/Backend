package gnu.project.pbl2.cart.controller;

import gnu.project.pbl2.auth.aop.Auth;
import gnu.project.pbl2.auth.aop.OnlyUser;
import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.cart.controller.docs.CartDocs;
import gnu.project.pbl2.cart.dto.request.CartItemAddBatchRequest;
import gnu.project.pbl2.cart.dto.request.CartItemUpdateRequest;
import gnu.project.pbl2.cart.dto.response.CartItemResponse;
import gnu.project.pbl2.cart.service.CartService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/carts")
public class CartController implements CartDocs {

    private final CartService cartService;

    @OnlyUser
    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponse>> getItems(@Auth final Accessor accessor) {
        return ResponseEntity.ok(cartService.getItems(accessor));
    }

    @OnlyUser
    @PostMapping("/items")
    public ResponseEntity<List<CartItemResponse>> addItems(
        @Auth final Accessor accessor,
        @Valid @RequestBody final CartItemAddBatchRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cartService.addItems(accessor, request));
    }

    @OnlyUser
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemResponse> updateItem(
        @PathVariable final Long cartItemId,
        @Valid @RequestBody final CartItemUpdateRequest request,
        @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(cartService.updateItem(cartItemId, request));
    }

    @OnlyUser
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteItem(
        @PathVariable final Long cartItemId,
        @Auth final Accessor accessor
    ) {
        cartService.deleteItem(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @OnlyUser
    @DeleteMapping("/items")
    public ResponseEntity<Void> deleteItems(
        @RequestBody final List<Long> cartItemIds,
        @Auth final Accessor accessor
    ) {
        cartService.deleteItems(cartItemIds);
        return ResponseEntity.noContent().build();
    }
}
