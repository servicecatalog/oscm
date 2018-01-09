/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.types.exceptions;

import static org.oscm.test.Numbers.L2;
import static org.junit.Assert.assertEquals;
import org.junit.Assert;

import org.junit.Test;

import org.oscm.types.enumtypes.ParameterType;
import org.oscm.types.enumtypes.ServiceStatus;
import org.oscm.types.exceptions.CurrencyException;
import org.oscm.types.exceptions.DeletionConstraintException;
import org.oscm.types.exceptions.DomainObjectException.ClassEnum;
import org.oscm.types.exceptions.ImportException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.OrganizationAuthorityException;
import org.oscm.types.exceptions.OrganizationDataException;
import org.oscm.types.exceptions.OrganizationRemovedException;
import org.oscm.types.exceptions.PSPCommunicationException;
import org.oscm.types.exceptions.RegistrationException;
import org.oscm.types.exceptions.ServiceChangedException;
import org.oscm.types.exceptions.ServiceOperationException;
import org.oscm.types.exceptions.ServiceParameterException;
import org.oscm.types.exceptions.ServiceStateException;
import org.oscm.types.exceptions.ServicesStillPublishedException;
import org.oscm.types.exceptions.SubscriptionMigrationException;
import org.oscm.types.exceptions.SubscriptionStillActiveException;
import org.oscm.types.exceptions.TechnicalServiceActiveException;
import org.oscm.types.exceptions.TechnicalServiceNotAliveException;
import org.oscm.types.exceptions.TechnicalServiceOperationException;
import org.oscm.types.exceptions.UpdateConstraintException;
import org.oscm.types.exceptions.UserActiveException;
import org.oscm.types.exceptions.UserDeletionConstraintException;
import org.oscm.types.exceptions.UserModificationConstraintException;

public class ExceptionTest {

    private static final Object[] PARAMS = new Object[] { "1", L2 };

    @Test
    public void testServiceParameterException() {
        ServiceParameterException ex = new ServiceParameterException("message",
                ParameterType.SERVICE_PARAMETER, "id", new Object[] { "value",
                        "Highlander" });

        Assert.assertEquals(
                "ex.ServiceParameterException.SERVICE_PARAMETER.id",
                ex.getMessageKey());
        Object[] params = ex.getMessageParams();
        Assert.assertEquals(2, params.length);
        Assert.assertEquals("value", params[0]);
        Assert.assertEquals("Highlander", params[1]);
    }

    @Test
    public void testSubscriptionMigrationFailed() {
        SubscriptionMigrationException ex = new SubscriptionMigrationException(
                "message", SubscriptionMigrationException.Reason.PARAMETER,
                PARAMS);

        Assert.assertEquals("ex.SubscriptionMigrationException.PARAMETER",
                ex.getMessageKey());
        checkParams(ex.getMessageParams());
    }

    @Test
    public void testTechnicalServiceNotAliveException() {
        TechnicalServiceNotAliveException ex = new TechnicalServiceNotAliveException(
                TechnicalServiceNotAliveException.Reason.CONNECTION_REFUSED,
                PARAMS, new Exception());

        Assert.assertEquals(
                "ex.TechnicalServiceNotAliveException.CONNECTION_REFUSED",
                ex.getMessageKey());
        checkParams(ex.getMessageParams());

        ex = new TechnicalServiceNotAliveException(
                TechnicalServiceNotAliveException.Reason.CONNECTION_REFUSED,
                new Exception());
        assertEquals(0, ex.getMessageParams().length);

        ex = new TechnicalServiceNotAliveException();
        Assert.assertEquals("ex.TechnicalServiceNotAliveException",
                ex.getMessageKey());

    }

    @Test
    public void testUserModificationConstraintViolation() {
        UserModificationConstraintException ex = new UserModificationConstraintException(
                UserModificationConstraintException.Reason.LAST_ADMIN);
        Assert.assertEquals(
                "ex.UserModificationConstraintException.LAST_ADMIN",
                ex.getMessageKey());
    }

    @Test
    public void testUserDeletionConstraintViolation() {
        UserDeletionConstraintException ex = new UserDeletionConstraintException(
                "message", UserDeletionConstraintException.Reason.LAST_ADMIN);
        Assert.assertEquals("ex.UserDeletionConstraintException.LAST_ADMIN",
                ex.getMessageKey());
    }

