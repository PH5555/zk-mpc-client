package com.zkrypto.zkmpc.application.tss;

import com.zkrypto.cryptolib.TssBridge;
import com.zkrypto.zkmpc.application.message.MessageBroker;
import com.zkrypto.zkmpc.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zkmpc.application.message.dto.ProtocolCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundEndEvent;
import com.zkrypto.zkmpc.application.tss.constant.Round;
import com.zkrypto.zkmpc.application.tss.dto.ContinueMessage;
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
     * @param initProtocolMessage initProtocolMessage
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
     * @param initProtocolMessage initProtocolMessage
     * @return input 데이터
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
                    initProtocolMessage.participantIds(),
                    tssAdapter.getAuxInfo(initProtocolMessage.sid()),
                    tssAdapter.getTShare(initProtocolMessage.sid())
            );
        }

        if(initProtocolMessage.participantType() == ParticipantType.SIGN) {
            return TssBridge.generateSignInput(
                    clientId,
                    initProtocolMessage.participantIds(),
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
     * @param message message
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
     * 라운드를 진행하는 메서드입니다.
     * continue 메시지일 경우 core 서버에 메시지를 전송합니다.
     * Done 메시지일 경우 type에 맞는 처리를 하고 core 서버에 종료 메시지를 전송합니다.
     * @param type 메시지 타입
     * @param message 메시지
     * @param sid 그룹 id
     */
    public void proceedRound(String type, String message, String sid) {
        // 메시지 처리 및 결과 파싱
        log.info("delegate Process 시작");
        String processResult = TssBridge.delegateProcessMessage(type, message);
        log.info("delegate Process 결과 : " + StringUtils.abbreviate(processResult, 200));

        DelegateOutput output = (DelegateOutput) JsonUtil.parse(processResult, DelegateOutput.class);

        // 결과 상태에 따라 분기 처리
        if (output.getDelegateOutputStatus() == DelegateOutputStatus.CONTINUE) {
            handleContinue(output, message, type, sid, processResult);
        } else if (output.getDelegateOutputStatus() == DelegateOutputStatus.DONE) {
            handleDone(output, type, sid);
        }
    }

    /**
     * continue 메시지를 처리하는 메서드입니다.
     * continue 배열이 비어있지 않으면 라운드 종료 메시지를 전송합니다.
     * continue 배열이 비어있으면 다음 라운드 진행을 위해 현재 라운드 완료 메시지를 전송합니다.
     * @param output delegate 결과 파싱 output
     * @param originalMessage 메시지
     * @param type 메시지 타입
     * @param sid 그룹 id
     * @param processResult delegate 결과
     */
    private void handleContinue(DelegateOutput output, String originalMessage, String type, String sid, String processResult) {
        if (!output.getContinueMessages().isEmpty()) {
            // 처리할 메시지가 남아있는 경우: 다음 라운드로 메시지 전달
            log.info("core 서버로 process 결과 전송");
            RoundEndEvent event = RoundEndEvent.builder()
                    .message(processResult)
                    .type(type)
                    .sid(sid)
                    .build();
            messageBroker.publish(event);
        } else {
            // 처리할 메시지가 없는 경우: 현재 라운드 완료
            ContinueMessage continueMessage = (ContinueMessage)JsonUtil.parse(originalMessage, ContinueMessage.class);
            Round round = continueMessage.extractRound();

            log.info("{} 완료 메시지 전송", round.getName());
            RoundCompleteEvent event = RoundCompleteEvent.builder()
                    .roundName(round.getName())
                    .sid(sid)
                    .type(type)
                    .build();
            messageBroker.publish(event);
        }
    }

    /**
     * Done 메시지를 처리하는 메서드입니다.
     * 메시지 타입에 따라 결과를 저장합니다.
     * core 서버에 종료 메시지를 전송합니다.
     * @param output 결과
     * @param type 메시지 타입
     * @param sid 그룹 id
     */
    private void handleDone(DelegateOutput output, String type, String sid) {
        // 결과 저장
        saveOutput(output, type, sid);

        // 프로토콜 종료 이벤트 발행
        log.info("{} 종료 메시지 전송", type);
        String publicKey = tssAdapter.getPublicKey(sid);
        ProtocolCompleteEvent event = ProtocolCompleteEvent.builder()
                .type(ParticipantType.of(type))
                .memberId(clientId)
                .sid(sid)
                .message(output.getDoneMessage().toString())
                .pk(publicKey)
                .build();
        messageBroker.publish(event);
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
            String publicKey = TssBridge.getMasterKey(output.getDoneMessage().toString());
            log.info("pk : {}", publicKey);
            tssAdapter.saveTShare(sid, output.getDoneMessage().toString());
            tssAdapter.savePublicKey(sid, publicKey);
            return;
        }
        if(type.equals(ParticipantType.TPRESIGN.getTypeName())) {
            tssAdapter.saveTPresign(sid, output.getDoneMessage().toString());
            return;
        }
    }
}
