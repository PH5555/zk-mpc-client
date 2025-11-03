package com.zkrypto.zkmpc.infrastructure.persistence.tss;

import com.zkrypto.zkmpc.application.tss.TssRepositoryAdapter;
import com.zkrypto.zkmpc.common.exception.ErrorCode;
import com.zkrypto.zkmpc.common.exception.TssException;
import com.zkrypto.zkmpc.domain.tss.Tss;
import com.zkrypto.zkmpc.domain.tss.TssRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TssRepositoryAdapterImpl implements TssRepositoryAdapter {
    private final TssRepository tssRepository;

    public void saveAuxInfo(String groupId, String auxInfo) {
        Tss tss = getTssByGroupId(groupId);
        tss.setAuxInfo(auxInfo);
        tssRepository.save(tss);
    }

    public String getAuxInfo(String groupId) {
        Tss tss = getTssByGroupId(groupId);
        return tss.getAuxInfo();
    }

    @Override
    public void saveTShare(String groupId, String tShare) {
        Tss tss = getTssByGroupId(groupId);
        tss.setShareInfo(tShare);
        tssRepository.save(tss);
    }

    public String getTShare(String groupId) {
        Tss tss = getTssByGroupId(groupId);
        return tss.getShareInfo();
    }

    @Override
    public void saveTPresign(String groupId, String tPresign) {
        Tss tss = getTssByGroupId(groupId);
        tss.setPreSign(tPresign);
        tssRepository.save(tss);
    }

    @Override
    public String getTPresign(String groupId) {
        Tss tss = getTssByGroupId(groupId);
        return tss.getPreSign();
    }

    public void saveGroup(String groupId) {
        Tss tss = Tss.create(groupId);
        tssRepository.save(tss);
    }

    public Tss getTssByGroupId(String groupId) {
        return tssRepository.findTssByGroupId(groupId)
                .orElseThrow(() -> new TssException(ErrorCode.NOT_FOUND_TSS));
    }

    @Override
    public Boolean existTssByGroupId(String groupId) {
        return tssRepository.existTssByGroupId(groupId);
    }
}
