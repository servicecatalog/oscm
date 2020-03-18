/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 03.07.17 14:51
 *
 ******************************************************************************/

package org.oscm.kafka.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Singleton
@Startup
public class ConsumerTrigger {

    private static final Logger LOGGER = LogManager.getLogger(ConsumerTrigger.class);

    ExecutorService executor = null;

    @PostConstruct
    public void regular() {
        if (KafkaServer.isEnabled()) {
            executor = Executors.newSingleThreadExecutor();
            executor.execute(new Consumer());
            LOGGER.info("Kafka consumer job started");
        }
    }

    @PreDestroy
    public void stop() {
        if (executor == null) {
            return;
        }
        
        LOGGER.debug("Kafka consumer job canceling...");
        try {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
            LOGGER.debug("Kafka consumer shutdown finished");
        }
    }

}
