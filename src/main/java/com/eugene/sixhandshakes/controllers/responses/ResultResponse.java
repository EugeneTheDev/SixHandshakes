package com.eugene.sixhandshakes.controllers.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.Document;

import java.util.List;

public class ResultResponse extends BaseResponse {

    private List<Document> result;

    public ResultResponse(List<Document> result) {
        super(true);
        this.result = result;
    }

    public List<Document> getResult() {
        return result;
    }

    @JsonIgnore
    public boolean isEmpty(){
        return result.isEmpty();
    }
}
