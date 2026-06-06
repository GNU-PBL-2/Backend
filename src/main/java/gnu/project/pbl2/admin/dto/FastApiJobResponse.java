package gnu.project.pbl2.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FastApiJobResponse(@JsonProperty("job_id") String jobId) {}
