package com.zkrypto.zkmpc.domain.tss;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "tss")
public class Tss {
    @Id
    private String id;
    private String groupId;
    private String[] groupMemberIds;
    @Setter
    private String auxInfo;
    @Setter
    private String shareInfo;
    @Setter
    private String preSign;

    private Tss(String groupId, String[] groupMemberIds) {
        this.groupId = groupId;
        this.groupMemberIds = groupMemberIds;
    }

    public static Tss create(String groupId, String[] groupMemberIds) {
        return new Tss(groupId, groupMemberIds);
    }
}
