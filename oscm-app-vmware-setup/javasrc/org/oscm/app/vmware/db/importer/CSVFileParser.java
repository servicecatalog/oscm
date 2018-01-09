/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.db.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVReader;

/**
 * Class for parsing a specified CSV file
 *
 * @author soehnges
 */
public abstract class CSVFileParser {

    private final CSVReader reader;
    private String[] mappings;
    private long line;
    private final String[] mandatory_cols;
    private final Map<String, String> data;
    private Set<String> lines;

    public CSVFileParser(InputStream stream, String encoding, char delimiter,
            final String mandatory_columns[],
            final String mandatory_column_values[]) throws Exception {

        this.lines = new HashSet<String>();
        this.mandatory_cols = mandatory_column_values;
        this.data = new LinkedHashMap<String, String>();
        reader = new CSVReader(new InputStreamReader(stream, encoding),
                delimiter);
        try {
            mappings = reader.readNext();
            if (mappings == null) {
                throw new IOException("Empty CSV file given.");
            }
            line = 1;
            List<String> checkCols = Arrays.asList(mappings);
            for (String reqCol : mandatory_columns) {
                if (!checkCols.contains(reqCol)) {
                    throw new Exception(
                            "Missing mandatory column '" + reqCol + "'.");
                }
            }

        } catch (Exception e) {
            reader.close();
            throw e;
        }

    }

    public void close() {
        try {
            reader.close();
        } catch (IOException io) {
            // ignore
        }
    }

    public Map<String, String> getData() {
        return data;
    }

    /**
     * Return next row as key-value map.
     *
     * Empty lines will be skipped.
     *
     * @return map with key-value data or NULL if no more line exists
     */
    public Map<String, String> readNext() throws Exception {
        String[] values;
        do {
            values = reader.readNext();
            if (values == null) {
                return null;
            }
            line++;
        } while (values.length == 1 && values[0].length() == 0);
        data.clear();
        StringBuffer nextline = new StringBuffer();
        for (int c = 0; c < values.length; c++) {
            if (values[c] != null && values[c].length() == 0) {
                values[c] = null;
            }
            data.put(mappings[c], values[c]);
            nextline.append(values[c]);
        }

        if (lines.contains(nextline.toString())) {
            throw new Exception(
                    "Duplicate line definition: " + nextline.toString());
        }

        lines.add(nextline.toString());

        for (String reqCol : mandatory_cols) {
            validateMandatoryValue(reqCol);
        }

        return data;
    }

    public void validateMandatoryValue(String column) throws Exception {
        if (!data.containsKey(column) || data.get(column) == null) {
            throw new Exception(
                    "Missing value for mandatory column '" + column + "'.");
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("#");
        sb.append(line);
        sb.append(": ");
        if (line == 1) {
            for (int c = 0; c < mappings.length; c++) {
                if (c > 0) {
                    sb.append(";");
                }
                sb.append(mappings[c]);
            }
        } else {
            int c = 0;
            for (String value : data.values()) {
                if (c++ > 0) {
                    sb.append(";");
                }
                sb.append(value);
            }
        }
        return sb.toString();
    }
}
