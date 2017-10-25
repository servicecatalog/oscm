/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.eventservice.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityExistsException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.GatheredEvent;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.eventservice.assembler.GatheredEventAssembler;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.intf.EventService;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.DuplicateEventException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOGatheredEvent;

/**
 * Session Bean implementation class EventServiceBean
 */
@Stateless
@Remote(EventService.class)
@LocalBean
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class EventServiceBean implements EventService {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(EventServiceBean.class);

    @EJB(beanInterface = DataService.class)
    private DataService em;

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void recordEventForInstance(String technicalServiceId,
            String instanceId, VOGatheredEvent event)
            throws OrganizationAuthoritiesException, DuplicateEventException,
            ObjectNotFoundException, ValidationException {

        ArgumentValidator.notNull("technicalServiceId", technicalServiceId);
        ArgumentValidator.notNull("instanceId", instanceId);
        ArgumentValidator.notNull("event", event);

        final Organization organization = em.getCurrentUser().getOrganization();

        Subscription subscription = null;
        try {

            TechnicalProduct techProduct = new TechnicalProduct();
            techProduct.setOrganization(organization);
            techProduct.setTechnicalProductId(technicalServiceId);
            techProduct = (TechnicalProduct) em
                    .getReferenceByBusinessKey(techProduct);

            Query query = em
                    .createNamedQuery("Subscription.getByInstanceIdOfTechProd");
            query.setParameter("productInstanceId", instanceId);
            query.setParameter("technicalProduct", techProduct);
            subscription = (Subscription) query.getSingleResult();
        } catch (ObjectNotFoundException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_RECORD_EVENT_FAILED);
            throw e;
        } catch (NoResultException e) {
            ObjectNotFoundException ex = new ObjectNotFoundException(
                    "No subscription with the given instanceId");
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_RECORD_EVENT_FAILED);
            throw ex;
        } catch (NonUniqueResultException e) {
            // should not occur
            SaaSSystemException se = new SaaSSystemException(
                    "recordEvent failed!", e);
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    se,
                    LogMessageIdentifier.ERROR_RECORD_EVENT_FAILED_SUBSCRIPTION_NOT_UNIQUE,
                    organization.getOrganizationId(), technicalServiceId,
                    instanceId);
            throw se;
        }

        ensureEventOfTechnicalProduct(subscription.getProduct()
                .getTechnicalProduct(), event.getEventId());

        GatheredEvent eventToStore = GatheredEventAssembler
                .toGatheredEvent(event);
        eventToStore.setSubscriptionTKey(subscription.getKey());
        eventToStore.setType(EventType.SERVICE_EVENT);
        recordEvent(eventToStore);

    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void recordEventForSubscription(long subscriptionKey,
            VOGatheredEvent event) throws DuplicateEventException,
            OrganizationAuthoritiesException, ObjectNotFoundException,
            ValidationException {

        ArgumentValidator.notNull("event", event);

        // the caller's organization must be the owner of the technical product
        Subscription subscription = em.getReference(Subscription.class,
                subscriptionKey);
        Organization provider = subscription.getProduct().getTechnicalProduct()
                .getOrganization();
        Organization callerOrg = em.getCurrentUser().getOrganization();
        if (provider.getKey() != callerOrg.getKey()) {
            OrganizationAuthoritiesException e = new OrganizationAuthoritiesException(
                    "The caller's organization is not the owner of the technical service.");
            logger.logError(
                    Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                    e,
                    LogMessageIdentifier.ERROR_ORGANIZATION_OF_CALLER_NOT_OWNER_OF_TECHNICAL_SERVICE);
            throw e;
        }

        ensureEventOfTechnicalProduct(subscription.getProduct()
                .getTechnicalProduct(), event.getEventId());

        GatheredEvent eventToStore = GatheredEventAssembler
                .toGatheredEvent(event);
        eventToStore.setSubscriptionTKey(subscriptionKey);
        eventToStore.setType(EventType.SERVICE_EVENT);
        recordEvent(eventToStore);

    }

    /**
     * Checks if the given technical product has an event with the specified
     * eventId.
     * 
     * @param techProd
     *            The technical product to be validated.
     * @param eventId
     *            The searched event identifier.
     * @throws ObjectNotFoundException
     *             When the technical product defines no event with the given
     *             identifier.
     */
    private void ensureEventOfTechnicalProduct(TechnicalProduct techProd,
            String eventId) throws ObjectNotFoundException {
        for (Event techProdEvent : techProd.getEvents()) {
            if (techProdEvent.getEventIdentifier().equals(eventId)) {
                return;
            }
        }
        ObjectNotFoundException e = new ObjectNotFoundException(
                ClassEnum.EVENT, eventId);
        logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                LogMessageIdentifier.WARN_INEXISTENT_OBJECT_WITH_BUSINESS_KEY,
                ClassEnum.EVENT.name(), eventId);
        throw e;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void recordEvent(GatheredEvent event) throws DuplicateEventException {

        try {
            em.persist(event);
            em.flush();
        } catch (NonUniqueBusinessKeyException e) {
            // Must not happen as Events don't have a business key.
            final SaaSSystemException sysex = new SaaSSystemException(
                    "Unexpected exception while writing event.", e);
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sysex,
                    LogMessageIdentifier.ERROR_UNEXPECTED_EXCEPTION_WHILE_WRITING_EVENT);
            throw sysex;
        } catch (EJBTransactionRolledbackException e) {
            if (isEntityExistsException(e)) {
                throw new DuplicateEventException(
                        "Duplicate event with unique id " + event.getUniqueId());
            } else {
                throw e;
            }
        }

    }

    /**
     * Tests whether this exception or any nested exception is a
     * {@link EntityExistsException}. Unfortunately {@link EJBException}
     * sometimes nests cause exception in {@link Throwable#getCause()},
     * sometimes in {@link EJBException#getCausedByException()}. Arrrrg.
     */
    private boolean isEntityExistsException(final Throwable e) {
        if (e == null) {
            return false;
        }
        if (e instanceof PersistenceException) {
            return true;
        }
        if (e instanceof EJBException) {
            final EJBException ejbex = (EJBException) e;
            if (isEntityExistsException(ejbex.getCausedByException())) {
                return true;
            }
        }
        return isEntityExistsException(e.getCause());
    }

}
