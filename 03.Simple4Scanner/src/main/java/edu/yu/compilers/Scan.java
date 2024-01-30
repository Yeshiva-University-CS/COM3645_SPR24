package edu.yu.compilers;

import antlr4.Simple4Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Scan {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: Scan sourceFileName");
            System.exit(-1);
        }

        String sourceFileName = args[0];

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

        var lexer = new Simple4Lexer(chars);

        // Test the scanner.
        testScanner(lexer);
    }

    /**
     * Test the scanner.
     *
     * @param lexer the lexer to test.
     */
    protected static void testScanner(Simple4Lexer lexer) {
        System.out.println("Tokens:");
        System.out.println();

        var vocabulary = lexer.getVocabulary();
        var tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();

        for (var token : tokenStream.getTokens()) {
            var symbolicName = vocabulary.getSymbolicName(token.getType());
            var tokenText = token.getText();

            if (symbolicName.equals("STRING")) {
                // Strip the single quotes from the string.
                var string = tokenText.substring(1, tokenText.length()-1);

                // Unescape embedded single quotes
                string = string.replace("''", "'");

                // print out the token with double quotes around the strings
                System.out.printf("%14s : \"%s\"\n", symbolicName, string);
            }
            else if (symbolicName.equals("CHARACTER")) {
                // Length will be 3, unless it is an escaped single quote
                if (tokenText.length() == 3) {
                    System.out.printf("%14s : '%s'\n", symbolicName, tokenText.charAt(1));
                }
                else {
                    System.out.printf("%14s : '\\%s'\n", symbolicName, tokenText.charAt(2));
                }
            }
            else {
                if (symbolicName.equals("ERROR"))
                    System.out.printf("TOKEN ERROR at line %d: Invalid token at '%s'\n", token.getLine(), tokenText);
        
                System.out.printf("%14s : %s\n", symbolicName, tokenText);
            }
        }
    }
}
