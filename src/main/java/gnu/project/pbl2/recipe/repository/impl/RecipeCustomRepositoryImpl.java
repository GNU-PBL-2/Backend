package gnu.project.pbl2.recipe.repository.impl;

import static com.querydsl.core.types.dsl.Expressions.asBoolean;
import static com.querydsl.core.types.dsl.Expressions.asNumber;
import static gnu.project.pbl2.fridge.entity.QFridge.fridge;
import static gnu.project.pbl2.recipe.entity.QFavorite.favorite;
import static gnu.project.pbl2.recipe.entity.QRecipe.recipe;
import static gnu.project.pbl2.recipe.entity.QRecipeIngredient.recipeIngredient;
import static gnu.project.pbl2.recipe.entity.QRecipeStep.recipeStep;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import gnu.project.pbl2.recipe.dto.request.RecipeSearchRequest;
import gnu.project.pbl2.recipe.dto.response.RecipeSearchResponse;
import gnu.project.pbl2.recipe.entity.Recipe;
import gnu.project.pbl2.recipe.repository.RecipeCustomRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class RecipeCustomRepositoryImpl implements RecipeCustomRepository {

    private final JPAQueryFactory queryFactory;


    /**
     * 레시피 검색을 수행하고 탭 조건에 따라 결과를 분기한다.
     * <p>
     * 사용자가 선택한 탭(전체, 조리 가능, 임박, 즐겨찾기)에 따라
     * 각각 다른 조건의 쿼리를 실행하여 페이징된 결과를 반환한다.
     *
     * @author Hong
     * @param request 검색 조건 (키워드, 탭, 페이지 정보)
     * @param userId 사용자 ID (개인화 필터링에 사용)
     * @return 탭 조건에 맞는 레시피 목록 페이지
     */
    @Override
    public Page<RecipeSearchResponse> searchRecipes(RecipeSearchRequest request, Long userId) {
        return switch (request.tab()) {
            case ALL -> searchAll(request);
            case COOKABLE -> searchCookable(request, userId);
            case EXPIRING -> searchExpiring(request, userId);
            case FAVORITE -> searchFavorite(request, userId);
        };
    }

    /**
     * 전체 레시피를 조회한다. ------- (1)
     * <p>
     * 키워드 검색 조건만 적용하며,
     * 최신 생성 순으로 정렬된 결과를 반환한다.
     *
     * @param request 검색 조건
     * @return 전체 레시피 목록 페이지
     */
    private Page<RecipeSearchResponse> searchAll(RecipeSearchRequest request) {
        return fetchPage(request,
            keywordContains(request.keyword()),
            recipe.createdAt.desc()
        );
    }
    /**
     * 사용자가 보유한 재료로 조리 가능한 레시피를 조회한다. ------- (1)
     * <p>
     * 대체 불가능한 재료 중 하나라도 부족한 경우를 제외하여
     * 실제 조리 가능한 레시피만 필터링한다.
     *
     * @param request 검색 조건
     * @param userId 사용자 ID
     * @return 조리 가능한 레시피 목록 페이지
     */
    private Page<RecipeSearchResponse> searchCookable(RecipeSearchRequest request,
        Long userId) {
        return fetchPage(request,
            noMissingIngredient(userId).and(keywordContains(request.keyword())),
            recipe.createdAt.desc()
        );
    }
    /**
     * 유통기한이 임박한 재료를 포함한 레시피를 조회한다. ------- (1)
     * <p>
     * 현재 기준 +3일 이내의 재료를 포함한 레시피만 필터링하며,
     * 임박 재료 개수가 많은 순으로 우선 정렬한다.
     *
     * @param request 검색 조건
     * @param userId 사용자 ID
     * @return 임박 재료 기반 레시피 목록 페이지
     */

    private Page<RecipeSearchResponse> searchExpiring(RecipeSearchRequest request,
        Long userId) {
        LocalDate threshold = LocalDate.now().plusDays(3);
        return fetchPage(request,
            hasExpiringIngredient(userId, threshold).and(keywordContains(request.keyword())),
            new OrderSpecifier<>(Order.DESC, expiringIngredientCount(userId, threshold)),
            recipe.createdAt.desc()
        );
    }
    /**
     * 사용자가 즐겨찾기한 레시피를 조회한다. ------- (1)
     * <p>
     * Favorite 테이블을 기준으로 해당 사용자가
     * 즐겨찾기한 레시피만 필터링하여 반환한다.
     *
     * @param request 검색 조건
     * @param userId 사용자 ID
     * @return 즐겨찾기 레시피 목록 페이지
     */
    private Page<RecipeSearchResponse> searchFavorite(RecipeSearchRequest request,
        Long userId) {
        return fetchPage(request,
            isFavorited(userId).and(keywordContains(request.keyword())),
            recipe.createdAt.desc()
        );
    }
    /**
     * 공통 검색 로직을 수행하여 페이징 결과를 반환한다. ------- (1)
     * <p>
     * 전달받은 조건과 정렬 기준을 기반으로 레시피를 조회하며,
     * offset/limit 방식으로 페이지네이션을 적용한다.
     *
     * @param request 검색 조건 (페이지, 사이즈 포함)
     * @param condition 필터링 조건
     * @param orders 정렬 조건 (가변 인자)
     * @return 페이징된 레시피 목록
     */
    private Page<RecipeSearchResponse> fetchPage(
        RecipeSearchRequest request,
        BooleanExpression condition,
        OrderSpecifier<?>... orders
    ) {
        List<RecipeSearchResponse> content = queryFactory
            .select(Projections.constructor(RecipeSearchResponse.class,
                recipe.id,
                recipe.title,
                recipe.thumbnailUrl,
                recipe.cookTimeMin,
                asBoolean(false),
                asNumber(0).intValue(),
                asBoolean(false)
            ))
            .from(recipe)
            .leftJoin(recipe.category)
            .where(condition, recipe.isDeleted.isFalse())
            .orderBy(orders)
            .offset((long) request.page() * request.size())
            .limit(request.size())
            .fetch();

        long total = queryFactory
            .select(recipe.count())
            .from(recipe)
            .where(condition)
            .fetchOne();

        return new PageImpl<>(content, PageRequest.of(request.page(), request.size()), total);
    }


    /**
     * 사용자가 가진 재료로 조리 가능한 레시피 ID 집합을 조회한다. ------- (1)
     * <p>
     * 주어진 레시피 목록 중, 부족한 재료가 없는 레시피만 필터링하여
     * 조리 가능한 레시피 ID 집합을 반환한다.
     *
     * @param recipeIds 대상 레시피 ID 목록
     * @param userId 사용자 ID
     * @return 조리 가능한 레시피 ID 집합
     */
    public Set<Long> findCookableRecipeIds(List<Long> recipeIds, Long userId) {
        return Set.copyOf(
            queryFactory
                .select(recipe.id)
                .from(recipe)
                .where(
                    recipe.id.in(recipeIds),
                    noMissingIngredient(userId)
                )
                .fetch()
        );
    }
    /**
     * 레시피별 임박 재료 개수를 조회한다. ------- (1)
     * <p>
     * 각 레시피에 포함된 재료 중,
     * 유통기한 임박 재료의 개수를 집계하여 Map 형태로 반환한다.
     *
     * @param recipeIds 대상 레시피 ID 목록
     * @param userId 사용자 ID
     * @return (레시피 ID → 임박 재료 개수) 매핑
     */
    public java.util.Map<Long, Long> findExpiringCountMap(List<Long> recipeIds, Long userId) {
        LocalDate threshold = LocalDate.now().plusDays(3);
        return queryFactory
            .select(recipeIngredient.recipe.id, recipeIngredient.count())
            .from(recipeIngredient)
            .where(
                recipeIngredient.recipe.id.in(recipeIds),
                recipeIngredient.ingredient.id.in(expiringIngredientIds(userId, threshold))
            )
            .groupBy(recipeIngredient.recipe.id)
            .fetch()
            .stream()
            .collect(java.util.stream.Collectors.toMap(
                t -> t.get(recipeIngredient.recipe.id),
                t -> t.get(recipeIngredient.count())
            ));
    }

    /**
     * 사용자가 즐겨찾기한 레시피 ID 집합을 조회한다. ------- (1)
     * <p>
     * Favorite 테이블 기준으로
     * 특정 사용자와 매칭되는 레시피 ID만 반환한다.
     *
     * @param recipeIds 대상 레시피 ID 목록
     * @param userId 사용자 ID
     * @return 즐겨찾기 레시피 ID 집합
     */
    public Set<Long> findFavoriteRecipeIds(List<Long> recipeIds, Long userId) {
        return Set.copyOf(
            queryFactory
                .select(favorite.recipe.id)
                .from(favorite)
                .where(
                    favorite.recipe.id.in(recipeIds),
                    favorite.user.id.eq(userId)
                )
                .fetch()
        );
    }
    /**
     * 레시피 상세 정보를 조회한다. ------- (1)
     * <p>
     * 카테고리, 맛, 재료, 조리 단계까지 fetch join을 통해
     * 한 번의 쿼리로 모든 연관 데이터를 로딩한다.
     *
     * @param recipeId 레시피 ID
     * @return 레시피 상세 정보 (Optional)
     */
    public Optional<Recipe> findDetailById(final Long recipeId) {
        Recipe result = queryFactory
            .selectFrom(recipe)
            .leftJoin(recipe.category).fetchJoin()
            .leftJoin(recipe.taste).fetchJoin()
            .leftJoin(recipe.ingredients, recipeIngredient).fetchJoin()
            .leftJoin(recipeIngredient.ingredient).fetchJoin()
            .leftJoin(recipe.steps, recipeStep).fetchJoin()
            .where(recipe.id.eq(recipeId))
            .fetchOne();

        return Optional.ofNullable(result);
    }
    /**
     * 특정 레시피의 즐겨찾기 여부를 확인한다. ------- (1)
     * <p>
     * Favorite 테이블에 해당 사용자와 레시피의 매핑이 존재하는지 검사한다.
     *
     * @param recipeId 레시피 ID
     * @param userId 사용자 ID
     * @return 즐겨찾기 여부 (true/false)
     */
    @Override
    public boolean isFavorite(Long recipeId, Long userId) {
        return queryFactory
            .selectOne()
            .from(favorite)
            .where(
                favorite.recipe.id.eq(recipeId),
                favorite.user.id.eq(userId)
            )
            .fetchFirst() != null;
    }
    /**
     * 레시피에 필요한 재료가 모두 존재하는지 검증한다. ------- (1)
     * <p>
     * 대체 불가능한 재료 중 하나라도 냉장고에 없는 경우를 찾아
     * 해당 레시피를 제외하는 조건을 구성한다.
     *
     * @param userId 사용자 ID
     * @return 재료 부족이 없는 레시피 조건식
     */

    private BooleanExpression noMissingIngredient(Long userId) {
        return JPAExpressions
            .selectOne()
            .from(recipeIngredient)
            .where(
                recipeIngredient.recipe.eq(recipe),
                recipeIngredient.isSubstitutable.isFalse(),
                recipeIngredient.ingredient.id.notIn(
                    JPAExpressions
                        .select(fridge.ingredient.id)
                        .from(fridge)
                        .where(fridge.user.id.eq(userId))
                )
            )
            .exists()
            .not();
    }
    /**
     * 유통기한 임박 재료를 포함한 레시피 조건을 생성한다. ------- (1)
     * <p>
     * 특정 기간 이내 만료되는 재료를 포함하는 레시피만 필터링한다.
     *
     * @param userId 사용자 ID
     * @param threshold 기준 날짜
     * @return 임박 재료 포함 조건식
     */
    private BooleanExpression hasExpiringIngredient(Long userId, LocalDate threshold) {
        return JPAExpressions
            .selectOne()
            .from(recipeIngredient)
            .where(
                recipeIngredient.recipe.eq(recipe),
                recipeIngredient.ingredient.id.in(expiringIngredientIds(userId, threshold))
            )
            .exists();
    }
    /**
     * 사용자가 즐겨찾기한 레시피 조건을 생성한다. ------- (1)
     * <p>
     * Favorite 테이블을 기준으로 현재 레시피가
     * 해당 사용자에 의해 즐겨찾기 되었는지 검사한다.
     *
     * @param userId 사용자 ID
     * @return 즐겨찾기 조건식
     */
    private BooleanExpression isFavorited(Long userId) {
        return JPAExpressions
            .selectOne()
            .from(favorite)
            .where(
                favorite.recipe.eq(recipe),
                favorite.user.id.eq(userId)
            )
            .exists();
    }
    /**
     * 레시피별 임박 재료 개수를 계산하는 서브쿼리를 생성한다. ------- (1)
     * <p>
     * 특정 레시피에 포함된 재료 중
     * 유통기한 임박 재료의 개수를 집계한다.
     *
     * @param userId 사용자 ID
     * @param threshold 기준 날짜
     * @return 임박 재료 개수 서브쿼리
     */
    private SubQueryExpression<Long> expiringIngredientCount(Long userId, LocalDate threshold) {
        return JPAExpressions
            .select(recipeIngredient.count())
            .from(recipeIngredient)
            .where(
                recipeIngredient.recipe.eq(recipe),
                recipeIngredient.ingredient.id.in(expiringIngredientIds(userId, threshold))
            );
    }
    /**
     * 레시피별 임박 재료 개수를 계산하는 서브쿼리를 생성한다. ------- (1)
     * <p>
     * 특정 레시피에 포함된 재료 중
     * 유통기한 임박 재료의 개수를 집계한다.
     *
     * @param userId 사용자 ID
     * @param threshold 기준 날짜
     * @return 임박 재료 개수 서브쿼리
     */
    private SubQueryExpression<Long> expiringIngredientIds(Long userId, LocalDate threshold) {
        return JPAExpressions
            .select(fridge.ingredient.id)
            .from(fridge)
            .where(
                fridge.user.id.eq(userId),
                fridge.expiryDate.loe(threshold)
            );
    }
    /**
     * 레시피 제목에 대한 키워드 검색 조건을 생성한다. ------- (1)
     * <p>
     * 키워드가 존재할 경우 대소문자를 무시한 포함 검색을 수행하며,
     * 키워드가 없으면 조건을 적용하지 않는다.
     *
     * @param keyword 검색 키워드
     * @return 제목 검색 조건식 또는 null
     */

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword)
            ? recipe.title.containsIgnoreCase(keyword)
            : null;
    }
}
