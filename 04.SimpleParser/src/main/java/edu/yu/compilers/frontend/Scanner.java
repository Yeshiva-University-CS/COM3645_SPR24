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
        // Skip blanks, comments, and other whitespace characters.
        char ch = nextNonBlankCharacter();
        
        if (Character.isLetter(ch))     return Token.word(ch, source);
        else if (Character.isDigit(ch)) return Token.number(ch, source);
        else if (ch == '\'')            return Token.characterOrString(ch, source);
        else                            return Token.specialSymbol(ch, source);
    }
    
    /**
     * Skip blanks, comments, and other whitespace characters
     * and return the next nonblank character.
     * @return the next nonblank character.
     */
    private char nextNonBlankCharacter()
    {
        char ch = source.currentChar();
        
        while ((ch == '{') || Character.isWhitespace(ch))
        {
            if (ch == '{')
            {
                // Consume characters of the comment.
                while ((ch != '}') && (ch != Source.EOF)) ch = source.nextChar();
            }
            
            ch = source.nextChar();  // consume character
        }
        
        return ch;  // nonblank character
    }
}
