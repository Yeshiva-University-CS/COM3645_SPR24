package edu.yu.compilers.intermediate;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class ParseTreePrinter extends AbstractParseTreeVisitor<Void> {

    private int depth = 0;

    private void printIndent() {
        for (int i = 0; i < depth; i++) {
            System.out.print("  ");
        }
    }

    @Override
    public Void visit(ParseTree tree) {
        if (tree instanceof TerminalNode) {
            TerminalNode node = (TerminalNode) tree;
            printIndent();
            System.out.println("<terminal type=\"" + node.getSymbol().getType() +
                    "\" text=\"" + node.getText() + "\"/>");
        } else if (tree instanceof ParserRuleContext) {
            ParserRuleContext ctx = (ParserRuleContext) tree;
            String nodeName = ctx.getClass().getSimpleName().replaceAll("Context$", "");
            printIndent();
            System.out.println("<" + nodeName + ">");
            depth++;
            for (int i = 0; i < ctx.getChildCount(); i++) {
                visit(ctx.getChild(i));
            }
            depth--;
            printIndent();
            System.out.println("</" + nodeName + ">");
        }
        return null;
    }
}
