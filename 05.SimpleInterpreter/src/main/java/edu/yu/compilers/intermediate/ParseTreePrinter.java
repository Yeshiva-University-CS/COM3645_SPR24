/**
 * Adapted from
 * Parse tree printer class for a simple interpreter.
 * (c) 2020 by Ronald Mak
 */
package edu.yu.compilers.intermediate;

import java.util.ArrayList;

import static edu.yu.compilers.intermediate.Node.NodeType.*;

public class ParseTreePrinter
{
    private static final String INDENT_SIZE = "    ";

    private String indentation;  // indentation of a line
    private final StringBuilder line;  // output line

    /**
     * Constructor
     */
    public ParseTreePrinter()
    {
        this.indentation = "";
        this.line = new StringBuilder();
    }

    /**
     * Print a parse tree.
     * @param node the parse tree's root node.
     */
    public void print(Node node)
    {
        // Opening tag.
        line.append(indentation).append("<").append(node.type);
        
        // Attributes.
        if      (node.type == PROGRAM)          line.append(" ").append(node.text);
        else if (node.type == VARIABLE)         line.append(" ").append(node.text);
        else if (node.type == INTEGER_CONSTANT) line.append(" ").append((long) node.value);
        else if (node.type == REAL_CONSTANT)    line.append(" ").append(node.value);
        else if (node.type == STRING_CONSTANT)  line.append(" '").append(node.value).append("'");
        if (node.lineNumber > 0)                line.append(" line ").append(node.lineNumber);

        // Print the node's children followed by the closing tag.
        ArrayList<Node> children = node.children;
        if ((children != null) && (children.size() > 0)) 
        {
            line.append(">");
            printLine();

            printChildren(children);
            line.append(indentation); line.append("</").append(node.type).append(">");
        }

        // No children: Close off the tag.
        else line.append(" />");

        printLine();
    }

    /**
     * Print a parse tree node's child nodes.
     * @param children the array list of child nodes.
     */
    private void printChildren(ArrayList<Node> children)
    {
        String saveIndentation = indentation;
        indentation += INDENT_SIZE;
        for (Node child : children) print(child);
        indentation = saveIndentation;
    }

    /**
     * Print an output line.
     */
    private void printLine()
    {
        System.out.println(line);
        line.setLength(0);
    }
}
