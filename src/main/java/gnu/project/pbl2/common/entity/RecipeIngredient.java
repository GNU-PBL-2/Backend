//package gnu.project.pbl2.recipeingredient.entity;
//
//import gnu.project.pbl2.domain.ingredient.entity.Ingredient;
//import jakarta.persistence.*;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.math.BigDecimal;
//
//@Entity
//@Table(name = "recipe_ingredient")
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class RecipeIngredient {
//
//    @EmbeddedId
//    private RecipeIngredientId id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("recipeId")
//    @JoinColumn(name = "recipe_id")
//    private Recipe recipe;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("ingredientId")
//    @JoinColumn(name = "ingredient_id")
//    private Ingredient ingredient;
//
//    @Column(name = "amount", precision = 10, scale = 2)
//    private BigDecimal amount;
//
//    @Column(name = "unit", length = 20)
//    private String unit;
//
//    @Column(name = "is_substitutable")
//    private Boolean isSubstitutable;
//
//    public RecipeIngredient(
//            Recipe recipe,
//            Ingredient ingredient,
//            BigDecimal amount,
//            String unit,
//            Boolean isSubstitutable
//    ) {
//        this.id = new RecipeIngredientId(recipe.getRecipeId(), ingredient.getIngredientId());
//        this.recipe = recipe;
//        this.ingredient = ingredient;
//        this.amount = amount;
//        this.unit = unit;
//        this.isSubstitutable = isSubstitutable;
//    }
//}