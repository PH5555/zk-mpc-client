package com.zkrypto.zkmpc.domain.tss;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tss")
public class Tss {
    @Id
    private String id;
    private Long groupId;
    private Long keyPiece;
    private String auxInfo;
}
