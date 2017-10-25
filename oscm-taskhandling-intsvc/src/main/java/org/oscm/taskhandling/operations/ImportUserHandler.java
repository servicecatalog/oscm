/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.BulkUserImportException;
import org.oscm.internal.types.exception.BulkUserImportException.Reason;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.string.Strings;
import org.oscm.taskhandling.payloads.ImportUserPayload;
import org.oscm.taskhandling.payloads.ImportUserPayload.UserDefinition;
import org.oscm.taskhandling.payloads.TaskPayload;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Asynchronous import of users for non-ldap organizations.
 * 
 * Importing multiple users must be performed asynchronously as background job
 * as it will be a long running task for a large list of users. Only one import
 * job is allowed per organization. Another import can only be started if no
 * other import job is running, otherwise the import attempt has to fail. It
 * must be ensured that if the creation of one or more users fails for some
 * reason, the remaining ones are imported nevertheless. For each created user
 * the usual 'Account created mail' will be sent.
 * 
 * The user who started the import job will get an email after the job has been
 * finished informing about
 * 
 * 
 * @author cheld
 * 
 */
public class ImportUserHandler extends TaskHandler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ImportUserHandler.class);

    ImportUserPayload payload;

    Report report;

    @Override
    public void execute() throws Exception {

        // preconditions
        Marketplace marketplace = loadMarketplace();
        PlatformUser executingUser = loadExecutingUser();
        report = new Report(payload.getUsersToBeImported().size());

        // import user definitions
        for (UserDefinition userToBeImported : payload.getUsersToBeImported()) {
            try {
                userToBeImported.getUserDetails().setOrganizationId(
                        payload.getOrganizationId());

                userToBeImported
                        .getUserDetails()
                        .setUserRoles(
                                new HashSet<UserRoleType>(
                                        removeRoles(
                                                userToBeImported.getRoles(),
                                                Arrays.asList(UserRoleType.UNIT_ADMINISTRATOR))));

                serviceFacade.getIdentityService().importUser(
                        userToBeImported.getUserDetails(),
                        payload.getMarketplaceId());
            } catch (SaaSApplicationException e) {
                String errorMessage = localizeException(marketplace,
                        executingUser, e);
                report.addErrorMessage(userToBeImported.getUserDetails()
                        .getUserId(), errorMessage);
            } catch (Exception e) {
                String errorMessage = localizeException(marketplace,
                        executingUser, new BulkUserImportException(
                                Reason.USER_CREATION_FAILED, e));
                report.addErrorMessage(userToBeImported.getUserDetails()
                        .getUserId(), errorMessage);
            }
        }

        // send report
        serviceFacade.getCommunicationService().sendMail(executingUser,
                report.buildMailType(), report.buildMailParams(), marketplace);

    }

    /**
     * Load marketplace if ID is given, or null otherwise
     */
    Marketplace loadMarketplace() throws ObjectNotFoundException {
        String marketplaceId = payload.getMarketplaceId();
        if (Strings.isEmpty(marketplaceId)) {
            return null;
        }
        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId(marketplaceId);
        marketplace = (Marketplace) serviceFacade.getDataService()
                .getReferenceByBusinessKey(marketplace);
        return marketplace;
    }

    /**
     * Load the user that triggered the import process
     */
    PlatformUser loadExecutingUser() throws ObjectNotFoundException {
        PlatformUser executingUser = serviceFacade.getDataService()
                .getReference(PlatformUser.class,
                        payload.getImportingUserKey().longValue());
        return executingUser;
    }

    /**
     * Load the translated string from resource bundle or db
     */
    String localizeException(Marketplace marketplace,
            PlatformUser executingUser, SaaSApplicationException e) {
        String text = serviceFacade.getLocalizerService()
                .getLocalizedTextFromBundle(
                        LocalizedObjectTypes.EXCEPTION_PROPERTIES, marketplace,
                        executingUser.getLocale(), e.getMessageKey());
        return formatMessage(text, e, executingUser.getLocale());
    }

    /**
     * Replace place-holders like {0} with parameters. Use key if no message is
     * available
     */
    String formatMessage(String text, SaaSApplicationException e, String locale) {
        if (Strings.isEmpty(text)) {
            text = e.getMessageKey();
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_BULK_USER_IMPORT_FAILED,
                    payload.getInfo());
        }
        MessageFormat mf = new MessageFormat(text,
                Locale.forLanguageTag(locale));
        String errorMessage = mf.format(e.getMessageParams(),
                new StringBuffer(), null).toString();
        return errorMessage;
    }

    List<UserRoleType> removeRoles(List<UserRoleType> roles,
            List<UserRoleType> rolesToRemove) {
        List<UserRoleType> result = new ArrayList<UserRoleType>();
        if (roles == null) {
            return result;
        }
        if (rolesToRemove == null) {
            return roles;
        }
        for (UserRoleType role : roles) {
            if (!rolesToRemove.contains(role)) {
                result.add(role);
            }
        }
        return result;
    }

    /**
     * Log the exception and try to send a mail to the user that triggered the
     * operation
     */
    @Override
    public void handleError(Exception cause) {
        logger.logWarn(Log4jLogger.SYSTEM_LOG, cause,
                LogMessageIdentifier.ERROR_BULK_USER_IMPORT_FAILED,
                payload.getInfo());
    }

    @Override
    void setPayload(TaskPayload payload) {
        this.payload = (ImportUserPayload) payload;
    }

}
