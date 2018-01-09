/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: June 8, 2011                                                      
 *                                                                              
 *  Completion Time: June 8, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

/**
 * @author Dirk Bernsau
 * 
 */
public class WebtestTaskException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WebtestTaskException(String message) {
        super(message);
    }
}
