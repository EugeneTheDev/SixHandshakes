package com.eugene.sixhandshakes.controllers.responses;

public class SuccessResponse extends BaseResponse {

    private int sourceId, targetId;

    public SuccessResponse(int sourceId, int targetId) {
        super(true);
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getTargetId() {
        return targetId;
    }
}
