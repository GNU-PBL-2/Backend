package gnu.project.pbl2.Fridge.entity;

import gnu.project.pbl2.common.entity.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 재료 마스터 엔티티 */
@Entity
@Table(name = "ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Ingredient {

    /** 재료 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column()
    private Long id;

    /** 재료명 */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 카테고리 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Ingredient(final String name, final Category category) {
        this.name = name;
        this.category = category;
    }
    public static Ingredient create(String name, Category category) {
        Ingredient ingredient = new Ingredient();
        ingredient.name = name;
        ingredient.category = category;
        return ingredient;
    }

    /** 이름 수정 */
    public void updateName(final String name) {
        this.name = name;
    }

    /** 카테고리 수정 */
    public void updateCategory(final Category category) {
        this.category = category;
    }
}
