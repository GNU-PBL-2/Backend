package gnu.project.pbl2.admin.dto;

public record ImportStatusResponse(
    String status,
    Long recipeId,
    String error
) {}
