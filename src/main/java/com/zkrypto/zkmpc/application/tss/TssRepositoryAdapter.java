package com.zkrypto.zkmpc.application.tss;

import com.zkrypto.zkmpc.domain.tss.Tss;

import java.util.List;

public interface TssRepositoryAdapter {
    List<String> getAllGroupMemberIds(String groupId);
    void saveAuxInfo(String groupId, String auxInfo);
    String getAuxInfo(String groupId);
    String getTShare(String groupId);
    String getTPresign(String groupId);
    void saveGroup(String groupId, String[] otherIds);
    Tss getTssByGroupId(String groupId);
}