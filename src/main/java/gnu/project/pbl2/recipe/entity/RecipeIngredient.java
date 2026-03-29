package gnu.project.pbl2.recipe.entity;


import gnu.project.pbl2.common.entity.BaseEntity;
import gnu.project.pbl2.common.entity.Ingredient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RecipeIngredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column
    private String amount;             // "보통", "적음" 또는 "200g"

    @Column
    private String unit;               // "g", "ml", "개" 등

    @Column
    private boolean isSubstitutable;   // 대체 재료 허용 여부

    public static RecipeIngredient of(
        final Recipe recipe,
        final Ingredient ingredient,
        final String amount,
        final String unit,
        final boolean isSubstitutable
    ) {
        RecipeIngredient recipeIngredient = new RecipeIngredient();
        recipeIngredient.recipe = recipe;
        recipeIngredient.ingredient = ingredient;
        recipeIngredient.amount = amount;
        recipeIngredient.unit = unit;
        recipeIngredient.isSubstitutable = isSubstitutable;
        return recipeIngredient;
    }
}