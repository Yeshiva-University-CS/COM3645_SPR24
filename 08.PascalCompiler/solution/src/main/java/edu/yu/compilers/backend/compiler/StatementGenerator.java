/**
 * <h1>StatementGenerator</h1>
 * <p>Emit code for executable statements.</p>
 * <p>Adapted from</p>
 * <p>Copyright (c) 2020 by Ronald Mak</p>
 */

package edu.yu.compilers.backend.compiler;

import antlr4.PascalParser;
import edu.yu.compilers.intermediate.symtable.Predefined;
import edu.yu.compilers.intermediate.symtable.SymTableEntry;
import edu.yu.compilers.intermediate.type.Typespec;
import edu.yu.compilers.intermediate.type.Typespec.Form;

import java.util.*;

import static edu.yu.compilers.backend.compiler.Instruction.*;
import static edu.yu.compilers.intermediate.type.Typespec.Form.ENUMERATION;
import static edu.yu.compilers.intermediate.type.Typespec.Form.SCALAR;

public class StatementGenerator extends CodeGenerator {
    /**
     * Constructor.
     *
     * @param parent   the parent generator.
     * @param compiler the compiler to use.
     */
    public StatementGenerator(CodeGenerator parent, Compiler compiler) {
        super(parent, compiler);
    }

    /**
     * Emit code for an assignment statement.
     *
     * @param ctx the AssignmentStatementContext.
     */
    public void emitAssignment(PascalParser.AssignmentStatementContext ctx) {
        PascalParser.VariableContext varCtx = ctx.lhs().variable();
        PascalParser.ExpressionContext exprCtx = ctx.rhs().expression();
        SymTableEntry varId = varCtx.entry;
        Typespec varType = varCtx.type;
        Typespec exprType = exprCtx.type;

        // The last modifier, if any, is the variable's last subscript or field.
        int modifierCount = varCtx.modifier().size();
        PascalParser.ModifierContext lastModCtx = modifierCount == 0 ? null : varCtx.modifier().get(modifierCount - 1);

        // The target variable has subscripts and/or fields.
        if (modifierCount > 0) {
            lastModCtx = varCtx.modifier().get(modifierCount - 1);
            compiler.visit(varCtx);
        }

        // Emit code to evaluate the expression.
        compiler.visit(exprCtx);

        // float variable := integer constant
        if ((varType == Predefined.realType) && (exprType.baseType() == Predefined.integerType))
            emit(I2F);

        // Emit code to store the expression value into the target variable.
        // The target variable has no subscripts or fields.
        if (lastModCtx == null)
            emitStoreValue(varId, varId.getType());

        // The target variable is a field.
        else if (lastModCtx.field() != null) {
            emitStoreValue(lastModCtx.field().entry, lastModCtx.field().type);
        }

        // The target variable is an array element.
        else {
            emitStoreValue(null, varType);
        }
    }

    /**
     * Emit code for an IF statement.
     *
     * @param ctx the IfStatementContext.
     */
    public void emitIf(PascalParser.IfStatementContext ctx) {
        /***** Complete this method. *****/
        Label nextLabel = new Label();
        compiler.visit(ctx.expression());
        if (ctx.ELSE() != null) {
            Label falseLabel = new Label();
            emit(IFEQ, falseLabel);
            compiler.visit(ctx.trueStatement());
            emit(GOTO, nextLabel);
            emitLabel(falseLabel);
            compiler.visit(ctx.falseStatement());
            emitLabel(nextLabel);
        } else {
            emit(IFEQ, nextLabel);
            compiler.visit(ctx.trueStatement());
        }
        emitLabel(nextLabel);
    }

