/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import org.apache.commons.configuration2.XMLConfiguration;

/**
 * Same as the default apache XML configuration entity, but with disabled
 * delimiter functionality.
 *
 * @author soehnges
 */
public class XMLHostConfiguration extends XMLConfiguration {

    private static final long serialVersionUID = -2209965493418921967L;

    public XMLHostConfiguration() {
    }
}
