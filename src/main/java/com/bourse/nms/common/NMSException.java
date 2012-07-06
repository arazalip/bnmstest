package com.bourse.nms.common;

/**
 * application exceptions
 */
public class NMSException extends Exception {
    private final int errorCode;
    private final String message;

    public enum ErrorCode {
        SUCCESS(0),
        INVALID_STATE_FOR_PUT_ORDER(-1),
        FILE_UPLOAD_EXCEPTION(-2),
        INVALID_SYMBOLS_FILE(-3),
        SETTINGS_ERROR(-4),
        INTERNAL_SERVER_ERROR(-5);

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
