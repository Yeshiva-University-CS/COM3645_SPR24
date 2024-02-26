package edu.yu.compilers;

import antlr4.PascalLexer;
import antlr4.PascalParser;
import edu.yu.compilers.backend.converter.Converter;
import edu.yu.compilers.backend.interpreter.Executor;
import edu.yu.compilers.frontend.Semantics;
import edu.yu.compilers.frontend.SyntaxErrorHandler;
import edu.yu.compilers.intermediate.symtable.SymTableEntry;
import edu.yu.compilers.intermediate.util.BackendMode;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
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

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OfficialTest {

    private final static Logger logger = LogManager.getLogger(OfficialTest.class);

    static {
        Configurator.setLevel("edu.yu.compilers", Level.INFO);
    }

    private static Stream<Arguments> semanticTestInput() {
        return Stream.of(
                Arguments.of("TypeTest", 6)
                , Arguments.of("TypeTestCase", 2)
                , Arguments.of("TypeTestIf", 2)
                , Arguments.of("TypeTestWhile", 2)
                );
    }

    private static Stream<Arguments> execTestInput() {
        return Stream.of(
                Arguments.of("HelloWorld", 3)
                , Arguments.of("TestCase", 124)
                , Arguments.of("TestProcedure", 34)
                , Arguments.of("TestProcedureVAR", 34)
                , Arguments.of("TestFunction", 6)
                );
    }

    private static Stream<Arguments> convertTestInput() {
        return Stream.of(
                Arguments.of("HelloWorld")
                , Arguments.of("TestCase")
                , Arguments.of("TestFor")
                , Arguments.of("TestIf")
                , Arguments.of("TestWhile")
                , Arguments.of("TestProcedure")
                , Arguments.of("TestFunction")
                );
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
    @MethodSource("semanticTestInput")
    @DisplayName("Semantic Checks")
    public void TestSemanticChecks(String fileName, int errorCount) {
        logger.info("===== {} =====", fileName);

        var tree = parseProgram(fileName);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        assertDoesNotThrow(() -> {
            try (PrintStream ps = new PrintStream(baos, true, utf8)) {
                System.setOut(ps);
                semanticChecks(tree, errorCount);
                System.setOut(System.out);
            }
        });

        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()));
        File outputFile = getResourceFile("/output/" + fileName + ".txt");
        int breaks = checkResults(isr, outputFile);

        assertEquals(0, breaks);
        logger.info("===== END {} =====", fileName);
    }

    @ParameterizedTest(name = "Test {0}")
    @MethodSource("execTestInput")
    @DisplayName("Pascal Execution")
    public void TestPascalExecution(String fileName, int execCount) {
        logger.info("===== {} =====", fileName);

        var tree = parseProgram(fileName);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        int executionCount = assertDoesNotThrow(() -> {
            try (PrintStream ps = new PrintStream(baos, true, utf8)) {
                var programId = semanticChecks(tree, 0);
                System.setOut(ps);
                var pass3 = new Executor(programId);
                pass3.visit(tree);
                System.setOut(System.out);
                return pass3.getExecutionCount();
            }
        });

        assertEquals(execCount, executionCount, "Code execution count differences");

        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()));
        File outputFile = getResourceFile("/output/" + fileName + ".txt");
        int breaks = checkResults(isr, outputFile);

        assertEquals(0, breaks);
        logger.info("===== END {} =====", fileName);
    }

    @ParameterizedTest(name = "Test {0}")
    @MethodSource("convertTestInput")
    @DisplayName("Java Conversion")
    public void TestJavaConversion(String fileName) {
        logger.info("===== {} =====", fileName);

        var tree = parseProgram(fileName);
        semanticChecks(tree, 0);
        String objectCode = convertCode(tree);
        var method = compileJavaSource(fileName, objectCode);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        assertDoesNotThrow(() -> {
            try (PrintStream ps = new PrintStream(baos, true, utf8)) {
                System.setOut(ps);
                method.invoke(null, new Object[] { null });
                System.setOut(System.out);
            }
        }, "Error invoking java code");

        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()));
        File outputFile = getResourceFile("/output/" + fileName + ".txt");
        int breaks = checkResults(isr, outputFile);

        assertEquals(0, breaks, "Output line compare breaks");
        logger.info("===== END {} =====", fileName);
    }

    private ParseTree parseProgram(String fileName) {
        var syntaxErrorHandler = new SyntaxErrorHandler();
        var chars = getResourceFileCharStream(fileName);
        var ts = newTokenStream(chars, syntaxErrorHandler);
        var parser = newParser(ts, syntaxErrorHandler);
        var tree = parser.program();

        int errorCount = syntaxErrorHandler.getCount();
        assertEquals(0, errorCount, "Syntax errors found");

        return tree;
    }

    private SymTableEntry semanticChecks(ParseTree tree, int expectedErrors) {
        var semantics = assertDoesNotThrow(() -> {
            var pass2 = new Semantics(BackendMode.EXECUTOR);
            pass2.visit(tree);
            return pass2;
        }, "Error while running semantic checks");

        assertEquals(expectedErrors, semantics.getErrorCount(), "Semantic check failures");
        return semantics.getProgramId();
    }

    private CharStream getResourceFileCharStream(String fileName) {
        var chstrm = assertDoesNotThrow(() -> {
            File inputFile = getResourceFile("/input/" + fileName + ".pas");
            return CharStreams.fromStream(new FileInputStream(inputFile));
        }, "Error reading resource file");
        assertNotNull(chstrm);
        return chstrm;
    }

    private CommonTokenStream newTokenStream(CharStream cs, SyntaxErrorHandler eh) {
        PascalLexer lexer = new PascalLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener(eh);
        return new CommonTokenStream(lexer);
    }

    private PascalParser newParser(CommonTokenStream tokens, SyntaxErrorHandler eh) {
        PascalParser parser = new PascalParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(eh);
        return parser;
    }

    private int checkResults(InputStreamReader isr, File outputFile) {
        int retval = assertDoesNotThrow(() -> {
            int breaks = 0;
            try (BufferedReader reader1 = new BufferedReader(isr);
                    BufferedReader reader2 = Files.newBufferedReader(outputFile.toPath())) {
                String line1 = reader1.readLine();
                String line2 = reader2.readLine();

                int lineNumber = 1;
                while (line1 != null || line2 != null) {
                    if (line1 == null && !line2.isEmpty()) {
                        logger.info("Line " + lineNumber + " extra in file2: " + line2);
                        breaks += 1;
                    } else if (line2 == null && !line1.isEmpty()) {
                        logger.info("Line " + lineNumber + " missing in file2: " + line1);
                        breaks += 1;
                    } else if (!Objects.equals(line1, line2)) {
                        logger.info("Line " + lineNumber + " in file1: " + line1);
                        logger.info("Line " + lineNumber + " in file2: " + line2);
                        breaks += 1;
                    }
                    line1 = reader1.readLine();
                    line2 = reader2.readLine();
                    lineNumber++;
                }
            }
            return breaks;
        });

        return retval;
    }

    private String convertCode(ParseTree tree) {
        return assertDoesNotThrow(() -> {
            Converter pass3 = new Converter();
            return (String) pass3.visit(tree);
        }, "Error while running java converter");
    }

    private Method compileJavaSource(String className, String code) {
        Method method = assertDoesNotThrow(() -> {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(new File("target/classes")));

            JavaFileObject source = new JavaSourceFromString(className, code);

            compiler.getTask(null, fileManager, null, null,
                    null, List.of(source))
                    .call();

            fileManager.close();

            // Load the compiled class and call its main method
            Class<?> clazz = Class.forName(className);
            return clazz.getMethod("main", String[].class);
        }, "Error compiling java code");
        assertNotNull(method);
        return method;
    }

    private static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}