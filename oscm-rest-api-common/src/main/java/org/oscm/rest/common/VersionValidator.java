/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 03.10.17 16:36
 *
 ******************************************************************************/

package org.oscm.rest.common;

import javax.ws.rs.WebApplicationException;

/**
 * Authored by dawidch
 */
public class VersionValidator {

    /**
     * Validates the version string and compares it with the existing version
     * numbers. Throws a NotFoundException if not valid.
     *
     * @param version
     *            the version string
     * @return the version as integer
     * @throws WebApplicationException
     */
    public int doIt(String version) throws WebApplicationException {

        if (version == null) {
            throw WebException.notFound()
                    .message(CommonParams.ERROR_INVALID_VERSION).build();
        }

        if (!version.matches(CommonParams.PATTERN_VERSION)) {
            throw WebException.notFound()
                    .message(CommonParams.ERROR_INVALID_VERSION).build();
        }

        int vnr = Integer.parseInt(
                version.substring(CommonParams.PATTERN_VERSION_OFFSET));

        boolean exists = false;
        for (int i : CommonParams.VERSIONS) {
            if (i == vnr) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            throw WebException.notFound()
                    .message(CommonParams.ERROR_INVALID_VERSION).build();
        }

        return vnr;
    }
}
