package com.zkrypto.zkmpc.application.message.dto;

import com.zkrypto.zkmpc.application.tss.constant.ParticipantType;
import lombok.Builder;

@Builder
public record InitProtocolEndEvent(
        ParticipantType type,
        String sid,
        String memberId
) {
}
