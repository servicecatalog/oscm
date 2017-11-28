/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogOperation.Operation;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogOperation.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceType;

/**
 * @author Stavreva
 * 
 */
public class PriceModelAuditLogCollector_Test {

    private PriceModelAuditLogCollector logCollector = new PriceModelAuditLogCollector();

    @Test
    public void determineOperation() {

        for (ServiceType type : ServiceType.values()) {

            PriceModel pm = givenPriceModel(type);

            for (Operation o : Operation.values()) {

                PriceModelAuditLogOperation op = logCollector
                        .determineOperation(pm, o);

                PriceModelType pmType = logCollector
                        .determinePriceModelType(pm);
                if (o == Operation.EDIT_ONETIME_FEE
                        && pmType == PriceModelType.SUBSCRIPTION) {
                    continue;
                }

                if (o == Operation.DELETE_PRICE_MODEL
                        && (pmType == PriceModelType.SERVICE || pmType == PriceModelType.SUBSCRIPTION)) {
                    continue;
                }
                if ((o == Operation.LOCALIZE_PRICE_MODEL
                        || o == Operation.EDIT_CHARGEABLE_PRICE_MODEL || o == Operation.EDIT_FREE_PRICE_MODEL)
                        && (pmType == PriceModelType.SUBSCRIPTION)) {
                    continue;
                }
                assertEquals(o, op.getPriceModelOperation());
                assertEquals(pmType, op.getPriceModelType());
                assertEquals(o.name() + "_FOR_" + pmType.name(), op.name());
            }
        }
    }

    @Test
    public void determinePriceModelType_Template() {

        // given
        PriceModel pm = givenPriceModel(ServiceType.TEMPLATE);

        // when
        PriceModelType type = logCollector.determinePriceModelType(pm);

        // then
        assertEquals(PriceModelType.SERVICE, type);
    }

    @Test
    public void determinePriceModelType_PartnerTemplate() {

        // given
        PriceModel pm = givenPriceModel(ServiceType.PARTNER_TEMPLATE);

        // when
        PriceModelType type = logCollector.determinePriceModelType(pm);

        // then
        assertEquals(PriceModelType.SERVICE, type);
    }

    @Test
    public void determinePriceModelType_CustomerTemplate() {

        // given
        PriceModel pm = givenPriceModel(ServiceType.CUSTOMER_TEMPLATE);

        // when
        PriceModelType type = logCollector.determinePriceModelType(pm);

        // then
        assertEquals(PriceModelType.CUSTOMER_SERVICE, type);
    }

    @Test
    public void determinePriceModelType_Subscription() {

        // given
        PriceModel pm = givenPriceModel(ServiceType.SUBSCRIPTION);

        // when
        PriceModelType type = logCollector.determinePriceModelType(pm);

        // then
        assertEquals(PriceModelType.SUBSCRIPTION, type);
    }

    @Test
    public void determinePriceModelType_CustomerSubscription() {

        // given
        PriceModel pm = givenPriceModel(ServiceType.CUSTOMER_SUBSCRIPTION);

        // when
        PriceModelType type = logCollector.determinePriceModelType(pm);

        // then
        assertEquals(PriceModelType.SUBSCRIPTION, type);
    }

    @Test
    public void determinePriceModelType_PartnerSubscription() {

        // given
        PriceModel pm = givenPriceModel(ServiceType.PARTNER_SUBSCRIPTION);

        // when
        PriceModelType type = logCollector.determinePriceModelType(pm);

        // then
        assertEquals(PriceModelType.SUBSCRIPTION, type);
    }

    private PriceModel givenPriceModel(ServiceType type) {
        PriceModel pm = new PriceModel();
        Product product = new Product();
        product.setType(type);
        pm.setProduct(product);
        return pm;
    }

}
