package gnu.project.pbl2.fridge.repository;

import gnu.project.pbl2.fridge.entity.Fridge;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {

    @Query("SELECT f.ingredient.id FROM Fridge f WHERE f.user.id = :userId")
    Set<Long> findIngredientIdsByUserId(@Param("userId") final Long userId);

    @Query("SELECT f.ingredient.id FROM Fridge f WHERE f.user.id = :userId AND f.expiryDate <= :threshold")
    Set<Long> findExpiringIngredientIds(
        @Param("userId") final Long userId,
        @Param("threshold") final LocalDate threshold
        );


}
