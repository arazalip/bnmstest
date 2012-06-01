package com.bourse.nms.web;

import com.bourse.nms.common.NMSException;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/1/12
 * Time: 6:29 PM
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
    @Override
    public String toString() {
        return "{" +
                "code=" + errorCode +
                ", message='" + message + '\'' +
                '}';
    }
}
