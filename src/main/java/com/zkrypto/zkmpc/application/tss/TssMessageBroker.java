package com.zkrypto.zkmpc.application.tss;

public interface TssMessageBroker {
    void publish(String recipient, String message, String type);
}
