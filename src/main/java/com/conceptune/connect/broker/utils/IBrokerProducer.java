package com.conceptune.connect.broker.utils;

import com.conceptune.connect.broker.dto.Message;

public interface IBrokerProducer {

    void produce(Message message, boolean useFallback);
}
