package com.zkrypto.zkmpc.infrastructure.amqp.dto;

import com.zkrypto.zkmpc.infrastructure.web.tss.constant.ParticipantType;

public record StartRoundCommand(
        ParticipantType participantType,
        String sid
) {
}
