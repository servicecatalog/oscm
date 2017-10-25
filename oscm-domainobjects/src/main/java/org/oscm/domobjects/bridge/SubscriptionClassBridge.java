/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 3, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects.bridge;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * Custom class bridge implementation for indexing subscription domain objects
 * together with related information.
 * 
 * @author miethaner
 */
public class SubscriptionClassBridge implements FieldBridge {

    public static final String NAME_SUBSCRIPTION_ID = "subscription.subscriptionid";
    public static final String NAME_REFERENCE = "subscription.reference";
    public static final String NAME_PARAMETER_VALUE = "parameter.value";
    public static final String NAME_UDA_VALUE = "uda.value";

    private static final String SEPARATOR = " ";

    @Override
    public void set(String name, Object value, Document doc,
            LuceneOptions options) {

        Subscription sub = (Subscription) value;

        // write subscription id to index
        String subId = "";

        if (sub.getSubscriptionId() != null) {
            subId = sub.getSubscriptionId();
        }

        Field field = new Field(NAME_SUBSCRIPTION_ID, subId,
                options.getStore(), options.getIndex(), options.getTermVector());
        field.setBoost(options.getBoost());
        doc.add(field);

        // write subscription reference to index
        String subRef = "";

        if (sub.getPurchaseOrderNumber() != null) {
            subRef = sub.getPurchaseOrderNumber();
        }

        field = new Field(NAME_REFERENCE, subRef, options.getStore(),
                options.getIndex(), options.getTermVector());
        field.setBoost(options.getBoost());
        doc.add(field);

        // write all string parameters of the corresponding service to index
        List<Parameter> params = new ArrayList<Parameter>();

        if (sub.getProduct() != null
                && sub.getProduct().getParameterSet() != null
                && sub.getProduct().getParameterSet().getParameters() != null) {

            params = sub.getProduct().getParameterSet().getParameters();
        }

        StringBuilder sb = new StringBuilder();

        for (Parameter p : params) {
            if (p.getParameterDefinition().getValueType() == ParameterValueType.STRING) {
                sb.append(p.getValue());
                sb.append(SEPARATOR);
            }
        }

        field = new Field(NAME_PARAMETER_VALUE, sb.toString(),
                options.getStore(), options.getIndex(), options.getTermVector());
        field.setBoost(options.getBoost());
        doc.add(field);

        // write all corresponding udas to index, if no uda exits for a
        // definition write default value
        sb = new StringBuilder();

        List<UdaDefinition> udaDefList = new ArrayList<UdaDefinition>();

        if (sub.getProduct() != null && sub.getProduct().getVendor() != null
                && sub.getProduct().getVendor().getUdaDefinitions() != null) {

            udaDefList = sub.getProduct().getVendor().getUdaDefinitions();
        }

        List<Uda> udaList;
        boolean exists;
        for (UdaDefinition udaDef : udaDefList) {
            if (udaDef.getTargetType() == UdaTargetType.CUSTOMER_SUBSCRIPTION
                    && udaDef.getConfigurationType() != UdaConfigurationType.SUPPLIER) {

                udaList = udaDef.getUdas();
                exists = false;
                for (Uda uda : udaList) {
                    final String udaValue = uda.getUdaValue();
                    if (uda.getTargetObjectKey() == sub.getKey() && StringUtils.isNotBlank(udaValue)) {
                        sb.append(uda.getUdaValue());
                        sb.append(SEPARATOR);

                        exists = true;
                        break;
                    }
                }

                final String udaDefDefaultValue = udaDef.getDefaultValue();
                if (!exists && StringUtils.isNotBlank(udaDefDefaultValue)) {
                    sb.append(udaDef.getDefaultValue());
                    sb.append(SEPARATOR);
                }
            }
        }

        field = new Field(NAME_UDA_VALUE, sb.toString(), options.getStore(),
                options.getIndex(), options.getTermVector());
        field.setBoost(options.getBoost());
        doc.add(field);
    }

}
