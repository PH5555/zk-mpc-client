package com.zkrypto.zkmpc.infrastructure.amqp.mapper;

import com.zkrypto.zkmpc.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zkmpc.application.message.dto.ProtocolCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundEndEvent;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.InitProtocolEndMessage;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.ProceedRoundMessage;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.ProtocolCompleteMessage;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.RoundCompleteMessage;

public class MessageMapper {
    public static ProceedRoundMessage from(RoundEndEvent event) {
        return ProceedRoundMessage.builder().message(event.message()).type(event.type()).sid(event.sid()).build();
    }

    public static InitProtocolEndMessage from(InitProtocolEndEvent event) {
        return InitProtocolEndMessage.builder()
                .sid(event.sid())
                .memberId(event.memberId())
                .type(event.type())
                .build();
    }

    public static ProtocolCompleteMessage from(ProtocolCompleteEvent event) {
        return ProtocolCompleteMessage.builder()
                .sid(event.sid())
                .memberId(event.memberId())
                .type(event.type())
                .build();
    }

    public static RoundCompleteMessage from(RoundCompleteEvent event) {
        return RoundCompleteMessage.builder()
                .roundName(event.roundName())
                .sid(event.sid())
                .type(event.type())
                .build();
    }
}
