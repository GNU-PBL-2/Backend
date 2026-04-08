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

@Entity
@Table(name = "ingredient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 냉장고에서 사용할 재료 마스터 정보 엔티티.
 * 재료명과 카테고리 같은 기준 정보를 저장한다.
 */
public class Ingredient {

    // 재료 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Long ingredientId;

    // 재료 이름
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // 재료가 속한 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 새 재료 기준 정보를 생성한다.
    public Ingredient(final String name, final Category category) {
        this.name = name;
        this.category = category;
    }

    // 재료명을 변경한다.
    public void updateName(final String name) {
        this.name = name;
    }

    // 재료 카테고리를 변경한다.
    public void updateCategory(final Category category) {
        this.category = category;
    }
}
