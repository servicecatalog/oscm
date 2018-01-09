/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2014-04-05                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.exceptions;

import java.util.List;

import org.oscm.app.v2_0.data.LocalizedText;

/**
 * Exception a controller can throw to request APP to abort the current
 * provisioning operation, for example, because an action failed at the
 * application side.
 * <p>
 * APP stops its automatic polling for events. It sends an email to the
 * technology managers of the technology provider organization which is
 * responsible for the application. It also sends an email to a dedicated
 * address of the customer organization that owns the subscription for which the
 * provisioning operation was started. The email contain the detail messages
 * given in the exception.
 */
public class AbortException extends APPlatformException {

    private static final long serialVersionUID = 6704636629758658840L;

    private List<LocalizedText> providerMessages;

    /**
     * Constructs a new exception with the specified localized text messages.
     * 
     * @param customerMessages
     *            the messages to be included in the email sent to the customer
     *            organization
     * @param providerMessages
     *            the messages to be included in the email sent to the
     *            technology managers of the technology provider organization
     *            responsible for the application
     */
    public AbortException(List<LocalizedText> customerMessages,
            List<LocalizedText> providerMessages) {
        super(customerMessages);
        this.providerMessages = providerMessages;
    }

    /**
     * Returns the localized text messages that will be included in the email
     * sent to the technology managers of the technology provider organization
     * which is responsible for the application
     * 
     * @return the messages
     */
    public List<LocalizedText> getProviderMessages() {
        return providerMessages;
    }
}
