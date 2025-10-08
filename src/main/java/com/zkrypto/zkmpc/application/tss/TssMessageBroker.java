package com.zkrypto.zkmpc.application.tss;

public interface TssMessageBroker {
    void publish(String message, String recipient, String type);
}
