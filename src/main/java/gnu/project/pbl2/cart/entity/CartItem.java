package gnu.project.pbl2.cart.entity;

import gnu.project.pbl2.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", nullable = false)
    private User member;

    // 마스터 재료 ID (레시피에서 추가된 경우 존재, 직접 입력 시 null)
    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "is_checked", nullable = false)
    private boolean isChecked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private CartItem(
        final User member,
        final Long ingredientId,
        final String name,
        final Integer quantity,
        final String unit
    ) {
        this.member = member;
        this.ingredientId = ingredientId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.isChecked = false;
        this.createdAt = LocalDateTime.now();
    }

    public static CartItem create(
        final User member,
        final Long ingredientId,
        final String name,
        final Integer quantity,
        final String unit
    ) {
        return new CartItem(member, ingredientId, name, quantity, unit);
    }

    public void addQuantity(final int delta) {
        this.quantity += delta;
    }

    public void updateQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public void updateChecked(final boolean isChecked) {
        this.isChecked = isChecked;
    }
}
