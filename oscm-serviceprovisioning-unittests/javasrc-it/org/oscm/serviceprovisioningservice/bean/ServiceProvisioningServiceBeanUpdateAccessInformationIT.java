/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * If the service is "DIRECT" or "USER", the access infomation for the
 * defaultlocale of platform is used as 'fall back' (if there is no access
 * infomation for the user's locale, the default locale one is used). In order
 * to satisfy it, if the user having non-platform-default locale registers
 * access infomation and the access infomation for the platform-default locale
 * is null, then create access infomation for platform default locale.
 * 
 * @author tokoda
 * 
 */
public class ServiceProvisioningServiceBeanUpdateAccessInformationIT
        extends EJBTestBase {

    private final String JAPANESE_VALUE = "Japanese Access Information";
    private final String ENGLISH_VALUE = "English Access Information";

    private DataService mgr;
    private ServiceProvisioningService svcProv;
    private LocalizerServiceLocal localizer;

    private Organization tpOrg;
    private long englishUserKey;
    private long japaneseUserKey;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new ServiceProvisioningServiceBean());

        mgr = container.get(DataService.class);
        localizer = container.get(LocalizerServiceLocal.class);
        svcProv = container.get(ServiceProvisioningService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tpOrg = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser englishUser = Organizations.createUserForOrg(mgr,
                        tpOrg, true, "admin");
                englishUserKey = englishUser.getKey();
                PlatformUser japaneseUser = Organizations.createUserForOrg(mgr,
                        tpOrg, true, "jpuser", "ja");
                japaneseUserKey = japaneseUser.getKey();
                return null;
            }
        });

    }

    @Test
    public void testSaveTechnicalProductDirectAccessNoAccessInformation()
            throws Exception {
        // given
        container.login(japaneseUserKey, ROLE_TECHNOLOGY_MANAGER);
        createTechnicalProduct(ServiceAccessType.DIRECT);
        VOTechnicalService techProd = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                .get(0);

        // when
        techProd.setAccessInfo(JAPANESE_VALUE);
        svcProv.saveTechnicalServiceLocalization(techProd);

        // then
        final VOTechnicalService techProdSaved = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                .get(0);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assert.assertEquals(JAPANESE_VALUE,
                        localizer.getLocalizedTextFromDatabase("en",
                                techProdSaved.getKey(),
                                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
                Assert.assertEquals(JAPANESE_VALUE,
                        localizer.getLocalizedTextFromDatabase("ja",
                                techProdSaved.getKey(),
                                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
                return null;
            }
        });

    }

    @Test
    public void testSaveTechnicalProductUserAccessNoAccessInformation()
            throws Exception {
        // given
        container.login(japaneseUserKey, ROLE_TECHNOLOGY_MANAGER);
        createTechnicalProduct(ServiceAccessType.USER);
        VOTechnicalService techProd = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                .get(0);

        // when
        techProd.setAccessInfo(JAPANESE_VALUE);
        svcProv.saveTechnicalServiceLocalization(techProd);

        // then
        final VOTechnicalService techProdSaved = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                .get(0);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assert.assertEquals(JAPANESE_VALUE,
                        localizer.getLocalizedTextFromDatabase("en",
                                techProdSaved.getKey(),
                                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
                Assert.assertEquals(JAPANESE_VALUE,
                        localizer.getLocalizedTextFromDatabase("ja",
                                techProdSaved.getKey(),
                                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
                return null;
            }
        });
    }

    @Test
    public void testSaveTechnicalProductEnglishAccessInfomationExisting()
            throws Exception {
        // given
        container.login(englishUserKey, ROLE_TECHNOLOGY_MANAGER);
        createTechnicalProduct(ServiceAccessType.DIRECT);
        VOTechnicalService techProd = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                .get(0);

        // when
        techProd.setAccessInfo(ENGLISH_VALUE);
        svcProv.saveTechnicalServiceLocalization(techProd);

        container.login(japaneseUserKey, ROLE_TECHNOLOGY_MANAGER);
        final VOTechnicalService techProdEnglish = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                .get(0);
        techProdEnglish.setAccessInfo(JAPANESE_VALUE);
        svcProv.saveTechnicalServiceLocalization(techProdEnglish);

        // then
        final VOTechnicalService techProdSaved = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                .get(0);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assert.assertEquals(ENGLISH_VALUE,
                        localizer.getLocalizedTextFromDatabase("en",
                                techProdSaved.getKey(),
                                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
                Assert.assertEquals(JAPANESE_VALUE,
                        localizer.getLocalizedTextFromDatabase("ja",
                                techProdSaved.getKey(),
                                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
                return null;
            }
        });

    }

    private TechnicalProduct createTechnicalProduct(
            final ServiceAccessType accessType) throws Exception {
        return runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                return TechnicalProducts.createTechnicalProduct(mgr, tpOrg,
                        "serviceId", false, accessType);
            }
        });
    }

}
