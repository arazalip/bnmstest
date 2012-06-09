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

    public enum ErrorCode {
        SUCCESS(0),
        INVALID_STATE_FOR_PUT_ORDER(-1);

        private ErrorCode(int code){
            this.code = code;
        }
        private final int code;

        public int getCode() {
            return code;
        }

    }

    public NMSException(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public NMSException(ErrorCode e, String msg) {
        this.errorCode = e.getCode();
        this.message = msg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
