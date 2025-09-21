package com.example.sehomallapi.service.exceptions;

import lombok.Getter;

@Getter
public class AccessDeniedException extends RuntimeException{
    private final String detailMessage;
    private final Object request;

    public AccessDeniedException(String detailMessage, Object request) {
        this.detailMessage = detailMessage;
        this.request = request;
    }
}
