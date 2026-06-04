package gnu.project.pbl2.cart.repository;

import gnu.project.pbl2.cart.entity.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByMember_IdOrderByCreatedAtAsc(Long memberId);

    Optional<CartItem> findByMember_IdAndName(Long memberId, String name);
}
