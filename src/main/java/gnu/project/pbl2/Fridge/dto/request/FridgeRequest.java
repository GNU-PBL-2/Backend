@Getter
@Setter
public class FridgeCreateRequest {

    private Long memberId;
    private Long ingredientId;
    private BigDecimal quantity;
    private String unit;
    private LocalDate expiryDate;
}