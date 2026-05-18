package gnu.project.pbl2.recipeingredient.entity;

import gnu.project.pbl2.Fridge.entity.Ingredient;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 레시피 재료 엔티티 */
@Entity
@Table(name = "recipe_ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeIngredient {

    /** 복합키 */
    @EmbeddedId
    private RecipeIngredientId id;

    /** 재료 */
    @MapsId("ingredientId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    /** 재료 양 */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** 단위 */
    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    /** 대체 가능 여부 */
    @Column(name = "is_substitutable", nullable = false)
    private Boolean isSubstitutable;

    private RecipeIngredient(
        final Long recipeId,
        final Ingredient ingredient,
        final BigDecimal amount,
        final String unit,
        final Boolean isSubstitutable
    ) {
        this.id = new RecipeIngredientId(recipeId, ingredient.getIngredientId());
        this.ingredient = ingredient;
        this.amount = amount;
        this.unit = unit;
        this.isSubstitutable = isSubstitutable;
    }

    /** 레시피 재료 생성 */
    public static RecipeIngredient create(
        final Long recipeId,
        final Ingredient ingredient,
        final BigDecimal amount,
        final String unit,
        final Boolean isSubstitutable
    ) {
        return new RecipeIngredient(recipeId, ingredient, amount, unit, isSubstitutable);
    }

    /** 레시피 ID 조회 */
    public Long getRecipeId() {
        return id.getRecipeId();
    }

    /** 재료 ID 조회 */
    public Long getIngredientId() {
        return id.getIngredientId();
    }

    /** 레시피 재료 수정 */
    public void update(
        final BigDecimal amount,
        final String unit,
        final Boolean isSubstitutable
    ) {
        this.amount = amount;
        this.unit = unit;
        this.isSubstitutable = isSubstitutable;
    }
}
