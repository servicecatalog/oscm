/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingadapterservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.PersistenceException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingAdapter;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.BillingAdapters;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author stavreva
 * 
 */
public class BillingAdapterDAOIT extends EJBTestBase {

    private final static String BILLING_ID = "Kill_Bill";

    private DataService ds;
    private BillingAdapterDAO billAdapterDAO;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(ds = new DataServiceBean());
        container.addBean(billAdapterDAO = new BillingAdapterDAO());
    }

    @Test
    public void getAllEmpty() throws Exception {
        // when
        List<BillingAdapter> list = getAll();

        // then
        assertEquals(0, list.size());
    }

    @Test
    public void getAllNativeBilling() throws Exception {
        // given
        creatBillingAdapter(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                false);

        // when
        List<BillingAdapter> list = getAll();

        // then
        assertEquals(1, list.size());
        assertEquals(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                list.get(0).getBillingIdentifier());
    }

    @Test
    public void getAllMoreAdapters() throws Exception {
        // given
        creatBillingAdapter(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                false);
        creatBillingAdapter(BILLING_ID, false);

        // when
        List<BillingAdapter> list = getAll();

        // then
        assertEquals(2, list.size());
        List<String> billingIds = new ArrayList<>();
        billingIds.add(list.get(0).getBillingIdentifier());
        billingIds.add(list.get(1).getBillingIdentifier());
        assertTrue(billingIds.contains(BILLING_ID));
        assertTrue(billingIds
                .contains(BillingAdapterIdentifier.NATIVE_BILLING.toString()));

    }

    @SuppressWarnings("boxing")
    @Test
    public void get() throws Exception {
        // given
        creatBillingAdapter(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                true);
        creatBillingAdapter(BILLING_ID, false);

        // when
        BillingAdapter ba = get(BILLING_ID);

        // then
        assertEquals(BILLING_ID, ba.getBillingIdentifier());
        assertEquals(false, ba.isDefaultAdapter());

    }

    @Test
    public void saveCreation() throws Exception {
        // given
        BillingAdapter adapter = new BillingAdapter();
        adapter.setBillingIdentifier(
                BillingAdapterIdentifier.NATIVE_BILLING.toString());
        adapter.setDefaultAdapter(true);
        adapter.setName("Adapter Name");

        // when
        save(adapter);

        // then
        List<BillingAdapter> list = getAll();

        assertEquals(1, list.size());
        List<String> billingIds = new ArrayList<>();
        billingIds.add(list.get(0).getBillingIdentifier());

        assertTrue(billingIds
                .contains(BillingAdapterIdentifier.NATIVE_BILLING.toString()));
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void saveNonUniqueBusinessKey() throws Exception {
        // given
        BillingAdapter adapter1 = new BillingAdapter();
        adapter1.setBillingIdentifier(
                BillingAdapterIdentifier.NATIVE_BILLING.toString());
        adapter1.setDefaultAdapter(true);
        adapter1.setName("Adapter Name");

        BillingAdapter adapter2 = new BillingAdapter();
        adapter2.setBillingIdentifier(
                BillingAdapterIdentifier.NATIVE_BILLING.toString());
        adapter2.setDefaultAdapter(false);
        adapter2.setName("Adapter Name");

        // when
        save(adapter1);
        save(adapter2);
    }

    @Test
    public void saveModification() throws Exception {
        // given

        BillingAdapter adapter1 = creatBillingAdapter(
                BillingAdapterIdentifier.NATIVE_BILLING.toString(), true);
        adapter1.setName("NewName1");

        BillingAdapter adapter2 = creatBillingAdapter(BILLING_ID, false);
        adapter2.setName("NewName2");

        // when
        save(adapter1);
        save(adapter2);

        // then
        List<BillingAdapter> list = getAll();

        assertEquals(2, list.size());
        List<String> billingIds = new ArrayList<>();
        billingIds.add(list.get(0).getBillingIdentifier());
        billingIds.add(list.get(1).getBillingIdentifier());
        assertTrue(billingIds.contains(BILLING_ID));
        assertTrue(billingIds
                .contains(BillingAdapterIdentifier.NATIVE_BILLING.toString()));
        List<String> adapterNames = new ArrayList<>();
        adapterNames.add(list.get(0).getName());
        adapterNames.add(list.get(1).getName());
        assertTrue(adapterNames.contains("NewName1"));
        assertTrue(adapterNames.contains("NewName2"));
    }

    @Test(expected = PersistenceException.class)
    public void saveDuplicateAdapter() throws Exception {
        // given
        BillingAdapter adapter1 = creatBillingAdapter(
                BillingAdapterIdentifier.NATIVE_BILLING.toString(), true);

        BillingAdapter adapter2 = creatBillingAdapter(BILLING_ID, true);

        // an adapter whose billing id is changed to an existing one
        modifyBillingId(BILLING_ID,
                BillingAdapterIdentifier.NATIVE_BILLING.toString());

        // when
        save(adapter1);
        save(adapter2);
    }

    @Test
    public void setDefaultAdapter() throws Exception {
        // given
        creatBillingAdapter(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                true);
        BillingAdapter adapter = creatBillingAdapter(BILLING_ID, false);
        adapter.setDefaultAdapter(true);

        // when
        setDefaultAdapter(adapter);

        // then
        List<BillingAdapter> list = getAll();

        assertEquals(2, list.size());
        List<String> billingIds = new ArrayList<>();
        billingIds.add(list.get(0).getBillingIdentifier());
        billingIds.add(list.get(1).getBillingIdentifier());
        assertTrue(billingIds.contains(BILLING_ID));
        assertTrue(billingIds
                .contains(BillingAdapterIdentifier.NATIVE_BILLING.toString()));
        BillingAdapter defaultAdapter = getDefaultBillingAdapter();
        assertEquals(adapter.getBillingIdentifier(),
                defaultAdapter.getBillingIdentifier());

    }

    @Test
    public void deleteByBusinessKey() throws Exception {
        // given
        creatBillingAdapter(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                false);
        BillingAdapter adapter = creatBillingAdapter(BILLING_ID, false);

        // when
        deleteBillingAdapterByBusinessKey(adapter);

        // then
        List<BillingAdapter> list = getAll();
        assertEquals(1, list.size());
        assertEquals(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                list.get(0).getBillingIdentifier());

    }

    @Test
    public void deleteByKey() throws Exception {
        // given
        creatBillingAdapter(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                false);
        BillingAdapter adapter = creatBillingAdapter(BILLING_ID, false);

        // when
        deleteBillingAdapterByKey(adapter.getKey());

        // then
        List<BillingAdapter> list = getAll();
        assertEquals(1, list.size());
        assertEquals(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                list.get(0).getBillingIdentifier());
    }

    @Test(expected = DeletionConstraintException.class)
    public void deleteDefaultAdapter() throws Exception {
        // given
        creatBillingAdapter(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                false);
        BillingAdapter adapter = creatBillingAdapter(BILLING_ID, true);

        // when
        try {
            deleteBillingAdapterByKey(adapter.getKey());
            fail();
        }
        // then
        catch (DeletionConstraintException d) {
            throw d;
        }

    }

    @Test
    public void getDefault() throws Exception {
        // given
        creatBillingAdapter(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                false);
        creatBillingAdapter(BILLING_ID, true);

        // when
        BillingAdapter defaultAdapter = getDefaultBillingAdapter();

        // then
        assertTrue(defaultAdapter.isDefaultAdapter());
        assertEquals(BILLING_ID, defaultAdapter.getBillingIdentifier());

    }

    @Test
    public void isDefaultValid() throws Exception {
        // given
        creatBillingAdapter(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                true);
        creatBillingAdapter(BILLING_ID, false);

        // when
        boolean isValid = isDefaultAdapterValid().booleanValue();

        // then
        assertTrue(isValid);
    }

    @Test
    public void isDefaultValidMultipleDefaultAdapters() throws Exception {
        // given
        creatBillingAdapter(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                true);
        creatBillingAdapter(BILLING_ID, true);

        // when
        boolean isValid = isDefaultAdapterValid().booleanValue();

        // then
        assertFalse(isValid);
    }

    @Test
    public void isDefaultValidNoDefaultAdapter() throws Exception {
        // given
        creatBillingAdapter(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                false);
        creatBillingAdapter(BILLING_ID, false);

        // when
        boolean isValid = isDefaultAdapterValid().booleanValue();

        // then
        assertFalse(isValid);
    }

    @Test
    public void isDefaultValidNoAdapterRegistered() throws Exception {
        // given
        // when
        boolean isValid = isDefaultAdapterValid().booleanValue();

        // then
        assertFalse(isValid);
    }

    // TODO use method of BillingAdapters
    private BillingAdapter creatBillingAdapter(final String billingIdentifier,
            final boolean isDefault) throws Exception {
        return runTX(new Callable<BillingAdapter>() {
            @Override
            public BillingAdapter call() throws Exception {
                BillingAdapter billingAdapter = BillingAdapters
                        .createBillingAdapter(ds, billingIdentifier, isDefault);
                return billingAdapter;
            }
        });
    }

    private Void deleteBillingAdapterByBusinessKey(
            final BillingAdapter billingAdapter) throws Exception {
        return runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billAdapterDAO.delete(billingAdapter);
                return null;
            }
        });
    }

    private Void deleteBillingAdapterByKey(final long key) throws Exception {
        return runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BillingAdapter billingAdapter = new BillingAdapter();
                billingAdapter.setKey(key);
                billAdapterDAO.delete(billingAdapter);
                return null;
            }
        });
    }

    private BillingAdapter getDefaultBillingAdapter() throws Exception {
        return runTX(new Callable<BillingAdapter>() {
            @Override
            public BillingAdapter call() throws Exception {
                return billAdapterDAO.getDefault();
            }
        });
    }

    private Boolean isDefaultAdapterValid() throws Exception {
        return runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return new Boolean(billAdapterDAO.isDefaultValid());
            }
        });
    }

    private List<BillingAdapter> getAll() throws Exception {
        return runTX(new Callable<List<BillingAdapter>>() {
            @Override
            public List<BillingAdapter> call() throws Exception {
                return billAdapterDAO.getAll();
            }
        });
    }

    private Void modifyBillingId(final String billingId,
            final String billingIdToModify) throws Exception {
        return runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BillingAdapter ba = new BillingAdapter();
                ba.setBillingIdentifier(billingId);
                BillingAdapter billingAdapter = billAdapterDAO.get(ba);
                billingAdapter.setBillingIdentifier(billingIdToModify);
                ds.flush();
                return null;
            }
        });
    }

    private BillingAdapter get(final String billingId) throws Exception {
        return runTX(new Callable<BillingAdapter>() {
            @Override
            public BillingAdapter call() throws Exception {
                BillingAdapter ba = new BillingAdapter();
                ba.setBillingIdentifier(billingId);
                return billAdapterDAO.get(ba);
            }
        });
    }

    private Void save(final BillingAdapter billingAdapter) throws Exception {
        return runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billAdapterDAO.save(billingAdapter);
                return null;
            }
        });
    }

    private Void setDefaultAdapter(final BillingAdapter billingAdapter)
            throws Exception {
        return runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billAdapterDAO.setDefaultAdapter(billingAdapter);
                return null;
            }
        });
    }
}
