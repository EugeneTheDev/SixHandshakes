package com.eugene.sixhandshakes.controllers.responses;

public class BaseResponse {

    private boolean isSuccess;

    public BaseResponse(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
