package com.zkrypto.zkmpc.infrastructure.amqp.mapper;

import com.zkrypto.dto.InitProtocolEndMessage;
import com.zkrypto.dto.ProceedRoundMessage;
import com.zkrypto.dto.ProtocolCompleteMessage;
import com.zkrypto.dto.RoundCompleteMessage;
import com.zkrypto.zkmpc.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zkmpc.application.message.dto.ProtocolCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundEndEvent;

public class MessageMapper {
    public static ProceedRoundMessage from(RoundEndEvent event) {
        return new ProceedRoundMessage(event.type(), event.message(), event.sid());
    }

    public static InitProtocolEndMessage from(InitProtocolEndEvent event) {
        return new InitProtocolEndMessage(event.type(), event.sid(), event.memberId());
    }

    public static ProtocolCompleteMessage from(ProtocolCompleteEvent event) {
        return new ProtocolCompleteMessage(event.sid(), event.memberId(), event.type(), event.message());
    }

    public static RoundCompleteMessage from(RoundCompleteEvent event) {
        return new RoundCompleteMessage(event.type(), event.roundName(), event.sid());
    }
}
