package com.eugene.sixhandshakes.controllers.responses;

import org.bson.Document;

public class ResultResponse extends BaseResponse {

    private Document result;

    public ResultResponse(Document result) {
        super(true);
        this.result = result;
    }

    public Document getResult() {
        return result;
    }
}
