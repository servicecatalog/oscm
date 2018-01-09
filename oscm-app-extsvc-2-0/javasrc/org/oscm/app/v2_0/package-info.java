/* 
 *  Copyright FUJITSU LIMITED 2018
 */
/**
 * Specifies the public API of the asynchronous provisioning
 * platform (APP) of Catalog Manager (OSCM).
 * <p>
 * APP is a framework for integrating applications with OSCM
 * using provisioning services that work in asynchronous 
 * mode. The framework provides a provisioning service as well 
 * as functions, data persistence, and notification features, 
 * which are always required for integrating applications in 
 * asynchronous mode.  
 * </p>
 * <p>
 * A provisioning service is a Web service which integrates an 
 * application with the subscription management of OSCM. Depending
 * on the access type of a subscription's underlying service, the 
 * provisioning service of an application is called when a customer 
 * creates or modifies the subscription or when user assignments 
 * for the subscription are changed.
 * </p>
 * APP also includes the operation service interface for executing 
 * technical service operations on the integrated applications 
 * from the OSCM user interface. Technical service operations 
 * can be used to access the resources of an application and 
 * perform administrative tasks without actually opening the 
 * application. The operations and the access information of 
 * the operation service must be specified in the technical  
 * service definition for the application. 
 * <p>
 * In order to integrate an application with OSCM using the APP
 * framework, you must create a corresponding service controller.
 * To do this, implement the methods of the <code>APPlatformController</code>
 * class as required for the application.
 * </p>
 */
package org.oscm.app.v2_0;
