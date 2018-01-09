/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.test.stubs;

import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.exception.MailOperationException;

public class CommunicationServiceStub implements CommunicationServiceLocal {

    @Override
    public SendMailStatus<PlatformUser> sendMail(EmailType type,
            Object[] params, Marketplace marketplace,
            PlatformUser... recipients) {
        return new SendMailStatus<PlatformUser>();
    }

    @Override
    public void sendMail(PlatformUser recipient, EmailType type,
            Object[] params, Marketplace marketplace)
            throws MailOperationException {
    }

    @Override
    public void sendMail(Organization organization, EmailType type,
            Object[] params, Marketplace marketplace)
            throws MailOperationException {

    }

    @Override
    public void sendMail(String emailAddress, EmailType type, Object[] params,
            Marketplace marketplace, String locale)
            throws MailOperationException {

    }

    @Override
    public String getMarketplaceUrl(String marketplaceId)
            throws MailOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMarketplaceUrlHttps(String marketplaceId)
            throws MailOperationException {
        return "";
    }

    @Override
    public SendMailStatus<Organization> sendMail(EmailType type,
            Object[] params, Marketplace marketplace,
            Organization... organizations) {
        return new SendMailStatus<Organization>();
    }

    @Override
    public String getBaseUrl() {
        return null;
    }

    @Override
    public String getBaseUrlHttps() {
        return null;
    }

    @Override
    public String getBaseUrlWithTenant(String tenantId) throws MailOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBaseUrlHttpsWithTenant(String tenantId) throws MailOperationException {
        throw new UnsupportedOperationException();
    }
}
