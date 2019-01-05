package com.eugene.sixhandshakes.controllers.responses;


public class ResultResponse<T> extends BaseResponse {

    private T result;

    public ResultResponse(T result) {
        super(true);
        this.result = result;
    }

    public T getResult() {
        return result;
    }

}
