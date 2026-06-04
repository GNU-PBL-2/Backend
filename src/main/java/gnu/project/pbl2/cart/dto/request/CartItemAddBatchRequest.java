package gnu.project.pbl2.cart.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CartItemAddBatchRequest(
    @NotEmpty @Valid List<CartItemAddRequest> items
) {}
