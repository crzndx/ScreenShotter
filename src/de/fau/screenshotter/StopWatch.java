package de.crzndx.screenshotter;

import java.lang.System;
import java.util.concurrent.TimeUnit;

/**
 * A simple stahp watch to measure how long a block of code executed.
 */
public class StopWatch {

    long starts;

    public static StopWatch start() {
        return new StopWatch();
    }

    private StopWatch() {
        reset();
    }

    public StopWatch reset() {
        starts = System.currentTimeMillis();
        return this;
    }

    public long time() {
        long ends = System.currentTimeMillis();
        return ends - starts;
    }

    public long time(TimeUnit unit) {
        return unit.convert(time(), TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return time() + "ms";
    }
}
