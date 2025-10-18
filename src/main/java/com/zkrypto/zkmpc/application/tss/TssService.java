package com.zkrypto.zkmpc.application.tss;

import com.zkrypto.cryptolib.TssBridge;
import com.zkrypto.zkmpc.application.message.MessageBroker;
import com.zkrypto.zkmpc.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zkmpc.application.message.dto.ProtocolCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundEndEvent;
import com.zkrypto.zkmpc.application.tss.dto.DelegateOutput;
import com.zkrypto.zkmpc.application.tss.constant.DelegateOutputStatus;
import com.zkrypto.zkmpc.common.exception.ErrorCode;
import com.zkrypto.zkmpc.common.exception.TssException;
import com.zkrypto.zkmpc.common.util.JsonUtil;
import com.zkrypto.zkmpc.application.tss.constant.ParticipantType;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.InitProtocolMessage;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.StartProtocolMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TssService {
    @Value("${client.id}")
    private String clientId;
    private final MessageBroker messageBroker;
    private final TssRepositoryAdapter tssAdapter;

    /**
     * 라운드를 시작하기 위해 participantFactory를 실행하는 메서드입니다.
     * @param initProtocolMessage
     */
    public void initProtocol(InitProtocolMessage initProtocolMessage) {
        // 팩토리 생성
        log.info("{} 팩토리 생성 시작", initProtocolMessage.participantType());
        TssBridge.participantFactory(
                initProtocolMessage.participantType().getTypeName(),
                initProtocolMessage.sid(),
                clientId,
                initProtocolMessage.otherIds(),
                generateInput(initProtocolMessage)
        );
        log.info("팩토리 생성 끝");

        // 타입이 AUXINFO이면 그룹 정보 저장
        if(initProtocolMessage.participantType() == ParticipantType.AUXINFO) {
            tssAdapter.saveGroup(initProtocolMessage.sid());
        }

        // 팩토리 생성 완료 메시지 전송
        log.info("프로토콜 초기화 완료 메시지 전송");
        InitProtocolEndEvent event = InitProtocolEndEvent.builder().memberId(clientId).type(initProtocolMessage.participantType()).sid(initProtocolMessage.sid()).build();
        messageBroker.publish(event);
    }

    /**
     * participantFactory를 실행하기 위한 input을 생성하는 메서드입니다.
     * @param initProtocolMessage
     * @return
     */
    private String generateInput(InitProtocolMessage initProtocolMessage) {
        if(initProtocolMessage.participantType() == ParticipantType.AUXINFO) {
            return "";
        }

        if(initProtocolMessage.participantType() == ParticipantType.TSHARE) {
            return TssBridge.generateTshareInput(
                    tssAdapter.getAuxInfo(initProtocolMessage.sid()),
                    initProtocolMessage.threshold()
            );
        }

        if(initProtocolMessage.participantType() == ParticipantType.TPRESIGN) {
            return TssBridge.generateTpresignInput(
                    initProtocolMessage.otherIds(),
                    tssAdapter.getAuxInfo(initProtocolMessage.sid()),
                    tssAdapter.getTShare(initProtocolMessage.sid())
            );
        }

        if(initProtocolMessage.participantType() == ParticipantType.SIGN) {
            return TssBridge.generateSignInput(
                    clientId,
                    initProtocolMessage.otherIds(),
                    tssAdapter.getTPresign(initProtocolMessage.sid()),
                    tssAdapter.getTShare(initProtocolMessage.sid()),
                    initProtocolMessage.messageBytes(),
                    initProtocolMessage.threshold()
            );
        }

        if(initProtocolMessage.participantType() == ParticipantType.TREFRESH) {
            return TssBridge.generateTrefreshInput(
                    tssAdapter.getTShare(initProtocolMessage.sid()),
                    tssAdapter.getAuxInfo(initProtocolMessage.sid()),
                    initProtocolMessage.threshold()
            );
        }

        throw new TssException(ErrorCode.PARTICIPANT_TYPE_ERROR);
    }

    /**
     * ready message를 생성하여 라운드를 시작하는 메서드입니다.
     * @param message
     */
    public void startProtocol(StartProtocolMessage message) {
        // ready message 생성
        log.info("{} ready message 생성 시작", message.type());
        String readyMessage = TssBridge.readyMessageFactory(
                message.type().getTypeName(),
                message.sid(),
                clientId
        );
        log.info("ready message 생성 완료 {}", StringUtils.abbreviate(readyMessage, 200));

        proceedRound(message.type().getTypeName(), readyMessage, message.sid());
    }

    /**
     * 메시지를 처리하고 다음 단계의 메시지를 생성 후 전달하는 메서드입니다.
     * @param message
     */
    public void proceedRound(String type, String message, String sid) {
        // 받은 메시지 delegateProcessMessage 실행
        log.info("delegate Process 시작");
        String processResult = TssBridge.delegateProcessMessage(type, message);
        log.info("delegate Process 결과 : " + StringUtils.abbreviate(processResult, 200));

        // output 파싱
        DelegateOutput output = (DelegateOutput)JsonUtil.parse(processResult, DelegateOutput.class);

        if(output.getDelegateOutputStatus() == DelegateOutputStatus.CONTINUE && !output.getContinueMessages().isEmpty()) {
            // output 결과가 continue 이고 빈 배열이 아니면 메시지 전송
            log.info("core 서버로 process 결과 전송");
            RoundEndEvent event = RoundEndEvent.builder().message(processResult).type(type).sid(sid).build();
            messageBroker.publish(event);
        }
        else if(output.getDelegateOutputStatus() == DelegateOutputStatus.CONTINUE && output.getContinueMessages().isEmpty()) {
            log.info("continue 빈배열");
        }
        else if(output.getDelegateOutputStatus() == DelegateOutputStatus.DONE) {
            // output 결과 저장
            saveOutput(output, type, sid);

            // 종료 메시지 전달
            log.info("{} 종료 메시지 전송", type);
            ProtocolCompleteEvent event = ProtocolCompleteEvent.builder()
                    .type(ParticipantType.of(type))
                    .memberId(clientId)
                    .sid(sid)
                    .build();
            messageBroker.publish(event);
        }
    }

    /**
     * 프로토콜 종료시 타입에 따라 데이터를 저장하는 메서드입니다.
     * @param output 종료 데이터
     * @param type 프로토콜 타입
     */
    private void saveOutput(DelegateOutput output, String type, String sid) {
        if(type.equals(ParticipantType.AUXINFO.getTypeName())) {
            tssAdapter.saveAuxInfo(sid, output.getDoneMessage().toString());
            return;
        }
        if(type.equals(ParticipantType.TSHARE.getTypeName())) {
            tssAdapter.saveTShare(sid, output.getDoneMessage().toString());
            return;
        }
        if(type.equals(ParticipantType.TPRESIGN.getTypeName())) {
            tssAdapter.saveTPresign(sid, output.getDoneMessage().toString());
            return;
        }
    }
}
