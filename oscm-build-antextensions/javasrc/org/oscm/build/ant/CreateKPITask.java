/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CreateKPITask extends Task {
    private String inputFile;
    private String outputFile;

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public void execute() throws BuildException {
        try {
            verifyInput();
            Statistics statistics = createStatistics(inputFile);
            writeStatistics(statistics, outputFile);

        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void verifyInput() throws IOException {
        if (inputFile == null || inputFile.trim().length() == 0) {
            throw new BuildException("Input file not set.");
        }
        validateFile(inputFile);

        if (outputFile == null || outputFile.trim().length() == 0) {
            throw new BuildException("Output file not set.");
        }
    }

    private void validateFile(String file) throws FileNotFoundException {
        File fileToValidation = new File(file);
        if (!fileToValidation.exists()) {
            throw new FileNotFoundException("File " + fileToValidation.getAbsolutePath() + " not found");
        }
    }

    Statistics createStatistics(String inputFile)
            throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        TestSuiteDefaultHandler handler = new TestSuiteDefaultHandler();
        saxParser.parse(new File(inputFile), handler);
        return new Statistics(handler.getTests(), handler.getBugs(),
                handler.getDuration(), new Date());
    }

    void writeStatistics(Statistics statistics, String outputFile)
            throws IOException {
        InputStream is = new ByteArrayInputStream(statistics
                .serializeToString().getBytes("UTF-8"));
        Files.copy(is, Paths.get(outputFile),
                StandardCopyOption.REPLACE_EXISTING);
    }

    static class Statistics {
        int tests = 0;
        int bugs = 0;
        double duration;
        Date currentDate;

        public Statistics(int tests, int bugs, double duration, Date currentDate) {
            this.tests = tests;
            this.bugs = bugs;
            this.duration = duration;
            this.currentDate = currentDate;
        }

        public String serializeToString() {
            return "tests=" + tests + " bugs=" + bugs + " duration="
                    + formatDuration();
        }

        String formatDuration() {
            return DurationFormatUtils.formatDuration((long) (1000 * duration), "H:mm:ss.SSS");
        }
    }

    class TestSuiteDefaultHandler extends DefaultHandler {
        private int tests = 0;
        private int bugs = 0;
        private double duration = 0;

        public int getTests() {
            return tests;
        }

        public int getBugs() {
            return bugs;
        }

        public double getDuration() {
            return duration;
        }

        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) {

            if (qName.equalsIgnoreCase("testsuite")) {
                tests += Integer.valueOf(attributes.getValue("tests"));
                bugs += Integer.valueOf(attributes.getValue("failures"));
                bugs += Integer.valueOf(attributes.getValue("errors"));
                duration += Double.valueOf(attributes.getValue("time"));
            }
        }
    }
}
