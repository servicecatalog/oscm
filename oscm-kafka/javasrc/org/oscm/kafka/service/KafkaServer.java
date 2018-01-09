/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 31.08.17 14:51
 *
 ******************************************************************************/

package org.oscm.kafka.service;

import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

public class KafkaServer {

    public static boolean isEnabled() {
        ConfigurationService configService = ServiceLocator
                .findService(ConfigurationService.class);
        String kafkaServer = configService
                .getVOConfigurationSetting(
                        ConfigurationKey.KAFKA_BOOTSTRAP_SERVERS, "global")
                .getValue();
        if (kafkaServer == null || kafkaServer.trim().isEmpty()) {
            return false;
        }

        return true;
    }
}
