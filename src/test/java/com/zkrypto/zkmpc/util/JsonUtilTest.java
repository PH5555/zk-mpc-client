package com.zkrypto.zkmpc.util;

import com.zkrypto.zkmpc.application.tss.dto.DelegateOutput;
import com.zkrypto.zkmpc.application.tss.constant.DelegateOutputStatus;
import com.zkrypto.zkmpc.common.util.JsonUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonUtilTest {
    @Test
    void parse_continue1() {
        String json = "{\n" +
                "    \"Continue\": [\n" +
                "      {\n" +
                "        \"message_type\": { \"Auxinfo\": \"R1CommitHash\" },\n" +
                "        \"identifier\": 307538744514897405711774840416830024438,\n" +
                "        \"from\": 171902912312527646629302371634440736718,\n" +
                "        \"to\": null,\n" +
                "        \"is_broadcast\": true,\n" +
                "        \"unverified_bytes\": [\n" +
                "          53, 69, 29, 119, 0, 253, 113, 142, 136, 202, 224, 58, 167, 178, 57,\n" +
                "          122, 107, 255, 41, 57, 81, 174, 17, 154, 121, 233, 213, 49, 112, 75,\n" +
                "          98, 76\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }";
        DelegateOutput message = (DelegateOutput) JsonUtil.parse(json, DelegateOutput.class);
        Assertions.assertThat(message.getDelegateOutputStatus()).isEqualTo(DelegateOutputStatus.CONTINUE);
        Assertions.assertThat(message.getContinueMessages().size()).isEqualTo(1);
    }

    @Test
    void parse_continue2() {
        String json = "{ \"Continue\": [] }";
        DelegateOutput message = (DelegateOutput) JsonUtil.parse(json, DelegateOutput.class);
        Assertions.assertThat(message.getDelegateOutputStatus()).isEqualTo(DelegateOutputStatus.CONTINUE);
        Assertions.assertThat(message.getContinueMessages().size()).isEqualTo(0);
    }

    @Test
    void parse_done() {
        String json = "{\n" +
                "    \"Done\": {\n" +
                "      \"public_auxinfo\": [\n" +
                "        {\n" +
                "          \"participant\": 324998800180728616167554087535394998643,\n" +
                "          \"pk\": \"e954f...\",\n" +
                "          \"params\": {\n" +
                "            \"scheme\": {\n" +
                "              \"modulus\": \"e954f...\",\n" +
                "              \"s\": \"af24d...\",\n" +
                "              \"t\": \"59213...\"\n" +
                "            },\n" +
                "            \"proof\": {\n" +
                "              \"commitments\": [\n" +
                "                \"b5ab3...\",\n" +
                "                \"4c1e7...\"\n" +
                "              ]\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }";
        DelegateOutput message = (DelegateOutput) JsonUtil.parse(json, DelegateOutput.class);
        Assertions.assertThat(message.getDelegateOutputStatus()).isEqualTo(DelegateOutputStatus.DONE);
        Assertions.assertThat(message.getDoneMessage()).isNotNull();
    }

    @Test
    void stringTest() {
        String json = "{\n" +
                "    \"Done\": {\n" +
                "      \"public_auxinfo\": [\n" +
                "        {\n" +
                "          \"participant\": 324998800180728616167554087535394998643,\n" +
                "          \"pk\": \"e954f...\",\n" +
                "          \"params\": {\n" +
                "            \"scheme\": {\n" +
                "              \"modulus\": \"e954f...\",\n" +
                "              \"s\": \"af24d...\",\n" +
                "              \"t\": \"59213...\"\n" +
                "            },\n" +
                "            \"proof\": {\n" +
                "              \"commitments\": [\n" +
                "                \"b5ab3...\",\n" +
                "                \"4c1e7...\"\n" +
                "              ]\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }";
        DelegateOutput message = (DelegateOutput) JsonUtil.parse(json, DelegateOutput.class);

        String result = JsonUtil.toString(message.getDoneMessage());
        Assertions.assertThat(result).isNotNull();
        System.out.println(result);
    }
}
