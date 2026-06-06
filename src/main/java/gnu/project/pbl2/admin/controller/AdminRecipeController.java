package gnu.project.pbl2.admin.controller;

import gnu.project.pbl2.admin.controller.docs.AdminRecipeControllerDocs;
import gnu.project.pbl2.admin.dto.AdminRecipeCreateRequest;
import gnu.project.pbl2.admin.dto.ImportStartResponse;
import gnu.project.pbl2.admin.dto.ImportStatusResponse;
import gnu.project.pbl2.admin.dto.RecipeImportRequest;
import gnu.project.pbl2.admin.service.RecipeImportService;
import gnu.project.pbl2.auth.aop.Auth;
import gnu.project.pbl2.auth.aop.OnlyAdmin;
import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.recipe.dto.request.RecipeUpdateRequest;
import gnu.project.pbl2.recipe.service.RecipeService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/recipes")
public class AdminRecipeController implements AdminRecipeControllerDocs {

    private final RecipeImportService recipeImportService;
    private final RecipeService recipeService;

    @OnlyAdmin
    @PostMapping("/import")
    @Override
    public ResponseEntity<ImportStartResponse> importRecipe(
        @RequestBody @Valid final RecipeImportRequest request,
        @Auth final Accessor accessor
    ) {
        ImportStartResponse response = recipeImportService.startImport(request.youtubeUrl());
        return ResponseEntity.accepted().body(response);
    }

    @OnlyAdmin
    @GetMapping("/import/status/{jobId}")
    @Override
    public ResponseEntity<ImportStatusResponse> getImportStatus(
        @PathVariable final String jobId,
        @Auth final Accessor accessor
    ) {
        ImportStatusResponse response = recipeImportService.checkImportStatus(jobId);
        if ("completed".equals(response.status())) {
            return ResponseEntity
                .created(URI.create("/api/v1/recipes/" + response.recipeId()))
                .body(response);
        }
        return ResponseEntity.ok(response);
    }

    @OnlyAdmin
    @PostMapping
    @Override
    public ResponseEntity<Void> createRecipe(
        @RequestBody @Valid final AdminRecipeCreateRequest request,
        @Auth final Accessor accessor
    ) {
        Long recipeId = recipeImportService.createManually(request);
        return ResponseEntity
            .created(URI.create("/api/v1/recipes/" + recipeId))
            .build();
    }

    @OnlyAdmin
    @PutMapping("/{id}")
    @Override
    public ResponseEntity<Long> updateRecipe(
        @PathVariable(name = "id") final Long id,
        @RequestBody final RecipeUpdateRequest request,
        @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(recipeService.updateRecipe(id, request, accessor));
    }

    @OnlyAdmin
    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Long> deleteRecipe(
        @PathVariable(name = "id") final Long id,
        @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(recipeService.deleteRecipe(id, accessor));
    }
}
