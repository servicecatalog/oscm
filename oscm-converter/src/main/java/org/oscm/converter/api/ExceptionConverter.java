/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.06.2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.converter.api;

import java.lang.reflect.Constructor;

import org.oscm.types.exceptions.AddMarketingPermissionException;
import org.oscm.types.exceptions.BillingAdapterNotFoundException;
import org.oscm.types.exceptions.BulkUserImportException;
import org.oscm.types.exceptions.CatalogEntryRemovedException;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.CurrencyException;
import org.oscm.types.exceptions.DeletionConstraintException;
import org.oscm.types.exceptions.DistinguishedNameException;
import org.oscm.types.exceptions.DomainObjectException;
import org.oscm.types.exceptions.DuplicateEventException;
import org.oscm.types.exceptions.ExecutionTargetException;
import org.oscm.types.exceptions.ImageException;
import org.oscm.types.exceptions.ImportException;
import org.oscm.types.exceptions.InvalidPhraseException;
import org.oscm.types.exceptions.MailOperationException;
import org.oscm.types.exceptions.MandatoryUdaMissingException;
import org.oscm.types.exceptions.MarketingPermissionNotFoundException;
import org.oscm.types.exceptions.MarketplaceAccessTypeUneligibleForOperationException;
import org.oscm.types.exceptions.MarketplaceRemovedException;
import org.oscm.types.exceptions.MarketplaceValidationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.OperationStateException;
import org.oscm.types.exceptions.OrganizationAlreadyBannedException;
import org.oscm.types.exceptions.OrganizationAlreadyExistsException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.OrganizationAuthorityException;
import org.oscm.types.exceptions.OrganizationDataException;
import org.oscm.types.exceptions.OrganizationRemovedException;
import org.oscm.types.exceptions.PSPCommunicationException;
import org.oscm.types.exceptions.PSPProcessingException;
import org.oscm.types.exceptions.PaymentDataException;
import org.oscm.types.exceptions.PaymentDeregistrationException;
import org.oscm.types.exceptions.PaymentInformationException;
import org.oscm.types.exceptions.PriceModelException;
import org.oscm.types.exceptions.PublishingToMarketplaceNotPermittedException;
import org.oscm.types.exceptions.RegistrationException;
import org.oscm.types.exceptions.SaaSApplicationException;
import org.oscm.types.exceptions.SecurityCheckException;
import org.oscm.types.exceptions.ServiceChangedException;
import org.oscm.types.exceptions.ServiceCompatibilityException;
import org.oscm.types.exceptions.ServiceNotPublishedException;
import org.oscm.types.exceptions.ServiceOperationException;
import org.oscm.types.exceptions.ServiceParameterException;
import org.oscm.types.exceptions.ServiceStateException;
import org.oscm.types.exceptions.SubscriptionAlreadyExistsException;
import org.oscm.types.exceptions.SubscriptionMigrationException;
import org.oscm.types.exceptions.SubscriptionStateException;
import org.oscm.types.exceptions.SubscriptionStillActiveException;
import org.oscm.types.exceptions.TechnicalServiceActiveException;
import org.oscm.types.exceptions.TechnicalServiceMultiSubscriptions;
import org.oscm.types.exceptions.TechnicalServiceNotAliveException;
import org.oscm.types.exceptions.TechnicalServiceOperationException;
import org.oscm.types.exceptions.TriggerDefinitionDataException;
import org.oscm.types.exceptions.TriggerProcessStatusException;
import org.oscm.types.exceptions.UnchangeableAllowingOnBehalfActingException;
import org.oscm.types.exceptions.UpdateConstraintException;
import org.oscm.types.exceptions.UserActiveException;
import org.oscm.types.exceptions.UserDeletionConstraintException;
import org.oscm.types.exceptions.UserModificationConstraintException;
import org.oscm.types.exceptions.UserRoleAssignmentException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.types.exceptions.beans.ApplicationExceptionBean;
import org.oscm.types.exceptions.beans.BulkUserImportExceptionBean;
import org.oscm.types.exceptions.beans.DeletionConstraintExceptionBean;
import org.oscm.types.exceptions.beans.DomainObjectExceptionBean;
import org.oscm.types.exceptions.beans.ImageExceptionBean;
import org.oscm.types.exceptions.beans.ImportExceptionBean;
import org.oscm.types.exceptions.beans.OperationPendingExceptionBean;
import org.oscm.types.exceptions.beans.RegistrationExceptionBean;
import org.oscm.types.exceptions.beans.SubscriptionMigrationExceptionBean;
import org.oscm.types.exceptions.beans.SubscriptionStateExceptionBean;
import org.oscm.types.exceptions.beans.UserModificationConstraintExceptionBean;
import org.oscm.types.exceptions.beans.ValidationExceptionBean;

