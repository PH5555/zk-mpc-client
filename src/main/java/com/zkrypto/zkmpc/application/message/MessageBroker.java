package com.zkrypto.zkmpc.application.message;

import com.zkrypto.zkmpc.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zkmpc.application.message.dto.ProtocolCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundEndEvent;

public interface MessageBroker {
    void publish(RoundEndEvent roundEndEvent);
    void publish(InitProtocolEndEvent initProtocolEndEvent);
    void publish(ProtocolCompleteEvent protocolCompleteEvent);
    void publish(RoundCompleteEvent roundCompleteEvent);
}
