package gnu.project.pbl2.common.repository;

import gnu.project.pbl2.common.entity.IngredientAllergy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IngredientAllergyRepository extends JpaRepository<IngredientAllergy, Long> {

    @Query("SELECT ia.ingredient.id FROM IngredientAllergy ia WHERE ia.allergy.id IN :allergyIds")
    List<Long> findIngredientIdsByAllergyIds(@Param("allergyIds") List<Long> allergyIds);
}
