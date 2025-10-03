package com.zkrypto.zkmpc.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    JSON_PARSE_ERROR("J001", HttpStatus.BAD_REQUEST, "Json 파싱에 실패했습니다."),
    PARTICIPANT_TYPE_ERROR("T001", HttpStatus.BAD_REQUEST, "participant type이 잘못됐습니다."),
    NOT_FOUND_TSS("T002", HttpStatus.NOT_FOUND, "TSS를 찾을 수 없습니다."),
    ;

    private String errorCode;
    private HttpStatus httpStatus;
    private String message;
}