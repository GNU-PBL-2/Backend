package gnu.project.pbl2.storagemethod.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "storage_method")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StorageMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storage_id")
    private Long storageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "storage_type", nullable = false, length = 20)
    private String storageType;

    @Column(name = "min_temp", precision = 5, scale = 1)
    private BigDecimal minTemp;

    @Column(name = "max_temp", precision = 5, scale = 1)
    private BigDecimal maxTemp;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "tip", columnDefinition = "TEXT")
    private String tip;

    public StorageMethod(
            Ingredient ingredient,
            String storageType,
            BigDecimal minTemp,
            BigDecimal maxTemp,
            Integer durationDays,
            String tip
    ) {
        this.ingredient = ingredient;
        this.storageType = storageType;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.durationDays = durationDays;
        this.tip = tip;
    }
}