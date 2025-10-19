package com.zkrypto.zkmpc.application.message.dto;

import lombok.Builder;

@Builder
public record RoundCompleteEvent(
        String type,
        String roundName,
        String sid
) {
}
