package gnu.project.pbl2.Fridge.repository;

import gnu.project.pbl2.Fridge.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 재료 기준 정보를 조회/저장하는 JPA 저장소.
 */
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

}
