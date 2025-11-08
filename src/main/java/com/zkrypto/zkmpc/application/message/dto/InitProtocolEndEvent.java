package com.zkrypto.zkmpc.application.message.dto;

import com.zkrypto.constant.ParticipantType;
import lombok.Builder;

@Builder
public record InitProtocolEndEvent(
        ParticipantType type,
        String sid,
        String memberId
) {
}
