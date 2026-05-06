package gnu.project.pbl2.recipe.repository;

import gnu.project.pbl2.recipe.entity.Favorite;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUser_IdAndRecipe_Id(Long userId, Long recipeId);
}
