package org.skywalking.apm.collector.stream.worker;

public class WorkerNotFoundException extends WorkerException {
    public WorkerNotFoundException(String message) {
        super(message);
    }
}
