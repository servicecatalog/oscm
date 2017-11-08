/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                          
 *                                                                              
 *  Creation Date: 14.06.2012                                                      
 *                                                                              
 *  Completion Time:
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.components.response;

import static org.oscm.test.matchers.JavaMatchers.hasItems;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Test class for Response
 * 
 * @author cheld
 */
public class ResponseTest {

    @Test
    public void getResult() {
        // given
        Response response = givenResponseWithTransferObjects();

        // when adding some value
        response.getResults().add("some transfer object");

        // then value can be found by type
        assertThat(response.getResult(String.class),
                equalTo("some transfer object"));

    }

    @Test
    public void getResult_notFound() {
        // given
        Response response = givenResponseWithTransferObjects();

        // when searching for a type that is not stored
        String nonExistingTO = response.getResult(String.class);

        // then
        assertThat(nonExistingTO, is(nullValue()));
    }

    private Response givenResponseWithTransferObjects() {
        List<Object> values = new ArrayList<Object>();
        values.add(Boolean.TRUE);
        values.add(new Long(5));
        Response response = new Response();
        response.setResults(values);
        return response;
    }

    @Test
    public void getResultList() {
        // given
        Response response = givenResponseWithLists();

        // when adding some value
        List<String> result = response.getResultList(String.class);

        // then value can be found by type
        assertThat(result.get(0), equalTo("1"));

    }

    private Response givenResponseWithLists() {
        List<Object> responseValues = new ArrayList<Object>();
        List<Object> booleanValues = new ArrayList<Object>();
        booleanValues.add(Boolean.TRUE);
        responseValues.add(booleanValues);
        List<Object> stringValues = new ArrayList<Object>();
        stringValues.add("1");
        stringValues.add("2");
        responseValues.add(stringValues);
        Response response = new Response();
        response.setResults(responseValues);
        return response;
    }

    @Test
    public void getInfos() {

        // given a response with WARNING and INFO
        Response response = givenResponseWithVariousReturnCodes();

        // when filtering out infos
        List<ReturnCode> infos = response.getInfos();

        // then sub-list has only return codes with severity info
        assertThat(infos, hasItems(1));
        assertThat(infos.get(0).getType(), equalTo(ReturnType.INFO));
    }

    @Test
    public void getWarnings() {

        // given a response with WARNING and INFO
        Response response = givenResponseWithVariousReturnCodes();

        // when filtering out warnings
        List<ReturnCode> warnings = response.getWarnings();

        // then sub-list has only return codes with severity warning
        assertThat(warnings, hasItems(1));
        assertThat(warnings.get(0).getType(), equalTo(ReturnType.WARNING));
    }

    private Response givenResponseWithVariousReturnCodes() {
        Response response = new Response();
        response.getReturnCodes().add(new ReturnCode(ReturnType.INFO, "e3"));
        response.getReturnCodes().add(new ReturnCode(ReturnType.WARNING, "e1"));
        return response;
    }

    @Test
    public void getMostSevereReturnCode() {
        // given a response with WARNING and INFO
        Response response = givenResponseWithVariousReturnCodes();

        // when
        ReturnCode returncode = response.getMostSevereReturnCode();

        // then only warning is returned
        assertThat(returncode.getType(), equalTo(ReturnType.WARNING));
    }

    @Test
    public void getMostSevereReturnCode_onlyWarning() {
        // given a response with only WARNING
        Response response = new Response();
        response.getReturnCodes().add(new ReturnCode(ReturnType.WARNING, "e1"));

        // when
        ReturnCode returncode = response.getMostSevereReturnCode();

        // then only error is returned
        assertThat(returncode.getType(), equalTo(ReturnType.WARNING));
    }

}
