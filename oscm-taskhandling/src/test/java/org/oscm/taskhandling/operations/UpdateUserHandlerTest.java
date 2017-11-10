/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 9, 2011                                                      
 *                                                                              
 *  Creation Date: Nov 10, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserRole;
import org.oscm.taskhandling.facade.ServiceFacade;
import org.oscm.taskhandling.payloads.UpdateUserPayload;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * @author tokoda
 * 
 */
public class UpdateUserHandlerTest {

    private static final long SUBSCRIPTION_KEY = 11111;
    private static final long USER_LICENSE_KEY = 22222;
    private static final String TECHNICAL_PRODUCT_ID = "TP1";
    private static final String TECHNICAL_PRODUCT_URL = "http;//www.test/";

    ApplicationServiceLocal applicationServiceMock;
    CommunicationServiceLocal communicationServiceMock;
    DataService dataServiceMock;

    UpdateUserHandler handler = null;
    UsageLicense usageLicenseMock = null;

    PlatformUser admin1 = null;
    PlatformUser admin2 = null;
    Subscription subscription = null;

    @Before
    public void setUp() throws Exception {

        handler = new UpdateUserHandler();
        handler.setPayload(new UpdateUserPayload(SUBSCRIPTION_KEY,
                USER_LICENSE_KEY));
        handler.setServiceFacade(createServiceFacade());
    }

    private ServiceFacade createServiceFacade() throws Exception {
        ServiceFacade facade = new ServiceFacade();

        applicationServiceMock = mock(ApplicationServiceLocal.class);
        facade.setApplicationService(applicationServiceMock);

        communicationServiceMock = mock(CommunicationServiceLocal.class);
        facade.setCommunicationService(communicationServiceMock);

        dataServiceMock = createDataServiceMock();
        facade.setDataService(dataServiceMock);
        return facade;
    }

    private DataService createDataServiceMock() throws Exception {

        UserRole userRole = new UserRole();
        userRole.setRoleName(UserRoleType.ORGANIZATION_ADMIN);

        RoleAssignment role1 = new RoleAssignment();
        role1.setRole(userRole);

        RoleAssignment role2 = new RoleAssignment();
        role2.setRole(userRole);

        admin1 = new PlatformUser();
        admin1.getAssignedRoles().add(role1);
        admin2 = new PlatformUser();
        admin2.getAssignedRoles().add(role2);

        Organization organization = new Organization();
        organization.addPlatformUser(admin1);
        organization.addPlatformUser(admin2);

        TechnicalProduct technicalProduct = new TechnicalProduct();
        technicalProduct.setOrganization(organization);
        technicalProduct.setTechnicalProductId(TECHNICAL_PRODUCT_ID);
        technicalProduct.setProvisioningURL(TECHNICAL_PRODUCT_URL);

        Product product = new Product();
        product.setTechnicalProduct(technicalProduct);

        subscription = new Subscription();
        subscription.bindToProduct(product);

        usageLicenseMock = mock(UsageLicense.class);

        DataService spyDataService = mock(DataService.class);
        when(spyDataService.getReference(Subscription.class, SUBSCRIPTION_KEY))
                .thenReturn(subscription);
        when(spyDataService.getReference(UsageLicense.class, USER_LICENSE_KEY))
                .thenReturn(usageLicenseMock);
        return spyDataService;
    }

    @Test
    public void executeTest() throws Exception {
        handler.execute();
        List<UsageLicense> licenses = new ArrayList<UsageLicense>();
        licenses.add(usageLicenseMock);
        verify(applicationServiceMock).updateUsers(subscription, licenses);
    }

    @Test
    public void errorHandlingTest() throws Exception {
        handler.handleError(null);
        verify(communicationServiceMock).sendMail(
                EmailType.USER_UPDATE_FOR_SUBSCRIPTION_FAILED,
                new String[] { TECHNICAL_PRODUCT_ID, TECHNICAL_PRODUCT_URL },
                null, admin1, admin2);
    }
}
