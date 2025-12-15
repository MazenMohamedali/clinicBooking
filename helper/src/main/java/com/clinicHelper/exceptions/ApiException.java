package com.clinicHelper.exceptions;
import org.springframework.http.HttpStatus;


public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    protected ApiException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }


    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "API_ERROR";
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
