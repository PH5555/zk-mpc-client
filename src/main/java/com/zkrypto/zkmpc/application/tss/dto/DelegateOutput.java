package com.zkrypto.zkmpc.application.tss.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zkrypto.zkmpc.common.serializer.DelegateOutputDeserializer;
import com.zkrypto.zkmpc.application.tss.constant.DelegateOutputStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@JsonDeserialize(using = DelegateOutputDeserializer.class)
public class DelegateOutput {
    private DelegateOutputStatus delegateOutputStatus;
    private List<ContinueMessage> continueMessages;
    private DoneMessage doneMessage;
}
