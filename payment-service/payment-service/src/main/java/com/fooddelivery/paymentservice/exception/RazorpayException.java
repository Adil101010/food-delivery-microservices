package com.fooddelivery.paymentservice.exception;

public class RazorpayException extends RuntimeException {
    private String errorCode;

    public RazorpayException(String message) {
        super(message);
    }

    public RazorpayException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public RazorpayException(String message, Throwable cause) {
        super(message, cause);
    }

    // THIS METHOD EXISTS
    public String getErrorCode() {
        return errorCode;
    }

    // THIS METHOD ALSO EXISTS (alias)
    public String getCode() {
        return errorCode;
    }
}
