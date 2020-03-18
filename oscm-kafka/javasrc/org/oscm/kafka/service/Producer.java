/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 10.07.17 07:42
 *
 ******************************************************************************/

package org.oscm.kafka.service;

import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.domobjects.Subscription;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.kafka.records.Operation;
import org.oscm.kafka.records.SubscriptionRecord;
import org.oscm.kafka.result.PublishingResult;
import org.oscm.kafka.serializer.DataSerializer;
import org.oscm.kafka.serializer.UUIDSerializer;

/**
 * Authored by dawidch
 */
public class Producer {
    private static final Logger LOGGER = LogManager.getLogger(Producer.class);
    private final static String TOPIC = "core-subscription";

    private KafkaProducer<UUID, Object> producer;

    public PublishingResult publish(Subscription subscription,
            Operation operation) {
        SubscriptionRecord record = new SubscriptionRecord(subscription,
                operation);
        produce(subscription.getUuid(), record);
        return PublishingResult.SUCCESS;
    }

    public void produce(UUID key, Object value) {
        ConfigurationService configService = ServiceLocator
                .findService(ConfigurationService.class);
        Properties kafkaProps = new Properties();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                configService.getVOConfigurationSetting(
                        ConfigurationKey.KAFKA_BOOTSTRAP_SERVERS, "global")
                        .getValue());

        this.producer = new KafkaProducer<>(kafkaProps, new UUIDSerializer(),
                new DataSerializer(value.getClass()));
        try {
            producer.send(new ProducerRecord<>(TOPIC, key, value));
        } catch (Exception e) {
            LOGGER.error("Producer closed");
            e.printStackTrace();
        } finally {
            producer.close();
            LOGGER.debug("Producer closed");
        }
    }
}
