package com.interpreters.lox;

import java.util.ArrayList;
import java.util.List;


public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private static class ParseError extends RuntimeException {}

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while(isNotAtEnd()){
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if(match(TokenType.VAR)) return varDeclaration();
            return statement();
        } catch (ParseError err) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name  =  consume(TokenType.VAR,"Expected a variable name");

        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

       consume(TokenType.SEMICOLON, "Expected a ',' after a variable declaration");
       return new Stmt.Var(name,initializer);
    }
    
    private Expr expression() {
        return  assignment();
    }


    private Expr assignment(){
        Expr  expr = equality();

        if ( match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable variable) {
                Token name = variable.name;
                return new Expr.Assign(name, value);
            }
            throw error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Stmt statement(){
        if(match(TokenType.PRINT)) return printStatement();
        if(match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON,"Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement(){
        Expr value = expression();
        consume(TokenType.SEMICOLON,"Expect ';' after expression.");
        return new Stmt.Expression(value);
    }


    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();

        while(!check(TokenType.RIGHT_BRACE) && isNotAtEnd()){
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }
    
    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.BANG_EQUAL,TokenType.EQUAL_EQUAL)){
            Token operator = previous();
            Expr right =  comparison();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

    private Expr comparison(){
        Expr expr = term();
        while (match(TokenType.GREATER,TokenType.GREATER_EQUAL,TokenType.LESS,TokenType.LESS_EQUAL)){
            Token operator = previous();
            Expr right  = term();
            expr =  new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

    private  Expr term() {
        Expr expr =  factor();
        while(match(TokenType.MINUS,TokenType.PLUS)){
            Token operator = previous();
            Expr right = factor();
            expr =  new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }


    private  Expr unary(){
        if (match(TokenType.BANG,TokenType.MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator,right);
        }
        return primary();
    }


    private Expr primary(){
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);
        if (match(TokenType.IDENTIFIER)) return new Expr.Variable(previous());
        if (match(TokenType.NUMBER,TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }
        throw error(peek(),"Expect expression.");
    }



    private void synchronize(){
        advance();
        while(isNotAtEnd()){
            if (previous().type == TokenType.SEMICOLON) return;
            switch (peek().type){
                case CLASS,FUN,VAR,FOR,IF,WHILE,PRINT,RETURN -> {
                    return;
                }
            }
        }
        advance();
    }

    private Token consume(TokenType type, String message){
        // TODO: add start column and end column
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token.line,message);
        return new ParseError();
    }

    private Boolean match(TokenType... types) {
        for(TokenType type: types){
            if(check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token advance(){
        if (isNotAtEnd()) current++;
        return previous();
    }

    private boolean isNotAtEnd(){
        return peek().type != TokenType.EOF;
    }


    private Token peek() {
        return tokens.get(current);
    }
    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean check(TokenType type) {
        if(isNotAtEnd()) return  false;
        return  peek().type  == type;
    }

}
