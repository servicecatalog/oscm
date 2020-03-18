/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 14.07.2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.kafka.service;

import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.ct.login.ProgrammaticLoginHandler;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.kafka.records.ReleaseRecord;
import org.oscm.kafka.serializer.DataSerializer;
import org.oscm.kafka.serializer.UUIDSerializer;

/**
 * @author stavreva
 *
 */
public class Consumer implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(Consumer.class);

    private final static String TOPIC = "provisioning-release";
    private final static String CONSUMER_GROUP = "oscm-group";
    private KafkaConsumer<UUID, Object> consumer;
    private SubscriptionService subscriptionService;

    public Consumer() {
        ConfigurationService configService = ServiceLocator
                .findService(ConfigurationService.class);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                configService.getVOConfigurationSetting(
                        ConfigurationKey.KAFKA_BOOTSTRAP_SERVERS, "global")
                        .getValue());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP);
        consumer = new KafkaConsumer<>(props, new UUIDSerializer(),
                new DataSerializer(ReleaseRecord.class));
        consumer.subscribe(Arrays.asList(TOPIC));
        LOGGER.debug("Kafka consumer subscribed to topic.");
    }

    @Override
    public void run() {
        LOGGER.debug("Kafka consumer started.");
        try {
            subscriptionService = ServiceLocator
                    .findService(SubscriptionService.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        while (true) {
            try {
                final ConsumerRecords<UUID, Object> consumerRecords = consumer
                        .poll(1000);

                consumerRecords.forEach(record -> {
                    System.out.printf("Consumer Record:(%s, %s, %d, %d)\n",
                            record.key(), record.value(), record.partition(),
                            record.offset());
                    processRecord(record);
                });

                consumer.commitAsync();
            } catch (Throwable t) {
                consumer.commitAsync();
                t.printStackTrace();
            }
        }
    }

    public void processRecord(ConsumerRecord<UUID, Object> record) {

        ReleaseRecord release = ReleaseRecord.class.cast(record.value());

        ReleaseRecord.Status status = release.getStatus();
        switch (status) {
        case DEPLOYED:
            completeSubscription(release);
            break;
        case PENDING:
        case CREATING:
        case DELETING:
        case UPDATING:
            progressSubscription(release);
            break;
        case FAILED:
        case DELETED:
            abortSubscription(release);
            break;
        default:
            break;
        }
    }

    /**
     * @param release
     */
    void completeSubscription(ReleaseRecord release) {
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId(release.getInstance());
        instanceInfo.setAccessInfo(release.getServices().get("endpoint"));

        VOSubscription subscription = subscriptionService
                .getSubscription(release.getId());
        try {
            login();
            if (SubscriptionStatus.PENDING.equals(subscription.getStatus()))
                subscriptionService.completeAsyncSubscription(release.getId(),
                        instanceInfo);
            else if (SubscriptionStatus.PENDING_UPD
                    .equals(subscription.getStatus())) {
                subscriptionService.completeAsyncModifySubscription(
                        release.getId(), instanceInfo);
            } else {
                // TODO error
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param release
     */
    void abortSubscription(ReleaseRecord release) {
        try {
            login();
            VOSubscription subscription = subscriptionService
                    .getSubscription(release.getId());

            if (SubscriptionStatus.PENDING.equals(subscription.getStatus())) {
                subscriptionService.abortAsyncSubscription(release.getId());
            } else if (SubscriptionStatus.PENDING_UPD
                    .equals(subscription.getStatus())) {
                // TODO get reason, failure...
                subscriptionService.abortAsyncModifySubscription(
                        release.getId(), "some reason");
            } else {
                // TODO error
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param release
     */
    void progressSubscription(ReleaseRecord release) {
        try {
            login();
            subscriptionService.updateAsyncSubscriptionProgress(release.getId(),
                    release.getStatus().name());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @throws Exception
     */
    void login() throws Exception {
        new ProgrammaticLoginHandler().login("1000", ("admin123"));
        // TODO dedicated kafka user? how to solve password?
    }
}
