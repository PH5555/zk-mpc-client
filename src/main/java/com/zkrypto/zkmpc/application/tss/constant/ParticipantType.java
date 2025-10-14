package com.zkrypto.zkmpc.application.tss.constant;

import com.zkrypto.zkmpc.common.exception.ErrorCode;
import com.zkrypto.zkmpc.common.exception.TssException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ParticipantType {
    AUXINFO("AuxInfo"),
    TSHARE("TShare"),
    TREFRESH("TRefresh"),
    TPRESIGN("TPreSign"),
    SIGN("Sign");

    private String typeName;

    public static ParticipantType of(String typeName) {
        return Arrays.stream(ParticipantType.values())
                .filter(type -> type.getTypeName().equals(typeName))
                .findFirst()
                .orElseThrow(() -> new TssException(ErrorCode.PARTICIPANT_TYPE_ERROR));
    }
}
