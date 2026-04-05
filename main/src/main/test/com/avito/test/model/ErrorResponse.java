package com.avito.test.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
    private Result result;
    private String status;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String message;
        private Messages messages;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Messages {
    }
}
