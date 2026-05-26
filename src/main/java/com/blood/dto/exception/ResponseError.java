package com.blood.dto.exception;

public class ResponseError extends ResponseData {

    public ResponseError(int status, String message) {
        super(status, message);
    }
}
