package com.zkrypto.zkmpc.infrastructure.amqp.dto;

import lombok.Builder;

@Builder
public record ProceedRoundCommand(
        String type,
        String message
) {
}
