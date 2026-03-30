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

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private Set<RecipeStep> steps = new LinkedHashSet<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private Set<RecipeIngredient> ingredients = new LinkedHashSet<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Favorite> favorites = new ArrayList<>();
}
