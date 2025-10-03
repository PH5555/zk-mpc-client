package com.zkrypto.zkmpc.domain.tss;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "tss")
public class Tss {
    @Id
    private String id;
    private String groupId;
    private String[] groupMemberIds;
    private String auxInfo;
    private String shareInfo;

    public static Tss createTss() {

    }
}
