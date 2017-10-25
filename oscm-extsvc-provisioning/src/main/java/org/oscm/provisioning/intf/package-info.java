/* 
 *  Copyright FUJITSU LIMITED 2017
 */
/**
 * Specifies the public provisioning service API of Catalog
 * Manager.
 * <p>
 * A provisioning service is a Web service which integrates an 
 * application with the subscription management of Catalog Manager. Depending
 * on the access type of a subscription's underlying service, the 
 * provisioning service of an application is called when a customer 
 * creates or modifies the subscription or when user assignments 
 * for the subscription are changed.
 * </p>
 * <p>
 * A provisioning service must implement the interface defined
 * in this package. The information on how to access the provisioning
 * service must be specified in the technical service definition 
 * for the application.
 * </p>
 */
package org.oscm.provisioning.intf;

