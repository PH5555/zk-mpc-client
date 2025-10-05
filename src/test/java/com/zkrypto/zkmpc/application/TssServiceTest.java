package com.zkrypto.zkmpc.application;

import com.zkrypto.zkmpc.application.tss.TssService;
import com.zkrypto.zkmpc.infrastructure.web.tss.constant.ParticipantType;
import com.zkrypto.zkmpc.infrastructure.web.tss.dto.InitProtocolCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
public class TssServiceTest {
    @Autowired
    TssService tssService;
}
