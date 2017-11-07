/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.trackingcode;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Marketplace;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.trackingCode.POTrackingCode;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author Tang
 */
public class TrackingCodeManagementServiceBeanTest {

    private final static String MARKETPLACE_ID = "Marketplace Id";
    private final static String TRACKING_CODE = "<script> tracking(code)</script>";

    private TrackingCodeManagementServiceBean trackingCodeManagementServiceBean;
    private Marketplace marketplace;
    private MarketplaceServiceLocal mpServiceLocal;
    private Response response;

    @Before
    public void setUp() throws Exception {
        trackingCodeManagementServiceBean = spy(new TrackingCodeManagementServiceBean());
        marketplace = new Marketplace();
        marketplace.setMarketplaceId(MARKETPLACE_ID);
        marketplace.setTrackingCode(TRACKING_CODE);

        mpServiceLocal = mock(MarketplaceServiceLocal.class);
        doReturn(marketplace).when(mpServiceLocal).getMarketplace(
                eq(MARKETPLACE_ID));
        doNothing().when(mpServiceLocal).updateMarketplaceTrackingCode(
                anyString(), anyInt(), anyString());

        response = new Response();

        trackingCodeManagementServiceBean.mpServiceLocal = mpServiceLocal;
        trackingCodeManagementServiceBean.sessionCtx = mock(SessionContext.class);
    }

    @Test
    public void saveTrackingCode() throws Exception {
        // given
        POTrackingCode trackingCode = new POTrackingCode();
        trackingCode.setMarketplaceId(MARKETPLACE_ID);
        trackingCode.setTrackingCode(TRACKING_CODE);

        ArrayList<Object> resultList = new ArrayList<Object>();
        resultList.add(trackingCode);

        response.setResults(resultList);
        doReturn(response).when(trackingCodeManagementServiceBean)
                .loadTrackingCodeForMarketplace(eq(MARKETPLACE_ID));

        // when
        Response resp = trackingCodeManagementServiceBean
                .saveTrackingCode(trackingCode);

        // then
        assertEquals(MARKETPLACE_ID, resp.getResult(POTrackingCode.class)
                .getMarketplaceId());
        assertEquals(TRACKING_CODE, resp.getResult(POTrackingCode.class)
                .getTrackingCode());
    }

    @Test
    public void saveTrackingCode_ObjectNotFound() throws Exception {
        // given
        POTrackingCode trackingCode = new POTrackingCode();
        trackingCode.setMarketplaceId(MARKETPLACE_ID);
        trackingCode.setTrackingCode(TRACKING_CODE);
        doThrow(new ObjectNotFoundException()).when(mpServiceLocal)
                .updateMarketplaceTrackingCode(anyString(), anyInt(),
                        anyString());
        try {
            // when
            trackingCodeManagementServiceBean.saveTrackingCode(trackingCode);
        } catch (ObjectNotFoundException e) {
            // then rollback transaction
            verify(trackingCodeManagementServiceBean.sessionCtx)
                    .setRollbackOnly();
        }
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveTrackingCode_ConcurrentModification() throws Exception {
        // given
        POTrackingCode trackingCode = new POTrackingCode();
        trackingCode.setMarketplaceId(MARKETPLACE_ID);
        trackingCode.setTrackingCode(TRACKING_CODE);
        doThrow(new ConcurrentModificationException()).when(mpServiceLocal)
                .updateMarketplaceTrackingCode(anyString(), anyInt(),
                        anyString());
        // when
        trackingCodeManagementServiceBean.saveTrackingCode(trackingCode);
    }

    @Test
    public void loadTrackingCodeForMarketplace() throws Exception {
        // given
        doReturn(TRACKING_CODE).when(mpServiceLocal)
                .getTrackingCodeFromMarketplace(eq(MARKETPLACE_ID));
        // when
        Response resp = trackingCodeManagementServiceBean
                .loadTrackingCodeForMarketplace(MARKETPLACE_ID);
        // then
        assertEquals(TRACKING_CODE, resp.getResult(POTrackingCode.class)
                .getTrackingCode());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void loadTrackingCodeForMarketplace_ObjectNotFound()
            throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(mpServiceLocal)
                .getMarketplace(eq(MARKETPLACE_ID));
        // when
        trackingCodeManagementServiceBean
                .loadTrackingCodeForMarketplace(MARKETPLACE_ID);
    }

}
