package com.ixingji.task.atomic.exception;

public class AtomicTaskFailedException extends Exception {

    public AtomicTaskFailedException(String message) {
        super(message);
    }

    public AtomicTaskFailedException(Throwable cause) {
        super(cause);
    }

}
