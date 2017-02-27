package ru.pushkarev.LogsSearcher.utils;


public class Stopwatch {

    private long startTime;

    public Stopwatch() {
        start();
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public long getDuration() {
        return System.currentTimeMillis() - startTime;
    }

    public String stop() {
        return ' ' + getDuration() + " ms";
    }
}
