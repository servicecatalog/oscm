/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.intf;

import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jws.WebParam;

import org.junit.Before;
import org.junit.Test;

import org.oscm.types.enumtypes.BillingSharesResultType;
import org.oscm.types.enumtypes.OperationStatus;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.types.enumtypes.PaymentInfoType;
import org.oscm.types.enumtypes.ReportType;
import org.oscm.types.enumtypes.SubscriptionStatus;
import org.oscm.types.enumtypes.TriggerProcessParameterType;
import org.oscm.types.enumtypes.UserAccountStatus;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.LdapProperties;
import org.oscm.vo.ListCriteria;
import org.oscm.vo.VOBillingContact;
import org.oscm.vo.VOCatalogEntry;
import org.oscm.vo.VOCountryVatRate;
import org.oscm.vo.VOGatheredEvent;
import org.oscm.vo.VOImageResource;
import org.oscm.vo.VOInstanceInfo;
import org.oscm.vo.VOLocalizedText;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationPaymentConfiguration;
import org.oscm.vo.VOOrganizationVatRate;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOPaymentInfo;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOPriceModelLocalization;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceActivation;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOServiceLocalization;
import org.oscm.vo.VOServicePaymentConfiguration;
import org.oscm.vo.VOServiceReview;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTechnicalService;
import org.oscm.vo.VOTechnicalServiceOperation;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.vo.VOTriggerProcessParameter;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUdaDefinition;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;
import org.oscm.vo.VOVatRate;

/**
 * Test case that verifies that ensures consistent parameter names.
 * 
 * @author hoffmann
 */
public class ParameterNamesTest {

    private Set<String> allowedNames = new HashSet<String>();

    @Before
    public void setup() {

        // Simple Types

        addAllowed(String.class, "email");
        addAllowed(String.class, "identification");
        addAllowed(String.class, "instanceId");
        addAllowed(String.class, "newPassword");
        addAllowed(String.class, "oldPassword");
        addAllowed(String.class, "organizationId");
        addAllowed(String.class, "password");
        addAllowed(String.class, "reason");
        addAllowed(String.class, "securityAnswer");
        addAllowed(String.class, "securityQuestion");
        addAllowed(String.class, "serviceId");
        addAllowed(String.class, "sessionId");
        addAllowed(String.class, "subscriptionId");
        addAllowed(String.class, "technicalServiceId");
        addAllowed(String.class, "userIdPattern");
        addAllowed(String.class, "userToken");
        addAllowed(String.class, "version");
        addAllowed(String.class, "targetType");
        addAllowed(String.class, "supplierId");
        addAllowed(String.class, "sellerId");
        addAllowed(String.class, "marketplaceId");
        addAllowed(String.class, "tagPattern");
        addAllowed(String.class, "locale");
        addAllowed(String.class, "requestId");
        addAllowed(String.class, "searchPhrase");
        addAllowed(String.class, "brandingUrl");
        addAllowed(String.class, "issueText");
        addAllowed(String.class, "subject");
        addAllowed(String.class, "groupName");
        addAllowed(String.class, "unitName");
        addAllowed(String.class, "transactionId");

        addAllowed(long.class, "targetObjectKey");
        addAllowed(long.class, "actionKey");
        addAllowed(long.class, "key");
        addAllowed(long.class, "organizationKey");
        addAllowed(long.class, "subscriptionKey");
        addAllowed(long.class, "serviceKey");
        addAllowed(Long.class, "from");
        addAllowed(Long.class, "to");
        addAllowed(Long.class, "serviceKey");
        addAllowed(long.class, "triggerKey");

        addAllowed(boolean.class, "admin");
        addAllowed(boolean.class, "attemptSuccessful");
        addAllowed(boolean.class, "includeDefaultLocale");

        addAllowed(byte[].class, "xml");
        addAllowed(byte[].class, "csvData");

        addAllowed(int.class, "limit");

        // Collections

        addAllowed(List.class, String.class, "organizationIds");
        addAllowed(List.class, Long.class, "actionKeys");
        addAllowed(List.class, VOLocalizedText.class, "reason");
        addAllowed(List.class, VOLocalizedText.class, "progress");
        addAllowed(List.class, VOOrganizationPaymentConfiguration.class,
                "customerConfigurations");
        addAllowed(List.class, VOServicePaymentConfiguration.class,
                "serviceConfigurations");
        addAllowed(List.class, VOParameter.class, "parameters");
        addAllowed(List.class, VOService.class, "compatibleServices");
        addAllowed(List.class, VOTechnicalService.class, "technicalServices");
        addAllowed(List.class, VOUsageLicense.class, "users");
        addAllowed(List.class, VOUsageLicense.class, "usersToBeAdded");
        addAllowed(List.class, VOUser.class, "usersToBeRevoked");
        addAllowed(List.class, VOUserDetails.class, "users");
        addAllowed(List.class, VOUdaDefinition.class, "udaDefinitionsToSave");
        addAllowed(List.class, VOUdaDefinition.class, "udaDefinitionsToDelete");
        addAllowed(List.class, VOUda.class, "udas");
        addAllowed(List.class, VOCountryVatRate.class, "countryVats");
        addAllowed(List.class, VOOrganizationVatRate.class, "organizationVats");
        addAllowed(List.class, VOCatalogEntry.class, "entries");
        addAllowed(List.class, UserRoleType.class, "roles");
        addAllowed(List.class, VOServiceActivation.class, "activations");
        addAllowed(List.class, VOUser.class, "usersToBeAdded");
        addAllowed(List.class, VOTriggerProcessParameter.class, "parameters");

        addAllowed(LdapProperties.class, "organizationProperties");

        addAllowed(Set.class, VOPaymentType.class, "defaultConfiguration");
        addAllowed(Set.class, VOPaymentType.class,
                "defaultServiceConfiguration");
        addAllowed(Set.class, SubscriptionStatus.class, "requiredStatus");

        // Enums

        addAllowed(PaymentInfoType.class, "paymentInfoType");
        addAllowed(UserAccountStatus.class, "newStatus");
        addAllowed(OrganizationRoleType.class, "role");
        addAllowed(ReportType.class, "filter");
        addAllowed(BillingSharesResultType.class, "resultType");
        addAllowed(OperationStatus.class, "status");

        // VO Parameters

        addAllowed(VOBillingContact.class, "billingContact");

        addAllowed(VOGatheredEvent.class, "event");

        addAllowed(VOImageResource.class, "imageResource");

        addAllowed(VOInstanceInfo.class, "instance");

        addAllowed(VOOrganization.class, "customer");
        addAllowed(VOOrganization.class, "organization");

        addAllowed(VOPaymentType.class, "paymentType");
        addAllowed(VOPaymentInfo.class, "paymentInfo");

        addAllowed(VOPriceModel.class, "priceModel");

        addAllowed(VOPriceModelLocalization.class, "localization");

        addAllowed(UserRoleType.class, "role");

        addAllowed(VOService.class, "service");

        addAllowed(VOServiceDetails.class, "service");

        addAllowed(VOServiceLocalization.class, "localization");

        addAllowed(VOSubscription.class, "subscription");

        addAllowed(VOTechnicalService.class, "technicalService");
        addAllowed(VOTechnicalServiceOperation.class, "operation");

        addAllowed(VOUser.class, "user");

        addAllowed(VOUserDetails.class, "admin");
        addAllowed(VOUserDetails.class, "user");

        addAllowed(VOVatRate.class, "defaultVat");
        addAllowed(ListCriteria.class, "listCriteria");
        addAllowed(VOServiceReview.class, "review");

        addAllowed(VOMarketplace.class, "marketplace");
        addAllowed(VOImageResource.class, "image");
        addAllowed(VOTriggerDefinition.class, "trigger");

        addAllowed(VOInstanceInfo.class, "instanceInfo");

        addAllowed(TriggerProcessParameterType.class, "paramType");
    }

