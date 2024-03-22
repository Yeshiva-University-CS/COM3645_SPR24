package edu.yu.compilers;

import antlr4.PascalLexer;
import antlr4.PascalParser;
import edu.yu.compilers.backend.compiler.Compiler;
import edu.yu.compilers.backend.converter.Converter;
import edu.yu.compilers.backend.interpreter.Executor;
import edu.yu.compilers.frontend.Semantics;
import edu.yu.compilers.frontend.SyntaxErrorHandler;
import edu.yu.compilers.intermediate.symtable.SymTableEntry;
import edu.yu.compilers.intermediate.util.BackendMode;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static edu.yu.compilers.intermediate.util.BackendMode.*;

public class PascalCC {

    public static void main(String[] args) throws Exception {
        String usageMessageString = """
            USAGE: PascalCC <option> <sourceFileName>
            Options:
                -tokens
                -parse
                -symbols 
                -ast 
                -execute
                -convert
                -compile
            """;
                    
        if (args.length != 2) {
            System.out.println(usageMessageString);
            System.exit(-1);
        }

        String operation = args[0];
        String sourceFileName = args[1];

        if (invalidOperation(operation)) {
            System.out.println(usageMessageString);
            System.exit(-1);
        }

        SyntaxErrorHandler syntaxErrorHandler = new SyntaxErrorHandler();

        PascalLexer lexer = createLexer(sourceFileName, syntaxErrorHandler);
        if (lexer == null)
            System.exit(-1);

        if (operation.equals("-tokens")) {
            var tokenStream = new CommonTokenStream(lexer);
            tokenStream.fill();
            int errorCount = syntaxErrorHandler.getCount();
            System.out.printf("\nThere were %d lexical errors.\n", errorCount);
            printTokens(tokenStream.getTokens(), lexer.getVocabulary());
            System.exit(errorCount);
        }

        // Pass 1: Parse the Pascal source file.

        PascalParser parser = createParser(lexer, syntaxErrorHandler);
        ParseTree tree = parser.program();
        int errorCount = syntaxErrorHandler.getCount();

        if (operation.equals("-parse")) {
            System.out.printf("\nThere were %d syntax errors.\n", errorCount);
            printParseTree(parser, tree, 0);
            System.exit(errorCount);
        }

        // Pass 2: Semantic operations.

        BackendMode mode = determineBackendMode(operation);
        Semantics pass2 = new Semantics(mode);
        pass2.visit(tree);
        errorCount = pass2.getErrorCount();

        if (operation.equals("-symbols")) {
            System.out.printf("\nThere were %d semantic errors.\n", errorCount);
            pass2.printSymbolTableStack();
            System.exit(errorCount);
        }

        // Pass 3: Translation.

        switch (mode) {
            case EXECUTOR -> {
                // Pass 3: Execute the Pascal program.
                SymTableEntry programId = pass2.getProgramId();
                Executor pass3 = new Executor(programId);
                pass3.visit(tree);
            }
            case CONVERTER -> {
                // Convert from Pascal to Java.
                Converter pass3 = new Converter();
                String objectCode = (String) pass3.visit(tree);
                System.out.println(objectCode);
            }
            case COMPILER -> {
                // Pass 3: Compile the Pascal program.
                SymTableEntry programId = pass2.getProgramId();
                Compiler pass3 = new Compiler(programId.getName());
                String objectCode = (String) pass3.visit(tree);
                System.out.println(objectCode);
            }
        }
    }

    private static boolean invalidOperation(String operation) {
        var validOperations = Set.of("-tokens", "-parse", "-symbols", "-ast", "-execute", "-convert", "-compile");
        return !validOperations.contains(operation);
    }

    private static BackendMode determineBackendMode(String operation) {
        switch (operation) {
            case "-convert":
                return CONVERTER;
            case "-compile":
                return COMPILER;
            default:
                return EXECUTOR;
        }
    }

    private static PascalLexer createLexer(String sourceFileName, SyntaxErrorHandler syntaxErrorHandler) {
        try {
            var lexer = new PascalLexer(CharStreams.fromFileName(sourceFileName));
            lexer.addErrorListener(syntaxErrorHandler);
            return lexer;
        } catch (IOException e) {
            System.out.println("Source file error: " + sourceFileName);
            return null;
        }
    }

    private static PascalParser createParser(PascalLexer lexer, SyntaxErrorHandler syntaxErrorHandler) {
        PascalParser parser = new PascalParser(new CommonTokenStream(lexer));
        parser.addErrorListener(syntaxErrorHandler);
        return parser;
    }

    protected static void printTokens(List<Token> tokens, Vocabulary vocabulary) {
        System.out.println("Tokens:");
        System.out.println();

        for (var token : tokens) {
            var symbolicName = vocabulary.getSymbolicName(token.getType());
            var tokenText = token.getText();

            if (symbolicName == null) {
                System.out.printf("%14s : '%s'\n", "", tokenText);
            } else if (symbolicName.equals("STRING")) {
                // Strip the single quotes from the string.
                var string = tokenText.substring(1, tokenText.length() - 1);

                // Unescape embedded single quotes
                string = string.replace("''", "'");

                // print out the token with double quotes around the strings
                System.out.printf("%14s : \"%s\"\n", symbolicName, string);
            } else if (symbolicName.equals("CHARACTER")) {
                // Length will be 3, unless it is an escaped single quote
                if (tokenText.length() == 3) {
                    System.out.printf("%14s : '%s'\n", symbolicName, tokenText.charAt(1));
                } else {
                    System.out.printf("%14s : '\\%s'\n", symbolicName, tokenText.charAt(2));
                }
            } else {
                if (symbolicName.equals("ERROR"))
                    System.out.printf("TOKEN ERROR at line %d: Invalid token at '%s'\n", token.getLine(), tokenText);

                System.out.printf("%14s : %s\n", symbolicName, tokenText);
            }
        }
    }

    protected static void printParseTree(PascalParser parser, ParseTree node, int level) {
        // Indentation
        for (int i = 0; i < level; ++i) {
            System.out.print("  ");
        }

        if (node instanceof ParserRuleContext) {
            // It's a rule context
            ParserRuleContext context = (ParserRuleContext) node;
            String ruleName = parser.getRuleNames()[context.getRuleIndex()];
            System.out.println(ruleName);

            // Recursively print all children
            for (int i = 0; i < node.getChildCount(); ++i) {
                printParseTree(parser, node.getChild(i), level + 1);
            }
        } else if (node.getChildCount() == 0) {
            // It's a leaf node (token)
            System.out.println("'" + node.getText() + "'");
        }
    }

}
