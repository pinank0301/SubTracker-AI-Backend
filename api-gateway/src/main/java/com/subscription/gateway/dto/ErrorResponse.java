package com.subscription.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success;
    private String  message;
    private String  path;
    private int     status;

    public static ErrorResponse of(String message, int status, String path) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .status(status)
                .path(path)
                .build();
    }

    public static ErrorResponse of(String message, int status) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .status(status)
                .build();
    }
}
