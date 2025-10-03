package com.zkrypto.zkmpc.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    JSON_PARSE_ERROR("J001", HttpStatus.BAD_REQUEST, "Json 파싱에 실패했습니다.");

    private String errorCode;
    private HttpStatus httpStatus;
    private String message;
}