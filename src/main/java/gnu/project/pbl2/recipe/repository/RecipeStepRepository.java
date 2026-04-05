package gnu.project.pbl2.recipe.repository;

import gnu.project.pbl2.recipe.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepRepository extends JpaRepository<RecipeStep,Long> {

}