public class ExceptionConverter {

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static org.oscm.internal.types.exception.ObjectNotFoundException convertToUp(
            org.oscm.types.exceptions.ObjectNotFoundException oldEx) {

        org.oscm.internal.types.exception.beans.DomainObjectExceptionBean bean = convertBeanToUp(
                getFaultInfo(oldEx),
                org.oscm.internal.types.exception.beans.DomainObjectExceptionBean.class);

        org.oscm.internal.types.exception.ObjectNotFoundException e = new org.oscm.internal.types.exception.ObjectNotFoundException(
                getExceptionMessage(oldEx), bean);
        e.setStackTrace(oldEx.getStackTrace());

        return e;
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static org.oscm.internal.types.exception.PaymentDataException convertToUp(
            org.oscm.types.exceptions.PaymentDataException oldEx) {
        return convertExceptionToUp(oldEx,
                org.oscm.internal.types.exception.PaymentDataException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static org.oscm.internal.types.exception.OperationNotPermittedException convertToUp(
            org.oscm.types.exceptions.OperationNotPermittedException oldEx) {
        return convertExceptionToUp(oldEx,
                org.oscm.internal.types.exception.OperationNotPermittedException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static PSPProcessingException convertToApi(
            org.oscm.internal.types.exception.PSPProcessingException oldEx) {
        return convertExceptionToApi(oldEx, PSPProcessingException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static PSPCommunicationException convertToApi(
            org.oscm.internal.types.exception.PSPCommunicationException oldEx) {
        return convertExceptionToApi(oldEx, PSPCommunicationException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static OrganizationAlreadyExistsException convertToApi(
            org.oscm.internal.types.exception.OrganizationAlreadyExistsException oldEx) {
        return convertExceptionToApi(oldEx,
                OrganizationAlreadyExistsException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static MandatoryUdaMissingException convertToApi(
            org.oscm.internal.types.exception.MandatoryUdaMissingException oldEx) {
        return convertExceptionToApi(oldEx, MandatoryUdaMissingException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static MandatoryUdaMissingException convertToApi(
            org.oscm.internal.types.exception.MandatoryCustomerUdaMissingException oldEx) {
        return convertExceptionToApi(oldEx, MandatoryUdaMissingException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static ConcurrentModificationException convertToApi(
            org.oscm.internal.types.exception.ConcurrentModificationException oldEx) {
        return convertExceptionToApi(oldEx,
                ConcurrentModificationException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static CurrencyException convertToApi(
            org.oscm.internal.types.exception.CurrencyException oldEx) {
        return convertExceptionToApi(oldEx, CurrencyException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static DeletionConstraintException convertToApi(
            org.oscm.internal.types.exception.DeletionConstraintException oldEx) {

        org.oscm.internal.types.exception.beans.DeletionConstraintExceptionBean bean = (org.oscm.internal.types.exception.beans.DeletionConstraintExceptionBean) getFaultInfo(
                oldEx);
        DeletionConstraintExceptionBean v13Bean = convertBeanToApi(bean,
                DeletionConstraintExceptionBean.class);

        if (bean != null) {
            DomainObjectException.ClassEnum v13ClassEnum = EnumConverter
                    .convert(bean.getClassEnum(),
                            DomainObjectException.ClassEnum.class);
            v13Bean.setClassEnum(v13ClassEnum);

            DomainObjectException.ClassEnum v13DependentClassEnum = EnumConverter
                    .convert(bean.getDependentClassEnum(),
                            DomainObjectException.ClassEnum.class);
            v13Bean.setDependentClassEnum(v13DependentClassEnum);
        }

        return newApiException(DeletionConstraintException.class,
                getExceptionMessage(oldEx), v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static DistinguishedNameException convertToApi(
            org.oscm.internal.types.exception.DistinguishedNameException oldEx) {
        return convertExceptionToApi(oldEx, DistinguishedNameException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static DuplicateEventException convertToApi(
            org.oscm.internal.types.exception.DuplicateEventException oldEx) {
        return convertExceptionToApi(oldEx, DuplicateEventException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static ExecutionTargetException convertToApi(
            org.oscm.internal.types.exception.ExecutionTargetException oldEx) {
        return convertExceptionToApi(oldEx, ExecutionTargetException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static ImportException convertToApi(
            org.oscm.internal.types.exception.ImportException oldEx) {

        org.oscm.internal.types.exception.beans.ImportExceptionBean bean = (org.oscm.internal.types.exception.beans.ImportExceptionBean) getFaultInfo(
                oldEx);
        ImportExceptionBean v13Bean = convertBeanToApi(bean,
                ImportExceptionBean.class);

        if (bean != null) {
            v13Bean.setDetails(bean.getDetails());
        }

        return newApiException(ImportException.class,
                getExceptionMessage(oldEx), v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static MailOperationException convertToApi(
            org.oscm.internal.types.exception.MailOperationException oldEx) {
        return convertExceptionToApi(oldEx, MailOperationException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static NonUniqueBusinessKeyException convertToApi(
            org.oscm.internal.types.exception.NonUniqueBusinessKeyException oldEx) {

        org.oscm.internal.types.exception.beans.DomainObjectExceptionBean bean = (org.oscm.internal.types.exception.beans.DomainObjectExceptionBean) getFaultInfo(
                oldEx);
        DomainObjectExceptionBean v13Bean = convertBeanToApi(bean,
                DomainObjectExceptionBean.class);

        if (bean != null) {
            DomainObjectException.ClassEnum v13ClassEnum = EnumConverter
                    .convert(bean.getClassEnum(),
                            DomainObjectException.ClassEnum.class);
            v13Bean.setClassEnum(v13ClassEnum);
        }

        return newApiException(NonUniqueBusinessKeyException.class,
                getExceptionMessage(oldEx), v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static ObjectNotFoundException convertToApi(
            org.oscm.internal.types.exception.ObjectNotFoundException oldEx) {

        org.oscm.internal.types.exception.beans.DomainObjectExceptionBean bean = (org.oscm.internal.types.exception.beans.DomainObjectExceptionBean) getFaultInfo(
                oldEx);
        DomainObjectExceptionBean v13Bean = convertBeanToApi(bean,
                DomainObjectExceptionBean.class);

        if (bean != null) {
            DomainObjectException.ClassEnum v13ClassEnum = EnumConverter
                    .convert(bean.getClassEnum(),
                            DomainObjectException.ClassEnum.class);
            v13Bean.setClassEnum(v13ClassEnum);
        }

        return newApiException(ObjectNotFoundException.class,
                getExceptionMessage(oldEx), v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static OperationNotPermittedException convertToApi(
            org.oscm.internal.types.exception.OperationNotPermittedException oldEx) {
        return convertExceptionToApi(oldEx,
                OperationNotPermittedException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static PublishingToMarketplaceNotPermittedException convertToApi(
            org.oscm.internal.types.exception.PublishingToMarketplaceNotPermittedException oldEx) {
        return convertExceptionToApi(oldEx,
                PublishingToMarketplaceNotPermittedException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static OrganizationAuthoritiesException convertToApi(
            org.oscm.internal.types.exception.OrganizationAuthoritiesException oldEx) {
        return convertExceptionToApi(oldEx,
                OrganizationAuthoritiesException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static OrganizationAuthorityException convertToApi(
            org.oscm.internal.types.exception.OrganizationAuthorityException oldEx) {
        return convertExceptionToApi(oldEx,
                OrganizationAuthorityException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static OrganizationDataException convertToApi(
            org.oscm.internal.types.exception.OrganizationDataException oldEx) {
        return convertExceptionToApi(oldEx, OrganizationDataException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static OrganizationRemovedException convertToApi(
            org.oscm.internal.types.exception.OrganizationRemovedException oldEx) {
        return convertExceptionToApi(oldEx, OrganizationRemovedException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static PaymentDataException convertToApi(
            org.oscm.internal.types.exception.PaymentDataException oldEx) {
        return convertExceptionToApi(oldEx, PaymentDataException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static PaymentDeregistrationException convertToApi(
            org.oscm.internal.types.exception.PaymentDeregistrationException oldEx) {
        return convertExceptionToApi(oldEx,
                PaymentDeregistrationException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static PaymentInformationException convertToApi(
            org.oscm.internal.types.exception.PaymentInformationException oldEx) {
        return convertExceptionToApi(oldEx, PaymentInformationException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static PriceModelException convertToApi(
            org.oscm.internal.types.exception.PriceModelException oldEx) {
        return convertExceptionToApi(oldEx, PriceModelException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static RegistrationException convertToApi(
            org.oscm.internal.types.exception.RegistrationException oldEx) {

        org.oscm.internal.types.exception.beans.RegistrationExceptionBean bean = (org.oscm.internal.types.exception.beans.RegistrationExceptionBean) getFaultInfo(
                oldEx);
        RegistrationExceptionBean v13Bean = convertBeanToApi(bean,
                RegistrationExceptionBean.class);

        if (bean != null) {
            RegistrationException.Reason v13Reason = EnumConverter.convert(
                    bean.getReason(), RegistrationException.Reason.class);
            v13Bean.setReason(v13Reason);
        }

        return newApiException(RegistrationException.class,
                getExceptionMessage(oldEx), v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static SecurityCheckException convertToApi(
            org.oscm.internal.types.exception.SecurityCheckException oldEx) {
        return convertExceptionToApi(oldEx, SecurityCheckException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static ServiceChangedException convertToApi(
            org.oscm.internal.types.exception.ServiceChangedException oldEx) {
        return convertExceptionToApi(oldEx, ServiceChangedException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static ServiceCompatibilityException convertToApi(
            org.oscm.internal.types.exception.ServiceCompatibilityException oldEx) {

        org.oscm.internal.types.exception.beans.ApplicationExceptionBean bean = getFaultInfo(
                oldEx);
        ApplicationExceptionBean v13Bean = convertBeanToApi(bean,
                ApplicationExceptionBean.class);

        ServiceCompatibilityException.Reason v13Reason = null;
        if (bean != null) {
            v13Reason = EnumConverter.convert(oldEx.getReason(),
                    ServiceCompatibilityException.Reason.class);
        }

        ServiceCompatibilityException newEx = null;
        String message = getExceptionMessage(oldEx);

        if (v13Reason == null) {
            newEx = new ServiceCompatibilityException(message, v13Bean);
        } else {
            // There is no constructor to set the
            // ServiceCompatibilityExceptionBean, thus we set
            // the reason via the constructor and the fields of the
            // ApplicationExceptionBean via the Setters.
            newEx = new ServiceCompatibilityException(message, v13Reason);
            newEx.setMessageKey(v13Bean.getMessageKey());
            newEx.setMessageParams(v13Bean.getMessageParams());
            newEx.setId(v13Bean.getId());
            newEx.setCauseStackTrace(v13Bean.getCauseStackTrace());
        }
        newEx.setStackTrace(oldEx.getStackTrace());

        return newEx;
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static ServiceOperationException convertToApi(
            org.oscm.internal.types.exception.ServiceOperationException oldEx) {
        return convertExceptionToApi(oldEx, ServiceOperationException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static ServiceParameterException convertToApi(
            org.oscm.internal.types.exception.ServiceParameterException oldEx) {
        return convertExceptionToApi(oldEx, ServiceParameterException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static ServiceStateException convertToApi(
            org.oscm.internal.types.exception.ServiceStateException oldEx) {
        return convertExceptionToApi(oldEx, ServiceStateException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static SubscriptionMigrationException convertToApi(
            org.oscm.internal.types.exception.SubscriptionMigrationException oldEx) {

        org.oscm.internal.types.exception.beans.SubscriptionMigrationExceptionBean bean = (org.oscm.internal.types.exception.beans.SubscriptionMigrationExceptionBean) getFaultInfo(
                oldEx);
        SubscriptionMigrationExceptionBean v13Bean = convertBeanToApi(bean,
                SubscriptionMigrationExceptionBean.class);

        if (bean != null) {
            SubscriptionMigrationException.Reason v13Reason = EnumConverter
                    .convert(bean.getReason(),
                            SubscriptionMigrationException.Reason.class);
            v13Bean.setReason(v13Reason);
        }

        return newApiException(SubscriptionMigrationException.class,
                getExceptionMessage(oldEx), v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static SubscriptionStateException convertToApi(
            org.oscm.internal.types.exception.SubscriptionStateException oldEx) {

        org.oscm.internal.types.exception.beans.SubscriptionStateExceptionBean bean = (org.oscm.internal.types.exception.beans.SubscriptionStateExceptionBean) getFaultInfo(
                oldEx);
        SubscriptionStateExceptionBean v13Bean = convertBeanToApi(bean,
                SubscriptionStateExceptionBean.class);

        if (bean != null) {
            SubscriptionStateException.Reason v13Reason = EnumConverter.convert(
                    bean.getReason(), SubscriptionStateException.Reason.class);
            v13Bean.setReason(v13Reason);
            v13Bean.setMember(bean.getMember());
        }

        return newApiException(SubscriptionStateException.class,
                getExceptionMessage(oldEx), v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static SubscriptionStillActiveException convertToApi(
            org.oscm.internal.types.exception.SubscriptionStillActiveException oldEx) {
        return convertExceptionToApi(oldEx,
                SubscriptionStillActiveException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static TechnicalServiceActiveException convertToApi(
            org.oscm.internal.types.exception.TechnicalServiceActiveException oldEx) {
        return convertExceptionToApi(oldEx,
                TechnicalServiceActiveException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static TriggerProcessStatusException convertToApi(
            org.oscm.internal.types.exception.TriggerProcessStatusException oldEx) {
        return convertExceptionToApi(oldEx,
                TriggerProcessStatusException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static TechnicalServiceNotAliveException convertToApi(
            org.oscm.internal.types.exception.TechnicalServiceNotAliveException oldEx) {
        return convertExceptionToApi(oldEx,
                TechnicalServiceNotAliveException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static TechnicalServiceOperationException convertToApi(
            org.oscm.internal.types.exception.TechnicalServiceOperationException oldEx) {
        return convertExceptionToApi(oldEx,
                TechnicalServiceOperationException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static UpdateConstraintException convertToApi(
            org.oscm.internal.types.exception.UpdateConstraintException oldEx) {

        org.oscm.internal.types.exception.beans.DomainObjectExceptionBean bean = (org.oscm.internal.types.exception.beans.DomainObjectExceptionBean) getFaultInfo(
                oldEx);
        DomainObjectExceptionBean v13Bean = convertBeanToApi(bean,
                DomainObjectExceptionBean.class);

        if (bean != null) {
            DomainObjectException.ClassEnum v13ClassEnum = EnumConverter
                    .convert(bean.getClassEnum(),
                            DomainObjectException.ClassEnum.class);
            v13Bean.setClassEnum(v13ClassEnum);
        }

        return newApiException(UpdateConstraintException.class,
                getExceptionMessage(oldEx), v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static UserActiveException convertToApi(
            org.oscm.internal.types.exception.UserActiveException oldEx) {
        return convertExceptionToApi(oldEx, UserActiveException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static UserDeletionConstraintException convertToApi(
            org.oscm.internal.types.exception.UserDeletionConstraintException oldEx) {
        return convertExceptionToApi(oldEx,
                UserDeletionConstraintException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static UserModificationConstraintException convertToApi(
            org.oscm.internal.types.exception.UserModificationConstraintException oldEx) {

        org.oscm.internal.types.exception.beans.UserModificationConstraintExceptionBean bean = (org.oscm.internal.types.exception.beans.UserModificationConstraintExceptionBean) getFaultInfo(
                oldEx);
        UserModificationConstraintExceptionBean v13Bean = convertBeanToApi(bean,
                UserModificationConstraintExceptionBean.class);

        if (bean != null) {
            UserModificationConstraintException.Reason v13Reason = EnumConverter
                    .convert(bean.getReason(),
                            UserModificationConstraintException.Reason.class);
            v13Bean.setReason(v13Reason);
        }

        return newApiException(UserModificationConstraintException.class,
                getExceptionMessage(oldEx), v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static ValidationException convertToApi(
            org.oscm.internal.types.exception.ValidationException oldEx) {

        org.oscm.internal.types.exception.beans.ValidationExceptionBean bean = (org.oscm.internal.types.exception.beans.ValidationExceptionBean) getFaultInfo(
                oldEx);
        ValidationExceptionBean v13Bean = convertBeanToApi(bean,
                ValidationExceptionBean.class);

        if (bean != null) {
            ValidationException.ReasonEnum v13Reason = EnumConverter.convert(
                    bean.getReason(), ValidationException.ReasonEnum.class);
            v13Bean.setReason(v13Reason);
            v13Bean.setMember(bean.getMember());
        }

        return newApiException(ValidationException.class,
                getExceptionMessage(oldEx), v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version ValidationException
     * 
     * @param oldEx
     *            Exception to convert.
     * @return ValidationException of target version.
     */
    public static UserRoleAssignmentException convertToApi(
            org.oscm.internal.types.exception.UserRoleAssignmentException oldEx) {
        return convertExceptionToApi(oldEx, UserRoleAssignmentException.class);
    }

    /**
     * Convert source version Exception to target version MarketplaceValidationException
     *
     * @param oldEx
     *            Exception to convert.
     * @return MarketplaceValidationException of target version.
     */
    public static MarketplaceValidationException convertToApi(
            org.oscm.internal.types.exception.MarketplaceValidationException oldEx) {
        return convertExceptionToApi(oldEx, MarketplaceValidationException.class);
    }

    /**
     * Convert source version Exception to target version ValidationException
     * 
     * @param oldEx
     *            Exception to convert.
     * @return ValidationException of target version.
     */
    public static AddMarketingPermissionException convertToApi(
            org.oscm.internal.types.exception.AddMarketingPermissionException oldEx) {
        return convertExceptionToApi(oldEx,
                AddMarketingPermissionException.class);
    }

    /**
     * Convert source version Exception to target version ValidationException
     * 
     * @param oldEx
     *            Exception to convert.
     * @return ValidationException of target version.
     */
    public static InvalidPhraseException convertToApi(
            org.oscm.internal.types.exception.InvalidPhraseException oldEx) {
        return convertExceptionToApi(oldEx, InvalidPhraseException.class);
    }

    /**
     * Convert source version Exception to target version ValidationException
     * 
     * @param oldEx
     *            Exception to convert.
     * @return ValidationException of target version.
     */
    public static ServiceNotPublishedException convertToApi(
            org.oscm.internal.types.exception.ServiceNotPublishedException oldEx) {
        return convertExceptionToApi(oldEx, ServiceNotPublishedException.class);
    }

    /**
     * Convert source version Exception to target version ValidationException
     * 
     * @param oldEx
     *            Exception to convert.
     * @return ValidationException of target version.
     */
    public static TechnicalServiceMultiSubscriptions convertToApi(
            org.oscm.internal.types.exception.TechnicalServiceMultiSubscriptions oldEx) {
        return convertExceptionToApi(oldEx,
                TechnicalServiceMultiSubscriptions.class);
    }

    /**
     * Convert source version Exception to target version ValidationException
     * 
     * @param oldEx
     *            Exception to convert.
     * @return ValidationException of target version.
     */
    public static UnchangeableAllowingOnBehalfActingException convertToApi(
            org.oscm.internal.types.exception.UnchangeableAllowingOnBehalfActingException oldEx) {
        return convertExceptionToApi(oldEx,
                UnchangeableAllowingOnBehalfActingException.class);
    }

    /**
     * Convert source version Exception to target version ValidationException
     * 
     * @param oldEx
     *            Exception to convert.
     * @return ValidationException of target version.
     */
    public static MarketplaceRemovedException convertToApi(
            org.oscm.internal.types.exception.MarketplaceRemovedException oldEx) {
        return convertExceptionToApi(oldEx, MarketplaceRemovedException.class);
    }

    /**
     * Convert source version Exception to target version ValidationException
     * 
     * @param oldEx
     *            Exception to convert.
     * @return ValidationException of target version.
     */
    public static CatalogEntryRemovedException convertToApi(
            org.oscm.internal.types.exception.CatalogEntryRemovedException oldEx) {
        return convertExceptionToApi(oldEx, CatalogEntryRemovedException.class);
    }

    /**
     * Convert source version Exception to target version ValidationException
     * 
     * @param oldEx
     *            Exception to convert.
     * @return ValidationException of target version.
     */
    public static SubscriptionAlreadyExistsException convertToApi(
            org.oscm.internal.types.exception.SubscriptionAlreadyExistsException oldEx) {
        return convertExceptionToApi(oldEx,
                SubscriptionAlreadyExistsException.class);
    }

    /**
     * Convert source version Exception to target version
     * BillingAdapterNotFoundException
     * 
     * @param oldEx
     *            Exception to convert.
     * @return BillingAdapterNotFoundException of target version.
     */
    public static BillingAdapterNotFoundException convertToApi(
            org.oscm.internal.types.exception.BillingAdapterNotFoundException oldEx) {
        return convertExceptionToApi(oldEx,
                BillingAdapterNotFoundException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static OperationPendingException convertToApi(
            org.oscm.internal.types.exception.OperationPendingException oldEx) {

        org.oscm.internal.types.exception.beans.OperationPendingExceptionBean bean = (org.oscm.internal.types.exception.beans.OperationPendingExceptionBean) getFaultInfo(
                oldEx);
        OperationPendingExceptionBean v13Bean = convertBeanToApi(bean,
                OperationPendingExceptionBean.class);

        if (bean != null) {
            OperationPendingException.ReasonEnum v13Reason = EnumConverter
                    .convert(bean.getReason(),
                            OperationPendingException.ReasonEnum.class);
            v13Bean.setReason(v13Reason);
        }

        return newApiException(OperationPendingException.class,
                getExceptionMessage(oldEx), v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static ImageException convertToApi(
            org.oscm.internal.types.exception.ImageException oldEx) {

        org.oscm.internal.types.exception.beans.ImageExceptionBean bean = (org.oscm.internal.types.exception.beans.ImageExceptionBean) getFaultInfo(
                oldEx);
        ImageExceptionBean v13Bean = convertBeanToApi(bean,
                ImageExceptionBean.class);

        if (bean != null) {
            ImageException.Reason v13Reason = EnumConverter
                    .convert(bean.getReason(), ImageException.Reason.class);
            v13Bean.setReason(v13Reason);
        }

        return newApiException(ImageException.class, getExceptionMessage(oldEx),
                v13Bean, oldEx.getStackTrace());
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static MarketingPermissionNotFoundException convertToApi(
            org.oscm.internal.types.exception.MarketingPermissionNotFoundException oldEx) {
        return convertExceptionToApi(oldEx,
                MarketingPermissionNotFoundException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static MarketplaceAccessTypeUneligibleForOperationException convertToApi(
            org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException oldEx) {
        return convertExceptionToApi(oldEx,
                MarketplaceAccessTypeUneligibleForOperationException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static OrganizationAlreadyBannedException convertToApi(
            org.oscm.internal.types.exception.OrganizationAlreadyBannedException oldEx) {
        return convertExceptionToApi(oldEx,
                OrganizationAlreadyBannedException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static TriggerDefinitionDataException convertToApi(
            org.oscm.internal.types.exception.TriggerDefinitionDataException oldEx) {
        return convertExceptionToApi(oldEx,
                TriggerDefinitionDataException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static OperationStateException convertToApi(
            org.oscm.internal.types.exception.OperationStateException oldEx) {
        return convertExceptionToApi(oldEx, OperationStateException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static org.oscm.internal.types.exception.PSPCommunicationException convertToUp(
            PSPCommunicationException e) {
        return convertExceptionToUp(e,
                org.oscm.internal.types.exception.PSPCommunicationException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static org.oscm.internal.types.exception.PaymentDeregistrationException convertToUp(
            PaymentDeregistrationException e) {
        return convertExceptionToUp(e,
                org.oscm.internal.types.exception.PaymentDeregistrationException.class);
    }

    /**
     * Convert source version Exception to target version Exception
     * 
     * @param oldEx
     *            Exception to convert.
     * @return Exception of target version.
     */
    public static org.oscm.internal.types.exception.PSPProcessingException convertToUp(
            PSPProcessingException e) {
        return convertExceptionToUp(e,
                org.oscm.internal.types.exception.PSPProcessingException.class);
    }

    private static <E extends org.oscm.internal.types.exception.SaaSApplicationException> E convertExceptionToUp(
            SaaSApplicationException sourceEx, Class<E> destinationClazz) {
        org.oscm.internal.types.exception.beans.ApplicationExceptionBean bean = convertBeanToUp(
                getFaultInfo(sourceEx),
                org.oscm.internal.types.exception.beans.ApplicationExceptionBean.class);

        return createException(destinationClazz, getExceptionMessage(sourceEx),
                bean, sourceEx.getStackTrace());
    }

    private static <B extends org.oscm.internal.types.exception.beans.ApplicationExceptionBean> B convertBeanToUp(
            ApplicationExceptionBean sourceBean, Class<B> destinationClazz) {
        B destBean;
        try {
            Constructor<B> constructor = destinationClazz.getConstructor();
            destBean = constructor.newInstance();
        } catch (Exception ex) {
            return null;
        }

        if (sourceBean != null) {
            convertBasicBeanFields(destBean, sourceBean);
        }
        return destBean;
    }

    private static <E extends org.oscm.internal.types.exception.SaaSApplicationException, B extends org.oscm.internal.types.exception.beans.ApplicationExceptionBean> E createException(
            Class<E> exceptionClazz, String message, B bean,
            StackTraceElement[] stackTrace) {
        try {
            Constructor<E> constructor = exceptionClazz
                    .getConstructor(String.class, bean.getClass());
            E ex = constructor.newInstance(message, bean);
            ex.setStackTrace(stackTrace);
            return ex;
        } catch (Exception e) {
            return null;
        }
    }

    private static <E extends SaaSApplicationException> E convertExceptionToApi(
            org.oscm.internal.types.exception.SaaSApplicationException sourceEx,
            Class<E> destinationClazz) {
        ApplicationExceptionBean bean = convertBeanToApi(getFaultInfo(sourceEx),
                ApplicationExceptionBean.class);

        return newApiException(destinationClazz, getExceptionMessage(sourceEx),
                bean, sourceEx.getStackTrace());
    }

    private static <B extends ApplicationExceptionBean> B convertBeanToApi(
            org.oscm.internal.types.exception.beans.ApplicationExceptionBean sourceBean,
            Class<B> destinationClazz) {
        B destBean;
        try {
            Constructor<B> constructor = destinationClazz.getConstructor();
            destBean = constructor.newInstance();
        } catch (Exception ex) {
            return null;
        }

        if (sourceBean != null) {
            convertBasicBeanFields(destBean, sourceBean);
        }
        return destBean;
    }

    private static <E extends SaaSApplicationException, B extends ApplicationExceptionBean> E newApiException(
            Class<E> exceptionClazz, String message, B bean,
            StackTraceElement[] stackTrace) {
        try {
            Constructor<E> constructor = exceptionClazz
                    .getConstructor(String.class, bean.getClass());
            E ex = constructor.newInstance(message, bean);
            ex.setStackTrace(stackTrace);
            return ex;
        } catch (Exception ex) {
            return null;
        }
    }

    private static void convertBasicBeanFields(
            ApplicationExceptionBean destination,
            org.oscm.internal.types.exception.beans.ApplicationExceptionBean source) {
        destination.setMessageKey(source.getMessageKey());
        destination.setMessageParams(source.getMessageParams());
        destination.setId(source.getId());
        destination.setCauseStackTrace(source.getCauseStackTrace());
    }

    private static void convertBasicBeanFields(
            org.oscm.internal.types.exception.beans.ApplicationExceptionBean destination,
            ApplicationExceptionBean source) {
        destination.setMessageKey(source.getMessageKey());
        destination.setMessageParams(source.getMessageParams());
        destination.setId(source.getId());
        destination.setCauseStackTrace(source.getCauseStackTrace());
    }

    private static ApplicationExceptionBean getFaultInfo(
            SaaSApplicationException ex) {
        try {
            return ex.getFaultInfo();
        } catch (Exception e) {
            return null;
        }
    }

    private static org.oscm.internal.types.exception.beans.ApplicationExceptionBean getFaultInfo(
            org.oscm.internal.types.exception.SaaSApplicationException ex) {
        try {
            return ex.getFaultInfo();
        } catch (Exception e) {
            return null;
        }
    }

    private static String getExceptionMessage(
            org.oscm.internal.types.exception.SaaSApplicationException saasEx) {
        if (getFaultInfo(saasEx) == null) {
            return null;
        } else {
            return exceptionMsgWithoutId(saasEx.getId(), saasEx.getMessage());
        }
    }

    private static String getExceptionMessage(SaaSApplicationException saasEx) {
        if (getFaultInfo(saasEx) == null) {
            return null;
        } else {
            return exceptionMsgWithoutId(saasEx.getId(), saasEx.getMessage());
        }
    }

    private static String exceptionMsgWithoutId(String exceptionId,
            String message) {
        String exceptionPrefix = "EXCEPTIONID " + exceptionId + ": ";
        if (message.startsWith(exceptionPrefix)) {
            return (message.substring(exceptionPrefix.length()));
        } else {
            return message;
        }
    }

    public static BulkUserImportException convertToApi(
            org.oscm.internal.types.exception.BulkUserImportException e) {

        org.oscm.internal.types.exception.beans.BulkUserImportExceptionBean bean = (org.oscm.internal.types.exception.beans.BulkUserImportExceptionBean) getFaultInfo(
                e);
        BulkUserImportExceptionBean apiBean = convertBeanToApi(bean,
                BulkUserImportExceptionBean.class);

        if (bean != null) {
            BulkUserImportException.Reason apiExceptionReason = EnumConverter
                    .convert(bean.getReason(),
                            BulkUserImportException.Reason.class);
            apiBean.setReason(apiExceptionReason);
        }

        return newApiException(BulkUserImportException.class,
                getExceptionMessage(e), apiBean, e.getStackTrace());
    }
}
