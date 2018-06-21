package org.oscm.app.vmware.parser;

import org.oscm.app.vmware.parser.model.VCenter;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VCenterParser extends CSVParser<VCenter> {
    enum Columns {
        TKEY("TKey"),
        NAME("Name"),
        IDENTIFIER("Identifier"),
        URL("URL"),
        USER_ID("UserId"),
        PASSWORD("Password");

        private final String text;

        Columns(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    public VCenterParser(InputStream stream) throws Exception {
        super(stream);
    }

    @Override
    public List<String> getRequiredColumns() {
        return Arrays.asList(
                Columns.TKEY.toString(),
                Columns.NAME.toString(),
                Columns.IDENTIFIER.toString(),
                Columns.URL.toString(),
                Columns.USER_ID.toString(),
                Columns.PASSWORD.toString()
        );
    }

    @Override
    public VCenter readNextObject() throws Exception {
        VCenter result = new VCenter();
        Map<String, String> entries = this.readNext();

        if(entries == null) {
            return null;
        }

        result.tKey = entries.get(Columns.TKEY.toString());
        result.name = entries.get(Columns.NAME.toString());
        result.identifier = entries.get(Columns.IDENTIFIER.toString());
        result.url = entries.get(Columns.URL.toString());
        result.userId = entries.get(Columns.USER_ID.toString());
        result.password = entries.get(Columns.PASSWORD.toString());

        return result;
    }
}
