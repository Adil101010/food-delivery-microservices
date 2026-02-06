package com.fooddelivery.paymentservice.exception;

public class PaymentException extends RuntimeException {
    private String errorCode;

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
