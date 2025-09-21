package com.example.sehomallapi.service.exceptions;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException{
    private final String detailMessage;
    private final Object request;

    public NotFoundException(String detailMessage, Object request) {
        this.detailMessage = detailMessage;
        this.request = request;
    }
}