    @Test
    public void testSubscriptionStillActive() {
        SubscriptionStillActiveException ex = new SubscriptionStillActiveException(
                "message",
                SubscriptionStillActiveException.Reason.ACTIVE_SESSIONS);
        Assert.assertEquals(
                "ex.SubscriptionStillActiveException.ACTIVE_SESSIONS",
                ex.getMessageKey());
    }

    @Test
    public void testServiceStillActive() {
        ServicesStillPublishedException ex = new ServicesStillPublishedException(
                "message",
                ServicesStillPublishedException.Reason.ACTIVE_SERVICES_OF_SELLER);
        Assert.assertEquals(
                "ex.ServicesStillPublishedException.ACTIVE_SERVICES_OF_SELLER",
                ex.getMessageKey());

        ex = new ServicesStillPublishedException(
                "message",
                ServicesStillPublishedException.Reason.ACTIVE_SERVICES_ON_MARKETPLACE);
        Assert.assertEquals(
                "ex.ServicesStillPublishedException.ACTIVE_SERVICES_ON_MARKETPLACE",
                ex.getMessageKey());

        ex = new ServicesStillPublishedException(
                "message",
                ServicesStillPublishedException.Reason.ACTIVE_SERVICES_OF_SELLER,
                new Object[] { "SUPP1" });

        Assert.assertEquals(
                "ex.ServicesStillPublishedException.ACTIVE_SERVICES_OF_SELLER",
                ex.getMessageKey());
        Object[] params = ex.getMessageParams();
        Assert.assertEquals(1, params.length);
        Assert.assertEquals("SUPP1", params[0]);

    }

    @Test
    public void testPSPCommunicationException() {
        PSPCommunicationException ex = new PSPCommunicationException("message",
                PSPCommunicationException.Reason.MISSING_RESPONSE_URL);
        Assert.assertEquals(
                "ex.PSPCommunicationException.MISSING_RESPONSE_URL",
                ex.getMessageKey());
    }

    @Test
    public void testRegistrationFailed() {
        RegistrationException ex = new RegistrationException("message",
                RegistrationException.Reason.TARGET_ORG_INVALID);
        Assert.assertEquals("ex.RegistrationException.TARGET_ORG_INVALID",
                ex.getMessageKey());
        ex = new RegistrationException("message");
        Assert.assertEquals("ex.RegistrationException", ex.getMessageKey());
    }

    @Test
    public void testServiceOperationFailed() {
        ServiceOperationException ex = new ServiceOperationException(
                ServiceOperationException.Reason.MISSING_PRICE_MODEL);
        Assert.assertEquals("ex.ServiceOperationException.MISSING_PRICE_MODEL",
                ex.getMessageKey());
        ex = new ServiceOperationException();
        Assert.assertEquals("ex.ServiceOperationException", ex.getMessageKey());
    }

    @Test
    public void testServiceChangedException() {
        ServiceChangedException ex = new ServiceChangedException(
                ServiceChangedException.Reason.SERVICE_MODIFIED);
        Assert.assertEquals("ex.ServiceChangedException.SERVICE_MODIFIED",
                ex.getMessageKey());
    }

    @Test
    public void testProductInvalidState() {
        ServiceStateException ex = new ServiceStateException(
                ServiceStatus.INACTIVE, ServiceStatus.ACTIVE);
        Object[] params = ex.getMessageParams();
        Assert.assertEquals(2, params.length);
        Assert.assertEquals("INACTIVE", params[0]);
        Assert.assertEquals("ACTIVE", params[1]);
    }

    @Test
    public void testNonUniqueBusinessKeyException() {
        NonUniqueBusinessKeyException ex = new NonUniqueBusinessKeyException(
                ClassEnum.ORGANIZATION, "12345");
        Assert.assertEquals(ClassEnum.ORGANIZATION,
                ex.getDomainObjectClassEnum());
        Object[] params = ex.getMessageParams();
        Assert.assertEquals(1, params.length);
        Assert.assertEquals("12345", params[0]);
        Assert.assertEquals("ex.NonUniqueBusinessKeyException.ORGANIZATION",
                ex.getMessageKey());
    }

