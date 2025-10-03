package com.zkrypto.zkmpc.infrastructure.web.tss.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ParticipantType {
    AUXINFO("AuxInfo"), TSHARE("TShare"), TREFRESH("TRefresh"), TPRESIGN("TPreSign"), SIGN("Sign");

    private String typeName;
}
