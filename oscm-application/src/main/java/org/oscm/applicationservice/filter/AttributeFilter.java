/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Oct 12, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.filter;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.ModifiedUda;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * Filters and converts the UDAs of an Subscription
 * 
 * @author miethaner
 */
public class AttributeFilter {

    /**
     * Converts the customer UDA list of the subscription related organization
     * into a ServiceAttribute list.
     * 
     * @param subscription
     *            the related subscription
     * 
     * @return the created ServiceAttribute list.
     */
    public static List<ServiceAttribute> getCustomAttributeList(
            Subscription subscription) {

        ArrayList<ServiceAttribute> list = new ArrayList<>();

        Organization org = subscription.getProduct().getVendor();

        for (UdaDefinition def : org.getUdaDefinitions()) {
            if (def.getTargetType() == UdaTargetType.CUSTOMER) {
                boolean exists = false;

                for (Uda uda : def.getUdas()) {
                    if (uda.getTargetObjectKey() == subscription
                            .getOrganizationKey()) {
                        ServiceAttribute attr = new ServiceAttribute();
                        attr.setAttributeId(def.getUdaId());
                        attr.setValue(uda.getUdaValue());
                        attr.setEncrypted(def.isEncrypted());
                        attr.setControllerId(def.getControllerId());
                        list.add(attr);
                        exists = true;
                    }
                }

                if (!exists) {
                    ServiceAttribute attr = new ServiceAttribute();
                    attr.setAttributeId(def.getUdaId());
                    attr.setValue(def.getDefaultValue());
                    attr.setEncrypted(def.isEncrypted());
                    attr.setControllerId(def.getControllerId());
                    list.add(attr);
                }
            }
        }

        return list;
    }

    /**
     * Converts the subscription UDA list into a ServiceAttribute list.
     * Overwrites corresponding UDAs with its modified value if available.
     * 
     * @param subscription
     *            the related subscription
     * @param modifiedUdas
     *            the list of modified UDAs
     * 
     * @return the created ServiceAttribute list.
     */
    public static List<ServiceAttribute> getSubscriptionAttributeList(
            Subscription subscription, List<ModifiedUda> modifiedUdas) {

        ArrayList<ServiceAttribute> list = new ArrayList<>();

        Organization org = subscription.getProduct().getVendor();

        for (UdaDefinition def : org.getUdaDefinitions()) {
            if (def.getTargetType() == UdaTargetType.CUSTOMER_SUBSCRIPTION) {
                boolean exists = false;

                for (Uda uda : def.getUdas()) {
                    if (uda.getTargetObjectKey() == subscription.getKey()) {

                        ServiceAttribute attr = new ServiceAttribute();
                        attr.setAttributeId(def.getUdaId());
                        attr.setValue(uda.getUdaValue());
                        attr.setEncrypted(def.isEncrypted());
                        attr.setControllerId(def.getControllerId());

                        for (ModifiedUda mod : modifiedUdas) {
                            if (mod.getTargetObjectKey() == uda.getKey()) {
                                attr.setValue(mod.getValue());
                                break;
                            }
                        }

                        list.add(attr);
                        exists = true;
                    }
                }

                if (!exists) {
                    ServiceAttribute attr = new ServiceAttribute();
                    attr.setAttributeId(def.getUdaId());
                    attr.setValue(def.getDefaultValue());
                    attr.setEncrypted(def.isEncrypted());
                    attr.setControllerId(def.getControllerId());
                    list.add(attr);
                }
            }
        }

        return list;
    }
}
