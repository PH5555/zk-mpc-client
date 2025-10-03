package com.zkrypto.zkmpc.infrastructure.web.tss;

import com.zkrypto.zkmpc.application.tss.TssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tss")
@RequiredArgsConstructor
public class TssController {
    private final TssService tssService;

}
