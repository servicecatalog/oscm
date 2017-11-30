/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.pricemodel.external;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import javax.faces.component.UIViewRoot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import org.oscm.internal.pricemodel.external.ExternalPriceModelException;
import org.oscm.internal.pricemodel.external.ExternalPriceModelService;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.stubs.FacesContextStub;

/**
 * @author iversen
 * 
 */
public class ExternalCustomerPriceModelCtrlTest extends ExternalPriceModelTest {

    private ExternalCustomerPriceModelCtrl ctrl;
    private ExternalPriceModelModel model;
    private Locale locale = Locale.ENGLISH;
    private ApplicationBean appbean;
    private SessionBean sessionBean;
    private ExternalPriceModelService externalPriceModelService;
    private ExternalPriceModelCtrl externalPriceModelCtrl;
    private static final UUID PRICE_MODEL_UUID1 = UUID
            .fromString("392b159e-e60a-41c4-8495-4fd4b6252ed9");

    @Before
    public void beforeClass() {
        ctrl = spy(new ExternalCustomerPriceModelCtrl());
        model = spy(new ExternalPriceModelModel());
        ctrl.setModel(model);
        ctrl.ui = mock(UiDelegate.class);

        appbean = mock(ApplicationBean.class);
        sessionBean = mock(SessionBean.class);
        doReturn(sessionBean).when(ctrl).getSessionBean();

        externalPriceModelService = mock(ExternalPriceModelService.class);
        externalPriceModelCtrl = mock(ExternalPriceModelCtrl.class);

        doReturn(locale).when(appbean).getDefaultLocale();
        doReturn(externalPriceModelService).when(ctrl)
                .getExternalPriceModelService();

        UIViewRoot viewRoot = mock(UIViewRoot.class);
        given(viewRoot.getLocale()).willReturn(locale);
        new FacesContextStub(locale).setViewRoot(viewRoot);
    }

    @Test
    public void testUpload() throws ExternalPriceModelException {

        // given
        PriceModel priceModel = createExternalPriceModel(PRICE_MODEL_UUID1,
                new Locale("en"));
        doReturn(priceModel).when(externalPriceModelService)
                .getExternalPriceModelForCustomer(
                        Mockito.any(VOServiceDetails.class),
                        Mockito.any(VOOrganization.class));

        VOServiceDetails service = new VOServiceDetails();

        VOOrganization customer = new VOOrganization();
        // when
        ctrl.upload(service, customer);

        // then
        verify(externalPriceModelService, times(1))
                .getExternalPriceModelForCustomer(
                        Mockito.any(VOServiceDetails.class),
                        Mockito.any(VOOrganization.class));

    }

    @Test
    public void testUploadEmpty() throws SaaSApplicationException {

        // given
        doReturn(null).when(externalPriceModelService)
                .getExternalPriceModelForService(
                        Mockito.any(VOServiceDetails.class));

        // when
        ctrl.upload(new VOServiceDetails(), new VOOrganization());

        // then
        verify(ctrl, times(0)).loadPriceModelContent(null);
    }

    @Test
    public void testDisplay() throws IOException, SaaSApplicationException {

        // given
        PriceModelContent createPriceModelContent = createPriceModelContent();
        doReturn(createPriceModelContent).when(model)
                .getSelectedPriceModelContent();

        // when
        ctrl.display();

    }

    @Test
    public void testReloadPriceModel_withCustomerTemplate() {
        // given
        VOServiceDetails voServiceDetails = new VOServiceDetails();
        voServiceDetails.setServiceType(ServiceType.CUSTOMER_TEMPLATE);

        // when
        ctrl.reloadPriceModel(voServiceDetails);

        // then
        verify(externalPriceModelCtrl, times(0)).resetPriceModel();
    }

}
