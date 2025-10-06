package com.zkrypto.zkmpc.infrastructure.web.tss;

import com.zkrypto.zkmpc.application.tss.TssService;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.StartRoundCommand;
import com.zkrypto.zkmpc.infrastructure.web.tss.dto.InitProtocolCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tss")
@RequiredArgsConstructor
public class TssController {
    private final TssService tssService;

    @PostMapping("/init")
    public void initProtocol(@RequestBody InitProtocolCommand initProtocolCommand) {
        tssService.initProtocol(initProtocolCommand);
    }

    @PostMapping("/start")
    public void startProtocol(@RequestBody StartRoundCommand startRoundCommand) {
        tssService.startRound(startRoundCommand);
    }
}
