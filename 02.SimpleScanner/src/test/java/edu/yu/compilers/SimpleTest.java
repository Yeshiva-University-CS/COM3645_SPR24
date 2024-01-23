package edu.yu.compilers;

import edu.yu.compilers.frontend.Source;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SimpleTest {

    private final static Logger logger = LogManager.getLogger(SimpleTest.class);

    static {
        Configurator.setLevel("edu.yu.compilers", Level.INFO);
    }

    private static Stream<Arguments> inputAndOutputFiles() {
        return Stream.of(
                Arguments.of("/input/HelloWorld.txt", "/output/HelloWorld.out.txt"),
                Arguments.of("/input/Newton.txt", "/output/Newton.out.txt"),
                Arguments.of("/input/SquareRootTable.txt", "/output/SquareRootTable.out.txt"),
                Arguments.of("/input/Temperature.txt", "/output/Temperature.out.txt"),
                Arguments.of("/input/ScannerTest.txt", "/output/ScannerTest.out.txt"));
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    private File getResourceFile(String name) {
        URL url = this.getClass().getResource(name);
        assertNotNull(url);
        File file = new File(url.getFile());
        assertTrue(file.exists());
        return file;
    }

    @ParameterizedTest(name = "Test {0}")
    @MethodSource("inputAndOutputFiles")
    @DisplayName("Pascal scanner")
    public void TestPascalScanning(String inputFileName, String outputFileName) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        try (PrintStream ps = new PrintStream(baos, true, utf8)) {
            System.setOut(ps);
            File inputFile = getResourceFile(inputFileName);
            Source source = new Source(inputFile.getAbsolutePath());
            Simple.testScanner(source);
        }
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
        assertEquals(0, breaks);
    }
}