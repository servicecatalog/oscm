package org.oscm.app.vmware.parser;

import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Class used for parsing a specified CSV file.
 *
 * @author floreks
 */
abstract class CSVParser<T> implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVParser.class);
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final char DEFAULT_SEPARATOR = ',';

    private final CSVReader reader;
    private Set<String> processedEntries = new HashSet<>();

    private boolean areRequiredColumnsSet(Set<String> columns) {
        if (columns == null) {
            return false;
        }

        for (String col : this.getRequiredColumns()) {
            if (!columns.contains(col)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Reads next line in file and returns list of values split based on provided separator.
     *
     * @return list of values split based on provided separator
     */
    private List<String> next() throws IOException {
        return Arrays.asList(Optional.ofNullable(this.reader.readNext()).orElse(new String[0]));
    }

    /**
     * Initializes parser and validates if required column names are present in the first line of the CSV file.
     */
    private void init() throws Exception {
        Set<String> columnsLine = new HashSet<>(this.next());
        if (!this.areRequiredColumnsSet(columnsLine)) {
            throw new Exception("Mandatory column missing. Required columns: " + this.getRequiredColumns().toString());
        }
    }

    CSVParser(InputStream stream) throws Exception {
        this.reader = new CSVReader(new InputStreamReader(stream, DEFAULT_ENCODING), DEFAULT_SEPARATOR);
        this.init();
    }

    CSVParser(InputStream stream, String encoding, char separator) throws Exception {
        this.reader = new CSVReader(new InputStreamReader(stream, encoding), separator);
        this.init();
    }


    public void close() throws Exception {
        if (this.reader != null) {
            reader.close();
        }
    }

    /**
     * Reads next line in file and validates read data, then transforms it into key[column name] - value[column value]
     * map and returns to the user.
     *
     * @return map of key-value entries build based on line read from the CSV file or null if EOF
     */
    Map<String, String> readNext() throws Exception {
        List<String> values = this.next();
        Map<String, String> result = new HashMap<>();
        if (values.size() == 0) {
            return null;
        }

        if (values.size() != this.getRequiredColumns().size()) {
            throw new Exception("Incorrect data format. Expected: " +
                    this.getRequiredColumns().size() +
                    " columns, got: " +
                    values.size() +
                    "."
            );
        }

        if (this.processedEntries.contains(values.toString())) {
            LOGGER.debug("Duplicated entry found. Skipping.");
            return this.readNext();
        }

        this.processedEntries.add(values.toString());

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).length() == 0) {
                throw new Exception("Missing value for required column " + this.getRequiredColumns().get(i) + ".");
            }

            result.put(this.getRequiredColumns().get(i), values.get(i));
        }

        return result;
    }

    /**
     * @return list of columns that have to be provided as the first line of the CSV file.
     */
    public abstract List<String> getRequiredColumns();

    /**
     * @return next model object created based on single line read from the CSV file.
     */
    public abstract T readNextObject() throws Exception;
}
