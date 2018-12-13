package com.eugene.sixhandshakes.controllers.responses;

public class ErrorResponse extends BaseResponse {

    private int errorCode;
    private String message;

    public ErrorResponse(String message, int errorCode) {
        super(false);
        this.message = message;
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
