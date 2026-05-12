package com.project.with_study.global.response;

import lombok.Getter;

@Getter
public class SingleApiResponse<T> extends BaseApiResponse{
    private T data;

    private SingleApiResponse(String message, T data) {
        super(message);
        this.data = data;
    }

    public static <T> SingleApiResponse<T> of(String message, T data){
        return new SingleApiResponse<>(message,data);
    }
}
