package com.zkrypto.zkmpc.infrastructure.web.tss.dto;

import com.zkrypto.zkmpc.infrastructure.web.tss.constant.ParticipantType;
import jakarta.validation.constraints.NotNull;

public record InitProtocolCommand(
        @NotNull ParticipantType participantType,
        @NotNull String sid,
        @NotNull String[] otherIds,
        Integer threshold,
        byte[] messageBytes
) {
}
