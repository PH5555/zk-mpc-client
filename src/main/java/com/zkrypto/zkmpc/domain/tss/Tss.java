package com.zkrypto.zkmpc.domain.tss;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "tss")
public class Tss {
    @Id
    private String id;
    @Indexed(unique = true)
    private String groupId;
    @Setter
    private String auxInfo;
    @Setter
    private String shareInfo;
    @Setter
    private String preSign;
    @Setter
    private String publicKey;

    private Tss(String groupId) {
        this.groupId = groupId;
    }

    public static Tss create(String groupId) {
        return new Tss(groupId);
    }
}
