package com.zkrypto.zkmpc.infrastructure.amqp.dto;

import com.zkrypto.zkmpc.application.tss.constant.ParticipantType;
import lombok.Builder;

@Builder
public record StartProtocolMessage(
        ParticipantType type,
        String sid
) {
}
