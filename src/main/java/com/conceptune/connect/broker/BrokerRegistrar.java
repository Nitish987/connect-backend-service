package com.conceptune.connect.broker;

import com.conceptune.connect.broker.core.Broker;
import com.conceptune.connect.broker.utils.IBrokerRegistrar;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class BrokerRegistrar implements IBrokerRegistrar {

    @Autowired
    private Broker broker;

    @Override
    public void register(String userId) {
        broker.registerUser(userId);
    }

    @Override
    public void unregister(String userId) {
        broker.unregisterUser(userId);
    }
}
