package edu.yu.compilers;

import edu.yu.compilers.frontend.Parser;
import edu.yu.compilers.frontend.Scanner;
import edu.yu.compilers.frontend.Source;
import edu.yu.compilers.frontend.Token;
import edu.yu.compilers.intermediate.Node;
import edu.yu.compilers.intermediate.ParseTreePrinter;
import edu.yu.compilers.intermediate.SymTable;

import static edu.yu.compilers.frontend.Token.TokenType.END_OF_FILE;

public class Simple {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: Simple -{scan | parse } <sourceFileName>");
            System.exit(-1);
        }

        String operation      = args[0];  // -scan, -parse, or -execute
        String sourceFileName = args[1];

        Source source = new Source(sourceFileName);

        if (operation.equalsIgnoreCase("-scan"))
        {
            testScanner(source);
        }
        else if (operation.equalsIgnoreCase("-parse"))
        {
            Parser parser = new Parser(new Scanner(source), new SymTable());
            testParser(parser);
        }
        else {
            System.out.println("Invalid operation: " + operation);
            System.out.println("Usage: Simple -{scan | parse } <sourceFileName>");
            System.exit(-1);
        }
    }

    /**
     * Test the scanner.
     *
     * @param source the input source.
     */
    protected static void testScanner(Source source) {
        System.out.println("Tokens:");
        System.out.println();

        Scanner scanner = new Scanner(source);  // create the scanner

        // Loop to extract and print each token from the source one at a time.
        for (Token token = scanner.nextToken(); token.type != END_OF_FILE; token = scanner.nextToken()) {
            System.out.printf("%14s : %s\n", token.type, token.text);
        }
    }

    /**
     * Test the parser.
     * @param scanner the scanner.
     * @param symTable the symbol table.
     */
    protected static int testParser(Parser parser)
    {
        Node programNode = parser.parseProgram();     // and parse the program
        int errorCount = parser.errorCount();

        System.out.println("There were " + errorCount + " syntax errors.");

        // If no errors, print the parse tree.
        if (errorCount == 0)
        {
            System.out.println("Parse tree:");
            ParseTreePrinter printer = new ParseTreePrinter();
            printer.print(programNode);
        }

        return errorCount;
    }

}
