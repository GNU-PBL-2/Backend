package gnu.project.pbl2.recipe.service;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.recipe.dto.request.FavoriteListRequest;
import gnu.project.pbl2.recipe.dto.request.RecipeSearchRequest;
import gnu.project.pbl2.recipe.dto.response.RecipeSearchResponse;
import gnu.project.pbl2.recipe.entity.Favorite;
import gnu.project.pbl2.recipe.entity.Recipe;
import gnu.project.pbl2.recipe.enumerated.RecipeTab;
import gnu.project.pbl2.recipe.repository.FavoriteRepository;
import gnu.project.pbl2.recipe.repository.RecipeRepository;
import gnu.project.pbl2.user.entity.User;
import gnu.project.pbl2.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final RecipeService recipeService;

    @Transactional
    public void add(final Long recipeId, final Accessor accessor) {
        final Long userId = accessor.getUserId();

        final Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND));

        final User user = userRepository.findActiveById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        try {
            favoriteRepository.saveAndFlush(Favorite.of(user, recipe));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.FAVORITE_ALREADY_EXISTS);
        }
    }

    @Transactional
    public void remove(final Long recipeId, final Accessor accessor) {
        final Long userId = accessor.getUserId();

        final Favorite favorite = favoriteRepository.findByUser_IdAndRecipe_Id(userId, recipeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_NOT_FOUND));

        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public Page<RecipeSearchResponse> getFavorites(final FavoriteListRequest request, final Accessor accessor) {
        RecipeSearchRequest searchRequest = new RecipeSearchRequest(
            request.keyword(), RecipeTab.FAVORITE, request.page(), request.size()
        );
        return recipeService.getRecipes(searchRequest, accessor);
    }
}
