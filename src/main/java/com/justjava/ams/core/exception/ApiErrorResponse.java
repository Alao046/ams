package com.justjava.ams.core.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    private String message;
    private String errorCode;
    private Integer status;
    private String path;
    private LocalDateTime timestamp;
    private Map<String, String> fieldErrors;
}