    @Test
    public void testAccountService() throws Exception {
        assertParameterNames(AccountService.class);
    }

    @Test
    public void testBillingService() throws Exception {
        assertParameterNames(BillingService.class);
    }

    @Test
    public void testEventService() throws Exception {
        assertParameterNames(EventService.class);
    }

    @Test
    public void testIdentityService() throws Exception {
        assertParameterNames(IdentityService.class);
    }

    @Test
    public void testReportingService() throws Exception {
        assertParameterNames(ReportingService.class);
    }

    @Test
    public void testServiceProvisioningService() throws Exception {
        assertParameterNames(ServiceProvisioningService.class);
    }

    @Test
    public void testSessionService() throws Exception {
        assertParameterNames(SessionService.class);
    }

    @Test
    public void testSubscriptionService() throws Exception {
        assertParameterNames(SubscriptionService.class);
    }

    @Test
    public void testTriggerService() throws Exception {
        assertParameterNames(TriggerService.class);
    }

    @Test
    public void testVatService() throws Exception {
        assertParameterNames(VatService.class);
    }

    @Test
    public void testMarketplaceService() throws Exception {
        assertParameterNames(MarketplaceService.class);
    }

    @Test
    public void testTagService() throws Exception {
        assertParameterNames(TagService.class);
    }

    @Test
    public void testReviewService() throws Exception {
        assertParameterNames(ReviewService.class);
    }

    protected void assertParameterNames(Class<?> service) throws Exception {
        for (final Method method : service.getMethods()) {
            final Annotation[][] annotations = method.getParameterAnnotations();
            final Type[] types = method.getGenericParameterTypes();
            for (int i = 0; i < types.length; i++) {
                final String name = getParameterName(annotations[i]);
                assertParameterName(method, types[i], name);
            }
        }
    }

    private String getParameterName(Annotation[] annotations) {
        for (Annotation a : annotations) {
            if (a instanceof WebParam) {
                WebParam webparam = (WebParam) a;
                return webparam.name();
            }
        }
        fail("Missing WebParam annotation");
        return null;
    }

    private void assertParameterName(Method method, Type type, String name)
            throws Exception {
        final String key = getKey(type, name);
        if (!allowedNames.contains(key)) {
            fail(String.format("Invalid parameter name %s in method %s.", key,
                    method));
        }
    }

    protected void addAllowed(Class<?> type, String name) {
        allowedNames.add(getKey(type, name));
    }

    protected void addAllowed(Class<?> type, Class<?> param, String name) {
        allowedNames.add(getKey(type, param, name));
    }

    private String getKey(Type type, String name) {
        return type + "/" + name;
    }

    private String getKey(Class<?> type, String name) {
        return type + "/" + name;
    }

    private String getKey(Class<?> type, Class<?> param, String name) {
        return type.getName() + "<" + param.getName() + ">/" + name;
    }

}
