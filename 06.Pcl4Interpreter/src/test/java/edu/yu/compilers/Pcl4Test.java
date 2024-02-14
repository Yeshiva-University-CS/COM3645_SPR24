package edu.yu.compilers;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import antlr4.Pcl4Lexer;
import antlr4.Pcl4Parser;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class Pcl44Test {

    private final static Logger logger = LogManager.getLogger(Pcl44Test.class);

    static {
        Configurator.setLevel("edu.yu.compilers", Level.INFO);
    }

    @TestFactory
    Stream<DynamicTest> dynamicTestsFromStream() {
        return Stream.of("HelloWorld", "Newton", "TestCase", "TestFor", "TestIf", "TestWhile")
        .map(testName -> {
            return DynamicTest.dynamicTest("Test " + testName, () -> {
                logger.info("===== BEGIN {} =====", testName);
                testExecute(testName);
                logger.info("===== END {} =====", testName);
            });
        });
    }

    private void testExecute(String testName) throws IOException {
        String inputFileName = "/input/" + testName + ".txt";
        String outputFileName = "/output/" + testName + ".execute.txt";

        int errorCount = 0;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        try (PrintStream ps = new PrintStream(baos, true, utf8)) {
            System.setOut(ps);
            CharStream chars = getResourceFileCharStream(inputFileName);
            var lexer = new Pcl4Lexer(chars);
            var tokens = new CommonTokenStream(lexer);
            var parser = new Pcl4Parser(tokens);
            var tree = parser.program();
            Pcl4.executeProgram(tree);
            System.setOut(System.out);
        }

        if (errorCount > 0) {
            new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())))
                    .lines()
                    .forEachOrdered(s -> logger.error(s));
        }
        assertEquals(0, errorCount);

        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()));
        File outputFile = getResourceFile(outputFileName);
        System.setOut(System.out);

        int breaks = 0;
        try (BufferedReader reader1 = new BufferedReader(isr);
                BufferedReader reader2 = Files.newBufferedReader(outputFile.toPath())) {
            String line1 = reader1.readLine();
            String line2 = reader2.readLine();

            int lineNumber = 1;
            while (line1 != null || line2 != null) {
                if (line1 == null) {
                    logger.info("Missing Line " + lineNumber + " from " + inputFileName + ": " + line2);
                    breaks += 1;
                } else if (line2 == null) {
                    breaks += 1;
                } else if (!line1.equals(line2)) {
                    logger.info("Break on Line " + lineNumber + " of " + inputFileName + ": ");
                    logger.info("  Expected: " + line2);
                    logger.info("  Actual:   " + line1);
                    breaks += 1;
                }
                line1 = reader1.readLine();
                line2 = reader2.readLine();
                lineNumber++;
            }
        }
        assertEquals(0, breaks, testName + " failed");
    }

    private File getResourceFile(String name) {
        URL url = this.getClass().getResource(name);
        assertNotNull(url);
        File file = new File(url.getFile());
        assertTrue(file.exists());
        return file;
    }

    private CharStream getResourceFileCharStream(String fileName) throws IOException{
        File inputFile = getResourceFile(fileName);
        return CharStreams.fromStream(new FileInputStream(inputFile));
    }

}