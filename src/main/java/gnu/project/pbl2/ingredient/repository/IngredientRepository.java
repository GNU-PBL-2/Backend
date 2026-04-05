package gnu.project.pbl2.ingredient.repository;

import gnu.project.pbl2.ingredient.entity.Ingredient;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {


    Optional<Ingredient> findByName(String name);
}
