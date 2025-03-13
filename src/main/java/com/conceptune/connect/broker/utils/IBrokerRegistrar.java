package com.conceptune.connect.broker.utils;

public interface IBrokerRegistrar {

    void register(String userId);
    void unregister(String userId);
}