    /**
     * Emit code for a CASE statement.
     *
     * @param ctx the CaseStatementContext.
     */
    public void emitCase(PascalParser.CaseStatementContext ctx) {
        compiler.visit(ctx.expression());
        // todo get hashcode if its string
        if (ctx.jumpTable.keySet().iterator().next() instanceof String) {
            emit(INVOKEVIRTUAL, "java/lang/String.hashCode()I");
        }

        emit(LOOKUPSWITCH);
        Map<PascalParser.StatementContext, Label> statementToLabel = new HashMap<>();
        for (PascalParser.StatementContext s : ctx.jumpTable.values()) {
            if (!statementToLabel.containsKey(s)) {
                Label l = new Label();
                statementToLabel.put(s, l);
            }
        }
        List<Integer> constantsSorted = new ArrayList<>();
        for (Object o : ctx.jumpTable.keySet()) {
            if (ctx.jumpTable.keySet().iterator().next() instanceof String) {
                constantsSorted.add(o.hashCode());
            } else if (ctx.jumpTable.keySet().iterator().next() instanceof Character) {
                constantsSorted.add((int) ((Character) o).charValue());
            } else {
                constantsSorted.add((int) o);
            }
        }
        Collections.sort(constantsSorted);
        Map<Integer, PascalParser.StatementContext> tempJumpTable = new HashMap<>();
        for (Object o : ctx.jumpTable.keySet()) {
            if (ctx.jumpTable.keySet().iterator().next() instanceof String) {
                tempJumpTable.put(o.hashCode(), ctx.jumpTable.get(o));
            } else if (ctx.jumpTable.keySet().iterator().next() instanceof Character) {
                tempJumpTable.put((int) ((Character) o).charValue(), ctx.jumpTable.get(o));
            } else {
                tempJumpTable.put((int) o, ctx.jumpTable.get(o));
            }
        }

        for (Integer i : constantsSorted) {
            emitLabel(i, statementToLabel.get(tempJumpTable.get(i)));
        }
        Label defaultLabel = new Label();
        emitLabel("default", defaultLabel);
        for (PascalParser.StatementContext s : statementToLabel.keySet()) {
            emitLabel(statementToLabel.get(s));
            compiler.visit(s);
            emit(GOTO, defaultLabel);
        }
        emitLabel(defaultLabel);

    }

    /**
     * Emit code for a REPEAT statement.
     *
     * @param ctx the RepeatStatementContext.
     */
    public void emitRepeat(PascalParser.RepeatStatementContext ctx) {
        Label loopTopLabel = new Label();
        Label loopExitLabel = new Label();

        emitLabel(loopTopLabel);

        compiler.visit(ctx.statementList());
        compiler.visit(ctx.expression());
        emit(IFNE, loopExitLabel);
        emit(GOTO, loopTopLabel);

        emitLabel(loopExitLabel);
    }

    /**
     * Emit code for a WHILE statement.
     *
     * @param ctx the WhileStatementContext.
     */
    public void emitWhile(PascalParser.WhileStatementContext ctx) {
        /***** Complete this method. *****/
        Label loopTopLabel = new Label();
        Label loopExitLabel = new Label();

        emitLabel(loopTopLabel);

        compiler.visit(ctx.expression());
        emit(IFEQ, loopExitLabel);
        compiler.visit(ctx.statement());
        emit(GOTO, loopTopLabel);

        emitLabel(loopExitLabel);
    }

    /**
     * Emit code for a FOR statement.
     *
     * @param ctx the ForStatementContext.
     */
    public void emitFor(PascalParser.ForStatementContext ctx) {
        /***** Complete this method. *****/
        emitForAssign(ctx);
        Label topLabel = new Label();
        // emit topLabel
        emitLabel(topLabel);
        // evaluate boolean
        // if TO VS DOWN
        emitLoadValue(ctx.variable().entry);
        compiler.visit(ctx.expression(1));
        Label continueLabel = new Label(); // L003
        if (ctx.TO() != null) {
            emit(IF_ICMPGT, continueLabel);
        } else {
            emit(IF_ICMPLT, continueLabel);
        }
        emit(ICONST_0);
        Label endForLabel = new Label();
        emit(GOTO, endForLabel);
        emitLabel(continueLabel);
        emit(ICONST_1);
        emitLabel(endForLabel);
        Label goodbyeForLoop = new Label();
        emit(IFNE, goodbyeForLoop);
        compiler.visit(ctx.statement());
        emitLoadValue(ctx.variable().entry);
        emit(ICONST_1);
        if (ctx.TO() != null) {
            emit(IADD);
        } else {
            emit(ISUB);
        }
        emitStoreValue(ctx.variable().entry, ctx.variable().type);
        emit(GOTO, topLabel);
        emitLabel(goodbyeForLoop);
    }

