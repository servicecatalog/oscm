/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.billingadapter;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.billingadapterservice.bean.BillingAdapterDAO;
import org.oscm.domobjects.BillingAdapter;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.billing.application.bean.BillingPluginBean;
import org.oscm.internal.assembler.BasePOAssembler;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.BillingApplicationException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.DuplicateAdapterException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * @author stavreva
 * 
 */
@Stateless
@Remote(BillingAdapterService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class BillingAdapterBean implements BillingAdapterService {

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(BillingAdapterBean.class);

    @Resource
    private SessionContext sessionCtx;

    @EJB
    private BillingAdapterDAO billingAdapter;

    @EJB
    private BillingPluginBean billingPluginBean;

    @Override
    public Response getBaseBillingAdapters() {

        List<BillingAdapter> adapters = billingAdapter.getAll();

        List<POBaseBillingAdapter> result = BillingAdapterAssembler
                .toPOBaseBillingAdapter(adapters);

        return new Response(result);
    }

    @Override
    public Response getBillingAdapters() {

        List<BillingAdapter> adapters = billingAdapter.getAll();

        List<POBillingAdapter> result = BillingAdapterAssembler
                .toPOBillingAdapter(adapters);

        return new Response(result);
    }

    @Override
    public Response getDefaultBaseBillingAdapter() {

        BillingAdapter adapter = billingAdapter.getDefault();

        POBaseBillingAdapter result = BillingAdapterAssembler
                .toPOBaseBillingAdapter(adapter);

        return new Response(result);
    }

    @Override
    public Response getDefaultBillingAdapter() {

        BillingAdapter adapter = billingAdapter.getDefault();

        POBillingAdapter result = BillingAdapterAssembler
                .toPOBillingAdapter(adapter);

        return new Response(result);
    }

    @Override
    public Response getBillingAdapter(String billingIdentifier) {
        BillingAdapter adapter = billingAdapter.get(billingIdentifier);

        POBillingAdapter result = BillingAdapterAssembler
                .toPOBillingAdapter(adapter);

        return new Response(result);
    }

    @Override
    public Response saveBillingAdapter(POBillingAdapter poBillingAdapter)
            throws SaaSApplicationException {
        Response response = new Response();

        try {

            BillingAdapter adapter = billingAdapter.get(poBillingAdapter
                    .getBillingIdentifier());

            if (adapter != null && poBillingAdapter.getKey() != 0) {
                BasePOAssembler.verifyVersionAndKey(adapter, poBillingAdapter);
            }
            billingAdapter.save(BillingAdapterAssembler
                    .toBillingAdapter(poBillingAdapter));
        } catch (IOException e) {
            // TODO create own exception
            throw new SaaSApplicationException(e);

        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw new ObjectNotFoundException(ClassEnum.BILLING_ADAPTER,
                    poBillingAdapter.getBillingIdentifier());
        } catch (NonUniqueBusinessKeyException | DuplicateAdapterException
                | ConcurrentModificationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        return response;
    }

    @Override
    public Response setDefaultBillingAdapter(POBillingAdapter poBillingAdapter)
            throws SaaSApplicationException {
        Response response = new Response();

        BillingAdapter adapter = billingAdapter.get(poBillingAdapter
                .getBillingIdentifier());

        if (adapter == null) {
            sessionCtx.setRollbackOnly();
            throw new ObjectNotFoundException(ClassEnum.BILLING_ADAPTER,
                    poBillingAdapter.getBillingIdentifier());
        } else {

            BasePOAssembler.verifyVersionAndKey(adapter, poBillingAdapter);

            try {
                billingAdapter.setDefaultAdapter(adapter);
            } catch (EJBTransactionRolledbackException e) {
                sessionCtx.setRollbackOnly();
                throw new SaaSApplicationException(e);
            }
        }

        return response;
    }

    @Override
    public Response deleteAdapter(POBillingAdapter poBillingAdapter)
            throws DeletionConstraintException, ObjectNotFoundException {
        Response response = new Response();
        try {
            BillingAdapter adapter = BillingAdapterAssembler
                    .toBillingAdapter(poBillingAdapter);
            billingAdapter.delete(adapter);
        } catch (IOException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR);
        } catch (ObjectNotFoundException e) {
            throw new ObjectNotFoundException(ClassEnum.BILLING_ADAPTER,
                    poBillingAdapter.getBillingIdentifier());
        }
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.oscm.internal.billingadapter.BillingAdapterService#isActive
     * (org.oscm.internal.billingadapter.POBillingAdapter)
     */
    @Override
    public Response isActive(POBillingAdapter poBillingAdapter)
            throws SaaSApplicationException {
        Response response;
        try {
            BillingAdapter adapter = BillingAdapterAssembler
                    .toBillingAdapter(poBillingAdapter);
            response = new Response(Boolean.valueOf(adapter.isActive()));

        } catch (IOException e) {
            throw new SaaSApplicationException(e);
        }
        return response;
    }

    @Override
    public Response testConnection(String billingIdentifier)
            throws BillingApplicationException {
        Response response = new Response();

        billingPluginBean.testConnection(billingIdentifier);

        return response;
    }

    @Override
    public Response testConnection(POBillingAdapter poBillingAdapter)
            throws SaaSApplicationException {
        Response response = new Response();
        BillingAdapter adapter;
        try {
            adapter = BillingAdapterAssembler
                    .toBillingAdapter(poBillingAdapter);
            billingPluginBean.testConnection(adapter);
            return response;
        } catch (IOException e) {
            throw new SaaSApplicationException(e);
        }
    }

    public BillingAdapterDAO getBillingAdapter() {
        return billingAdapter;
    }

    public void setBillingAdapter(BillingAdapterDAO billingAdapter) {
        this.billingAdapter = billingAdapter;
    }

    public BillingPluginBean getBillingPluginBean() {
        return billingPluginBean;
    }

    public void setBillingPluginBean(BillingPluginBean billingPluginBean) {
        this.billingPluginBean = billingPluginBean;
    }

    public void setSessionCtx(SessionContext sessionCtx) {
        this.sessionCtx = sessionCtx;
    }
}