    @Test
    public void testInvalidOrganizationAuthority() {
        OrganizationAuthorityException ex = new OrganizationAuthorityException(
                "message", PARAMS);
        checkParams(ex.getMessageParams());
    }

    @Test
    public void testTechnicalProductOperationFailed() {
        TechnicalServiceOperationException ex = new TechnicalServiceOperationException(
                "message", PARAMS, new Exception());
        checkParams(ex.getMessageParams());
    }

    @Test
    public void testInsufficientOrganizationAuthorities() {
        OrganizationAuthoritiesException ex = new OrganizationAuthoritiesException(
                "message", PARAMS);
        checkParams(ex.getMessageParams());
    }

    @Test
    public void testUserActiveException() {
        UserActiveException ex = new UserActiveException("message", PARAMS);
        checkParams(ex.getMessageParams());
    }

    @Test
    public void testImportFailed() {
        ImportException ex = new ImportException("details");
        Object[] params = ex.getMessageParams();
        Assert.assertEquals(1, params.length);
        Assert.assertEquals("details", params[0]);
    }

    @Test
    public void testDeletionConstraintException() {
        DeletionConstraintException ex = new DeletionConstraintException(
                ClassEnum.ORGANIZATION, "organizationId",
                ClassEnum.SUBSCRIPTION);
        Assert.assertEquals(ClassEnum.ORGANIZATION,
                ex.getDomainObjectClassEnum());
        Assert.assertEquals(ClassEnum.SUBSCRIPTION, ex.getDependentDomClass());
        Object[] params = ex.getMessageParams();
        Assert.assertEquals(1, params.length);
        Assert.assertEquals("organizationId", params[0]);
        Assert.assertEquals("ex.DeletionConstraintException.ORGANIZATION",
                ex.getMessageKey());
    }

    @Test
    public void testObjectNotFoundException() {
        ObjectNotFoundException ex = new ObjectNotFoundException(
                ClassEnum.ORGANIZATION, "organizationId");
        Assert.assertEquals(ClassEnum.ORGANIZATION,
                ex.getDomainObjectClassEnum());
        Object[] params = ex.getMessageParams();
        Assert.assertEquals(1, params.length);
        Assert.assertEquals("organizationId", params[0]);
        Assert.assertEquals("ex.ObjectNotFoundException.ORGANIZATION",
                ex.getMessageKey());
    }

    @Test
    public void testUpdateConstraintException() {
        UpdateConstraintException ex = new UpdateConstraintException(
                ClassEnum.ORGANIZATION, "organizationId");
        Assert.assertEquals(ClassEnum.ORGANIZATION,
                ex.getDomainObjectClassEnum());
        Object[] params = ex.getMessageParams();
        Assert.assertEquals(1, params.length);
        Assert.assertEquals("organizationId", params[0]);
        Assert.assertEquals("ex.UpdateConstraintException.ORGANIZATION",
                ex.getMessageKey());
    }

    @Test
    public void testInsufficientOrganizationData() {
        OrganizationDataException ex = new OrganizationDataException("message",
                OrganizationDataException.Reason.CREATE_PAYMENT);
        Assert.assertEquals("ex.OrganizationDataException.CREATE_PAYMENT",
                ex.getMessageKey());
        ex = new OrganizationDataException("message");
        Assert.assertEquals("ex.OrganizationDataException", ex.getMessageKey());
    }

    @Test
    public void testOrganizationRemovedException() {
        OrganizationRemovedException ex = new OrganizationRemovedException(
                "message", PARAMS, new Exception());
        checkParams(ex.getMessageParams());
    }

    @Test
    public void testUnsupportedCurrency() {
        CurrencyException ex = new CurrencyException("message", PARAMS);
        checkParams(ex.getMessageParams());
    }

    @Test
    public void testTechnicalProductActiveException() {
        TechnicalServiceActiveException ex = new TechnicalServiceActiveException(
                PARAMS);
        checkParams(ex.getMessageParams());
    }

    private void checkParams(Object[] params) {
        Assert.assertEquals(PARAMS.length, params.length);
        Assert.assertEquals(String.valueOf(PARAMS[0]), params[0]);
        Assert.assertEquals(String.valueOf(PARAMS[1]), params[1]);
    }
}
