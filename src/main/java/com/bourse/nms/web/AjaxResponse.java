package com.bourse.nms.web;

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

    @Override
    public String toString() {
        return "{" +
                "errorCode=" + errorCode +
                ", message='" + message + '\'' +
                '}';
    }
}
