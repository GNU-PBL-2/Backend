package gnu.project.pbl2.storage.repository;

import gnu.project.pbl2.storage.entity.StorageMethod;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** 보관 방법 저장소 */
public interface StorageMethodRepository extends JpaRepository<StorageMethod, Long> {

    /** 전체 조회 */
    @EntityGraph(attributePaths = {"ingredient", "ingredient.category"})
    List<StorageMethod> findAllByOrderByStorageIdAsc();

    /** 재료별 조회 */
    @EntityGraph(attributePaths = {"ingredient", "ingredient.category"})
    List<StorageMethod> findAllByIngredient_IngredientIdOrderByStorageIdAsc(Long ingredientId);

    /** 단건 조회 */
    @EntityGraph(attributePaths = {"ingredient", "ingredient.category"})
    Optional<StorageMethod> findByStorageId(Long storageId);

    /** 재료 사용 여부 확인 */
    boolean existsByIngredient_IngredientId(Long ingredientId);

    /** 재료 기준 일괄 삭제 */
    void deleteAllByIngredient_IngredientId(Long ingredientId);
}
