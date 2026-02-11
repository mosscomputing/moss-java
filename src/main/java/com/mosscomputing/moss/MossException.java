package com.mosscomputing.moss;

/**
 * Exception thrown by MOSS operations.
 */
public class MossException extends Exception {
    
    public MossException(String message) {
        super(message);
    }
    
    public MossException(String message, Throwable cause) {
        super(message, cause);
    }
}
