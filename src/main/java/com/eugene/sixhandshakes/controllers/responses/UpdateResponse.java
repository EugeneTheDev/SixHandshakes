package com.eugene.sixhandshakes.controllers.responses;

public class UpdateResponse extends BaseResponse {

    private long processed, queue;
    private double average;

    public UpdateResponse(long processed, long queue, double average) {
        super(true);
        this.processed = processed;
        this.queue = queue;
        this.average = average;
    }

    public long getProcessed() {
        return processed;
    }

    public long getQueue() {
        return queue;
    }

    public double getAverage() {
        return average;
    }
}
