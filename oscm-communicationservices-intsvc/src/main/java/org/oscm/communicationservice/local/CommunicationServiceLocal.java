/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.communicationservice.local;

import javax.ejb.Local;

import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Service interface providing functionality to send e-mails.
 * 
 * <p>
 * The service uses the configuration service to obtain the mail server address
 * and the reply mail address.
 * </p>
 * 
 * @author Mike J&auml;ger
 * 
 */
@Local
public interface CommunicationServiceLocal {

    /**
     * Sends an email to the given platform users.
     * 
     * @param type
     *            The email content type.
     * @param params
     *            Parameters for the email content text.
     * @param marketplace
     *            the marketplace to get customized texts from - can be
     *            <code>null</code>
     * @param recipients
     *            The platform users the mail will be sent to.
     * @return mail status of every sended mail encapsulated in a separate
     *         instance
     */
    public SendMailStatus<PlatformUser> sendMail(EmailType type,
            Object[] params, Marketplace marketplace,
            PlatformUser... recipients);

    /**
     * Sends an email to the given platform user.
     * 
     * @param recipient
     *            The platform user the mail will be sent to.
     * @param type
     *            The email content type.
     * @param params
     *            Parameters for the email content text.
     * @param marketplace
     *            the marketplace to get customized texts from - can be
     *            <code>null</code>
     * @throws MailOperationException
     *             Thrown in case the mail cannot be initialized or sent.
     */
    public void sendMail(PlatformUser recipient, EmailType type,
            Object[] params, Marketplace marketplace)
            throws MailOperationException;

    /**
     * Send mail to organization.
     * 
     * @param organization
     *            Organization to getting mail.
     * @param type
     *            Mail type.
     * @param params
     *            Mail parameters,
     * @param marketplace
     *            The marketplace to get customized texts from - can be
     *            <code>null</code>
     * @throws MailOperationException
     *             On error mail sending.
     */
    public void sendMail(Organization organization, EmailType type,
            Object[] params, Marketplace marketplace)
            throws MailOperationException;

    /**
     * Send an email of given type to the given address.
     * 
     * @param emailAddress
     *            The address to which the mail will be send to.
     * @param type
     *            Mail type.
     * @param params
     *            Mail parameters,
     * @param marketplace
     *            Marketpalce of subscription,
     * @param locale
     *            Locale information of mail receiver,
     * @throws MailOperationException
     *             On error mail sending.
     * @throws ValidationException
     *             if the format of the email address is not valid
     */
    public void sendMail(String emailAddress, EmailType type, Object[] params,
            Marketplace marketplace, String locale)
            throws MailOperationException, ValidationException;

    /**
     * Send mail to given organizations.
     * 
     * @param type
     *            Mail type.
     * @param params
     *            Mail parameters,
     * @param marketplace
     *            The marketplace to get customized texts from - can be
     *            <code>null</code>
     * @param organizations
     *            The organizations the mail will be sent to.
     * @return mail status of every sended mail encapsulated in a separate
     *         instance
     */
    public SendMailStatus<Organization> sendMail(EmailType type,
            Object[] params, Marketplace marketplace,
            Organization... organizations);

    /**
     * Get the marketplace URL for the given platform user.
     * 
     * @param marketplaceId
     *            the marketplace id
     * @return The marketplace URL for the given platform user.
     * @throws MailOperationException
     *             Thrown if an UnsupportedEncodingException occurs during the
     *             URL construction.
     */
    public String getMarketplaceUrl(String marketplaceId)
            throws MailOperationException;

    /**
     * Get the marketplace URL for the given platform user.
     *
     * @param marketplaceId
     *            the marketplace id
     * @return The secure marketplace URL (https) for the given platform user.
     * @throws MailOperationException
     *             Thrown if an UnsupportedEncodingException occurs during the
     *             URL construction.
     */
    public String getMarketplaceUrlHttps(String marketplaceId)
            throws MailOperationException;

    /**
     * Gets the administration URL of the blue portal
     * 
     * @return the administration URL of the blue portal
     */
    public String getBaseUrl();

    /**
     * Gets the secure administration URL (https) of the blue portal
     *
     * @return the secure administration URL (https) of the blue portal
     */
    public String getBaseUrlHttps();

    /**
     * Gets the administration URL of the blue portal with ID of the tenant
     *
     * @param tenantId
     *            the tenant ID
     * @return
     */
    public String getBaseUrlWithTenant(String tenantId) throws MailOperationException;

    /**
     * Gets the administration secure URL of the blue portal with ID of the tenant
     *
     * @param tenantId
     *            the tenant ID
     * @return
     */
    public String getBaseUrlHttpsWithTenant(String tenantId) throws MailOperationException;
}
