package com.clinicHelper.exceptions;

import org.springframework.http.HttpStatus;

public class BusinessException  extends ApiException  {
  public BusinessException(String message) {
    super(HttpStatus.BAD_REQUEST, "BUSINESS_ERROR", message);
  }

  public BusinessException(String message, Throwable cause) {
    super(HttpStatus.BAD_REQUEST, "BUSINESS_ERROR", message);
    initCause(cause);
  }
}
