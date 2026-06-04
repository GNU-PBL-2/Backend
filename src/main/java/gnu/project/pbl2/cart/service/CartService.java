package gnu.project.pbl2.cart.service;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.cart.dto.request.CartItemAddBatchRequest;
import gnu.project.pbl2.cart.dto.request.CartItemAddRequest;
import gnu.project.pbl2.cart.dto.request.CartItemUpdateRequest;
import gnu.project.pbl2.cart.dto.response.CartItemResponse;
import gnu.project.pbl2.cart.entity.CartItem;
import gnu.project.pbl2.cart.repository.CartItemRepository;
import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.user.entity.User;
import gnu.project.pbl2.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public List<CartItemResponse> getItems(final Accessor accessor) {
        return cartItemRepository
            .findAllByMember_IdOrderByCreatedAtAsc(accessor.getUserId())
            .stream()
            .map(CartItemResponse::from)
            .toList();
    }

    @Transactional
    public List<CartItemResponse> addItems(
        final Accessor accessor,
        final CartItemAddBatchRequest request
    ) {
        final User member = userRepository.findById(accessor.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return request.items().stream()
            .map(req -> upsertItem(member, req))
            .map(CartItemResponse::from)
            .toList();
    }

    // 같은 이름의 항목이 이미 있으면 수량만 합산, 없으면 신규 생성
    private CartItem upsertItem(final User member, final CartItemAddRequest req) {
        return cartItemRepository
            .findByMember_IdAndName(member.getId(), req.name())
            .map(existing -> {
                existing.addQuantity(req.quantity());
                return existing;
            })
            .orElseGet(() -> cartItemRepository.save(
                CartItem.create(
                    member,
                    req.ingredientId(),
                    req.name(),
                    req.quantity(),
                    req.unit()
                )
            ));
    }

    @Transactional
    public CartItemResponse updateItem(
        final Long cartItemId,
        final CartItemUpdateRequest request
    ) {
        final CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (request.quantity() != null) {
            item.updateQuantity(request.quantity());
        }
        if (request.isChecked() != null) {
            item.updateChecked(request.isChecked());
        }
        return CartItemResponse.from(item);
    }

    @Transactional
    public void deleteItem(final Long cartItemId) {
        final CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        cartItemRepository.delete(item);
    }

    @Transactional
    public void deleteItems(final List<Long> cartItemIds) {
        cartItemRepository.deleteAllById(cartItemIds);
    }
}
