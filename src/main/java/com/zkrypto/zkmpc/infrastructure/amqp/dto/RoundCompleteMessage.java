package com.zkrypto.zkmpc.infrastructure.amqp.dto;

import lombok.Builder;

@Builder
public record RoundCompleteMessage(
        String type,
        String roundName,
        String sid
) {
}