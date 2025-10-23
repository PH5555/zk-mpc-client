package com.zkrypto.zkmpc.application.message.dto;

import com.zkrypto.zkmpc.application.tss.constant.ParticipantType;
import lombok.Builder;

@Builder
public record ProtocolCompleteEvent(
        String sid,
        String memberId,
        ParticipantType type,
        String message,
        String pk
) {
}
