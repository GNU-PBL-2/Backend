package gnu.project.pbl2.recipe.entity;

import gnu.project.pbl2.common.entity.BaseEntity;
import gnu.project.pbl2.common.entity.Ingredient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
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

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private Long tasteId;

    @Column()
    private Integer cookTimeMin;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String youtubeUrl;

    @Column()
    private String thumbnailUrl;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<RecipeStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Ingredient> ingredients = new ArrayList<>();
}
