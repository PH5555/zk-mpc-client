package com.zkrypto.zkmpc.infrastructure.amqp.dto;

import lombok.Builder;

@Builder
public record ProceedRoundMessage(
        String type,
        String message,
        String sid
) {
}
