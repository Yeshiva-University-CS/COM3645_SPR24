package edu.yu.compilers;

import edu.yu.compilers.frontend.*;

import static edu.yu.compilers.frontend.Token.TokenType.END_OF_FILE;

public class Simple {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: Simple <sourceFileName>");
            System.exit(-1);
        }

        String sourceFileName = args[0];
        Source source = new Source(sourceFileName);
        testScanner(source);
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
}
