package com.bourse.nms.common;

/**
 * Created by IntelliJ IDEA.
 * User: araz
 * Date: 6/1/12
 * Time: 6:21 PM
 */
public class NMSException extends Exception {
    private final int errorCode;
    private final String message;

    public NMSException(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
