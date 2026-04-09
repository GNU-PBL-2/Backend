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
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 재료 마스터 엔티티 */
@Entity
@Table(name = "ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingredient {

    /** 재료 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Long ingredientId;

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

    /** 재료 생성 */
    public static Ingredient create(final String name, final Category category) {
        return new Ingredient(name, category);
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
