/**
 * Adapted from
 * Parse tree node class for a simple interpreter.
 * (c) 2020 by Ronald Mak
 */
package edu.yu.compilers.intermediate;

import java.util.ArrayList;

public class Node
{
    public enum NodeType
    {
        PROGRAM, COMPOUND, ASSIGN, LOOP, TEST, WRITE, WRITELN,
        ADD, SUBTRACT, MULTIPLY, DIVIDE,
        EQ, LT,
        VARIABLE, INTEGER_CONSTANT, REAL_CONSTANT, STRING_CONSTANT
    }

    public NodeType type;
    public int lineNumber;
    public String text;
    public SymTableEntry entry;
    public Object value;
    public ArrayList<Node> children;
    
    /**
     * Constructor
     * @param type node type.
     */
    public Node(NodeType type)
    {
        this.type = type;
        this.lineNumber = 0;
        this.text = null;
        this.entry = null;
        this.value = null;
        this.children = new ArrayList<Node>();
    }
    
    /**
     * Adopt a child node.
     * @param child the child node.
     */
    public void adopt(Node child) { children.add(child); }
    
    /**
     * Make a copy of this node, but not the children.
     * @return the copy.
     */
    public Node copy()
    {
        Node copyNode = new Node(type);
        copyNode.lineNumber = lineNumber;
        copyNode.text = text;
        copyNode.entry = entry;
        copyNode.value = value;
        copyNode.children = new ArrayList<Node>();
        
        return copyNode;
    }
}
