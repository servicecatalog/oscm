/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.trackingcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.trackingCode.POTrackingCode;
import org.oscm.internal.trackingCode.TrackingCodeManagementService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.marketplace.bean.MarketplaceServiceLocalBean;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author Tang
 * 
 */
public class TrackingCodeManagementServiceIT extends EJBTestBase {

    TrackingCodeManagementService trackingCodeManagementService;
    MarketplaceServiceLocal MPService;
    DataService dataService;

    private static final String GLOBAL_MP_ID = "GLOBAL_MP";
    private Organization mpOwner;

    protected long mpOwnerUserKey;
    protected long mpOwnerUserKey2;
    private String trackingCode = "<script> it is some scripts ()</script>";

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());

        container.addBean(new MarketplaceServiceLocalBean());
        container.addBean(new TrackingCodeManagementServiceBean());

        dataService = container.get(DataService.class);

        // create marketplace + corresponding owner
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                mpOwner = Organizations.createOrganization(dataService,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(dataService, mpOwner, true, "admin");
                mpOwnerUserKey = createUserForOrg.getKey();

                PlatformUsers.grantRoles(dataService, createUserForOrg,
                        UserRoleType.MARKETPLACE_OWNER);

                return null;
            }
        });

        runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return Marketplaces.createMarketplace(mpOwner, GLOBAL_MP_ID,
                        false, dataService);
            }
        });

        trackingCodeManagementService = container
                .get(TrackingCodeManagementService.class);

    }

    /**
     * Save tracking Code. Backend service must be called and data reloaded.
     */
    @Test
    public void saveTrackingCode() throws Exception {
        POTrackingCode toBeSaved = new POTrackingCode();
        toBeSaved.setMarketplaceId(GLOBAL_MP_ID);
        toBeSaved.setTrackingCode(trackingCode);
        container.login("admin", "MARKETPLACE_OWNER");
        // when saving
        Response response = trackingCodeManagementService
                .saveTrackingCode(toBeSaved);

        POTrackingCode code = response.getResult(POTrackingCode.class);

        assertEquals(trackingCode, code.getTrackingCode());
        assertEquals(GLOBAL_MP_ID, code.getMarketplaceId());
    }

    @Test(expected = EJBException.class)
    public void saveTrackingCode_NotPermitted() throws Exception {
        POTrackingCode toBeSaved = new POTrackingCode();
        toBeSaved.setMarketplaceId(GLOBAL_MP_ID);
        toBeSaved.setTrackingCode(trackingCode);
        container.login("admin", "SUPPLIER");
        // when saving
        try {
            trackingCodeManagementService.saveTrackingCode(toBeSaved);
        } catch (Exception e) {
            assertTrue(e.getCause().getMessage()
                    .equals("Allowed roles are: [MARKETPLACE_OWNER]"));
            throw e;
        }
    }

}
