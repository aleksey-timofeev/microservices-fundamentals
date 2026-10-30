package com.alextim.resource.exception;

public class TransientPublishException extends RuntimeException {
    public TransientPublishException() {
        super();
    }

    public TransientPublishException(String message) {
        super(message);
    }

    public TransientPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
