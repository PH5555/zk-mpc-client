package com.zkrypto.zkmpc.application.message;

import java.util.List;

public interface MessagePurger {
    Boolean purge();
    String purgeWithSid();
}
