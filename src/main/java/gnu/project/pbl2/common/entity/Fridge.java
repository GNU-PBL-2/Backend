package gnu.project.pbl2.fridge.entity;

import gnu.project.pbl2.domain.ingredient.entity.Ingredient;
import gnu.project.pbl2.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fridge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fridge_id")
    private Long fridgeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    public Fridge(
            Member member,
            Ingredient ingredient,
            BigDecimal quantity,
            String unit,
            LocalDate expiryDate,
            LocalDateTime registeredAt
    ) {
        this.member = member;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.registeredAt = registeredAt;
    }

    public void updateQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void updateUnit(String unit) {
        this.unit = unit;
    }

    public void updateExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}