    private void emitForAssign(PascalParser.ForStatementContext ctx) {
        PascalParser.VariableContext varCtx = ctx.variable();
        PascalParser.ExpressionContext exprCtx = ctx.expression(0);
        SymTableEntry varId = varCtx.entry;
        Typespec varType = varCtx.type;
        Typespec exprType = exprCtx.type;

        // The last modifier, if any, is the variable's last subscript or field.
        int modifierCount = varCtx.modifier().size();
        PascalParser.ModifierContext lastModCtx = modifierCount == 0 ? null : varCtx.modifier().get(modifierCount - 1);

        // The target variable has subscripts and/or fields.
        if (modifierCount > 0) {
            lastModCtx = varCtx.modifier().get(modifierCount - 1);
            compiler.visit(varCtx);
        }

        // Emit code to evaluate the expression.
        compiler.visit(exprCtx);

        // float variable := integer constant
        if ((varType == Predefined.realType) && (exprType.baseType() == Predefined.integerType))
            emit(I2F);

        // Emit code to store the expression value into the target variable.
        // The target variable has no subscripts or fields.
        if (lastModCtx == null)
            emitStoreValue(varId, varId.getType());

        // The target variable is a field.
        else if (lastModCtx.field() != null) {
            emitStoreValue(lastModCtx.field().entry, lastModCtx.field().type);
        }

        // The target variable is an array element.
        else {
            emitStoreValue(null, varType);
        }
    }

    /**
     * Emit code for a procedure call statement.
     *
     * @param ctx the ProcedureCallStatementContext.
     */
    public void emitProcedureCall(PascalParser.ProcedureCallStatementContext ctx) {
        /***** Complete this method. *****/
        emitCall(ctx.procedureName().entry, ctx.argumentList());
    }

    /**
     * Emit code for a function call statement.
     *
     * @param ctx the FunctionCallContext.
     */
    public void emitFunctionCall(PascalParser.FunctionCallContext ctx) {
        /***** Complete this method. *****/
        emitCall(ctx.functionName().entry, ctx.argumentList());
    }

    /**
     * Emit a call to a procedure or a function.
     *
     * @param routineId  the routine name's symbol table entry.
     * @param argListCtx the ArgumentListContext.
     */
    private void emitCall(SymTableEntry routineId, PascalParser.ArgumentListContext argListCtx) {
        /***** Complete this method. *****/
        if (argListCtx != null) {
            for (int i = 0; i < routineId.getRoutineParameters().size(); i++) {
                PascalParser.ArgumentContext a = argListCtx.argument(i);
                compiler.visitExpression(a.expression());
                String argType = a.expression().type.getIdentifier().getName();
                String paramType = routineId.getRoutineParameters().get(i).getType().getIdentifier().getName();
                // If casting is required
                if (argType.equals("integer") && paramType.equals("real")) {
                    emit(I2F);
                }
            }
        }

        String routinePart = routineId.getName() + "(";

        for (SymTableEntry s : routineId.getRoutineParameters()) {
            String type = s.getType().getIdentifier().getName();
            switch (type) {
                case "integer" -> routinePart += "I";
                case "real" -> routinePart += "F";
                case "boolean" -> routinePart += "Z";
            }
        }
        routinePart += ")";

        if (routineId.getKind() == SymTableEntry.Kind.FUNCTION) {
            String type = routineId.getType().getIdentifier().getName();
            switch (type) {
                case "integer" -> routinePart += "I";
                case "real" -> routinePart += "F";
                case "boolean" -> routinePart += "Z";
            }
        } else {
            routinePart += "V";
        }
        // May need to do some casting... for arguments that don't match params

        emit(INVOKESTATIC, programName + "/" + routinePart);
    }

