/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.domobjects.Event;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * Tests to ensure correct behaviour of the technical product assembler.
 * 
 * @author pock
 * 
 */
public class TechnicalProductAssemblerTest {

    private final LocalizerFacade facade = new LocalizerFacade(
            new LocalizerServiceStub() {

                @Override
                public String getLocalizedTextFromDatabase(String localeString,
                        long objectKey, LocalizedObjectTypes objectType) {
                    return "";
                }
            }, "en");

    @Test(expected = ValidationException.class)
    public void testUpdateTechnicalProductEmptyTechnicalProductId()
            throws Exception {
        VOTechnicalService vo = new VOTechnicalService();

        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateTechnicalProductEmptyVersion() throws Exception {
        VOTechnicalService vo = new VOTechnicalService();
        vo.setTechnicalServiceId("technicalProductId");

        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateTechnicalProductDirectEmptyProvisioningUrl()
            throws Exception {
        VOTechnicalService vo = createVOTechnicalProduct();
        vo.setAccessType(ServiceAccessType.DIRECT);

        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateTechnicalProductDirectWrongProvisioningUrl()
            throws Exception {
        VOTechnicalService vo = createVOTechnicalProduct();
        vo.setAccessType(ServiceAccessType.DIRECT);
        vo.setProvisioningUrl("provisioningUrl");

        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);
    }

    @Test
    public void testUpdateTechnicalProductDirect() throws Exception {
        VOTechnicalService vo = createVOTechnicalProduct();
        vo.setAccessType(ServiceAccessType.DIRECT);
        vo.setProvisioningUrl("http://localhost");
        vo.setProvisioningVersion("1");

        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);
        Assert.assertEquals(vo.getTechnicalServiceId(),
                domObj.getTechnicalProductId());
        Assert.assertEquals(vo.getAccessType(), domObj.getAccessType());
        Assert.assertEquals(vo.getProvisioningUrl(),
                domObj.getProvisioningURL());
    }

    @Test
    public void testUpdateTechnicalProductDirectEmptyBaseUrl() throws Exception {
        VOTechnicalService vo = createVOTechnicalProduct();
        vo.setAccessType(ServiceAccessType.DIRECT);
        vo.setProvisioningUrl("http://localhost");

        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);
    }

