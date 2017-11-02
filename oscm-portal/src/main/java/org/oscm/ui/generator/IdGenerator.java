/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.04.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.generator;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.ui.model.Service;
import org.oscm.ui.model.ServiceDetails;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;

/**
 * Generator for ids suggested in the UI when creating new objects (e. g on copy
 * or subscribe to service).
 * 
 * @author weiser
 * 
 */
public class IdGenerator {

    private final String prefix;
    private final String baseId;
    private final Set<String> excludedIds;

    /**
     * Constructor for generating service id on copy service.
     * 
     * @param prefix
     *            the prefix to use for the generated service id
     * @param service
     *            the service to copy - its id will be used as base for the
     *            generated service id
     * @param excluded
     *            the list of existing services - their ids will be in the
     *            exclude set for the generation of the id
     */
    public IdGenerator(String prefix, ServiceDetails service,
            List<VOService> excluded) {
        this.prefix = getNonNullValue(prefix);
        this.baseId = service.getServiceId();
        Set<String> svcIds = new HashSet<String>();
        for (VOService svc : excluded) {
            svcIds.add(svc.getServiceId());
        }
        this.excludedIds = Collections.unmodifiableSet(svcIds);
    }

    /**
     * Constructor for generating subscription id on subscribe to service. As
     * the service name - which may contain characters that are not allowed for
     * an Id - is used as base for the subscription id, special characters will
     * be removed.
     * 
     * @param prefix
     *            the prefix to use for the generated subscription id
     * @param service
     *            the service to subscribe to - its name will be used as base
     *            for the generated subscription id
     * @param excluded
     *            the list of existing subscriptions - their ids will be in the
     *            exclude set for the generation of the id
     */
    public IdGenerator(String prefix, Service service,
            List<VOSubscription> excluded) {
        this.prefix = getNonNullValue(prefix);
        if (service == null) {
            this.baseId = "";
        } else {
            this.baseId = getNonNullValue(service.getName());
        }
        Set<String> subIds = new HashSet<String>();
        for (VOSubscription sub : excluded) {
            subIds.add(sub.getSubscriptionId());
        }
        this.excludedIds = Collections.unmodifiableSet(subIds);
    }

    private String getNonNullValue(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    /**
     * Generates a new id based on the set prefix and base id excluding the ones
     * in the exclude set. The schema is
     * &lt;prefix&gt;&lt;baseId&gt;[(&lt;n&gt;)] where n is a number from 2 up
     * to {@link Short#MAX_VALUE}, The number will only be appended if the value
     * without brackets and number is excluded. The result will be trimmed to
     * match the restrictions for id constraints.
     * 
     * @return the generated id
     */
    public String generateNewId() {
        String temp = (prefix + baseId).trim();
        if (temp.length() > ADMValidator.LENGTH_ID) {
            temp = temp.substring(0, ADMValidator.LENGTH_ID);
        }
        String template = temp + "%s";
        if (temp.length() > (ADMValidator.LENGTH_ID - 7)) {
            template = template.substring(0, ADMValidator.LENGTH_ID - 7) + "%s";
        }
        for (int index = 2; index < Short.MAX_VALUE
                && excludedIds.contains(temp); index++) {
            temp = String.format(template, "(" + index + ")");
        }
        return temp;
    }

}
