/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 09.11.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class V2_0_51_1__AdaptLocales extends DatabaseUpgradeTask implements JdbcMigration {

    @Override
    public void execute() throws Exception {

    }

    private void adaptLocaleSetting(final Connection conn, String locales,
            String tableName) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(String.format(
                "UPDATE %s SET locale = 'en' WHERE locale NOT IN (%s)",
                tableName, locales));
    }

    @Override
    public void migrate(Connection connection) throws Exception {
        setConnection(connection);
        final Connection conn = getConnection();

        // get all valid locales
        Locale[] validLocales = Locale.getAvailableLocales();
        StringBuffer localeQueryPart = new StringBuffer();
        for (Locale locale : validLocales) {
            if (localeQueryPart.length() > 0) {
                localeQueryPart.append(", ");
            }
            localeQueryPart.append("'");
            localeQueryPart.append(locale.toString());
            localeQueryPart.append("'");
        }
        String locales = localeQueryPart.toString();

        adaptLocaleSetting(conn, locales, "platformuser");
        adaptLocaleSetting(conn, locales, "organization");
    }
}
