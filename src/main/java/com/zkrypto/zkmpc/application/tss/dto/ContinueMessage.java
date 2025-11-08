package com.zkrypto.zkmpc.application.tss.dto;

import com.zkrypto.zkmpc.application.tss.constant.Round;
import com.zkrypto.zkmpc.common.exception.ErrorCode;
import com.zkrypto.zkmpc.common.exception.TssException;
import lombok.Getter;
import lombok.ToString;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@ToString
@Getter
public class ContinueMessage {
    private Object type;
    private Map<String, String> message_type;
    private BigInteger identifier;
    private BigInteger from;
    private BigInteger to;
    private Boolean is_broadcast;
    private List<Integer> unverified_bytes;

    /**
     * 이 메시지가 어떤 라운드에 해당하는지 추출하여 반환합니다.
     * @return 해당하는 Round enum
     */
    public Round extractRound() {
        if (this.message_type == null || this.message_type.isEmpty()) {
            throw new TssException(ErrorCode.NOT_FOUND_ROUND_ERROR);
        }

        return this.message_type.values().stream()
                .findFirst()
                .map(Round::fromName)
                .orElseThrow(() -> new TssException(ErrorCode.NOT_FOUND_ROUND_ERROR));
    }
}
