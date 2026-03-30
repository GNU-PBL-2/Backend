package gnu.project.pbl2.recipe.dto.response;


public record RecipeSearchResponse(
    Long id,
    String title,
    String thumbnailUrl,
    Integer cookTimeMin,
    boolean cookable,               // "조리가능" 뱃지
    int expiringIngredientCount,    // "임박재료 N개" 뱃지
    boolean isFavorite                // 하트 채움 여부
) {
    // 1단계: 레시피 기본 정보만으로 생성 (뱃지는 기본값)
    public static RecipeSearchResponse ofBase(
        Long id, String title, String thumbnailUrl, Integer cookTimeMin
    ) {
        return new RecipeSearchResponse(id, title, thumbnailUrl, cookTimeMin, false, 0, false);
    }

    // 2단계: 뱃지 정보 조립
    public RecipeSearchResponse withBadge(boolean cookable, int expiringCount, boolean favorite) {
        return new RecipeSearchResponse(
            this.id, this.title, this.thumbnailUrl, this.cookTimeMin,
            cookable, expiringCount, favorite
        );
    }
}
