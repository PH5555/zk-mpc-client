package com.zkrypto.zkmpc.application.tss;

import com.zkrypto.cryptolib.TssBridge;
import com.zkrypto.zkmpc.application.tss.dto.DelegateOutput;
import com.zkrypto.zkmpc.application.tss.constant.DelegateOutputStatus;
import com.zkrypto.zkmpc.common.exception.ErrorCode;
import com.zkrypto.zkmpc.common.exception.TssException;
import com.zkrypto.zkmpc.common.util.JsonUtil;
import com.zkrypto.zkmpc.application.tss.dto.ContinueMessage;
import com.zkrypto.zkmpc.domain.tss.Tss;
import com.zkrypto.zkmpc.domain.tss.TssRepository;
import com.zkrypto.zkmpc.application.tss.TssRepositoryAdapter;
import com.zkrypto.zkmpc.infrastructure.web.tss.constant.ParticipantType;
import com.zkrypto.zkmpc.infrastructure.web.tss.dto.InitProtocolCommand;
import com.zkrypto.zkmpc.infrastructure.web.tss.dto.ProceedRoundCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TssService {
    @Value("${client.id}")
    private String clientId;
    private final TssRepositoryAdapter tssAdapter;
    private final TssMessageBroker tssMessageBroker;

    /**
     * 프로토콜에 대한 메시지를 생성하고 라운드를 시작하는 메서드입니다.
     * @param command
     */
    public void initProtocol(InitProtocolCommand command) {
        // 팩토리 생성
        log.info("팩토리 생성 시작");
        TssBridge.participantFactory(
                command.participantType().getTypeName(),
                command.sid(),
                clientId,
                command.otherIds(),
                generateInput(command)
        );
        log.info("팩토리 생성 끝");

        // ready message 생성
        log.info("ready message 생성 시작");
        String readyMessage = TssBridge.readyMessageFactory(
                command.participantType().getTypeName(),
                command.sid(),
                clientId
        );
        log.info("ready message 생성 완료 {}", readyMessage.substring(10) + "...");

        proceedRound(readyMessage);
    }

    /**
     * participantFactory를 실행하기 위한 input을 생성하는 메서드입니다.
     * @param command
     * @return
     */
    private String generateInput(InitProtocolCommand command) {
        if(command.participantType() == ParticipantType.AUXINFO) {
            tssAdapter.saveGroup(command.sid(), command.otherIds());
            return "";
        }

        if(command.participantType() == ParticipantType.TSHARE) {
            return TssBridge.generateTshareInput(
                    tssAdapter.getAuxInfo(command.sid()),
                    command.threshold()
            );
        }

        if(command.participantType() == ParticipantType.TPRESIGN) {
            return TssBridge.generateTpresignInput(
                    command.otherIds(),
                    tssAdapter.getAuxInfo(command.sid()),
                    tssAdapter.getTShare(command.sid())
            );
        }

        if(command.participantType() == ParticipantType.SIGN) {
            return TssBridge.generateSignInput(
                    clientId,
                    command.otherIds(),
                    tssAdapter.getTPresign(command.sid()),
                    tssAdapter.getTShare(command.sid()),
                    command.messageBytes(),
                    command.threshold()
            );
        }

        if(command.participantType() == ParticipantType.TREFRESH) {
            return TssBridge.generateTrefreshInput(
                    tssAdapter.getTShare(command.sid()),
                    tssAdapter.getAuxInfo(command.sid()),
                    command.threshold()
            );
        }

        throw new TssException(ErrorCode.PARTICIPANT_TYPE_ERROR);
    }

    /**
     * 메시지를 처리하고 다음 단계의 메시지를 생성 후 전달하는 메서드입니다.
     * @param message
     */
    public void proceedRound(String message) {
        // 받은 메시지에서 type 추출
        ContinueMessage parsedMessage = (ContinueMessage)JsonUtil.parse(message, ContinueMessage.class);
        String type = parsedMessage.getMessage_type().keySet().stream().findFirst().get();

        // 받은 메시지 delegateProcessMessage 실행
        log.info("delegate Process 시작");
        String processResult = TssBridge.delegateProcessMessage(type, message);

        // output 파싱
        DelegateOutput output = (DelegateOutput)JsonUtil.parse(processResult, DelegateOutput.class);
        log.info("delegate output: {}", output.toString());

        if(output.getDelegateOutputStatus() == DelegateOutputStatus.CONTINUE && !output.getContinueMessages().isEmpty()) {
            // output 결과가 continue 이고 빈 배열이 아니면 메시지 전송
            log.info("메시지 전송 시작");
            sendAllMessages(output.getContinueMessages());
        }
        else if(output.getDelegateOutputStatus() == DelegateOutputStatus.DONE) {
            // output 결과가 Done 이면 auxinfo 저장
            log.info("auxinfo 저장");
            tssAdapter.saveAuxInfo("temp", JsonUtil.toString(output.getDoneMessage()));
        }
    }

    private void sendAllMessages(List<ContinueMessage> continueMessages) {
        // broadcast 먼저 처리하도록 정렬
        continueMessages.sort(Comparator.comparing(ContinueMessage::getIs_broadcast).reversed());

        // 메시지 목록을 순회하며 각 메시지를 처리
        continueMessages.forEach(this::processAndSendMessage);
    }

    private void processAndSendMessage(ContinueMessage message) {
        // 메시지 수신자 결정
        List<String> recipients = message.getIs_broadcast()
                ? tssAdapter.getAllGroupMemberIds(message.getIdentifier().toString()) // Is_broadcast이면 모든 참여자
                : List.of(message.getTo().toString()); // 아니면 한명

        // 각 수신자에게 메시지 전송
        recipients.forEach(recipient -> {
            tssMessageBroker.publish(recipient, JsonUtil.toString(message));
        });
    }
}
