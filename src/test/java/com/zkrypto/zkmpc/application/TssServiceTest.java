package com.zkrypto.zkmpc.application;

import com.zkrypto.zkmpc.application.tss.TssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TssServiceTest {
    @Autowired
    TssService tssService;
}
