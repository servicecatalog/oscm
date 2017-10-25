/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 09.07.15 11:27
 *
 *******************************************************************************/

package org.oscm.dbtask;

import java.beans.XMLDecoder;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.oscm.utils.PreparedStatementBuilder;
import org.oscm.utils.XmlStringCleaner;
import org.oscm.converter.XMLSerializer;
import org.oscm.stream.Streams;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SessionType;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOTriggerProcessParameter;

/**
 * Related to BUG 11739. Prepared to update configurable parameter on service
 * parameters in serialized trigger process parameter value.
 */
public class MigrateTriggerProcessParameters extends DatabaseUpgradeTask {

    static final String QUERY_ALL_PRODUCT_PARAMETERS = "SELECT tpp.tkey, tpp.serializedvalue as value FROM triggerprocessparameter tpp WHERE tpp.name='PRODUCT'";
    static final String QUERY_IS_CONFIGURABLE_PARAMETER = "SELECT p.configurable FROM parameter p where p.tkey = ?";

    static final String UPDATE_PRODUCT_PARAMETER = "UPDATE triggerprocessparameter SET serializedValue = ? WHERE tkey = ?";

    private static final Class<?> enumArray[] = {
            EventType.class,
            OrganizationRoleType.class,
            ParameterType.class,
            ParameterValueType.class,
            PaymentCollectionType.class,
            PricingPeriod.class,
            ServiceAccessType.class,
            ServiceStatus.class,
            Salutation.class,
            SessionType.class,
            SettingType.class,
            SubscriptionStatus.class,
            TriggerType.class,
            TriggerProcessStatus.class,
            org.oscm.types.enumtypes.TriggerProcessParameterName.class,
            UserAccountStatus.class, UserRoleType.class,
            UdaConfigurationType.class, ParameterModificationType.class,
            OfferingType.class };

    private PreparedStatementBuilder statementBuilder;

    public void setStatementBuilder(PreparedStatementBuilder statementBuilder) {
        this.statementBuilder = statementBuilder;
    }

    /**
     * Will update only rows where mismatch between original 'configurable'
     * value and serialized was found.
     * 
     * @throws Exception
     */
    @Override
    public void execute() throws Exception {
        statementBuilder = new PreparedStatementBuilder(getConnection());
        List<VOTriggerProcessParameter> parameterList = findTriggerProcessParameters();

        for (VOTriggerProcessParameter param : parameterList) {
            VOService service = (VOService) param.getValue();
            if (updateVOParameters(service.getParameters())) {
                updateTriggerProcessParameter(param.getKey(),
                        getSerializedValue(param.getValue()));
            }
        }
    }

    /**
     * Checks and updates if parameter in database has the same 'configurable'
     * value as given parameter.
     * 
     * @param parameters
     *            - list of voparameters from deserialized voservice
     * @return true if at least 1 parameter was updated, false otherwise
     * @throws SQLException
     */
    private boolean updateVOParameters(List<VOParameter> parameters)
            throws SQLException {
        boolean wasUpdated = false;
        for (VOParameter parameter : parameters) {
            boolean configurable = isConfigurable(parameter.getKey());
            if (parameter.isConfigurable() != configurable) {
                wasUpdated = true;
                parameter.setConfigurable(configurable);
            }
        }

        return wasUpdated;
    }

    private List<VOTriggerProcessParameter> findTriggerProcessParameters()
            throws SQLException {
        ResultSet resultSet = statementBuilder
                .setQuery(QUERY_ALL_PRODUCT_PARAMETERS).build().executeQuery();

        List<VOTriggerProcessParameter> result = new ArrayList<>();
        while (resultSet.next()) {
            VOTriggerProcessParameter parameter = new VOTriggerProcessParameter();
            parameter.setKey(resultSet.getLong(1));
            parameter.setValue(getObjectFromXML(resultSet, VOService.class));

            if (parameter.getValue() != null) {
                result.add(parameter);
            }
        }

        return result;
    }

    private boolean isConfigurable(long parameterKey) throws SQLException {
        final int colIdx = 1;

        ResultSet result = statementBuilder
                .setQuery(QUERY_IS_CONFIGURABLE_PARAMETER)
                .addLongParam(colIdx, parameterKey).build().executeQuery();

        return result.next() && result.getBoolean(colIdx);
    }

    private void updateTriggerProcessParameter(long triggerParamKey,
            String serializedValue) throws SQLException {
        statementBuilder.setQuery(UPDATE_PRODUCT_PARAMETER)
                .addLongParam(2, triggerParamKey)
                .addStringParam(1, serializedValue).build().executeUpdate();
    }

    private String getSerializedValue(Object value) {
        String xml = XMLSerializer.toXml(value, enumArray);
        return XmlStringCleaner.cleanString(xml);
    }

    /**
     * Retrieves the object from the serialized format.
     *
     * @param <T>
     *            The object type.
     * @param is
     *            The result set, containing the serialized format of the
     *            object.
     * @param expectedClass
     *            The expected class of the object.
     * @return The object.
     */
    <T> T getObjectFromXML(ResultSet resultSet, Class<T> expectedClass)
            throws SQLException {
        InputStream is = null;
        try {
            is = resultSet.getBinaryStream("value");
            XMLDecoder decoder = new XMLDecoder(is);
            return expectedClass.cast(decoder.readObject());
        } finally {
            Streams.close(is);
        }
    }
}
