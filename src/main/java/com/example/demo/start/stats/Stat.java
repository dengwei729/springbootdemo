package com.example.demo.start.stats;

public class Stat {
    long startTimestamp;
    long endTimestamp;

    public Stat(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getTimeCost() {
        assert endTimestamp > 0L;
        return endTimestamp - startTimestamp;
    }
}
