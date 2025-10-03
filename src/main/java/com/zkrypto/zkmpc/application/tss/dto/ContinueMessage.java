package com.zkrypto.zkmpc.application.tss.dto;

import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@Getter
public class ContinueMessage {
    private Object message_type;
    private BigInteger identifier;
    private BigInteger from;
    private BigInteger to;
    private Boolean is_broadcast;
    private List<Integer> unverified_bytes;
}
