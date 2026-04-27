package gnu.project.pbl2.recipe.entity;

import gnu.project.pbl2.common.entity.BaseEntity;
import gnu.project.pbl2.common.entity.Category;
import gnu.project.pbl2.common.entity.Taste;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Recipe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taste_id", nullable = false)
    private Taste taste;

    @Column()
    private Integer cookTimeMin;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String youtubeUrl;

    @Column()
    private String thumbnailUrl;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecipeStep> steps = new LinkedHashSet<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecipeIngredient> ingredients = new LinkedHashSet<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();
    public static Recipe create(
        String title,
        Category category,
        Taste taste,
        Integer cookTimeMin,
        String description,
        String youtubeUrl
    ) {
        Recipe recipe = new Recipe();
        recipe.title = title;
        recipe.category = category;
        recipe.taste = taste;
        recipe.cookTimeMin = cookTimeMin;
        recipe.description = description;
        recipe.youtubeUrl = youtubeUrl;
        return recipe;
    }


    // 수정 로직 (정적 메서드가 아닌 인스턴스 메서드)
    public void update(
        String title,
        Category category,
        Taste taste,
        Integer cookTimeMin,
        String description
    ) {
        this.title = title;
        this.category = category;
        this.taste = taste;
        this.cookTimeMin = cookTimeMin;
        this.description = description;
    }

    // 기존 재료와 단계를 초기화하는 메서드
    public void clearCollections() {
        this.ingredients.clear();
        this.steps.clear();
    }

    public void addIngredients(List<RecipeIngredient> recipeIngredients) {
        this.ingredients.addAll(recipeIngredients);
    }

    public void addSteps(List<RecipeStep> recipeSteps) {
        this.steps.addAll(recipeSteps);
    }

}
