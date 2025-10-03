package com.zkrypto.zkmpc.infrastructure.persistence.tss;

import com.zkrypto.zkmpc.domain.tss.Tss;
import com.zkrypto.zkmpc.domain.tss.TssRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TssRepositoryImpl implements TssRepository {
    private final TssMongoRepository tssMongoRepository;

    @Override
    public Optional<Tss> findTssByGroupId(String groupId) {
        return tssMongoRepository.findByGroupId(groupId);
    }

    @Override
    public void save(Tss tss) {
        tssMongoRepository.save(tss);
    }
}
