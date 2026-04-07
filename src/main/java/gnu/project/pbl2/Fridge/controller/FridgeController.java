package gnu.project.pbl2.Fridge.controller;

import gnu.project.pbl2.Fridge.dto.request.FridgeCreateRequest;
import gnu.project.pbl2.Fridge.dto.request.FridgeUpdateRequest;
import gnu.project.pbl2.Fridge.dto.response.FridgeResponse;
import gnu.project.pbl2.Fridge.service.FridgeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fridge")
public class FridgeController {

    private final FridgeService fridgeService;

    @GetMapping("/{memberId}")
    public ResponseEntity<List<FridgeResponse>> getFridge(@PathVariable final Long memberId) {
        return ResponseEntity.ok(fridgeService.getFridgeByMemberId(memberId));
    }

    @PostMapping
    public ResponseEntity<FridgeResponse> addIngredient(
        @RequestBody final FridgeCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(fridgeService.addIngredient(request));
    }

    @PatchMapping("/{fridgeId}")
    public ResponseEntity<FridgeResponse> updateIngredient(
        @PathVariable final Long fridgeId,
        @RequestBody final FridgeUpdateRequest request
    ) {
        return ResponseEntity.ok(fridgeService.updateIngredient(fridgeId, request));
    }

    @DeleteMapping("/{fridgeId}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable final Long fridgeId) {
        fridgeService.deleteIngredient(fridgeId);
        return ResponseEntity.noContent().build();
    }
}
