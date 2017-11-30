/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

import javax.persistence.Query;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
@Ignore
public class ServiceProvisioningServiceBeanTriggerIdIT extends EJBTestBase {

    protected DataService mgr;
    protected ServiceProvisioningService svcProv;

    private TriggerProcessMessageData triggerProcessData;
    private TriggerProcess tp;

    private PlatformUser supplierUser;
    private Organization supplier;

    private VOServiceDetails service;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);

        TriggerQueueServiceLocal triggerQueueServiceLocal = mock(
                TriggerQueueServiceLocal.class);
        container.addBean(triggerQueueServiceLocal);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ServiceProvisioningServiceBean());

        svcProv = container.get(ServiceProvisioningService.class);
        mgr = container.get(DataService.class);

        // Initialize the test setup data.
        initData();

        // mock the answer of the trigger queue
        doAnswer(new Answer<List<TriggerProcessMessageData>>() {
            @Override
            public List<TriggerProcessMessageData> answer(
                    InvocationOnMock invocation) throws Throwable {
                return Collections.singletonList(triggerProcessData);
            }
        }).when(triggerQueueServiceLocal)
                .sendSuspendingMessages(anyListOf(TriggerMessage.class));

    }

    @Test
    public void activateService_PendingOperationExists() throws Exception {
        createTriggerData(true, TriggerType.ACTIVATE_SERVICE);

        try {
            svcProv.activateService(service);
            fail();
        } catch (OperationPendingException e) {
            assertEquals("ex.OperationPendingException.ACTIVATE_SERVICE",
                    e.getMessageKey());
            String[] messageParams = e.getMessageParams();
            assertEquals(1, messageParams.length);
            assertEquals(String.valueOf(service.getServiceId()),
                    messageParams[0]);
        }
    }

    @Test
    public void testActivateService_TpIdsGenerated() throws Exception {
        createTriggerData(false, TriggerType.ACTIVATE_SERVICE);
        svcProv.activateService(service);

        List<TriggerProcessIdentifier> processIdentifiers = getProcessIdentifiers();
        assertNotNull(processIdentifiers);
        assertEquals(2, processIdentifiers.size());
    }

    @Test
    public void testActivateService_NoTriggerDefinition() throws Exception {
        createTriggerData(false, TriggerType.ACTIVATE_SERVICE);
        tp.setTriggerDefinition(null);
        svcProv.activateService(service);

        List<TriggerProcessIdentifier> processIdentifiers = getProcessIdentifiers();
        assertNotNull(processIdentifiers);
        assertTrue(processIdentifiers.isEmpty());
    }

    @Test
    public void deactivateService_PendingOperationExists() throws Exception {
        createTriggerData(true, TriggerType.DEACTIVATE_SERVICE);

        try {
            svcProv.deactivateService(service);
            fail();
        } catch (OperationPendingException e) {
            assertEquals("ex.OperationPendingException.DEACTIVATE_SERVICE",
                    e.getMessageKey());
            String[] messageParams = e.getMessageParams();
            assertEquals(1, messageParams.length);
            assertEquals(String.valueOf(service.getServiceId()),
                    messageParams[0]);
        }
    }

    @Test
    public void testDeactivateService_TpIdsGenerated() throws Exception {
        createTriggerData(false, TriggerType.DEACTIVATE_SERVICE);
        svcProv.deactivateService(service);

        List<TriggerProcessIdentifier> processIdentifiers = getProcessIdentifiers();
        assertNotNull(processIdentifiers);
        assertEquals(2, processIdentifiers.size());
    }

    @Test
    public void testDeactivateService_NoTriggerDefinition() throws Exception {
        createTriggerData(false, TriggerType.DEACTIVATE_SERVICE);
        tp.setTriggerDefinition(null);
        svcProv.deactivateService(service);

        List<TriggerProcessIdentifier> processIdentifiers = getProcessIdentifiers();
        assertNotNull(processIdentifiers);
        assertTrue(processIdentifiers.isEmpty());
    }

    /**
     * Initializes the test setup data, creates currencies, a supplier
     * organization with roles supplier and technology provider, a technical
     * product and product and fetches the VOServiceDetails of the corresponding
     * product.
     * 
     * @throws Exception
     */
    private void initData() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                // Create supported currencies;
                createSupportedCurrencies(mgr);

                // Create a supplier
                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);

                // Create the supplier user
                supplierUser = Organizations.createUserForOrg(mgr, supplier,
                        true, "supplierAdmin");

                // Create a marketplace
                Marketplace mp = Marketplaces.ensureMarketplace(supplier,
                        supplier.getOrganizationId(), mgr);

                // Create a technical service
                TechnicalProduct techProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "techProdId",
                                false, ServiceAccessType.LOGIN);

                // Create a product
                Products.createProduct(supplier, techProd, true, "productId",
                        "priceModelId", mp, mgr);

                container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER,
                        ROLE_TECHNOLOGY_MANAGER);

                // Get the service details of the product.
                List<VOService> products = svcProv.getSuppliedServices();
                Assert.assertEquals(1, products.size());
                service = svcProv.getServiceDetails(products.get(0));

                return null;
            }

        });
    }

    /**
     * Creates trigger definition and process.
     * 
     * @param createTriggerProcessIds
     *            Indicates if trigger process identifiers have to be set.
     * @param triggerType
     *            The type of the trigger to be created.
     * @throws Exception
     */
    private void createTriggerData(final boolean createTriggerProcessIds,
            final TriggerType triggerType) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                TriggerDefinition td = TriggerDefinitions
                        .createSuspendingTriggerDefinition(mgr, supplier,
                                triggerType);
                tp = TriggerProcesses.createPendingTriggerProcess(mgr,
                        supplierUser, td);
                mgr.flush();
                tp.addTriggerProcessParameter(
                        TriggerProcessParameterName.PRODUCT, service);
                triggerProcessData = new TriggerProcessMessageData(tp,
                        new TriggerMessage());
                if (createTriggerProcessIds) {
                    createTriggerIds(tp);
                }
                return null;
            }

        });

    }

    /**
     * Creates the trigger process identifiers for the registration of a
     * customer organization for a supplier and initializes the trigger process
     * meta-data accordingly.
     * 
     * @param tp
     *            The trigger process the identifiers belong to.
     */
    private void createTriggerIds(TriggerProcess tp) {
        List<TriggerProcessParameter> params = Collections.emptyList();
        TriggerType triggerType = tp.getTriggerDefinition().getType();
        TriggerMessage tm = new TriggerMessage(triggerType, params,
                Collections.singletonList(supplier));
        TriggerProcessIdentifier tpi1 = new TriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(tp.getUser().getOrganization().getKey()));
        tpi1.setTriggerProcess(tp);
        tp.setTriggerProcessIdentifiers(Arrays.asList(tpi1));
        if (triggerType == TriggerType.ACTIVATE_SERVICE
                || triggerType == TriggerType.DEACTIVATE_SERVICE) {
            TriggerProcessIdentifier tpi2 = new TriggerProcessIdentifier(
                    TriggerProcessIdentifierName.SERVICE_KEY,
                    String.valueOf(service.getKey()));
            tpi2.setTriggerProcess(tp);
            tp.setTriggerProcessIdentifiers(Arrays.asList(tpi1, tpi2));
        }
        triggerProcessData = new TriggerProcessMessageData(tp, tm);
    }

    /**
     * Retrieves all currently available trigger process identifier objects from
     * the database.
     * 
     * @param tpKeys
     *            the trigger process keys to filter for. If none is specified,
     *            all identifiers are returned.
     * 
     * @return The trigger process identifiers.
     * @throws Exception
     */
    private List<TriggerProcessIdentifier> getProcessIdentifiers(
            final Long... tpKeys) throws Exception {
        return runTX(new Callable<List<TriggerProcessIdentifier>>() {
            @Override
            public List<TriggerProcessIdentifier> call() throws Exception {
                String queryString = "SELECT tpi FROM TriggerProcessIdentifier tpi";
                List<Long> keys = null;
                if (tpKeys.length > 0) {
                    queryString += " WHERE tpi.triggerProcess.key IN (:keys)";
                    keys = Arrays.asList(tpKeys);
                }
                Query query = mgr.createQuery(queryString);
                if (keys != null) {
                    query.setParameter("keys", keys);
                }
                return ParameterizedTypes.list(query.getResultList(),
                        TriggerProcessIdentifier.class);
            }
        });
    }

}
