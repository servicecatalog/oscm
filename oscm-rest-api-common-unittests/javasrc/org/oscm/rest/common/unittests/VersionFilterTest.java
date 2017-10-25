/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 19, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.common.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.oscm.rest.common.*;

/**
 * Unit test for VersionFilter
 * 
 * @author miethaner
 */
public class VersionFilterTest {

    @SuppressWarnings("boxing")
    private MultivaluedMap<String, String> testVersionFilter(String version) throws WebApplicationException, NoSuchMethodException {

        ResourceInfo resourceInfo = mock(ResourceInfo.class);
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock(UriInfo.class);
        when(request.getUriInfo()).thenReturn(uriInfo);

        Method method = SinceClass.class.getMethod("dummy");

        MultivaluedMap<String, String> prop = new MultivaluedHashMap<>();
        prop.putSingle(CommonParams.PARAM_VERSION, version);
        when(uriInfo.getPathParameters()).thenReturn(prop);

        VersionFilter filter = spy(new VersionFilter());
        when(filter.getResourceInfo()).thenReturn(resourceInfo);
        when(resourceInfo.getResourceMethod()).thenReturn(method);
        filter.filter(request);

        return prop;
    }

    @SuppressWarnings("boxing")
    @Test
    public void testVersionFilterVersionPositive() throws NoSuchMethodException {

        String version = "v" + CommonParams.VERSION_1;

        MultivaluedMap<String, String> prop = testVersionFilter(version
        );

        assertNotNull(
                prop.get(CommonParams.PARAM_VERSION));
        assertEquals(version,
                prop.get(CommonParams.PARAM_VERSION).get(0));
    }

    @Test
    public void testVersionFilterVersionNegativePrefix() throws NoSuchMethodException {

        String version = "n42";

        try {
            testVersionFilter(version);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    @Test
    public void testVersionFilterVersionNegativeNumber() throws NoSuchMethodException {

        String version = "v22";

        try {
            testVersionFilter(version);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    @SuppressWarnings("boxing")
    @Test
    public void testVersionFilterVersionNotExisting() {

        try {
            UriInfo info = mock(UriInfo.class);
            MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
            ContainerRequestContext request = mock(
                    ContainerRequestContext.class);
            when(info.getPathParameters()).thenReturn(map);
            when(request.getUriInfo()).thenReturn(info);

            VersionFilter filter = new VersionFilter();
            filter.filter(request);
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    @Test
    public void testVersionFilterVersionNull() throws NoSuchMethodException {

        try {
            testVersionFilter(null);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    @SuppressWarnings("boxing")
    @Test
    public void testVersionFilterSincePositive() throws NoSuchMethodException {

        String version = "v" + CommonParams.VERSION_1;

        MultivaluedMap<String, String> prop = testVersionFilter(version
        );

        assertNotNull(prop.get(CommonParams.PARAM_VERSION));
        assertEquals(version,
                prop.get(CommonParams.PARAM_VERSION).get(0));
    }

    @Test
    public void testVersionFilterSinceNegative() throws NoSuchMethodException {

        String version = "v-1";

        try {
            testVersionFilter(version
            );
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    @SuppressWarnings("boxing")
    @Test
    public void testVersionFilterUntilPositive() throws NoSuchMethodException {

        String version = "v" + CommonParams.VERSION_1;

        MultivaluedMap<String, String> prop = testVersionFilter(version
        );

        assertNotNull(prop.get(CommonParams.PARAM_VERSION));
        assertEquals(version,
                prop.get(CommonParams.PARAM_VERSION).get(0));
    }

    @Test
    public void testVersionFilterUntilNegative() throws NoSuchMethodException {

        String version = "v3";

        try {
            testVersionFilter(version);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    public static class SinceClass {
        @Since(1)
        @Until(2)
        public void dummy() {}
    }
}
