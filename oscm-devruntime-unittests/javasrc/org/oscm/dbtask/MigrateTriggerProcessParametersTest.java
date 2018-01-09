/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                  
 *  Creation Date: 09.07.15 11:27
 *
 *******************************************************************************/

package org.oscm.dbtask;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOService;
import org.oscm.utils.PreparedStatementBuilder;

public class MigrateTriggerProcessParametersTest {

    /**
     * System Under Test
     */
    private MigrateTriggerProcessParameters SUT;

    private Connection connection;
    private PreparedStatement tppStatement;
    private PreparedStatement paramStatement;
    private PreparedStatement updateStatement;
    private PreparedStatementBuilder statementBuilder;

    @Before
    public void setup() throws Exception {
        AESEncrypter.generateKey();
        SUT = stubSUT();
        connection = mock(Connection.class);
        tppStatement = mock(PreparedStatement.class);
        paramStatement = mock(PreparedStatement.class);
        updateStatement = mock(PreparedStatement.class);

        mockStatementBuilder();

        SUT.setConnection(connection);
        SUT.setStatementBuilder(statementBuilder);

        doReturn(tppStatement).when(connection).prepareStatement(
                MigrateTriggerProcessParameters.QUERY_ALL_PRODUCT_PARAMETERS);
        doReturn(paramStatement).when(connection).prepareStatement(
                MigrateTriggerProcessParameters.QUERY_IS_CONFIGURABLE_PARAMETER);
        doReturn(updateStatement).when(connection).prepareStatement(
                MigrateTriggerProcessParameters.UPDATE_PRODUCT_PARAMETER);
        doReturn(Integer.valueOf(0)).when(updateStatement).executeUpdate();
    }

    private MigrateTriggerProcessParameters stubSUT() throws SQLException {
        return new MigrateTriggerProcessParameters() {
            @Override
            <T> T getObjectFromXML(ResultSet resultSet, Class<T> expectedClass)
                    throws SQLException {
                VOService voService = new VOService();
                List<VOParameter> parameters = new ArrayList<>();
                parameters.add(new VOParameter());
                voService.setParameters(parameters);
                return expectedClass.cast(voService);
            }
        };
    }

    private void mockStatementBuilder() throws SQLException {
        statementBuilder = spy(new PreparedStatementBuilder(connection) {
            @Override
            public PreparedStatementBuilder addLongParam(int paramIndex,
                    long param) throws SQLException {
                this.statement = paramStatement;
                return super.addLongParam(paramIndex, param);
            }

            @Override
            public PreparedStatementBuilder addStringParam(int paramindex,
                    String param) throws SQLException {
                this.statement = updateStatement;
                return super.addStringParam(paramindex, param);
            }
        });
    }

    private void mockResultSet(PreparedStatement statement, boolean isNext)
            throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        doReturn(resultSet).when(statement).executeQuery();
        doReturn(Boolean.valueOf(isNext)).doReturn(Boolean.FALSE)
                .when(resultSet).next();
        doReturn(Boolean.valueOf(isNext)).when(resultSet).getBoolean(anyInt());
    }

    @Test
    public void execute_nothingToUpdate() throws Exception {
        // given
        mockResultSet(tppStatement, false);

        // when
        SUT.execute();

        // then
        verify(tppStatement, times(1)).executeQuery();
        verify(paramStatement, times(0)).executeQuery();
        verify(updateStatement, never()).executeUpdate();
    }

    @Test
    public void execute_noParamsToUpdate() throws Exception {
        // given
        mockResultSet(tppStatement, true);
        mockResultSet(paramStatement, false);

        // when
        SUT.execute();

        // then
        verify(tppStatement, times(1)).executeQuery();
        verify(paramStatement, times(1)).executeQuery();
        verify(updateStatement, never()).executeUpdate();

    }

    @Test
    public void execute_updated() throws Exception {
        // given
        mockResultSet(tppStatement, true);
        mockResultSet(paramStatement, true);

        // when
        SUT.execute();

        // then
        verify(tppStatement, times(1)).executeQuery();
        verify(paramStatement, times(1)).executeQuery();
        verify(updateStatement, times(1)).executeUpdate();
    }

}
