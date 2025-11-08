package com.zkrypto.zkmpc.application.message.dto;

import com.zkrypto.constant.ParticipantType;
import lombok.Builder;

@Builder
public record ProtocolCompleteEvent(
        String sid,
        String memberId,
        ParticipantType type
) {
}
