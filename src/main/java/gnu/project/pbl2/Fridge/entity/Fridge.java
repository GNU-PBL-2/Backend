package gnu.project.pbl2.Fridge.entity;

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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fridge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 회원 냉장고에 들어 있는 재료 한 건을 나타내는 엔티티.
 * 어떤 회원이 어떤 재료를 얼마만큼 보유하는지와 유통기한을 저장한다.
 */
public class Fridge {

    // 냉장고 재료 레코드 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fridge_id")
    private Long fridgeId;

    // 이 재료를 보유한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", nullable = false)
    private User member;

    // 냉장고에 담긴 재료 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    // 보유 수량
    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    // 수량 단위
    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    // 유통기한
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // 냉장고에 등록된 시각
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    // 정적 팩토리 메서드를 통해서만 생성하도록 private 생성자를 사용한다.
    private Fridge(
        final User member,
        final Ingredient ingredient,
        final BigDecimal quantity,
        final String unit,
        final LocalDate expiryDate
    ) {
        this.member = member;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.registeredAt = LocalDateTime.now();
    }

    // 새 냉장고 재료 엔티티를 생성한다.
    public static Fridge create(
        final User member,
        final Ingredient ingredient,
        final BigDecimal quantity,
        final String unit,
        final LocalDate expiryDate
    ) {
        return new Fridge(member, ingredient, quantity, unit, expiryDate);
    }

    // 재료 수량을 변경한다.
    public void updateQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    // 재료 단위를 변경한다.
    public void updateUnit(final String unit) {
        this.unit = unit;
    }

    // 재료 유통기한을 변경한다.
    public void updateExpiryDate(final LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}
