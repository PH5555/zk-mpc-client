package com.zkrypto.zkmpc.application.tss;

import com.zkrypto.zkmpc.domain.tss.Tss;

import java.util.Optional;

public interface TssRepositoryAdapter {
    void saveAuxInfo(String groupId, String auxInfo);
    String getAuxInfo(String groupId);

    void saveTShare(String groupId, String tShare);
    String getTShare(String groupId);

    void saveTPresign(String groupId, String tPresign);
    String getTPresign(String groupId);
    void saveGroup(String groupId);
    Tss getTssByGroupId(String groupId);
    Boolean existTssByGroupId(String groupId);
}