    @Test
    public void testUpdateTechnicalProductUserEmptyBaseUrl() throws Exception {
        VOTechnicalService vo = createVOTechnicalProduct();
        vo.setAccessType(ServiceAccessType.USER);
        vo.setProvisioningUrl("http://localhost");

        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);
    }

    @Test
    public void testUpdateTechnicalProductProxy() throws Exception {
        VOTechnicalService vo = createVOTechnicalProduct();
        vo.setAccessType(ServiceAccessType.LOGIN);
        vo.setBaseUrl("http://localhost");
        vo.setProvisioningUrl("http://localhost");

        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);
        Assert.assertEquals(vo.getAccessType(), domObj.getAccessType());
        Assert.assertEquals(vo.getBaseUrl(), domObj.getBaseURL());
    }

    @Test
    public void testUpdateTechnicalProductLogin() throws Exception {
        VOTechnicalService vo = createVOTechnicalProduct();
        vo.setAccessType(ServiceAccessType.LOGIN);
        vo.setBaseUrl("http://localhost");
        vo.setProvisioningUrl("");
        vo.setProvisioningVersion("1");

        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);
        Assert.assertEquals(vo.getAccessType(), domObj.getAccessType());
        Assert.assertEquals(vo.getBaseUrl(), domObj.getBaseURL());
    }

    @Test
    public void testUpdateTechnicalProductLogin_BaseUrlWithSlash()
            throws Exception {
        // given a technical service with a base url with slash
        VOTechnicalService vo = createVOTechnicalProduct();
        vo.setAccessType(ServiceAccessType.USER);
        vo.setBaseUrl("http://localhost/");
        vo.setProvisioningUrl("/provurl");
        vo.setProvisioningVersion("1");
        vo.setLoginPath("/login");

        // when updating the technical product
        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);

        // verify that the base url, the provisioning url and the login path
        // of the dom object are correct.
        Assert.assertEquals("http://localhost/", domObj.getBaseURL());
        Assert.assertEquals("http://localhost/provurl",
                domObj.getProvisioningURL());
        Assert.assertEquals("/login", domObj.getLoginPath());
    }

    @Test
    public void testUpdateTechnicalProductLogin_BaseUrlWithoutSlash()
            throws Exception {
        // given a technical service with a base url without slash
        VOTechnicalService vo = createVOTechnicalProduct();
        vo.setAccessType(ServiceAccessType.USER);
        vo.setBaseUrl("http://localhost");
        vo.setProvisioningUrl("/provurl");
        vo.setProvisioningVersion("1");
        vo.setLoginPath("/login");
        vo.setBillingIdentifier("BID");

        // when updating the technical product
        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);

        // verify that the base url, the provisioning url and the login path
        // of the dom object are correct.
        Assert.assertEquals("http://localhost", domObj.getBaseURL());
        Assert.assertEquals("http://localhost/provurl",
                domObj.getProvisioningURL());
        Assert.assertEquals("/login", domObj.getLoginPath());
    }

    @Test(expected = ValidationException.class)
    public void testUpdateTechnicalProductLogin_LoginPathWithoutSlash()
            throws Exception {
        // given a technical service with a base url with slash
        // and no relative login path
        VOTechnicalService vo = createVOTechnicalProduct();
        vo.setAccessType(ServiceAccessType.LOGIN);
        vo.setBaseUrl("http://localhost/");
        vo.setProvisioningUrl("/provurl");
        vo.setProvisioningVersion("1");
        vo.setLoginPath("login");

        // when updating the technical product a validation exception is
        // expected
        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);
    }

    @Test
    public void testUpdateTechnicalProductPlatform() throws Exception {
        VOTechnicalService vo = createVOTechnicalProduct();
        vo.setAccessType(ServiceAccessType.LOGIN);
        vo.setBaseUrl("http://localhost/");
        vo.setProvisioningUrl("");
        vo.setProvisioningVersion("1");

        TechnicalProduct domObj = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(domObj, vo);
        Assert.assertEquals(vo.getAccessType(), domObj.getAccessType());
        Assert.assertEquals("http://localhost/", domObj.getBaseURL());
    }

    @Test
    public void testToVOTechnicalProduct() throws Exception {
        TechnicalProduct domObj = new TechnicalProduct();
        domObj.setTechnicalProductId("technicalProductId");
        domObj.setTechnicalProductBuildId("technicalProductBuildId");
        domObj.setAccessType(ServiceAccessType.DIRECT);
        domObj.setBaseURL("baseURL");
        domObj.setLoginPath("loginPath");
        domObj.setProvisioningURL("provisioningURL");
        domObj.setProvisioningVersion("1.0");
        domObj.setBillingIdentifier("BID");

        TechnicalProductOperation technicalProductOperation = new TechnicalProductOperation();
        technicalProductOperation.setKey(1234);
        technicalProductOperation.setOperationId("OPERATION_IDENTIFIER");
        domObj.setTechnicalProductOperations(Collections
                .singletonList(technicalProductOperation));

        VOTechnicalService vo = TechnicalProductAssembler.toVOTechnicalProduct(
                domObj, new ArrayList<ParameterDefinition>(),
                new ArrayList<Event>(), facade, false);

        Assert.assertEquals(domObj.getTechnicalProductId(),
                vo.getTechnicalServiceId());
        Assert.assertEquals(domObj.getTechnicalProductBuildId(),
                vo.getTechnicalServiceBuildId());
        Assert.assertEquals(domObj.getAccessType(), vo.getAccessType());
        Assert.assertEquals(domObj.getBaseURL(), vo.getBaseUrl());
        Assert.assertEquals(domObj.getLoginPath(), vo.getLoginPath());
        Assert.assertEquals(domObj.getProvisioningURL(),
                vo.getProvisioningUrl());
        Assert.assertEquals(domObj.getProvisioningVersion(),
                vo.getProvisioningVersion());

        List<VOTechnicalServiceOperation> operations = vo
                .getTechnicalServiceOperations();
        Assert.assertNotNull(operations);
        Assert.assertEquals(1, operations.size());
        VOTechnicalServiceOperation operation = operations.get(0);
        Assert.assertEquals(1234, operation.getKey());
        Assert.assertEquals("OPERATION_IDENTIFIER", operation.getOperationId());
        Assert.assertEquals("", operation.getOperationDescription());
        Assert.assertEquals("", operation.getOperationName());
        Assert.assertEquals("BID", vo.getBillingIdentifier());
    }

    @Test
    public void testUpdateTechnicalProduct_UserAccess() throws Exception {
        VOTechnicalService technicalService = createVOTechnicalProduct();
        technicalService.setAccessType(ServiceAccessType.USER);
        technicalService.setBaseUrl("http://localhost");
        technicalService.setProvisioningUrl("");
        technicalService.setProvisioningVersion("1");
        technicalService.setLoginPath("");

        TechnicalProduct technicalProduct = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(technicalProduct,
                technicalService);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateTechnicalProductEmptyBaseUrl_UserAccess()
            throws Exception {
        VOTechnicalService technicalService = createVOTechnicalProduct();
        technicalService.setAccessType(ServiceAccessType.USER);
        technicalService.setBaseUrl("");
        technicalService.setProvisioningUrl("");
        technicalService.setProvisioningVersion("1");

        TechnicalProduct technicalProduct = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(technicalProduct,
                technicalService);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateTechnicalProductNullBaseUrl_UserAccess()
            throws Exception {
        VOTechnicalService technicalService = createVOTechnicalProduct();
        technicalService.setAccessType(ServiceAccessType.USER);
        technicalService.setBaseUrl(null);
        technicalService.setProvisioningUrl("");
        technicalService.setProvisioningVersion("1");

        TechnicalProduct technicalProduct = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(technicalProduct,
                technicalService);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateTechnicalProductWrongBaseUrl_UserAccess()
            throws Exception {
        VOTechnicalService technicalService = createVOTechnicalProduct();
        technicalService.setAccessType(ServiceAccessType.USER);
        technicalService.setBaseUrl("invalid base url");
        technicalService.setProvisioningUrl("");
        technicalService.setProvisioningVersion("1");

        TechnicalProduct technicalProduct = new TechnicalProduct();
        TechnicalProductAssembler.updateTechnicalProduct(technicalProduct,
                technicalService);
    }

    private VOTechnicalService createVOTechnicalProduct() {
        VOTechnicalService vo = new VOTechnicalService();
        vo.setTechnicalServiceId("technicalProductId");
        return vo;
    }

}