    /**
     * Emit code for a WRITE statement.
     *
     * @param ctx the WriteStatementContext.
     */
    public void emitWrite(PascalParser.WriteStatementContext ctx) {
        emitWrite(ctx.writeArguments(), false);
    }

    /**
     * Emit code for a WRITELN statement.
     *
     * @param ctx the WritelnStatementContext.
     */
    public void emitWriteln(PascalParser.WritelnStatementContext ctx) {
        emitWrite(ctx.writeArguments(), true);
    }

    /**
     * Emit code for a call to WRITE or WRITELN.
     *
     * @param argsCtx the WriteArgumentsContext.
     * @param needLF  true if you need a line feed.
     */
    private void emitWrite(PascalParser.WriteArgumentsContext argsCtx, boolean needLF) {
        emit(GETSTATIC, "java/lang/System/out", "Ljava/io/PrintStream;");

        // WRITELN with no arguments.
        if (argsCtx == null) {
            emit(INVOKEVIRTUAL, "java/io/PrintStream.println()V");
            localStack.decrease(1);
        }

        // Generate code for the arguments.
        else {
            StringBuffer format = new StringBuffer();
            int exprCount = createWriteFormat(argsCtx, format, needLF);

            // Load the format string.
            emit(LDC, format.toString());

            // Emit the arguments array.
            if (exprCount > 0) {
                emitArgumentsArray(argsCtx, exprCount);

                emit(INVOKEVIRTUAL,
                        "java/io/PrintStream/printf(Ljava/lang/String;[Ljava/lang/Object;)" + "Ljava/io/PrintStream;");
                localStack.decrease(2);
                emit(POP);
            } else {
                emit(INVOKEVIRTUAL, "java/io/PrintStream/print(Ljava/lang/String;)V");
                localStack.decrease(2);
            }
        }
    }

    /**
     * Create the printf format string.
     *
     * @param argsCtx the WriteArgumentsContext.
     * @param format  the format string to create.
     * @return the count of expression arguments.
     */
    private int createWriteFormat(PascalParser.WriteArgumentsContext argsCtx, StringBuffer format, boolean needLF) {
        int exprCount = 0;
        format.append("\"");

        // Loop over the arguments.
        for (PascalParser.WriteArgumentContext argCtx : argsCtx.writeArgument()) {
            Typespec type = argCtx.expression().type;
            String argText = argCtx.getText();

            // Append any literal strings.
            if (argText.charAt(0) == '\'') {
                format.append(convertString(argText));
            }

            // For any other expressions, append a field specifier.
            else {
                exprCount++;
                format.append("%");

                PascalParser.FieldWidthContext fwCtx = argCtx.fieldWidth();
                if (fwCtx != null) {
                    String sign = ((fwCtx.sign() != null) && (fwCtx.sign().getText().equals("-"))) ? "-" : "";
                    format.append(sign).append(fwCtx.integerConstant().getText());

                    PascalParser.DecimalPlacesContext dpCtx = fwCtx.decimalPlaces();
                    if (dpCtx != null) {
                        format.append(".").append(dpCtx.integerConstant().getText());
                    }
                }

                String typeFlag = type == Predefined.integerType ? "d"
                        : type == Predefined.realType ? "f"
                                : type == Predefined.booleanType ? "b" : type == Predefined.charType ? "c" : "s";
                format.append(typeFlag);
            }
        }

        format.append(needLF ? "\\n\"" : "\"");

        return exprCount;
    }

