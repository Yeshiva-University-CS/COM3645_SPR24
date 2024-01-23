/**
 * Adapted from
 * Scanner class for a simple interpreter.
 * (c) 2020 by Ronald Mak
 */
package edu.yu.compilers.frontend;

public class Scanner
{
    private final Source source;
    
    /**
     * Constructor.
     * @param source the input source.
     */
    public Scanner(Source source)
    {
        this.source = source;
    }
    
    /**
     * Extract the next token from the source.
     * @return the token.
     */
    public Token nextToken()
    {
        char ch = source.currentChar();
        
        // Skip blanks and other whitespace characters.
        while (Character.isWhitespace(ch)) ch = source.nextChar();
        
        if (Character.isLetter(ch))     return Token.word(ch, source);
        else if (Character.isDigit(ch)) return Token.number(ch, source);
        else if (ch == '\'')            return Token.string(ch, source);
        else                            return Token.specialSymbol(ch, source);
    }
}
