package com.zkrypto.zkmpc.application.message.dto;

import lombok.Builder;

@Builder
public record RoundEndEvent(
        String message,
        String type,
        String sid
) {
}
