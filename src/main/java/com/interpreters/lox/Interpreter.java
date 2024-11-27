package com.interpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private static final Map<TokenType, BiFunction<Double, Double, Object>> ARITHMETIC_OPERATORS = new HashMap<>();
    private static final Map<TokenType, BiFunction<Double, Double, Boolean>> COMPARISON_OPERATORS = new HashMap<>();
    private Environment environment = new Environment(null);
     
    static {
        // Initialize arithmetic operators
        ARITHMETIC_OPERATORS.put(TokenType.MINUS, (a, b) -> a - b);
        ARITHMETIC_OPERATORS.put(TokenType.SLASH, (a, b) -> a / b);
        ARITHMETIC_OPERATORS.put(TokenType.STAR, (a, b) -> a * b);
        ARITHMETIC_OPERATORS.put(TokenType.MODULUS, (a, b) -> a % b);

        // Initialize comparison operators
        COMPARISON_OPERATORS.put(TokenType.GREATER_EQUAL, (a, b) -> a >= b);
        COMPARISON_OPERATORS.put(TokenType.LESS_EQUAL, (a, b) -> a <= b);
        COMPARISON_OPERATORS.put(TokenType.GREATER, (a, b) -> a > b);
        COMPARISON_OPERATORS.put(TokenType.LESS, (a, b) -> a < b);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        // Handle special cases first
        if (expr.operator.type == TokenType.PLUS) {
            return handlePlusOperator(expr.operator, left, right);
        }
        if (expr.operator.type == TokenType.BANG_EQUAL) {
            checkNumberOperands(expr.operator, left, right);
            return !isEqual(left, right);
        }
        if (expr.operator.type == TokenType.EQUAL_EQUAL) {
            checkNumberOperands(expr.operator, left, right);
            return isEqual(left, right);
        }

        // Handle arithmetic operations
        if (ARITHMETIC_OPERATORS.containsKey(expr.operator.type)) {
            checkNumberOperands(expr.operator, left, right);
            return ARITHMETIC_OPERATORS.get(expr.operator.type)
                    .apply((Double) left, (Double) right);
        }

        // Handle comparison operations
        if (COMPARISON_OPERATORS.containsKey(expr.operator.type)) {
            checkNumberOperands(expr.operator, left, right);
            return COMPARISON_OPERATORS.get(expr.operator.type)
                    .apply((Double) left, (Double) right);
        }

        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                yield -(double) right;
            }
            case BANG -> {yield !isTruthy(right);}
            default -> right;
        };
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Boolean condition = (Boolean) evaluate(expr.expression);
        return condition ?
                evaluate(expr.truth_side) :
                evaluate(expr.false_side);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return  environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr);
        environment.assign(expr.name, value);
        return value;
    }

    private Object handlePlusOperator(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return (Double) left + (Double) right;
        }
        if (left instanceof String && right instanceof String) {
            return (String) left + (String) right;
        }
        // support instance where S + N or  N + S
        if (left instanceof String && right instanceof Double) {
            return (String) left + stringify(right);
        }
        if (left instanceof  Double && right instanceof String ) {
            return stringify(left) + (String) right;
        }
        throw new RuntimeError(operator, "Operands must be two numbers or two strings");
    }

    private Boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null) return false;
        return left.equals(right);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    Object evaluate(Expr expression) {
        return expression.accept(this);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers");
    }

    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2); // truncate for whole number
            }
            return text;
        }
        return object.toString();
    }

    private void execute(Stmt statement){
      statement.accept(this);
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
       Object value = evaluate(stmt.expression);
       System.out.println(stringify(value));
       return null;
    }


    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        // store a value in global variable environment 
        // or store the result after evaluation 
        Object value =  null; // supports var _name; <=> {token: "_name", value=}
        if (stmt.initializer != null){
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements,
                      Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }
}