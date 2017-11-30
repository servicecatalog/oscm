/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import java.util.Locale;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.vo.VOServiceDetails;

@SuppressWarnings("boxing")
public class ServiceDetailsTest {

    VOServiceDetails voDetails;

    @Before
    public void setup() throws Exception {
        voDetails = new VOServiceDetails();
        voDetails.setImageDefined(true);
        voDetails.setAccessType(ServiceAccessType.DIRECT);
        voDetails.setDescription("marketingName");
        voDetails.setFeatureURL("technicalURL");
        voDetails.setName("name");
        voDetails.setServiceId("serviceId");
        voDetails.setStatus(ServiceStatus.ACTIVE);
        voDetails.setSellerId("supplierId");
        voDetails.setSellerName("supplierName");
        voDetails.setTechnicalId("technicalId");

        new FacesContextStub(Locale.ENGLISH);
    }

    @Test
    public void testService() throws Exception {
        ServiceDetails serviceDetails = new ServiceDetails(voDetails);
        Assert.assertEquals(voDetails.isImageDefined(),
                serviceDetails.isImageDefined());
        Assert.assertEquals(voDetails.getDescription(),
                serviceDetails.getDescription());
        Assert.assertEquals(voDetails.getFeatureURL(),
                serviceDetails.getFeatureURL());
        Assert.assertEquals(voDetails.getName(), serviceDetails.getName());
        Assert.assertEquals(voDetails.getServiceId(),
                serviceDetails.getServiceId());
        Assert.assertEquals(voDetails.getSellerName(),
                serviceDetails.getSupplierName());
        Assert.assertEquals(voDetails.getTechnicalId(),
                serviceDetails.getTechnicalId());
    }

    @Test
    public void testEmptyName() {
        voDetails.setName("");
        ServiceDetails serviceDetails = new ServiceDetails(voDetails);
        Assert.assertEquals("", serviceDetails.getName());
    }

    @Test
    public void testGetNameToDisplay_NullName() {
        VOServiceDetails voDetails = new VOServiceDetails();
        voDetails.setName(null);

        ServiceDetails details = new ServiceDetails(voDetails);
        String result = details.getNameToDisplay();
        Assert.assertEquals(JSFUtils.getText("service.name.undefined", null),
                result);
    }

    @Test
    public void testGetNameToDisplay_EmptyName() {
        VOServiceDetails voDetails = new VOServiceDetails();
        voDetails.setName("");

        ServiceDetails details = new ServiceDetails(voDetails);
        String result = details.getNameToDisplay();
        Assert.assertEquals(JSFUtils.getText("service.name.undefined", null),
                result);
    }

    @Test
    public void testGetNameToDisplay_NotEmptyName() {
        VOServiceDetails voDetails = new VOServiceDetails();
        voDetails.setName("serviceDetail1");

        ServiceDetails details = new ServiceDetails(voDetails);
        String result = details.getNameToDisplay();
        Assert.assertEquals(voDetails.getNameToDisplay(), result);
    }

}
