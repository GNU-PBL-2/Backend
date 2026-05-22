package gnu.project.pbl2.recipeingredient.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 레시피 재료 복합키 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecipeIngredientId implements Serializable {

    /** 레시피 ID */
    @Column(name = "recipe_id")
    private Long recipeId;

    /** 재료 ID */
    @Column(name = "ingredient_id")
    private Long ingredientId;
}
