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
import com.zkrypto.zkmpc.infrastructure.web.tss.constant.ParticipantType;
import com.zkrypto.zkmpc.infrastructure.web.tss.dto.InitProtocolCommand;
import com.zkrypto.zkmpc.infrastructure.web.tss.dto.ProceedRoundCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TssService {
    @Value("${client.id}")
    private String clientId;

    private final TssRepository tssRepository;

    /**
     * 프로토콜에 대한 메시지를 생성하고 라운드를 시작하는 메서드입니다.
     * @param command
     */
    public void initProtocol(InitProtocolCommand command) {
        // 팩토리 생성
        TssBridge.participantFactory(
                command.participantType().getTypeName(),
                command.sid(),
                clientId,
                command.otherIds(),
                generateInput(command)
        );

        // ready message 생성
        String readyMessage = TssBridge.readyMessageFactory(
                command.participantType().getTypeName(),
                command.sid(),
                clientId
        );

        proceedRound(new ProceedRoundCommand(command.participantType(), readyMessage));
    }

    private String generateInput(InitProtocolCommand command) {
        if(command.participantType() == ParticipantType.AUXINFO) {
            return "";
        }

        if(command.participantType() == ParticipantType.TSHARE) {
            return TssBridge.generateTshareInput(
                    getTssByGroupId(command.sid()).getAuxInfo(),
                    3 //TODO: 스레스홀드 임시 데이터
            );
        }

        if(command.participantType() == ParticipantType.TPRESIGN) {
            return TssBridge.generateTpresignInput();
        }

        if(command.participantType() == ParticipantType.SIGN) {
            return TssBridge.generateSignInput();
        }

        if(command.participantType() == ParticipantType.TREFRESH) {
            return TssBridge.generateTrefreshInput();
        }

        throw new TssException(ErrorCode.PARTICIPANT_TYPE_ERROR);
    }

    /**
     * 메시지를 처리하고 다음 단계의 메시지를 생성 후 전달하는 메서드입니다.
     * @param command
     */
    public void proceedRound(ProceedRoundCommand command) {
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
        List<String> recipients = message.getIs_broadcast()
                ? getAllGroupMemberIds(message.getIdentifier().toString())    // Is_broadcast이면 모든 참여자
                : List.of(message.getTo().toString()); // 아니면 한명

        // TODO: 메시지 큐 전달
        // messageQueue.send(recipients, message.getContent(), sid);
        log.info(recipients.size() + "명에게 메시지 전송: " + recipients);
    }

    private List<String> getAllGroupMemberIds(String groupId) {
        Tss tss = getTssByGroupId(groupId);
        return List.of(tss.getGroupMemberIds());
    }

    private void saveAuxInfo(String auxInfo) {
        // TODO: 저장 구현

    }

    private Tss getTssByGroupId(String groupId) {
        return tssRepository.findTssByGroupId(groupId)
                .orElseThrow(() -> new TssException(ErrorCode.NOT_FOUND_TSS));
    }
}
