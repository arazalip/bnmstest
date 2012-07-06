package com.bourse.nms.web;

import com.bourse.nms.common.NMSException;

/**
 * the response that is to be returned by servlets
 */
public class AjaxResponse {

    private final int errorCode;
    private final String message;

    public AjaxResponse(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public AjaxResponse(NMSException e){
        this.errorCode = e.getErrorCode();
        this.message = e.getMessage();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "{" +
                "code=" + errorCode +
                ", message='" + message + '\'' +
                '}';
    }
}
