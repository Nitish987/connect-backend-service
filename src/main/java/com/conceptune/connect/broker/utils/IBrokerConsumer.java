package com.conceptune.connect.broker.utils;

import com.conceptune.connect.broker.dto.Message;

public interface IBrokerConsumer {

    void consume(Message message);
}
