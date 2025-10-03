package com.zkrypto.zkmpc.infrastructure.persistence.tss;

import com.zkrypto.zkmpc.domain.tss.TssRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TssRepositoryImpl implements TssRepository {
    private final TssMongoRepository tssMongoRepository;
}
