package com.zkrypto.zkmpc.domain.tss;

import java.util.Optional;

public interface TssRepository {
    Optional<Tss> findTssByGroupId(String groupId);

    void save(Tss tss);
}
