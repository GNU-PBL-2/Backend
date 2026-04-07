package gnu.project.pbl2.Fridge.repository;

import gnu.project.pbl2.Fridge.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

}
