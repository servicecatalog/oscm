/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 17.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.applicationservice.bean.ApplicationServiceStub;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.UsageLicense;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTechnicalServiceOperation;
import org.oscm.operation.data.OperationResult;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.DataServiceStub;
import org.oscm.types.enumtypes.OperationParameterType;

/**
 * @author weiser
 * 
 */
public class SubscriptionServiceBeanExecuteServiceOperationIT
        extends EJBTestBase {

    private static final int ORG_KEY = 1234;
    private static final int OP_KEY = 1234;
    private static final int TP_KEY = 1234;
    private static final String SUB_ID = "SUB_ID";
    private static final String OP_ID = "OP_ID";
    private static final String PROD_INST_ID = "PROD_INST_ID";
    private static final String APP_USER_ID = "APP_USER_ID";

    private SubscriptionService subSvc;
    private PlatformUser user;
    private Organization organization;
    private Subscription subscription;
    private TechnicalProductOperation operation;

    private Object[] passedValues;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.enableInterfaceMocking(true);
        container.addBean(new ApplicationServiceStub() {

            @Override
            public OperationResult executeServiceOperation(String userId,
                    Subscription subscription, String transactionId,
                    TechnicalProductOperation operation,
                    Map<String, String> parameters) {
                passedValues = new Object[] { userId, subscription,
                        transactionId, operation, parameters };
                return new OperationResult();
            }

        });
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceStub() {

            @Override
            public PlatformUser getCurrentUser() {
                return user;
            }

            @Override
            public DomainObject<?> getReferenceByBusinessKey(
                    DomainObject<?> findTemplate)
                    throws ObjectNotFoundException {
                if (findTemplate instanceof Subscription) {
                    Subscription sub = (Subscription) findTemplate;
                    if (sub.getOrganizationKey() == subscription
                            .getOrganizationKey()
                            && sub.getSubscriptionId()
                                    .equals(subscription.getSubscriptionId())) {
                        return subscription;
                    }
                }
                throw new ObjectNotFoundException(ClassEnum.SUBSCRIPTION,
                        "subId");
            }

            @Override
            public <T extends DomainObject<?>> T getReference(Class<T> objclass,
                    long key) throws ObjectNotFoundException {
                if (objclass.equals(TechnicalProductOperation.class)
                        && key == OP_KEY) {
                    return objclass.cast(operation);
                }
                throw new ObjectNotFoundException(ClassEnum.ORGANIZATION,
                        String.valueOf(key));
            }

        });
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new ManageSubscriptionBean());

        subSvc = container.get(SubscriptionService.class);

        organization = new Organization();
        organization.setKey(ORG_KEY);
        addOrgToRole(organization, OrganizationRoleType.CUSTOMER);

        TechnicalProduct technicalProduct = new TechnicalProduct();
        technicalProduct.setKey(TP_KEY);

        Product product = new Product();
        product.setTechnicalProduct(technicalProduct);

        operation = new TechnicalProductOperation();
        operation.setKey(OP_KEY);
        operation.setTechnicalProduct(technicalProduct);
        operation.setOperationId(OP_ID);

        user = new PlatformUser();
        user.setUserId("Admin");
        user.setOrganization(organization);

        subscription = new Subscription();
        subscription.setOrganization(organization);
        subscription.setSubscriptionId(SUB_ID);
        subscription.setOrganizationKey(ORG_KEY);
        subscription.bindToProduct(product);
        subscription.setProductInstanceId(PROD_INST_ID);
        UsageLicense lic = subscription.addUser(user, null);
        lic.setApplicationUserId(APP_USER_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteServiceOperation_SubscriptionNull()
            throws Exception {
        try {
            subSvc.executeServiceOperation(null, getOp());
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteServiceOperation_OperationNull() throws Exception {
        try {
            subSvc.executeServiceOperation(new VOSubscription(), null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testExecuteServiceOperation_SubNotFoundWrongOrg()
            throws Exception {
        Organization org = new Organization();
        org.setKey(9876);
        user.setOrganization(org);

        subSvc.executeServiceOperation(getSub(), getOp());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testExecuteServiceOperation_SubNotFoundWrongId()
            throws Exception {
        VOSubscription sub = new VOSubscription();
        sub.setSubscriptionId("otherid");

        subSvc.executeServiceOperation(sub, getOp());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testExecuteServiceOperation_OpNotFound() throws Exception {
        subSvc.executeServiceOperation(getSub(),
                new VOTechnicalServiceOperation());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testExecuteServiceOperation_OpNotPartOfTP() throws Exception {
        operation.setTechnicalProduct(new TechnicalProduct());

        subSvc.executeServiceOperation(getSub(), getOp());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testExecuteServiceOperation_UserNotAssignedToSub()
            throws Exception {
        subscription.revokeUser(user);

        subSvc.executeServiceOperation(getSub(), getOp());
    }

    @Test
    public void testExecuteServiceOperation() throws Exception {
        subSvc.executeServiceOperation(getSub(), getOp());

        assertEquals(APP_USER_ID, passedValues[0]);
        assertEquals(subscription, passedValues[1]);
        assertNotNull(passedValues[2]);
        assertEquals(operation, passedValues[3]);
    }

    @Test(expected = ValidationException.class)
    public void testExecuteServiceOperation_MandatoryParamMissing()
            throws Exception {
        addParameters(operation, true);
        VOTechnicalServiceOperation op = getOp();
        addParameters(op, true);
        op.getOperationParameters().get(0).setParameterValue(null);

        subSvc.executeServiceOperation(getSub(), op);
    }

    @Test
    public void testExecuteServiceOperation_MandatoryParamPassed()
            throws Exception {
        addParameters(operation, true, false);
        VOTechnicalServiceOperation op = getOp();
        addParameters(op, true, false);

        subSvc.executeServiceOperation(getSub(), op);

        assertEquals(APP_USER_ID, passedValues[0]);
        assertEquals(subscription, passedValues[1]);
        assertNotNull(passedValues[2]);
        HashMap<String, String> expected = new HashMap<>();
        expected.put("param1", "param1");
        expected.put("param2", "param2");
        assertEquals(expected, passedValues[4]);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testExecuteServiceOperationConcurrencyViolationSubscription()
            throws Exception {
        VOSubscription sub = getSub();
        sub.setVersion(sub.getVersion() - 1);

        subSvc.executeServiceOperation(sub, getOp());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testExecuteServiceOperationConcurrencyViolationOperation()
            throws Exception {
        VOTechnicalServiceOperation op = getOp();
        op.setVersion(op.getVersion() - 1);

        subSvc.executeServiceOperation(getSub(), op);
    }

    private static final void addParameters(VOTechnicalServiceOperation op,
            boolean... mandatory) {
        int index = 1;
        for (boolean b : mandatory) {
            VOServiceOperationParameter sop = new VOServiceOperationParameter();
            sop.setKey(index);
            String id = "param" + (index++);
            sop.setParameterId(id);
            sop.setParameterValue(id);
            sop.setMandatory(b);
            op.getOperationParameters().add(sop);
        }
    }

    private static final void addParameters(TechnicalProductOperation tpo,
            boolean... mandatory) {
        int index = 1;
        for (boolean b : mandatory) {
            OperationParameter op = new OperationParameter();
            op.setKey(index);
            op.setId("param" + (index++));
            op.setMandatory(b);
            op.setTechnicalProductOperation(tpo);
            op.setType(OperationParameterType.INPUT_STRING);
            tpo.getParameters().add(op);
        }
    }

    private static final VOTechnicalServiceOperation getOp() {
        VOTechnicalServiceOperation op = new VOTechnicalServiceOperation();
        op.setKey(OP_KEY);
        op.setOperationId(OP_ID);
        return op;
    }

    private static final VOSubscription getSub() {
        VOSubscription sub = new VOSubscription();
        sub.setSubscriptionId(SUB_ID);
        return sub;
    }

    private static final void addOrgToRole(Organization organization,
            OrganizationRoleType type) {
        OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganization(organization);
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(type);
        orgToRole.setOrganizationRole(role);
        organization.getGrantedRoles().add(orgToRole);
    }

}
