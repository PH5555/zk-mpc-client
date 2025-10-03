package com.zkrypto.zkmpc.application.tss;

import com.zkrypto.cryptolib.TssBridge;
import com.zkrypto.zkmpc.application.tss.dto.DelegateOutput;
import com.zkrypto.zkmpc.application.tss.constant.DelegateOutputStatus;
import com.zkrypto.zkmpc.common.util.JsonUtil;
import com.zkrypto.zkmpc.application.tss.dto.ContinueMessage;
import com.zkrypto.zkmpc.infrastructure.web.tss.dto.ProcessMessageCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class TssService {


    public void proceedRound(ProcessMessageCommand command) {
        // 받은 메시지 delegateProcessMessage 실행
        String processResult = TssBridge.delegateProcessMessage(command.participantType().getTypeName(), command.message());

        // output 파싱
        DelegateOutput output = (DelegateOutput)JsonUtil.parse(processResult, DelegateOutput.class);

        if(output.getDelegateOutputStatus() == DelegateOutputStatus.CONTINUE && !output.getContinueMessages().isEmpty()) {
            // output 결과가 continue 이고 빈 배열이 아니면 메시지 전송
            sendAllMessages(output.getContinueMessages());
        }
        else if(output.getDelegateOutputStatus() == DelegateOutputStatus.DONE) {
            // output 결과가 Done 이면 auxinfo 저장
            saveAuxInfo(JsonUtil.toString(output.getDoneMessage()));
        }
    }

    private void sendAllMessages(List<ContinueMessage> continueMessages) {
        // 메시지 목록을 순회하며 각 메시지를 처리
        continueMessages.forEach(this::processAndSendMessage);
    }

    private void processAndSendMessage(ContinueMessage message) {
        // 메시지 수신자 결정
        List<BigInteger> recipients = message.getIs_broadcast()
                ? getAllGroupMemberIds()    // Is_broadcast이면 모든 참여자
                : List.of(message.getTo()); // 아니면 한명

        // TODO: 메시지 큐 전달
        // messageQueue.send(recipients, message.getContent());
        log.info(recipients.size() + "명에게 메시지 전송: " + recipients);
    }

    private List<BigInteger> getAllGroupMemberIds() {
        // TODO: api 호출 구현
        return null;
    }

    private void saveAuxInfo(String auxInfo) {
        // TODO: 저장 구현

    }
}
