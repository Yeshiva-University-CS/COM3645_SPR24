/**
 * Adapted from
 * Executor class for a simple interpreter.
 * (c) 2020 by Ronald Mak
 */
package edu.yu.compilers.backend;

import edu.yu.compilers.intermediate.Node;
import edu.yu.compilers.intermediate.SymTableEntry;

import java.util.ArrayList;
import java.util.HashSet;

import static edu.yu.compilers.intermediate.Node.NodeType.*;

public class Executor {
    private static final HashSet<Node.NodeType> singletons;
    private static final HashSet<Node.NodeType> relationals;

    static {
        singletons = new HashSet<>();
        relationals = new HashSet<>();

        singletons.add(VARIABLE);
        singletons.add(INTEGER_CONSTANT);
        singletons.add(REAL_CONSTANT);
        singletons.add(STRING_CONSTANT);

        relationals.add(EQ);
        relationals.add(LT);
    }

    private int lineNumber;

    public Executor() {
    }

    public Object visit(Node node) {
        return switch (node.type) {
            case PROGRAM -> visitProgram(node);
            case COMPOUND, ASSIGN, LOOP, WRITE, WRITELN -> visitStatement(node);
            case TEST -> visitTest(node);
            default -> visitExpression(node);
        };
    }

    private Object visitProgram(Node programNode) {
        Node compoundNode = programNode.children.get(0);
        return visit(compoundNode);
    }

    private Object visitStatement(Node statementNode) {
        lineNumber = statementNode.lineNumber;

        return switch (statementNode.type) {
            case COMPOUND -> visitCompound(statementNode);
            case ASSIGN -> visitAssign(statementNode);
            case LOOP -> visitLoop(statementNode);
            case WRITE -> visitWrite(statementNode);
            case WRITELN -> visitWriteln(statementNode);
            default -> null;
        };
    }

    private Object visitCompound(Node compoundNode) {
        for (Node statementNode : compoundNode.children) visit(statementNode);

        return null;
    }

    private Object visitAssign(Node assignNode) {
        Node lhs = assignNode.children.get(0);
        Node rhs = assignNode.children.get(1);

        // Evaluate the right-hand-side expression;
        Double value = (Double) visit(rhs);

        // Store the value into the variable's symbol table entry.
        SymTableEntry variableId = lhs.entry;
        variableId.setValue(value);

        return null;
    }

    private Object visitLoop(Node loopNode) {
        boolean b = false;
        do {
            for (Node node : loopNode.children) {
                Object value = visit(node);  // statement or test

                // Evaluate the test condition. Stop looping if true.
                b = (node.type == TEST) && ((boolean) value);
                if (b) break;
            }
        } while (!b);

        return null;
    }

    private Object visitTest(Node testNode) {
        return visit(testNode.children.get(0));
    }

    private Object visitWrite(Node writeNode) {
        printValue(writeNode.children);
        return null;
    }

    private Object visitWriteln(Node writelnNode) {
        if (writelnNode.children.size() > 0) printValue(writelnNode.children);
        System.out.println();

        return null;
    }

    private void printValue(ArrayList<Node> children) {
        long fieldWidth = -1;
        long decimalPlaces = 0;

        // Use any specified field width and count of decimal places.
        if (children.size() > 1) {
            double fw = (Double) visit(children.get(1));
            fieldWidth = (long) fw;

            if (children.size() > 2) {
                double dp = (Double) visit(children.get(2));
                decimalPlaces = (long) dp;
            }
        }

        // Print the value with a format.
        Node valueNode = children.get(0);
        String format = "%";
        if (valueNode.type == VARIABLE) {
            if (fieldWidth >= 0) format += fieldWidth;
            if (decimalPlaces >= 0) format += "." + decimalPlaces;
            format += "f";

            Double value = (Double) visit(valueNode);
            System.out.printf(format, value);
        } else  // node type STRING_CONSTANT
        {
            if (fieldWidth > 0) format += fieldWidth;
            format += "s";

            String value = (String) visit(valueNode);
            System.out.printf(format, value);
        }
    }

    private Object visitExpression(Node expressionNode) {
        // Single-operand expressions.
        if (singletons.contains(expressionNode.type)) {
            return switch (expressionNode.type) {
                case VARIABLE -> visitVariable(expressionNode);
                case INTEGER_CONSTANT -> visitIntegerConstant(expressionNode);
                case REAL_CONSTANT -> visitRealConstant(expressionNode);
                case STRING_CONSTANT -> visitStringConstant(expressionNode);
                default -> null;
            };
        }

        // Binary expressions.
        double value1 = (Double) visit(expressionNode.children.get(0));
        double value2 = (Double) visit(expressionNode.children.get(1));

        // Relational expressions.
        if (relationals.contains(expressionNode.type)) {
            boolean value = false;

            switch (expressionNode.type) {
                case EQ -> value = value1 == value2;
                case LT -> value = value1 < value2;
                default -> {
                }
            }

            return value;
        }

        double value = 0.0;

        // Arithmetic expressions.
        switch (expressionNode.type) {
            case ADD -> value = value1 + value2;
            case SUBTRACT -> value = value1 - value2;
            case MULTIPLY -> value = value1 * value2;
            case DIVIDE -> {
                if (value2 != 0.0) value = value1 / value2;
                else {
                    runtimeError(expressionNode, "Division by zero");
                    return 0.0;
                }

            }
            default -> {
            }
        }

        return value;
    }

    private Object visitVariable(Node variableNode) {
        // Obtain the variable's value from its symbol table entry.
        SymTableEntry variableId = variableNode.entry;

        return variableId.getValue();
    }

    private Object visitIntegerConstant(Node integerConstantNode) {
        long value = (Long) integerConstantNode.value;
        return (double) value;
    }

    private Object visitRealConstant(Node realConstantNode) {
        return realConstantNode.value;
    }

    private Object visitStringConstant(Node stringConstantNode) {
        return stringConstantNode.value;
    }

    private void runtimeError(Node node, String message) {
        System.out.printf("RUNTIME ERROR at line %d: %s: %s\n", lineNumber, message, node.text);
        System.exit(-2);
    }
}
