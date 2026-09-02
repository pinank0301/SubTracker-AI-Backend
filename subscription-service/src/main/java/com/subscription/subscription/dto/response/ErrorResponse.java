package com.subscription.subscription.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response payload")
public class ErrorResponse {

    @Schema(description = "Always false for errors")
    private boolean success;

    @Schema(description = "Error message")
    private String message;

    @Schema(description = "HTTP status code")
    private int status;

    @Schema(description = "Request path")
    private String path;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Error timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Field-level validation errors")
    private Map<String, String> validationErrors;
}
