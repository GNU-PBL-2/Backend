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


    @Override
    public Page<RecipeSearchResponse> searchRecipes(RecipeSearchRequest request, Long userId) {
        return switch (request.tab()) {
            case ALL -> searchAll(request);
            case COOKABLE -> searchCookable(request, userId);
            case EXPIRING -> searchExpiring(request, userId);
            case FAVORITE -> searchFavorite(request, userId);
        };
    }

    // ── 탭별 조건/정렬 조합 ───────────────────────────────────

    private Page<RecipeSearchResponse> searchAll(RecipeSearchRequest request) {
        return fetchPage(request,
            keywordContains(request.keyword()),
            recipe.createdAt.desc()
        );
    }

    private Page<RecipeSearchResponse> searchCookable(RecipeSearchRequest request,
        Long userId) {
        return fetchPage(request,
            noMissingIngredient(userId).and(keywordContains(request.keyword())),
            recipe.createdAt.desc()
        );
    }

    private Page<RecipeSearchResponse> searchExpiring(RecipeSearchRequest request,
        Long userId) {
        LocalDate threshold = LocalDate.now().plusDays(3);
        return fetchPage(request,
            hasExpiringIngredient(userId, threshold).and(keywordContains(request.keyword())),
            new OrderSpecifier<>(Order.DESC, expiringIngredientCount(userId, threshold)),
            recipe.createdAt.desc()
        );
    }

    private Page<RecipeSearchResponse> searchFavorite(RecipeSearchRequest request,
        Long userId) {
        return fetchPage(request,
            isFavorited(userId).and(keywordContains(request.keyword())),
            recipe.createdAt.desc()
        );
    }

    // ── 공통 페이지 조회 ──────────────────────────────────────

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
            .where(condition,recipe.isDeleted.isFalse())
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

    // ── 뱃지 계산용 public 메서드 (Service에서 호출) ──────────

    // 조리 가능한 레시피 ID 집합
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

    // 레시피별 임박 재료 개수 맵 (recipeId → count)
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

    // 즐겨찾기한 레시피 ID 집합
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

    public Optional<Recipe> findDetailById(Long recipeId) {
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

    private SubQueryExpression<Long> expiringIngredientCount(Long userId, LocalDate threshold) {
        return JPAExpressions
            .select(recipeIngredient.count())
            .from(recipeIngredient)
            .where(
                recipeIngredient.recipe.eq(recipe),
                recipeIngredient.ingredient.id.in(expiringIngredientIds(userId, threshold))
            );
    }

    private SubQueryExpression<Long> expiringIngredientIds(Long userId, LocalDate threshold) {
        return JPAExpressions
            .select(fridge.ingredient.id)
            .from(fridge)
            .where(
                fridge.user.id.eq(userId),
                fridge.expiryDate.loe(threshold)
            );
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword)
            ? recipe.title.containsIgnoreCase(keyword)
            : null;
    }
}
