package gnu.project.pbl2.storage.service;

import gnu.project.pbl2.Fridge.entity.Ingredient;
import gnu.project.pbl2.Fridge.repository.FridgeRepository;
import gnu.project.pbl2.Fridge.repository.IngredientRepository;
import gnu.project.pbl2.common.entity.Category;
import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.common.repository.CategoryRepository;
import gnu.project.pbl2.storage.dto.request.IngredientCreateRequest;
import gnu.project.pbl2.storage.dto.request.IngredientUpdateRequest;
import gnu.project.pbl2.storage.dto.request.StorageMethodCreateRequest;
import gnu.project.pbl2.storage.dto.request.StorageMethodUpdateRequest;
import gnu.project.pbl2.storage.dto.response.IngredientResponse;
import gnu.project.pbl2.storage.dto.response.StorageMethodResponse;
import gnu.project.pbl2.storage.entity.StorageMethod;
import gnu.project.pbl2.storage.repository.StorageMethodRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** storage 비즈니스 로직 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageService {

    /** 재료 저장소 */
    private final IngredientRepository ingredientRepository;
    /** 보관 방법 저장소 */
    private final StorageMethodRepository storageMethodRepository;
    /** 카테고리 저장소 */
    private final CategoryRepository categoryRepository;
    /** 냉장고 참조 확인용 저장소 */
    private final FridgeRepository fridgeRepository;

    /** 재료 전체 조회 */
    public List<IngredientResponse> getIngredients() {
        final List<Ingredient> ingredients = ingredientRepository.findAllByOrderByIdAsc();
        final Map<Long, List<StorageMethod>> storageMethodsByIngredient =
            storageMethodRepository.findAllByOrderByStorageIdAsc()
                .stream()
                .collect(Collectors.groupingBy(
                    storageMethod -> storageMethod.getIngredient().getId()
                ));

        return ingredients.stream()
            .map(ingredient -> IngredientResponse.from(
                ingredient,
                storageMethodsByIngredient.getOrDefault(ingredient.getId(), List.of())
            ))
            .toList();
    }

    /** 재료 단건 조회 */
    public IngredientResponse getIngredient(final Long ingredientId) {
        final Ingredient ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));

        final List<StorageMethod> storageMethods =
            storageMethodRepository.findAllByIngredient_IdOrderByStorageIdAsc(ingredientId);

        return IngredientResponse.from(ingredient, storageMethods);
    }

    /** 재료 등록 */
    @Transactional
    public IngredientResponse createIngredient(final IngredientCreateRequest request) {
        final Category category = getCategory(request.categoryId());
        final Ingredient ingredient = ingredientRepository.save(
            Ingredient.create(request.name(), category)
        );
        return IngredientResponse.from(ingredient, List.of());
    }

    /** 재료 수정 */
    @Transactional
    public IngredientResponse updateIngredient(
        final Long ingredientId,
        final IngredientUpdateRequest request
    ) {
        final Ingredient ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));
        final Category category = getCategory(request.categoryId());

        ingredient.updateName(request.name());
        ingredient.updateCategory(category);

        final List<StorageMethod> storageMethods =
            storageMethodRepository.findAllByIngredient_IdOrderByStorageIdAsc(ingredientId);

        return IngredientResponse.from(ingredient, storageMethods);
    }

    /** 재료 삭제 */
    @Transactional
    public void deleteIngredient(final Long ingredientId) {
        final Ingredient ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));

        if (fridgeRepository.existsByIngredient_Id(ingredientId)) {
            throw new IllegalArgumentException("냉장고에 등록된 재료는 삭제할 수 없습니다.");
        }

        storageMethodRepository.deleteAllByIngredient_Id(ingredientId);
        ingredientRepository.delete(ingredient);
    }

    /** 보관 방법 목록 조회 */
    public List<StorageMethodResponse> getStorageMethods(final Long ingredientId) {
        final List<StorageMethod> storageMethods = ingredientId == null
            ? storageMethodRepository.findAllByOrderByStorageIdAsc()
            : storageMethodRepository.findAllByIngredient_IdOrderByStorageIdAsc(ingredientId);

        return storageMethods.stream()
            .map(StorageMethodResponse::from)
            .toList();
    }

    /** 보관 방법 단건 조회 */
    public StorageMethodResponse getStorageMethod(final Long storageId) {
        final StorageMethod storageMethod = storageMethodRepository.findByStorageId(storageId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORAGE_METHOD_NOT_FOUND));

        return StorageMethodResponse.from(storageMethod);
    }

    /** 보관 방법 등록 */
    @Transactional
    public StorageMethodResponse createStorageMethod(final StorageMethodCreateRequest request) {
        final Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
            .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));

        final StorageMethod storageMethod = storageMethodRepository.save(
            StorageMethod.create(
                ingredient,
                request.storageType(),
                request.minTemp(),
                request.maxTemp(),
                request.durationDays(),
                request.tip()
            )
        );

        return StorageMethodResponse.from(storageMethod);
    }

    /** 보관 방법 수정 */
    @Transactional
    public StorageMethodResponse updateStorageMethod(
        final Long storageId,
        final StorageMethodUpdateRequest request
    ) {
        final StorageMethod storageMethod = storageMethodRepository.findById(storageId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORAGE_METHOD_NOT_FOUND));

        storageMethod.update(
            request.storageType(),
            request.minTemp(),
            request.maxTemp(),
            request.durationDays(),
            request.tip()
        );

        return StorageMethodResponse.from(storageMethod);
    }

    /** 보관 방법 삭제 */
    @Transactional
    public void deleteStorageMethod(final Long storageId) {
        final StorageMethod storageMethod = storageMethodRepository.findById(storageId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORAGE_METHOD_NOT_FOUND));

        storageMethodRepository.delete(storageMethod);
    }

    /** 카테고리 조회 */
    private Category getCategory(final Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
