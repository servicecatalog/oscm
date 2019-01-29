/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 12.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.domain;

import java.util.EnumSet;

/**
 * Represents the current provisioning status of a service instance.
 * 
 * @author Mike J&auml;ger
 * 
 */
public enum ProvisioningStatus {

    /**
     * Indicates that the provisioning has not yet been completed and the
     * technical system (e.g. a virtual machine) is not yet created and running.
     */
    WAITING_FOR_SYSTEM_CREATION,

    /**
     * Indicates that the modification of the technical system has not yet been
     * completed and the system is either about to be reconfigured or currently
     * restarted.
     */
    WAITING_FOR_SYSTEM_MODIFICATION,

    /**
     * Indicates that the upgrade of the technical system has not yet been
     * completed and the system is either about to be reconfigured or currently
     * restarted.
     */
    WAITING_FOR_SYSTEM_UPGRADE,

    /**
     * Indicates that the execution of a service operation has not yet been
     * completed.
     */
    WAITING_FOR_SYSTEM_OPERATION,

    /**
     * Indicates that the activation of the technical system has not yet been
     * completed.
     */
    WAITING_FOR_SYSTEM_ACTIVATION,

    /**
     * Indicates that the deactivation of the technical system has not yet been
     * completed.
     */
    WAITING_FOR_SYSTEM_DEACTIVATION,

    /**
     * Indicates that the creation of users has not yet been completed.
     */
    WAITING_FOR_USER_CREATION,

    /**
     * Indicates that the modification of users has not yet been completed.
     */
    WAITING_FOR_USER_MODIFICATION,

    /**
     * Indicates that the deletion of users has not yet been completed.
     */
    WAITING_FOR_USER_DELETION,

    /**
     * Indicates that the provisioning has been completed. So the system and the
     * service should be running.
     */
    COMPLETED,

    /**
     * Indicates that the deprovisioning has not yet been completed and the
     * technical system (e.g. a virtual machine) is not yet deleted.
     */
    WAITING_FOR_SYSTEM_DELETION;

    public static EnumSet<ProvisioningStatus> getWaiting() {
        return EnumSet.of(WAITING_FOR_SYSTEM_CREATION,
                WAITING_FOR_SYSTEM_DELETION, WAITING_FOR_SYSTEM_MODIFICATION,
                WAITING_FOR_SYSTEM_UPGRADE, WAITING_FOR_SYSTEM_OPERATION,
                WAITING_FOR_SYSTEM_ACTIVATION, WAITING_FOR_SYSTEM_DEACTIVATION,
                WAITING_FOR_USER_CREATION, WAITING_FOR_USER_MODIFICATION,
                WAITING_FOR_USER_DELETION);
    }

    public static EnumSet<ProvisioningStatus> getWaitingForModification() {
        return EnumSet.of(WAITING_FOR_SYSTEM_MODIFICATION,
                WAITING_FOR_SYSTEM_UPGRADE);
    }

    public static EnumSet<ProvisioningStatus> getWaitingForCreation() {
        return EnumSet.of(WAITING_FOR_SYSTEM_CREATION);
    }

    public static EnumSet<ProvisioningStatus> getWaitingForDeletion() {
        return EnumSet.of(WAITING_FOR_SYSTEM_DELETION);
    }

    public static EnumSet<ProvisioningStatus> getWaitingForOperation() {
        return EnumSet.of(WAITING_FOR_SYSTEM_OPERATION);
    }

    public static EnumSet<ProvisioningStatus> getWaitingForActivation() {
        return EnumSet.of(WAITING_FOR_SYSTEM_ACTIVATION);
    }

    public static EnumSet<ProvisioningStatus> getWaitingForDeactivation() {
        return EnumSet.of(WAITING_FOR_SYSTEM_DEACTIVATION);
    }

    public static EnumSet<ProvisioningStatus> getWaitingForUserAction() {
        return EnumSet.of(WAITING_FOR_USER_CREATION, WAITING_FOR_USER_DELETION,
                WAITING_FOR_USER_MODIFICATION);
    }

    public String getSuspendMailMessage() {
        String mailMessage = "mail_inconsistent_instance_state";
        if (this.isWaitingForCreation()) {
            mailMessage = "mail_suspend_error_create";
        } else if (this.isWaitingForDeletion()) {
            mailMessage = "mail_suspend_error_delete";
        } else if (this.isWaitingForModification()) {
            mailMessage = "mail_suspend_error_update";
        } else if (this.isWaitingForActivation()) {
            mailMessage = "mail_suspend_error_activate";
        } else if (this.isWaitingForDeactivation()) {
            mailMessage = "mail_suspend_error_deactivate";
        } else if (this.isWaitingForOperation()) {
            mailMessage = "mail_suspend_error_operation";
        }
        return mailMessage;
    }

    public String getErrorMailMessage() {
        String mailMessage = "mail_inconsistent_instance_state";
        if (this.isWaitingForCreation()) {
            mailMessage = "mail_create_beserror";
        } else if (this.isWaitingForDeletion()) {
            mailMessage = "mail_delete_error";
        } else if (this.isWaitingForModification()) {
            mailMessage = "mail_update_error";
        } else if (this.isWaitingForActivation()) {
            mailMessage = "mail_activation_error";
        } else if (this.isWaitingForDeactivation()) {
            mailMessage = "mail_deactivation_error";
        } else if (this.isWaitingForOperation()) {
            mailMessage = "mail_operation_error";
        } else if (this.isWaitingForUserAction()) {
            mailMessage = "mail_user_error";
        } 
        return mailMessage;
    }

    public String getBesNotificationErrorMailMessage() {
        String mailMessage = "mail_inconsistent_instance_state";
        if (this.isWaitingForCreation() || this.isWaitingForDeletion()
                || this.isWaitingForModification()
                || this.isWaitingForActivation()
                || this.isWaitingForDeactivation()
                || this.isWaitingForOperation()) {
            mailMessage = "mail_bes_notification_error";
        }
        return mailMessage;
    }

    public boolean isWaitingForCreation() {
        return getWaitingForCreation().contains(this);
    }

    public boolean isWaitingForDeletion() {
        return getWaitingForDeletion().contains(this);
    }

    public boolean isWaitingForModification() {
        return getWaitingForModification().contains(this);
    }

    public boolean isWaitingForActivation() {
        return getWaitingForActivation().contains(this);
    }

    public boolean isWaitingForDeactivation() {
        return getWaitingForDeactivation().contains(this);
    }

    public boolean isWaitingForOperation() {
        return getWaitingForOperation().contains(this);
    }

    public boolean isWaitingForUserAction() {
        return getWaitingForUserAction().contains(this);
    }

    public boolean isCompleted() {
        return COMPLETED.equals(this);
    }

}