    /**
     * Emit the printf arguments array.
     *
     * @param argsCtx   write argument context
     * @param exprCount expression count
     */
    private void emitArgumentsArray(PascalParser.WriteArgumentsContext argsCtx, int exprCount) {
        // Create the arguments array.
        emitLoadConstant(exprCount);
        emit(ANEWARRAY, "java/lang/Object");

        int index = 0;

        // Loop over the arguments to fill the arguments array.
        for (PascalParser.WriteArgumentContext argCtx : argsCtx.writeArgument()) {
            String argText = argCtx.getText();
            PascalParser.ExpressionContext exprCtx = argCtx.expression();
            Typespec type = exprCtx.type.baseType();

            // Skip string constants, which were made part of
            // the format string.
            if (argText.charAt(0) != '\'') {
                emit(DUP);
                emitLoadConstant(index++);

                compiler.visit(exprCtx);

                Form form = type.getForm();
                if (((form == SCALAR) || (form == ENUMERATION)) && (type != Predefined.stringType)) {
                    emit(INVOKESTATIC, valueOfSignature(type));
                }

                // Store the value into the array.
                emit(AASTORE);
            }
        }
    }

    /**
     * Emit code for a READ statement.
     *
     * @param ctx the ReadStatementContext.
     */
    public void emitRead(PascalParser.ReadStatementContext ctx) {
        emitRead(ctx.readArguments(), false);
    }

    /**
     * Emit code for a READLN statement.
     *
     * @param ctx the ReadlnStatementContext.
     */
    public void emitReadln(PascalParser.ReadlnStatementContext ctx) {
        emitRead(ctx.readArguments(), true);
    }

    /**
     * Generate code for a call to READ or READLN.
     *
     * @param argsCtx  the ReadArgumentsContext.
     * @param needSkip true if you need to skip the rest of the input line.
     */
    private void emitRead(PascalParser.ReadArgumentsContext argsCtx, boolean needSkip) {
        int size = argsCtx.variable().size();

        // Loop over read arguments.
        for (int i = 0; i < size; i++) {
            PascalParser.VariableContext varCtx = argsCtx.variable().get(i);
            Typespec varType = varCtx.type;

            if (varType == Predefined.integerType) {
                emit(GETSTATIC, programName + "/_sysin Ljava/util/Scanner;");
                emit(INVOKEVIRTUAL, "java/util/Scanner/nextInt()I");
                emitStoreValue(varCtx.entry, null);
            } else if (varType == Predefined.realType) {
                emit(GETSTATIC, programName + "/_sysin Ljava/util/Scanner;");
                emit(INVOKEVIRTUAL, "java/util/Scanner/nextFloat()F");
                emitStoreValue(varCtx.entry, null);
            } else if (varType == Predefined.booleanType) {
                emit(GETSTATIC, programName + "/_sysin Ljava/util/Scanner;");
                emit(INVOKEVIRTUAL, "java/util/Scanner/nextBoolean()Z");
                emitStoreValue(varCtx.entry, null);
            } else if (varType == Predefined.charType) {
                emit(GETSTATIC, programName + "/_sysin Ljava/util/Scanner;");
                emit(LDC, "\"\"");
                emit(INVOKEVIRTUAL, "java/util/Scanner/useDelimiter(Ljava/lang/String;)" + "Ljava/util/Scanner;");
                emit(POP);
                emit(GETSTATIC, programName + "/_sysin Ljava/util/Scanner;");
                emit(INVOKEVIRTUAL, "java/util/Scanner/next()Ljava/lang/String;");
                emit(ICONST_0);
                emit(INVOKEVIRTUAL, "java/lang/String/charAt(I)C");
                emitStoreValue(varCtx.entry, null);

                emit(GETSTATIC, programName + "/_sysin Ljava/util/Scanner;");
                emit(INVOKEVIRTUAL, "java/util/Scanner/reset()Ljava/util/Scanner;");

            } else // string
            {
                emit(GETSTATIC, programName + "/_sysin Ljava/util/Scanner;");
                emit(INVOKEVIRTUAL, "java/util/Scanner/next()Ljava/lang/String;");
                emitStoreValue(varCtx.entry, null);
            }
        }

        // READLN: Skip the rest of the input line.
        if (needSkip) {
            emit(GETSTATIC, programName + "/_sysin Ljava/util/Scanner;");
            emit(INVOKEVIRTUAL, "java/util/Scanner/nextLine()Ljava/lang/String;");
            emit(POP);
        }
    }
}