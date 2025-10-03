package com.zkrypto.zkmpc.infrastructure.web.tss.dto;

import com.zkrypto.zkmpc.infrastructure.web.tss.constant.ParticipantType;
import jakarta.validation.constraints.NotNull;

public record ProceedRoundCommand(
        @NotNull
        ParticipantType participantType,
        @NotNull
        String message
) {
}
