package gnu.project.pbl2.fridge.repository;

import gnu.project.pbl2.fridge.entity.Ingredient;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** 재료 저장소 */
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    /** 전체 조회 */
    @EntityGraph(attributePaths = "category")
    List<Ingredient> findAllByOrderByIdAsc();

    Optional<Ingredient> findByName(String name);


}
