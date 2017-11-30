/* 
 *  Copyright FUJITSU LIMITED 2017
 **
 * 
 */
package org.oscm.ui.dialog.classic.trackingCode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.stubs.UiDelegateStub;
import org.oscm.internal.components.POMarketplace;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.trackingCode.POTrackingCode;
import org.oscm.internal.trackingCode.TrackingCodeManagementService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author tang
 * 
 */
public class TrackingCodeCtrlTest {

    private static final String CONCURRENT_MODIFICATION_ERROR = "concurrentModification";

    private TrackingCodeCtrl controller;
    private TrackingCodeModel model;
    private SessionBean session;
    private TrackingCodeManagementService tcMgmServ;
    private POTrackingCode trackingCode;
    private List<POMarketplace> marketplaces;
    private int MARKETPLACE_NUMBER = 10;
    private String TRACKINGCODE = "<script> tracking code demo<script>";
    private String TRACKINGCODE_MODIFY = "<script> changed tracking code demo<script>";

    @Captor
    ArgumentCaptor<POTrackingCode> pcCap;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        controller = spy(new TrackingCodeCtrl() {

            private static final long serialVersionUID = 1L;

            protected String getMarketplaceId() {
                return "mid";
            }

        });
        model = spy(new TrackingCodeModel());
        controller.setModel(model);
        tcMgmServ = mock(TrackingCodeManagementService.class);

        session = mock(SessionBean.class);
        controller.setSessionBean(session);

        marketplaces = new LinkedList<POMarketplace>();
        for (int i = 0; i < MARKETPLACE_NUMBER; i++) {
            marketplaces.add(new POMarketplace("mp" + i, "Marketplace " + i));
        }

        doReturn(marketplaces).when(tcMgmServ).getMarketplaceSelections();
        doReturn(tcMgmServ).when(controller).getTrackingCodeManagementService();
        controller.ui = new UiDelegateStub();
        trackingCode = new POTrackingCode();
    }

    @Test
    public void initializeModel() {
        // when
        controller.initializeModel();
        // then
        assertNotNull(controller.getModel());
        assertSame(model, controller.getModel());

        assertNull(model.getMarketplaceId());
        assertEquals(MARKETPLACE_NUMBER, model.getMarketplaces().size());

        assertNull(model.getTrackingCodeObject().getMarketplaceId());
        assertNull(model.getTrackingCodeObject().getTrackingCode());
        assertNull(model.getSelectedMarketplace());
    }

    @Test
    public void marketplaceChanged() throws Exception {
        model.setSelectedMarketplace("mp");
        doReturn(new Response()).when(tcMgmServ)
                .loadTrackingCodeForMarketplace(anyString());

        // when
        String result = controller.marketplaceChanged();

        // then
        assertEquals("", result);
        verify(controller, never()).initSelectableMarketplaces();
    }

    @Test
    public void marketplaceChanged_concurrentChanged() throws Exception {
        model.setSelectedMarketplace("mp");
        doThrow(new ObjectNotFoundException()).when(tcMgmServ)
                .loadTrackingCodeForMarketplace(anyString());

        // when
        String result = controller.marketplaceChanged();

        // then
        assertEquals(CONCURRENT_MODIFICATION_ERROR, result);
        verify(controller, times(1)).loadTrackingCode(anyString());
        verify(controller, times(1)).initSelectableMarketplaces();

    }

    @Test
    public void save() throws Exception {
        model.setSelectedMarketplace("mp5");
        model.setTrackingCodeObject(trackingCode);
        when(tcMgmServ.saveTrackingCode(pcCap.capture())).thenReturn(
                new Response());

        // when
        model.setTrackingCodeValue(TRACKINGCODE_MODIFY);
        String result = controller.save();
        // then
        assertEquals(null, result);
        assertEquals(TRACKINGCODE_MODIFY, pcCap.getValue().getTrackingCode());
    }

    @Test
    public void save_error() throws Exception {
        model.setSelectedMarketplace("mp7");
        doThrow(new ConcurrentModificationException()).when(tcMgmServ)
                .saveTrackingCode(any(POTrackingCode.class));
        doNothing().when(controller).concurrentModification();

        // when
        String result = controller.save();

        // then
        assertEquals(TrackingCodeCtrl.CONCURRENT_MODIFICATION_ERROR, result);
        assertTrue(controller.ui.hasErrors());
    }

    @Test
    public void getTrackingCodeForCurrentMarketplace_inSession() {
        doReturn(TRACKINGCODE).when(session).getMarketplaceTrackingCode();
        String marketplace = controller.getTrackingCodeForCurrentMarketplace();
        assertEquals(TRACKINGCODE, marketplace);
    }

    @Test
    public void getTrackingCodeForCurrentMarketplace_inDB() throws Exception {
        model.setSelectedMarketplace("mp8");
        doReturn(null).when(session).getMarketplaceTrackingCode();
        doReturn(Boolean.TRUE).when(controller).loadTrackingCode(anyString());
        doReturn(TRACKINGCODE).when(model).getTrackingCodeValue();
        // when
        String marketplace = controller.getTrackingCodeForCurrentMarketplace();
        // then
        verify(controller, times(1)).loadTrackingCode(anyString());
        verify(session, times(1)).setMarketplaceTrackingCode(eq(TRACKINGCODE));
        assertEquals(TRACKINGCODE, marketplace);
    }

    @Test
    public void getTrackingCodeForCurrentMarketplace_notInDB() throws Exception {
        model.setSelectedMarketplace("mp8");
        doReturn(null).when(session).getMarketplaceTrackingCode();
        doReturn(Boolean.FALSE).when(controller).loadTrackingCode(anyString());
        // when
        String marketplace = controller.getTrackingCodeForCurrentMarketplace();
        // then
        verify(session, times(1)).setMarketplaceTrackingCode(eq(""));

        assertEquals("", marketplace);
    }
}
