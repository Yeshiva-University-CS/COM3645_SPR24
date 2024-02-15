package edu.yu.compilers;

import antlr4.Pcl4Lexer;
import antlr4.Pcl4Parser;
import edu.yu.compilers.backend.interpreter.Executor;
import edu.yu.compilers.intermediate.ParseTreePrinter;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.IOException;

public class Pcl4 {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: Pcl4 -{parse | execute} <sourceFileName>");
            System.exit(-1);
        }

        String operation = args[0]; // -parse, -execute
        String sourceFileName = args[1];

        // Parse the source program.
        ParseTree tree = parseProgram(sourceFileName);

        if (operation.equalsIgnoreCase("-parse")) {
            testParser(tree);

        } else if (operation.equalsIgnoreCase("-execute")) {
            executeProgram(tree);

        } else {
            System.out.println("Invalid operation: " + operation);
            System.out.println("Usage: Simple -{parse | execute} <sourceFileName>");
            System.exit(-1);
        }
    }

    /**
     * Parse the source program.
     *
     * @param sourceFileName the name of the source file.
     * @return the parse tree root.
     */
    protected static ParseTree parseProgram(String sourceFileName) {
        CharStream chars = null; // character stream
        try {
            // Create the character stream from the input stream
            chars = CharStreams.fromStream(new FileInputStream(sourceFileName));
        } catch (IOException ex) {
            System.out.println("Source file error: " + sourceFileName);
            System.exit(-1);
        }

        // Create a lexer which scans the character stream
        // to create a token stream.

        // Create a parser which parses the token stream
        // to create a parse tree.

        var lexer = new Pcl4Lexer(chars);
        var tokens = new CommonTokenStream(lexer);
        var parser = new Pcl4Parser(tokens);
        return parser.program();
    }

    /**
     * Parse the source program.
     *
     * @param tree the parse tree root.
     */
    protected static void testParser(ParseTree tree) {
        System.out.println(new ParseTreePrinter().visit(tree));
    }

    /**
     * Execute the source program.
     *
     * @param tree the parse tree root.
     */
    protected static void executeProgram(ParseTree tree) {
        Executor executor = new Executor();
        executor.visit(tree);
    }
}
