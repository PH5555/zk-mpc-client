package com.zkrypto.zkmpc.infrastructure.persistence.tss;

import com.zkrypto.zkmpc.common.exception.ErrorCode;
import com.zkrypto.zkmpc.common.exception.TssException;
import com.zkrypto.zkmpc.domain.tss.Tss;
import com.zkrypto.zkmpc.domain.tss.TssRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TssRepositoryAdapter {
    private final TssRepository tssRepository;

    public List<String> getAllGroupMemberIds(String groupId) {
        Tss tss = getTssByGroupId(groupId);
        return List.of(tss.getGroupMemberIds());
    }

    public void saveAuxInfo(String groupId, String auxInfo) {
        Tss tss = getTssByGroupId(groupId);
        tss.setAuxInfo(auxInfo);
        tssRepository.save(tss);
    }

    public String getAuxInfo(String groupId) {
        Tss tss = getTssByGroupId(groupId);
        return tss.getAuxInfo();
    }

    public String getTShare(String groupId) {
        Tss tss = getTssByGroupId(groupId);
        return tss.getShareInfo();
    }

    public void saveGroup(String groupId, String[] otherIds) {
        Tss tss = Tss.create(groupId, otherIds);
        tssRepository.save(tss);
    }

    public Tss getTssByGroupId(String groupId) {
        return tssRepository.findTssByGroupId(groupId)
                .orElseThrow(() -> new TssException(ErrorCode.NOT_FOUND_TSS));
    }
}
