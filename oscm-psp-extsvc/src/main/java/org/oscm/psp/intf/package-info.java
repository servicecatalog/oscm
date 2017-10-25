/* 
 *  Copyright FUJITSU LIMITED 2017
 */
/**
 * Specifies the interface which must be implemented by a PSP integration
 * adapter. A PSP integration adapter is required for every payment service
 * provider (PSP) that is to be connected to the platform.
 * <p>
 * Additionally, the package includes the remote interface of the
 * payment registration service. This interface is invoked via 
 * the callback component for a PSP after the PSP specific actions 
 * for registering a customer's payment information have been executed 
 * at the PSP side.
 * </p>
 */
@javax.xml.bind.annotation.XmlSchema(namespace = "http://oscm.org/xsd", elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
package org.oscm.psp.intf;
