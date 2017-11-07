/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 18.11.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.brandservice.bean;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.ImageResourceServiceBean;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.BrandService;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author pock
 */
public class BrandServiceBeanIT extends EJBTestBase {

    private final static byte[] TRANSPARENT_PIXEL = new byte[] { 'G', 'I', 'F',
            '8', '9', 'a', 0x01, 0x00, 0x01, 0x00, (byte) 0x80, 0x01, 0x00,
            0x10, 0x3C, 0x63, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x21,
            (byte) 0xf9, 0x04, 0x01, 0x14, 0x00, 0x01, 0x00, 0x2c, 0x00, 0x00,
            0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x02, 0x02, 0x4c, 0x01,
            0x00, 0x3b };

    private String globalMplId = "FUJITSU";
    private String globalMplStage = "STAGE_CONTENT";
    private String globalMplLocale = "en";

    private String globalMpl2Id = "anotherGlobalMpl";

    private String customStageContent = "<b>CUSTOM_CONTENT</b>";

    private LocalizedResource lrDefault;

    private DataService mgr;
    private BrandService brandMgmt;
    private LocalizerServiceLocal localizer;

    private String mId;
    private long supplierUserKey;
    private long supplierUserKeyNoMP;
    private long customerUserKey;
    private long operatorUserKey;
    private long operatorUserKeyNoMP;

