/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 27.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.jmx.internal;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.oscm.jmx.internal.bean.EJBClientFacade;
import org.oscm.jmx.internal.common.MBeanRegistration;

public class JmxStartupServlet extends HttpServlet {
    @EJB
    private EJBClientFacade ejbClientFacade;

    private MBeanRegistration mbeanRegistration;
    private static final long serialVersionUID = -6472674427968083019L;

    @Override
    public void init() throws ServletException {
        super.init();
        mbeanRegistration = new MBeanRegistration(
                "org.oscm.jmx.internal", ejbClientFacade);
        mbeanRegistration
                .registerFromFolder("org/oscm/jmx/internal/mbean");
    }

    @Override
    public void destroy() {
        super.destroy();
        mbeanRegistration.unregister();
    }
}
