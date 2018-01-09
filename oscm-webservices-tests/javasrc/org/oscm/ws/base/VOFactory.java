/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 04.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws.base;

import java.util.LinkedList;
import java.util.List;

import org.oscm.internal.vo.VOTenant;
import org.oscm.types.enumtypes.PriceModelType;
import org.oscm.types.enumtypes.PricingPeriod;
import org.oscm.vo.VOBillingContact;
import org.oscm.vo.VOCatalogEntry;
import org.oscm.vo.VOGatheredEvent;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * @author kulle
 * 
 */
public class VOFactory {

    public VOOrganization createOrganizationVO() throws Exception {
        String mailAddress = WebserviceTestBase.getMailReader()
                .getMailAddress();
        VOOrganization org = new VOOrganization();
        org.setEmail(mailAddress);
        org.setAddress("address");
        org.setLocale("en");
        org.setName("Fujitsu EST");
        org.setPhone("+49 89 000000");
        org.setDomicileCountry("DE");
        org.setUrl("http://de.fujitsu.com");
        org.setSupportEmail(mailAddress);
        return org;
    }

    public VOSubscription createSubscriptionVO(String subscriptionId) {
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(subscriptionId);
        return subscription;
    }

    public VOGatheredEvent createVOGatheredEvent(long occurencetime,
            String eventId) {
        VOGatheredEvent event = new VOGatheredEvent();
        event.setActor("anyUser");
        event.setOccurrenceTime(occurencetime);
        event.setEventId(eventId);
        event.setMultiplier(1L);
        event.setUniqueId(WebserviceTestBase.createUniqueKey());
        return event;
    }

    public VOUsageLicense createUsageLicenceVO(String userId) {
        VOUser user = new VOUser();
        user.setUserId(userId);

        return createUsageLicenceVO(user);
    }

    public VOUsageLicense createUsageLicenceVO(VOUser user) {
        VOUsageLicense usageLicence = new VOUsageLicense();
        usageLicence.setUser(user);
        return usageLicence;
    }

    public VOBillingContact createBillingContactVO() {
        VOBillingContact voBillingContact = new VOBillingContact();
        voBillingContact.setAddress("address");
        voBillingContact.setCompanyName("companyName");
        voBillingContact.setEmail("test@mail.de");
        voBillingContact.setOrgAddressUsed(true);
        voBillingContact.setId(
                "billingContactId" + WebserviceTestBase.createUniqueKey());
        return voBillingContact;
    }

    public VOPriceModel createPriceModelVO() {
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        return priceModel;
    }

    public VOPriceModel createPriceModelVO(String currencyISOCode) {
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode(currencyISOCode);
        priceModel.setPeriod(PricingPeriod.MONTH);
        return priceModel;
    }

    public VOMarketplace createMarketplaceVO(String ownerId, boolean isOpen,
            String name) {
        return createMarketplaceVO(ownerId, isOpen, name, null);
    }

    public VOMarketplace createMarketplaceVO(String ownerId, boolean isOpen,
            String name, String id) {
        VOMarketplace marketPlace = new VOMarketplace();
        marketPlace.setName(name);
        marketPlace.setOwningOrganizationId(ownerId);
        marketPlace.setOpen(isOpen);
        marketPlace.setMarketplaceId(id);
        return marketPlace;
    }

    public List<VOCatalogEntry> createCatalogEntryVO(VOMarketplace marketplace,
            VOService service) {
        List<VOCatalogEntry> catalogEntries = new LinkedList<VOCatalogEntry>();

        VOCatalogEntry ce = new VOCatalogEntry();
        ce.setMarketplace(marketplace);
        ce.setService(service);

        catalogEntries.add(ce);
        return catalogEntries;
    }

    public VOService createMarketableServiceVO(String serviceId) {
        VOService marketableService = new VOService();
        marketableService.setName(serviceId);
        marketableService.setServiceId(serviceId);
        return marketableService;
    }

    public VOUserDetails createUserVO(String userId) throws Exception {
        return createUserVO(userId,
                WebserviceTestBase.getMailReader().getMailAddress());
    }

    public VOUserDetails createUserVO(String userId, String email) {
        VOUserDetails user = new VOUserDetails();
        user.setUserId(userId);
        user.setEMail(email);
        user.setLocale("en");
        return user;
    }

    public VOTenant createTenantVo(String tenantId) {

        VOTenant tenant = new VOTenant();
        tenant.setTenantId(tenantId);
        tenant.setName("customName");

        return tenant;
    }
}
