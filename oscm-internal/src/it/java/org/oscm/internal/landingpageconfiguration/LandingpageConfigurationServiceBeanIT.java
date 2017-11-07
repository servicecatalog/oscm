/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                         
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.landingpageconfiguration;

import static org.oscm.test.matchers.JavaMatchers.hasItems;
import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import org.junit.Test;
import org.mockito.ArgumentMatcher;

import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.landingpageService.local.VOPublicLandingpage;
import org.oscm.landingpageService.local.VOLandingpageService;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exceptions.FillinOptionNotSupportedException;
import org.oscm.internal.vo.VOService;

/**
 * @author cheld
 * 
 */
public class LandingpageConfigurationServiceBeanIT extends EJBTestBase {

    LandingpageConfigurationService configureLandingpageService;

    LandingpageConfigurationServiceBean configureLandingpageServiceBean;

    LandingpageServiceLocal landingpageService;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        configureLandingpageServiceBean = new LandingpageConfigurationServiceBean();
        container.addBean(configureLandingpageServiceBean);
        landingpageService = mock(LandingpageServiceLocal.class);
        container.addBean(landingpageService);
        configureLandingpageService = container
                .get(LandingpageConfigurationService.class);
        configureLandingpageServiceBean.sessionCtx = mock(SessionContext.class);
    }

    private List<VOService> twoVOServices() {
        List<VOService> result = new ArrayList<VOService>();
        result.add(createService(1, ServiceStatus.ACTIVE));
        result.add(createService(2, ServiceStatus.OBSOLETE));
        return result;
    }

    private VOService createService(long key, ServiceStatus status) {
        VOService service = new VOService();
        service.setKey(key);
        service.setVersion(3);
        service.setSellerName("supplier name");
        service.setName("name");
        service.setStatus(status);
        return service;
    }

    /**
     * Empty list is returned in case of exception
     */
    @Test
    public void availableServices_exception() throws Exception {

        // given
        given(landingpageService.availableServices("mId")).willThrow(
                new ObjectNotFoundException());

        // when
        Response r = configureLandingpageService.loadPublicLandingpageConfig("mId");
        List<POService> services = r.getResultList(POService.class);

        // then
        assertThat(services, hasNoItems());

    }

    /**
     * Empty list is returned in case of null input
     */
    @Test
    public void availableServices_nullId() throws Exception {
        Response r = configureLandingpageService.loadPublicLandingpageConfig(null);
        List<POService> services = r.getResultList(POService.class);
        assertThat(services, hasNoItems());
    }

    /**
     * Save landing page configuration. Backend service must be called and data
     * reloaded.
     */
    @Test
    public void saveLandingpageConfig() throws Exception {

        // given a landing page configuration
        POPublicLandingpageConfig toBeSaved = givenLandingpageConfiguration();
        container.login("admin", "MARKETPLACE_OWNER");

        // when saving
        Response response = configureLandingpageService
                .savePublicLandingpageConfig(toBeSaved);

        // then back-end service is called and reloaded data is added to
        // response
        verify(landingpageService).savePublicLandingpageConfig(
                isCalledWithMappedVOLandingPage());
        assertThat(response.getResult(POPublicLandingpageConfig.class),
                notNullValue());
        assertThat(response.getResultList(POService.class), hasItems());
    }

    private POPublicLandingpageConfig givenLandingpageConfiguration()
            throws Exception {

        // prepare PO to be saved
        POPublicLandingpageConfig configToBeSaved = new POPublicLandingpageConfig();
        configToBeSaved.setMarketplaceId("mId");

        // prepare data for reloading after save
        when(landingpageService.loadPublicLandingpageConfig(anyString())).thenReturn(
                voLandingPage("mId"));
        when(landingpageService.availableServices(anyString())).thenReturn(
                twoVOServices());
        return configToBeSaved;
    }

    private VOPublicLandingpage isCalledWithMappedVOLandingPage() {
        return argThat(new ArgumentMatcher<VOPublicLandingpage>() {
            @Override
            public boolean matches(Object argument) {
                VOPublicLandingpage vo = (VOPublicLandingpage) argument;
                if (vo.getMarketplaceId().equals("mId")) {
                    return true;
                }
                return false;
            }
        });
    }

    @Test
    public void buildVOLandingpage() {

        // given
        POPublicLandingpageConfig poConfig = new POPublicLandingpageConfig();
        poConfig.setKey(1);
        poConfig.setVersion(2);
        poConfig.setMarketplaceId("mId");
        poConfig.setFillinCriterion(FillinCriterion.NO_FILLIN);
        poConfig.setNumberOfServicesOnLp(7);

        POService poService = new POService(1, 2);
        poConfig.getFeaturedServices().add(poService);

        // when
        VOPublicLandingpage voConfig = configureLandingpageServiceBean
                .buildVOLandingpage(poConfig);

        // then
        assertTrue(voConfig.getKey() == 1);
        assertTrue(voConfig.getVersion() == 2);
        assertThat(voConfig.getMarketplaceId(), equalTo("mId"));
        assertThat(voConfig.getFillinCriterion(),
                equalTo(FillinCriterion.NO_FILLIN));
        assertTrue(voConfig.getNumberServices() == 7);
        assertTrue(voConfig.getLandingpageServices().get(0).getKey() == 1);
        assertTrue(voConfig.getLandingpageServices().get(0).getVersion() == 2);
    }

    @Test
    public void saveLandingpageConfig_ObjectNotFoundException()
            throws Exception {
        // given
        willThrow(new ObjectNotFoundException()).given(landingpageService)
                .savePublicLandingpageConfig(any(VOPublicLandingpage.class));
        container.login("admin", "MARKETPLACE_OWNER");

        // when
        try {
            configureLandingpageService
                    .savePublicLandingpageConfig(new POPublicLandingpageConfig());
        } catch (ObjectNotFoundException e) {
            // then
            verify(configureLandingpageServiceBean.sessionCtx)
                    .setRollbackOnly();
        }
    }

    @Test
    public void saveLandingpageConfig_ValidationException() throws Exception {
        // given
        willThrow(new ValidationException()).given(landingpageService)
                .savePublicLandingpageConfig(any(VOPublicLandingpage.class));
        container.login("admin", "MARKETPLACE_OWNER");

        // when
        try {
            configureLandingpageService
                    .savePublicLandingpageConfig(new POPublicLandingpageConfig());
        } catch (ValidationException e) {
            // then
            verify(configureLandingpageServiceBean.sessionCtx)
                    .setRollbackOnly();
        }
    }

    @Test
    public void saveLandingpageConfig_FillinOptionNotSupportedException()
            throws Exception {
        // given
        willThrow(new FillinOptionNotSupportedException()).given(
                landingpageService).savePublicLandingpageConfig(
                any(VOPublicLandingpage.class));
        container.login("admin", "MARKETPLACE_OWNER");

        // when
        try {
            configureLandingpageService
                    .savePublicLandingpageConfig(new POPublicLandingpageConfig());
        } catch (FillinOptionNotSupportedException e) {
            // then
            verify(configureLandingpageServiceBean.sessionCtx)
                    .setRollbackOnly();
        }
    }

    @Test
    public void resetLandingPage() throws Exception {

        // given
        givenLandingpageConfiguration();
        container.login("admin", "MARKETPLACE_OWNER");

        // when resetting
        Response response = configureLandingpageService.resetLandingPage("mId");

        // then back-end service is called and reloaded data is added to
        // response
        verify(landingpageService).resetLandingpage("mId");
        assertThat(response.getResult(POPublicLandingpageConfig.class),
                notNullValue());
        assertThat(response.getResultList(POService.class), hasItems());
    }

    @Test
    public void resetLandingPage_ObjectNotFoundException() throws Exception {
        // given
        willThrow(new ObjectNotFoundException()).given(landingpageService)
                .resetLandingpage("mId");
        container.login("admin", "MARKETPLACE_OWNER");

        try {
            // when resetting
            configureLandingpageService.resetLandingPage("mId");
            fail();
        } catch (ObjectNotFoundException e) {
            // then rollback transaction
            verify(configureLandingpageServiceBean.sessionCtx)
                    .setRollbackOnly();
        }
    }

    @Test
    public void testForCoverage() {
        configureLandingpageService.getNumOfServicesRange();
    }

    @Test
    public void assemblePOLandingpageConfig() {

        // given
        VOPublicLandingpage vo = voLandingPage("mId");

        vo.getLandingpageServices().add(
                createLandingpageService(1, ServiceStatus.ACTIVE));
        vo.getLandingpageServices().add(
                createLandingpageService(2, ServiceStatus.OBSOLETE));

        // when
        POPublicLandingpageConfig po = configureLandingpageServiceBean
                .assemblePOLandingpageConfig(vo);

        // then
        assertTrue(po.getKey() == 1);
        assertTrue(po.getVersion() == 2);
        assertThat(po.getMarketplaceId(), equalTo("mId"));
        assertTrue(po.getNumberOfServicesOnLp() == 5);
        assertThat(po.getFillinCriterion(), equalTo(FillinCriterion.NO_FILLIN));
        assertTrue(po.getFeaturedServices().get(0).getKey() == 1);
        assertTrue(po.getFeaturedServices().get(0).getVersion() == 3);
        assertThat(po.getFeaturedServices().get(0).getPictureUrl(),
                equalTo("/image?type=SERVICE_IMAGE&amp;serviceKey=1"));
        assertThat(po.getFeaturedServices().get(0).getProviderName(),
                equalTo("supplier name"));
        assertThat(po.getFeaturedServices().get(0).getServiceName(),
                equalTo("name"));
        assertThat(po.getFeaturedServices().get(0).getStatusSymbol(),
                equalTo("status_ACTIVE"));
        assertThat(po.getFeaturedServices().get(1).getStatusSymbol(),
                equalTo("status_NOT_ACTIVE"));
    }

    private VOLandingpageService createLandingpageService(long key,
            ServiceStatus status) {
        VOLandingpageService landningPageService1 = new VOLandingpageService();
        VOService service1 = createService(key, status);
        landningPageService1.setService(service1);
        return landningPageService1;
    }

    @Test
    public void loadLandingpageConfig() throws Exception {

        // given
        given(landingpageService.loadPublicLandingpageConfig("mId")).willReturn(
                voLandingPage("mId"));

        // when
        Response r = configureLandingpageService.loadPublicLandingpageConfig("mId");
        POPublicLandingpageConfig po = r.getResult(POPublicLandingpageConfig.class);
        assertThat(po.getMarketplaceId(), equalTo("mId"));
    }

    private VOPublicLandingpage voLandingPage(String mId) {
        VOPublicLandingpage vo = new VOPublicLandingpage();
        vo.setMarketplaceId(mId);
        vo.setKey(1);
        vo.setVersion(2);
        vo.setMarketplaceId("mId");
        vo.setNumberServices(5);
        vo.setFillinCriterion(FillinCriterion.NO_FILLIN);
        return vo;
    }

    @Test(expected = ObjectNotFoundException.class)
    public void loadLandingpageConfig_exception() throws Exception {
        // given
        given(landingpageService.loadPublicLandingpageConfig("mId")).willThrow(
                new ObjectNotFoundException());

        // when
        configureLandingpageService.loadPublicLandingpageConfig("mId");
    }
}
