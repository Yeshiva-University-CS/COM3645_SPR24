package edu.yu.compilers;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import antlr4.ExprLexer;
import antlr4.ExprParser;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String sourceFileName = args[0];
        FileInputStream source = null;  // input stream
        CharStream chars = null;        // character stream

        try {
            // Create the input stream.
            source = new FileInputStream(sourceFileName);

            // Create the character stream from the input stream.
            chars = CharStreams.fromStream(source);
        } catch (IOException ex) {
            System.out.println("Source file error: " + sourceFileName);
            System.exit(-1);
        }

        // Create a lexer which scans the character stream
        // to create a token stream.
        ExprLexer lexer = new ExprLexer(chars);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Print the token stream.
        System.out.println("Tokens:");
        tokens.fill();
        for (Token token : tokens.getTokens()) {
            System.out.println(token.toString());
        }

        // Create a parser which parses the token stream
        // to create a parse tree.
        ExprParser parser = new ExprParser(tokens);
        ParseTree tree = parser.program();

        // Print the parse tree in Lisp format.
        System.out.println("\nParse tree (Lisp format):");
        System.out.println(tree.toStringTree(parser));
    }
}