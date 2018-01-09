/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2015年1月22日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

/**
 * @author kongsl.fnst
 *
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSteppedPrice;

/**
 * Custom ANT task creating marketable services using the WS-API.
 * 
 * @author Dirk Bernsau
 */
public class PricemodelDefineForCustomerTask extends WebtestTask {

    private String serviceId;
    private String description; // default="@{serviceId}" />
    private Long customerOrgKey;
    private boolean isFree = true;

    private String basePrice = "50";
    private String perUser;
    private String perSub;

    private boolean steppedPriceForUser = false;
    private String steppedPriceForEvent;
    private String steppedPriceForParameter;
    private String type = PriceModelType.PRO_RATA.name();

    public void setServiceId(String value) {
        serviceId = value;
    }

    public void setBasePrice(String value) {
        basePrice = value;
    }

    public void setPerUser(String value) {
        perUser = value;
    }

    public void setPerSub(String value) {
        perSub = value;
    }

    @Override
    public void setDescription(String value) {
        description = value;
    }

    public void setIsFree(String value) {
        isFree = Boolean.parseBoolean(value);
    }

    public void setSteppedPriceForUser(boolean steppedPriceForUser) {
        this.steppedPriceForUser = steppedPriceForUser;
    }

    public void setSteppedPriceForEvent(String steppedPriceForEvent) {
        this.steppedPriceForEvent = steppedPriceForEvent;
    }

    public void setSteppedPriceForParameter(String steppedPriceForParameter) {
        this.steppedPriceForParameter = steppedPriceForParameter;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCustomerOrgKey() {
        return customerOrgKey;
    }

    public void setCustomerOrgKey(Long customerOrgKey) {
        this.customerOrgKey = customerOrgKey;
    }

    @Override
    public void executeInternal() throws BuildException,
            SaaSApplicationException {

        if (description == null) {
            description = serviceId;
        }
        if (perUser == null) {
            perUser = basePrice;
        }
        if (perSub == null) {
            perSub = basePrice;
        }

        validatePriceModelType();
        ServiceProvisioningService spsSvc = getServiceInterface(ServiceProvisioningService.class);

        VOServiceDetails details = null;
        List<VOService> services = spsSvc.getSuppliedServices();
        for (VOService sv : services) {
            if (serviceId.equals(sv.getServiceId())) {
                details = spsSvc.getServiceDetails(sv);
                break;
            }
        }
        if (details == null) {
            throw new WebtestTaskException("No service with ID " + serviceId
                    + " available!");
        }

        VOOrganization voOrganzation = new VOOrganization();
        if (customerOrgKey == null) {
            AccountService accSvc = getServiceInterface(AccountService.class);
            String currentOrgId = accSvc.getOrganizationData()
                    .getOrganizationId();
            List<VOOrganization> orgs = accSvc.getMyCustomers();
            if (orgs != null && !orgs.isEmpty()) {
                for (VOOrganization org : orgs) {
                    if (!currentOrgId.equals(org.getOrganizationId())) {
                        voOrganzation = org;
                        break;
                    }
                }
            }
        } else {
            voOrganzation.setKey(customerOrgKey.longValue());
        }

        VOPriceModel pm = new VOPriceModel();
        pm.setCurrencyISOCode("EUR");
        pm.setPeriod(PricingPeriod.MONTH);
        pm.setType(PriceModelType.FREE_OF_CHARGE);
        pm.setDescription(description);
        if (!isFree) {
            pm.setType(PriceModelType.valueOf(type));
            pm.setOneTimeFee(new BigDecimal(basePrice));
            pm.setPricePerPeriod(new BigDecimal(perSub));
            pm.setPricePerUserAssignment(new BigDecimal(perUser));
            pm.setSteppedPrices(defineSteppedPriceForUser());
            if (pm.getSteppedPrices() != null) {
                pm.setPricePerUserAssignment(BigDecimal.ZERO);
            }
            pm.setConsideredEvents(defineEventWithSteppedPrice(details
                    .getTechnicalService().getEventDefinitions()));
            pm.setSelectedParameters(definePricedParameterWithSteppedPrice(details
                    .getParameters()));
        }
        spsSvc.savePriceModelForCustomer(details, pm, voOrganzation);
        log("Created pricemodel " + pm.getDescription() + " for service "
                + serviceId);
    }

    private void validatePriceModelType() {
        try {
            PriceModelType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new WebtestTaskException("Invalid pricemodel type " + type);
        }
    }

    private List<VOSteppedPrice> defineSteppedPriceForUser() {
        if (!steppedPriceForUser) {
            return null;
        }
        List<VOSteppedPrice> steppedPrices = createSteppedPrices(1,
                BigDecimal.ONE);
        return steppedPrices;
    }

    private List<VOPricedEvent> defineEventWithSteppedPrice(
            List<VOEventDefinition> eventDefinitions) {
        List<VOPricedEvent> pricedEvents = new ArrayList<VOPricedEvent>();
        VOEventDefinition eventDefUsed = null;

        if (steppedPriceForEvent == null) {
            return pricedEvents;
        }

        for (VOEventDefinition eventDefinition : eventDefinitions) {
            if (steppedPriceForEvent.equals(eventDefinition.getEventId())) {
                eventDefUsed = eventDefinition;
                break;
            }
        }

        if (eventDefUsed == null) {
            return pricedEvents;
        }

        VOPricedEvent event = new VOPricedEvent();
        event.setEventDefinition(eventDefUsed);
        event.setEventPrice(BigDecimal.ZERO);
        event.setSteppedPrices(createSteppedPrices(1, BigDecimal.ONE));
        pricedEvents.add(event);
        return pricedEvents;
    }

    private List<VOPricedParameter> definePricedParameterWithSteppedPrice(
            List<VOParameter> parameters) {
        List<VOPricedParameter> pricedParemeters = new ArrayList<VOPricedParameter>();
        VOParameterDefinition parameterDefUsed = null;

        if (steppedPriceForParameter == null) {
            return pricedParemeters;
        }

        for (VOParameter parameter : parameters) {
            if (steppedPriceForParameter.equals(parameter
                    .getParameterDefinition().getParameterId())) {
                parameterDefUsed = parameter.getParameterDefinition();
                break;
            }
        }

        if (parameterDefUsed == null) {
            return pricedParemeters;
        }

        VOPricedParameter pricedParameter = new VOPricedParameter();
        pricedParameter.setVoParameterDef(parameterDefUsed);
        pricedParameter
                .setSteppedPrices(createSteppedPrices(12, BigDecimal.ONE));
        pricedParameter.setPricePerSubscription(BigDecimal.ZERO);
        pricedParemeters.add(pricedParameter);
        return pricedParemeters;
    }

    private List<VOSteppedPrice> createSteppedPrices(long limit,
            BigDecimal price) {
        List<VOSteppedPrice> steppedPrices = new ArrayList<VOSteppedPrice>();
        VOSteppedPrice steppedPrice1 = new VOSteppedPrice();
        steppedPrice1.setLimit(Long.valueOf(limit));
        steppedPrice1.setPrice(price);
        steppedPrices.add(steppedPrice1);
        VOSteppedPrice steppedPrice2 = new VOSteppedPrice();
        steppedPrice2.setPrice(BigDecimal.ZERO);
        steppedPrices.add(steppedPrice2);
        return steppedPrices;
    }
}
