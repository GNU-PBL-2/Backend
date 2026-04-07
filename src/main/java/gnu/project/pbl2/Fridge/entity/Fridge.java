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
public class Fridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fridge_id")
    private Long fridgeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", nullable = false)
    private User member;

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

    public static Fridge create(
        final User member,
        final Ingredient ingredient,
        final BigDecimal quantity,
        final String unit,
        final LocalDate expiryDate
    ) {
        return new Fridge(member, ingredient, quantity, unit, expiryDate);
    }

    public void updateQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void updateUnit(final String unit) {
        this.unit = unit;
    }

    public void updateExpiryDate(final LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}
