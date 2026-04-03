package gnu.project.pbl2.recipe.repository;

import gnu.project.pbl2.recipe.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient,Long> {

}
