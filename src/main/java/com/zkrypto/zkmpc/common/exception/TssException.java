package com.zkrypto.zkmpc.common.exception;

public class TssException extends RuntimeException{
    private final ErrorCode errorCode;

    public TssException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
