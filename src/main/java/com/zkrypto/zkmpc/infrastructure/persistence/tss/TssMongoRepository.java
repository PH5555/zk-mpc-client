package com.zkrypto.zkmpc.infrastructure.persistence.tss;

import com.zkrypto.zkmpc.domain.tss.Tss;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TssMongoRepository extends MongoRepository<Tss, String> {
    Optional<Tss> findByGroupId(String groupId);
}