    @Override
    public void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ImageResourceServiceBean());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new BrandServiceBean());

        // lookup bean references
        mgr = container.get(DataService.class);
        brandMgmt = container.get(BrandService.class);
        localizer = container.get(LocalizerServiceLocal.class);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);

                Organization supplierNoMP = Organizations
                        .createOrganization(mgr, OrganizationRoleType.SUPPLIER);

                PlatformUser createUserForOrgNoMP = Organizations
                        .createUserForOrg(mgr, supplierNoMP, true, "admin");
                PlatformUsers.grantRoles(mgr, createUserForOrgNoMP,
                        UserRoleType.SERVICE_MANAGER);
                supplierUserKeyNoMP = createUserForOrgNoMP.getKey();

                Organization platformOperatorNoMP = Organizations
                        .createOrganization(mgr,
                                OrganizationRoleType.PLATFORM_OPERATOR);
                createUserForOrgNoMP = Organizations.createUserForOrg(mgr,
                        platformOperatorNoMP, true, "admin");
                operatorUserKeyNoMP = createUserForOrgNoMP.getKey();

                Organization supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                mId = Marketplaces.ensureMarketplace(supplier, null, mgr)
                        .getMarketplaceId();

                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(mgr, supplier, true, "admin");
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.SERVICE_MANAGER);
                supplierUserKey = createUserForOrg.getKey();

                mgr.flush();

                Organization customer = Organizations.createCustomer(mgr,
                        supplier);

                createUserForOrg = Organizations.createUserForOrg(mgr, customer,
                        true, "admin");
                customerUserKey = createUserForOrg.getKey();

                Organization provider = Organizations.createOrganization(mgr,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                createUserForOrg = Organizations.createUserForOrg(mgr, provider,
                        true, "admin");
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.PLATFORM_OPERATOR);
                operatorUserKey = createUserForOrg.getKey();

                Marketplace mp = Marketplaces.createMarketplace(provider,
                        globalMplId, false, mgr);
                mp.setCatalogEntries(new ArrayList<CatalogEntry>());
                mgr.flush();

                // Create another global marketplace which is assigned to
                // another Org
                Organization mplOwner2 = Organizations.createOrganization(mgr,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                createUserForOrg = Organizations.createUserForOrg(mgr,
                        mplOwner2, true, "admin");
                Marketplace mp2 = Marketplaces.createMarketplace(mplOwner2,
                        globalMpl2Id, false, mgr);
                mp2.setCatalogEntries(new ArrayList<CatalogEntry>());
                mgr.flush();

                lrDefault = new LocalizedResource();
                lrDefault.setLocale(globalMplLocale);
                lrDefault.setObjectType(LocalizedObjectTypes.MARKETPLACE_STAGE);
                lrDefault.setObjectKey(mp.getKey());
                lrDefault.setValue(globalMplStage);
                mgr.persist(lrDefault);

                LocalizedResource lr_ja = new LocalizedResource();
                lr_ja.setLocale("ja");
                lr_ja.setObjectType(LocalizedObjectTypes.MARKETPLACE_STAGE);
                lr_ja.setObjectKey(mp.getKey());
                lr_ja.setValue(globalMplStage + "ja");
                mgr.persist(lr_ja);

                return null;
            }
        });

        container.login(supplierUserKey, new String[] { ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_MARKETPLACE_OWNER });

    }

    @Test
    public void testLoad() throws Exception {
        Properties properties;

        properties = brandMgmt.loadMessageProperties(mId,
                Locale.ENGLISH.toString());
        Assert.assertTrue(
                "The message properties consist of the mail proeprties.",
                properties.size() > 0);

        properties = brandMgmt.loadMessageProperties(mId,
                Locale.GERMAN.toString());
        Assert.assertTrue(
                "The message properties consist of the mail proeprties.",
                properties.size() > 0);

        VOImageResource img = brandMgmt.loadImage(mId,
                ImageType.SHOP_LOGO_LEFT);
        Assert.assertNull("Initially there is no image", img);
    }

    @Test
    public void testLoadImageWrongType() throws Exception {
        Assert.assertNull(brandMgmt.loadImage(mId, ImageType.SERVICE_IMAGE));
    }

    @Test
    public void testLoadImageWrongOrganizationId() throws Exception {
        Assert.assertNull(
                brandMgmt.loadImage("wrong", ImageType.SHOP_LOGO_LEFT));
    }

    @Test
    public void testSaveMessageProperties() throws Exception {
        final String key = "USER_CREATED.subject";
        final String en = "en";
        final String de = "de";
        final String de_DE = "de_DE";

        Map<String, Properties> propertiesMap = prepareProps(key, en, de,
                de_DE);
        brandMgmt.saveMessageProperties(propertiesMap, mId);
        verifySavedProps(mId, key, en, de, de_DE);
    }

    @Test
    public void testSaveMessageProperties_GlobalMaretplace() throws Exception {
        container.login(operatorUserKey, ROLE_MARKETPLACE_OWNER);
        final String key = "USER_CREATED.subject";
        final String en = "en";
        final String de = "de";
        final String de_DE = "de_DE";

        Map<String, Properties> propertiesMap = prepareProps(key, en, de,
                de_DE);
        brandMgmt.saveMessageProperties(propertiesMap, globalMplId);
        verifySavedProps(globalMplId, key, en, de, de_DE);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSaveMessageProperties_marketplaceNotFound()
            throws Exception {
        final String key = "USER_CREATED.subject";
        final String en = "en";
        final String de = "de";
        final String de_DE = "de_DE";

        Map<String, Properties> propertiesMap = prepareProps(key, en, de,
                de_DE);
        brandMgmt.saveMessageProperties(propertiesMap, "INVALID_ID");
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSaveMessageProperties_callerNotOwner() throws Exception {
        final String key = "USER_CREATED.subject";
        final String en = "en";
        final String de = "de";
        final String de_DE = "de_DE";

        Map<String, Properties> propertiesMap = prepareProps(key, en, de,
                de_DE);
        brandMgmt.saveMessageProperties(propertiesMap, globalMpl2Id);
    }

    @Test
    public void testSaveImage() throws Exception {
        // Create a buffered image that supports transparency
        BufferedImage img = new BufferedImage(1, 34,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.drawLine(0, 1, 0, 32);

        ByteArrayOutputStream bos;

        bos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", bos);
        bos.close();
        byte[] bg = bos.toByteArray();

        bos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bos);
        bos.close();
        byte[] left = bos.toByteArray();

        List<VOImageResource> list = new ArrayList<>();
        VOImageResource vo;

        vo = new VOImageResource();
        vo.setImageType(ImageType.SHOP_LOGO_BACKGROUND);
        vo.setBuffer(bg);
        list.add(vo);

        vo = new VOImageResource();
        vo.setImageType(ImageType.SHOP_LOGO_LEFT);
        vo.setBuffer(left);
        list.add(vo);

        vo = new VOImageResource();
        vo.setImageType(ImageType.SHOP_LOGO_RIGHT);
        vo.setBuffer(TRANSPARENT_PIXEL);
        list.add(vo);

        brandMgmt.saveImages(list, mId);

        vo = brandMgmt.loadImage(mId, ImageType.SHOP_LOGO_BACKGROUND);
        Assert.assertEquals("image/jpeg", vo.getContentType());
        Assert.assertEquals(ImageType.SHOP_LOGO_BACKGROUND, vo.getImageType());
        Assert.assertTrue(Arrays.equals(bg, vo.getBuffer()));

        vo = brandMgmt.loadImage(mId, ImageType.SHOP_LOGO_LEFT);
        Assert.assertEquals("image/png", vo.getContentType());
        Assert.assertEquals(ImageType.SHOP_LOGO_LEFT, vo.getImageType());
        Assert.assertTrue(Arrays.equals(left, vo.getBuffer()));

        vo = brandMgmt.loadImage(mId, ImageType.SHOP_LOGO_RIGHT);
        Assert.assertEquals("image/gif", vo.getContentType());
        Assert.assertEquals(ImageType.SHOP_LOGO_RIGHT, vo.getImageType());
        Assert.assertTrue(Arrays.equals(TRANSPARENT_PIXEL, vo.getBuffer()));
    }

    @Test
    public void testSaveImageMissingImageTypes() throws Exception {
        brandMgmt.saveImages(null, null);
    }

    @Test
    public void testSaveImageWrongType() throws Exception {
        List<VOImageResource> list = new ArrayList<>();
        VOImageResource vo;

        vo = new VOImageResource();
        vo.setImageType(ImageType.SERVICE_IMAGE);
        vo.setBuffer(TRANSPARENT_PIXEL);
        list.add(vo);

        brandMgmt.saveImages(list, mId);
    }

    @Test
    public void testDelete() throws Exception {

        Properties properties;

        // delete messages properties
        brandMgmt.deleteAllMessageProperties(mId);
        properties = brandMgmt.loadMessageProperties(mId,
                Locale.ENGLISH.toString());
        Assert.assertTrue("The mail properties must still exist.",
                properties.size() > 0);

        // delete images
        List<ImageType> list = new ArrayList<>();
        list.add(ImageType.SHOP_LOGO_LEFT);
        list.add(ImageType.SHOP_LOGO_RIGHT);
        brandMgmt.deleteImages(list);

        VOImageResource img = brandMgmt.loadImage(mId,
                ImageType.SHOP_LOGO_LEFT);
        Assert.assertNull("The image must have been deleted", img);
    }

    @Test
    public void testDeleteImagesAsPlatformOperator() throws Exception {
        container.login(operatorUserKey, new String[] { ROLE_PLATFORM_OPERATOR,
                ROLE_MARKETPLACE_OWNER });

        // delete images
        List<ImageType> list = new ArrayList<>();
        list.add(ImageType.SHOP_LOGO_LEFT);
        list.add(ImageType.SHOP_LOGO_RIGHT);
        brandMgmt.deleteImages(list);

        VOImageResource img = brandMgmt.loadImage(mId,
                ImageType.SHOP_LOGO_LEFT);
        Assert.assertNull("The image must have been deleted", img);
    }

    @Test
    public void testDeleteImagesWithoutMarketplace() throws Exception {
        container.login(supplierUserKeyNoMP,
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_SERVICE_MANAGER,
                        ROLE_MARKETPLACE_OWNER });

        // delete images
        List<ImageType> list = new ArrayList<>();
        list.add(ImageType.SHOP_LOGO_LEFT);
        list.add(ImageType.SHOP_LOGO_RIGHT);
        brandMgmt.deleteImages(list);

        container.login(operatorUserKeyNoMP, new String[] {
                ROLE_PLATFORM_OPERATOR, ROLE_MARKETPLACE_OWNER });
        brandMgmt.deleteImages(list);

    }

    @Test
    public void testDeleteImagesWithoutShop() throws Exception {
        List<ImageType> list = new ArrayList<>();
        list.add(ImageType.SHOP_LOGO_LEFT);
        list.add(ImageType.SHOP_LOGO_RIGHT);
        brandMgmt.deleteImages(list);
    }

    @Test
    public void testDeleteImageWrongType() throws Exception {
        List<VOImageResource> voList = new ArrayList<>();
        VOImageResource vo = new VOImageResource();
        vo.setImageType(ImageType.SHOP_LOGO_LEFT);
        vo.setBuffer(TRANSPARENT_PIXEL);
        voList.add(vo);

        brandMgmt.saveImages(voList, mId);

        List<ImageType> list = new ArrayList<>();
        list.add(ImageType.SERVICE_IMAGE);

        brandMgmt.deleteImages(list);

        VOImageResource img = brandMgmt.loadImage(mId,
                ImageType.SHOP_LOGO_LEFT);
        Assert.assertNotNull("The image must still exist", img);
    }

    @Test
    public void testDeleteAllMessageProperties() throws Exception {
        brandMgmt.deleteAllMessageProperties(mId);
    }

    @Test
    public void testDeleteAllMessageProperties_GlobalMarketplace()
            throws Exception {
        container.login(operatorUserKey, ROLE_MARKETPLACE_OWNER);
        brandMgmt.deleteAllMessageProperties(globalMplId);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeleteAllMessageProperties_marketplaceNotFound()
            throws Exception {
        brandMgmt.deleteAllMessageProperties("INVALID_ID");
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testDeleteAllMessageProperties_callerNotOwner()
            throws Exception {
        brandMgmt.deleteAllMessageProperties(globalMpl2Id);
    }

    @Test
    public void testDeleteMissingImageTypes() throws Exception {
        brandMgmt.deleteImages(null);
    }

    /**
     * Test all good \o/ (reads the localized resource from the db)
     */
    @Test
    public void testGetMarketplaceStage_OK() throws Exception {
        Assert.assertEquals(globalMplStage,
                brandMgmt.getMarketplaceStage(globalMplId, globalMplLocale));
    }

    /**
     * Test for invalid marketplace id.
     */
    @Test
    public void testGetMarketplaceStage_invMplId() throws Exception {
        Assert.assertEquals("",
                brandMgmt.getMarketplaceStage("invid", globalMplLocale));
    }

    /**
     * A localized resource does not exists so the resource will be returned in
     * the default language.
     */
    @Test
    public void testGetMarketplaceStage_Default() throws Exception {
        Assert.assertEquals(globalMplStage,
                brandMgmt.getMarketplaceStage(globalMplId, "de"));
    }

    /**
     * Test for a different locale.
     */
    @Test
    public void testGetMarketplaceStage_diffMplLocale() throws Exception {
        Assert.assertEquals(globalMplStage + "ja",
                brandMgmt.getMarketplaceStage(globalMplId, "ja"));
    }

    /**
     * Test if no resource exists at all.
     */
    @Test
    public void testGetMarketplaceStage_noResource() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource template = new LocalizedResource(
                        globalMplLocale, lrDefault.getObjectKey(),
                        LocalizedObjectTypes.MARKETPLACE_STAGE);
                LocalizedResource storedResource = (LocalizedResource) mgr
                        .find(template);
                mgr.remove(storedResource);
                mgr.flush();
                return null;
            }
        });
        Assert.assertEquals("",
                brandMgmt.getMarketplaceStage(globalMplId, globalMplLocale));
    }

    /**
     * mpl id is null
     */
    @Test
    public void testGetMarketplaceStage_null1() throws Exception {
        Assert.assertEquals("",
                brandMgmt.getMarketplaceStage(null, globalMplLocale));
    }

    /**
     * Test if the default locale will be used.
     */
    @Test
    public void testGetMarketplaceStage_null2() throws Exception {
        Assert.assertEquals(globalMplStage,
                brandMgmt.getMarketplaceStage(globalMplId, null));
    }

    /**
     * all paraem are null.
     */
    @Test
    public void testGetMarketplaceStage_null3() throws Exception {
        Assert.assertEquals("", brandMgmt.getMarketplaceStage(null, null));
    }

    /**
     * locale is null => use default locale + no resource exsits at all
     */
    @Test
    public void testGetMarketplaceStage_null4() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource template = new LocalizedResource(
                        globalMplLocale, lrDefault.getObjectKey(),
                        LocalizedObjectTypes.MARKETPLACE_STAGE);
                LocalizedResource storedResource = (LocalizedResource) mgr
                        .find(template);
                mgr.remove(storedResource);
                mgr.flush();
                return null;
            }
        });
        Assert.assertEquals("",
                brandMgmt.getMarketplaceStage(globalMplId, null));
    }

    /**
     * good case test for storing the stage content.
     */
    @Test
    public void testSetMarketplaceStage_ok() throws Exception {
        container.login(operatorUserKey, ROLE_MARKETPLACE_OWNER);
        brandMgmt.setMarketplaceStage(customStageContent, globalMplId,
                globalMplLocale);
        assertStoredStage(globalMplLocale, customStageContent);
    }

    /**
     * pass "" and delete the localized entry from the DB
     */
    @Test
    public void testSetMarketplaceStage_delete() throws Exception {
        container.login(operatorUserKey, ROLE_MARKETPLACE_OWNER);
        brandMgmt.setMarketplaceStage(customStageContent, globalMplId,
                globalMplLocale);
        assertStoredStage(globalMplLocale, customStageContent);

        brandMgmt.setMarketplaceStage("", globalMplId, globalMplLocale);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource template = new LocalizedResource(
                        globalMplLocale, lrDefault.getObjectKey(),
                        LocalizedObjectTypes.MARKETPLACE_STAGE);
                LocalizedResource storedResource = (LocalizedResource) mgr
                        .find(template);
                Assert.assertNull(storedResource);
                return null;
            }
        });
    }

    /**
     * test the service call with a invalid market place id
     */
    @Test(expected = ObjectNotFoundException.class)
    public void testSetMarketplaceStage_invMpl() throws Exception {
        brandMgmt.setMarketplaceStage(customStageContent, "INVALID",
                globalMplLocale);
    }

    /**
     * save for another locale should not effect anything
     */
    @Test(expected = AssertionError.class)
    public void testSetMarketplaceStage_diffLocale() throws Exception {
        container.login(operatorUserKey,
                new String[] { ROLE_MARKETPLACE_OWNER });
        brandMgmt.setMarketplaceStage(customStageContent, globalMplId, "DE");
        assertStoredStage(globalMplLocale, customStageContent);
    }

    /**
     * Test the anonymous access
     */
    @Test(expected = EJBException.class)
    public void testSetMarketplaceStage_notAuthorized_anonymous()
            throws Exception {
        container.logout();
        brandMgmt.setMarketplaceStage(customStageContent, globalMplId, "DE");
    }

    /**
     * caller is service manager but not provider of the mpl
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testSetMarketplaceStage_callerNotOwner() throws Exception {
        container.login(supplierUserKey, new String[] { ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_MARKETPLACE_OWNER });
        brandMgmt.setMarketplaceStage(customStageContent, globalMplId, "EN");
    }

    /**
     * Test the customer access (noneservice manager)
     */
    @Test(expected = EJBException.class)
    public void testSetMarketplaceStage_notAuthorized_cust() throws Exception {
        container.logout();
        container.login(customerUserKey,
                new String[] { ROLE_ORGANIZATION_ADMIN });
        brandMgmt.setMarketplaceStage(customStageContent, globalMplId, "DE");
    }

    @Test(expected = EJBException.class)
    public void testSetMarketplaceStage_null1() throws Exception {
        brandMgmt.setMarketplaceStage(customStageContent, globalMplId, null);
    }

    @Test(expected = EJBException.class)
    public void testSetMarketplaceStage_null2() throws Exception {
        brandMgmt.setMarketplaceStage(customStageContent, null, "EN");
    }

    @Test(expected = EJBException.class)
    public void testSetMarketplaceStage_null3() throws Exception {
        brandMgmt.setMarketplaceStage(null, globalMplId, "EN");
    }

    @Test
    public void testGetMarketplaceStageLocalization() throws Exception {
        container.login(operatorUserKey, ROLE_MARKETPLACE_OWNER);
        brandMgmt.setMarketplaceStage("stage_en", globalMplId, "en");
        brandMgmt.setMarketplaceStage("stage_de", globalMplId, "de");
        brandMgmt.setMarketplaceStage("stage_ja", globalMplId, "ja");

        List<VOLocalizedText> localization = brandMgmt
                .getMarketplaceStageLocalization(globalMplId);
        Assert.assertNotNull(localization);
        Assert.assertEquals(3, localization.size());
        Map<String, String> map = map(localization);
        Assert.assertTrue(map.containsKey("en"));
        Assert.assertEquals("stage_en", map.get("en"));
        Assert.assertTrue(map.containsKey("de"));
        Assert.assertEquals("stage_de", map.get("de"));
        Assert.assertTrue(map.containsKey("ja"));
        Assert.assertEquals("stage_ja", map.get("ja"));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetMarketplaceStageLocalization_NotFound()
            throws Exception {
        container.login(operatorUserKey, ROLE_MARKETPLACE_OWNER);
        brandMgmt.getMarketplaceStageLocalization("invalid");
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetMarketplaceStageLocalization_CallerNotOwner()
            throws Exception {
        container.login(supplierUserKey, ROLE_MARKETPLACE_OWNER);
        brandMgmt.getMarketplaceStageLocalization(globalMplId);
    }

    /**
     * Asserts the content of the marketplace stage in the localized resource
     * table equals the passed value for a specific locale.
     */
    private void assertStoredStage(final String locale,
            final String exceptedStageContent) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource template = new LocalizedResource(locale,
                        lrDefault.getObjectKey(),
                        LocalizedObjectTypes.MARKETPLACE_STAGE);
                LocalizedResource storedResource = (LocalizedResource) mgr
                        .find(template);
                Assert.assertEquals(exceptedStageContent,
                        storedResource.getValue());
                return null;
            }
        });
    }

    private void verifySavedProps(final String marketplaceId, final String key,
            final String en, final String de, final String de_DE)
            throws Exception {
        // direct verification
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    String text;
                    Marketplace mp = new Marketplace();
                    mp.setMarketplaceId(marketplaceId);
                    mp = (Marketplace) mgr.getReferenceByBusinessKey(mp);
                    text = localizer.getLocalizedTextFromBundle(
                            LocalizedObjectTypes.MAIL_CONTENT, mp,
                            Locale.ENGLISH.toString(), key);
                    Assert.assertEquals(en, text);

                    text = localizer.getLocalizedTextFromBundle(
                            LocalizedObjectTypes.MAIL_CONTENT, mp,
                            Locale.GERMAN.toString(), key);
                    Assert.assertEquals(de, text);

                    text = localizer.getLocalizedTextFromBundle(
                            LocalizedObjectTypes.MAIL_CONTENT, mp,
                            Locale.GERMANY.toString(), key);
                    Assert.assertEquals(de_DE, text);

                    text = localizer.getLocalizedTextFromBundle(
                            LocalizedObjectTypes.MAIL_CONTENT, mp,
                            "de_CH_123456", key);
                    Assert.assertEquals(de, text);

                    text = localizer.getLocalizedTextFromBundle(
                            LocalizedObjectTypes.MAIL_CONTENT, mp, "de__123456",
                            key);
                    Assert.assertEquals(de, text);

                    text = localizer.getLocalizedTextFromBundle(
                            LocalizedObjectTypes.MAIL_CONTENT, mp,
                            "de_DE_123456", key);
                    Assert.assertEquals(de_DE, text);

                    text = localizer.getLocalizedTextFromBundle(
                            LocalizedObjectTypes.MAIL_CONTENT, mp,
                            Locale.FRENCH.toString(), key);
                    Assert.assertEquals(en, text);

                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    private static Map<String, Properties> prepareProps(final String key,
            final String en, final String de, final String de_DE) {
        Map<String, Properties> propertiesMap = new HashMap<>();

        Properties properties;

        properties = new Properties();
        properties.put(key, en);
        propertiesMap.put(Locale.ENGLISH.toString(), properties);

        properties = new Properties();
        properties.put(key, de);
        propertiesMap.put(Locale.GERMAN.toString(), properties);

        properties = new Properties();
        properties.put(key, de_DE);
        propertiesMap.put(Locale.GERMANY.toString(), properties);
        return propertiesMap;
    }

    private static Map<String, String> map(List<VOLocalizedText> localization) {
        HashMap<String, String> map = new HashMap<>();
        for (VOLocalizedText text : localization) {
            map.put(text.getLocale(), text.getText());
        }
        return map;
    }

}
