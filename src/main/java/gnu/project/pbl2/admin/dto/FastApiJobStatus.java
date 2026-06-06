package gnu.project.pbl2.admin.dto;

public record FastApiJobStatus(
    String status,
    GeminiRecipeDto result,
    String error
) {}
