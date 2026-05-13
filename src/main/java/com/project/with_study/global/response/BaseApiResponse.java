package com.project.with_study.global.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class BaseApiResponse {
    private final boolean success = true;
    private final String message;
    private final LocalDateTime timestamp;

    protected BaseApiResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
