package gnu.project.pbl2.storage.entity;

import gnu.project.pbl2.Fridge.entity.Ingredient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 재료별 보관 방법 엔티티 */
@Entity
@Table(name = "storage_method")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StorageMethod {

    /** 보관 방법 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storage_id")
    private Long storageId;

    /** 대상 재료 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    /** 보관 유형 */
    @Column(name = "storage_type", nullable = false, length = 20)
    private String storageType;

    /** 최소 온도 */
    @Column(name = "min_temp", precision = 5, scale = 1)
    private BigDecimal minTemp;

    /** 최대 온도 */
    @Column(name = "max_temp", precision = 5, scale = 1)
    private BigDecimal maxTemp;

    /** 보관 일수 */
    @Column(name = "duration_days")
    private Integer durationDays;

    /** 보관 팁 */
    @Column(name = "tip", columnDefinition = "TEXT")
    private String tip;

    private StorageMethod(
        final Ingredient ingredient,
        final String storageType,
        final BigDecimal minTemp,
        final BigDecimal maxTemp,
        final Integer durationDays,
        final String tip
    ) {
        this.ingredient = ingredient;
        this.storageType = storageType;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.durationDays = durationDays;
        this.tip = tip;
    }

    /** 보관 방법 생성 */
    public static StorageMethod create(
        final Ingredient ingredient,
        final String storageType,
        final BigDecimal minTemp,
        final BigDecimal maxTemp,
        final Integer durationDays,
        final String tip
    ) {
        return new StorageMethod(ingredient, storageType, minTemp, maxTemp, durationDays, tip);
    }

    /** 보관 방법 수정 */
    public void update(
        final String storageType,
        final BigDecimal minTemp,
        final BigDecimal maxTemp,
        final Integer durationDays,
        final String tip
    ) {
        this.storageType = storageType;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.durationDays = durationDays;
        this.tip = tip;
    }
}
