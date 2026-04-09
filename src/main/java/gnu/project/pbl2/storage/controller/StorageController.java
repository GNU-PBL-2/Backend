package gnu.project.pbl2.storage.controller;

import gnu.project.pbl2.storage.dto.request.IngredientCreateRequest;
import gnu.project.pbl2.storage.dto.request.IngredientUpdateRequest;
import gnu.project.pbl2.storage.dto.request.StorageMethodCreateRequest;
import gnu.project.pbl2.storage.dto.request.StorageMethodUpdateRequest;
import gnu.project.pbl2.storage.dto.response.IngredientResponse;
import gnu.project.pbl2.storage.dto.response.StorageMethodResponse;
import gnu.project.pbl2.storage.service.StorageService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 재료와 보관 방법 API */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storage")
public class StorageController {

    /** storage 서비스 */
    private final StorageService storageService;

    /** 재료 목록 조회 */
    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientResponse>> getIngredients() {
        return ResponseEntity.ok(storageService.getIngredients());
    }

    /** 재료 단건 조회 */
    @GetMapping("/ingredients/{ingredientId}")
    public ResponseEntity<IngredientResponse> getIngredient(@PathVariable final Long ingredientId) {
        return ResponseEntity.ok(storageService.getIngredient(ingredientId));
    }

    /** 재료 등록 */
    @PostMapping("/ingredients")
    public ResponseEntity<IngredientResponse> createIngredient(
        @Valid @RequestBody final IngredientCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(storageService.createIngredient(request));
    }

    /** 재료 수정 */
    @PatchMapping("/ingredients/{ingredientId}")
    public ResponseEntity<IngredientResponse> updateIngredient(
        @PathVariable final Long ingredientId,
        @Valid @RequestBody final IngredientUpdateRequest request
    ) {
        return ResponseEntity.ok(storageService.updateIngredient(ingredientId, request));
    }

    /** 재료 삭제 */
    @DeleteMapping("/ingredients/{ingredientId}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable final Long ingredientId) {
        storageService.deleteIngredient(ingredientId);
        return ResponseEntity.noContent().build();
    }

    /** 보관 방법 목록 조회 */
    @GetMapping("/methods")
    public ResponseEntity<List<StorageMethodResponse>> getStorageMethods(
        @RequestParam(required = false) final Long ingredientId
    ) {
        return ResponseEntity.ok(storageService.getStorageMethods(ingredientId));
    }

    /** 보관 방법 단건 조회 */
    @GetMapping("/methods/{storageId}")
    public ResponseEntity<StorageMethodResponse> getStorageMethod(
        @PathVariable final Long storageId
    ) {
        return ResponseEntity.ok(storageService.getStorageMethod(storageId));
    }

    /** 보관 방법 등록 */
    @PostMapping("/methods")
    public ResponseEntity<StorageMethodResponse> createStorageMethod(
        @Valid @RequestBody final StorageMethodCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(storageService.createStorageMethod(request));
    }

    /** 보관 방법 수정 */
    @PatchMapping("/methods/{storageId}")
    public ResponseEntity<StorageMethodResponse> updateStorageMethod(
        @PathVariable final Long storageId,
        @Valid @RequestBody final StorageMethodUpdateRequest request
    ) {
        return ResponseEntity.ok(storageService.updateStorageMethod(storageId, request));
    }

    /** 보관 방법 삭제 */
    @DeleteMapping("/methods/{storageId}")
    public ResponseEntity<Void> deleteStorageMethod(@PathVariable final Long storageId) {
        storageService.deleteStorageMethod(storageId);
        return ResponseEntity.noContent().build();
    }
}
