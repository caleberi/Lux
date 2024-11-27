package com.interpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private int start  = 0;
    private int current  = 0;
    private int line  =  1;
    private final List<Token> tokens =   new  ArrayList<>();

    private static final Map<String,TokenType> keywords;

    private interface Yielder {
        int yield();

        void reset();
    }

    private Yielder getIdx() {
        return  new Yielder(){
            int state = current;
            @Override
            public int yield() {
                return state++;
            }

            @Override
            public void reset() {
                state = current;
            }
        };
    }

    static  {
        keywords = new HashMap<>();
        keywords.put("and",TokenType.AND);
        keywords.put("or",TokenType.OR);
        keywords.put("class",TokenType.CLASS);
        keywords.put("else",TokenType.ELSE);
        keywords.put("false",TokenType.FALSE);
        keywords.put("for",TokenType.FOR);
        keywords.put("fx",TokenType.FUN);
        keywords.put("if",TokenType.IF);
        keywords.put("nil",TokenType.NIL);
        keywords.put("print",TokenType.PRINT);
        keywords.put("return",TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this",TokenType.THIS);
        keywords.put("true",TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while",TokenType.WHILE);
    }

    Scanner (String source) {
        this.source =  source;
    }

    /* `scanTokens` consume each character till EOF
    * creating a token on discovery and ending it with EOF token
    */
    List<Token> scanTokens() {
        while (!this.isAtEnd()){
            start = current;
            scanToken();
        }
        int fileEndOffset = source.length();
        tokens.add(new Token(TokenType.EOF,"",null,line,fileEndOffset-1, fileEndOffset));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= this.source.length();
    }

    private void scanToken(){
        char c =  advance();
        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '*' -> addToken(TokenType.STAR);
            case '%' -> addToken(TokenType.MODULUS);
            case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
            case '=' -> addToken(match( '=' )? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '<' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '>' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '?' -> {
                while (lookAhead() != ':' && !isAtEnd()) {}
                if (isAtEnd())  { Lox.error(line, "Unexpected character.");}
                addToken(TokenType.QUESTION);
            }
            case ':' -> addToken(TokenType.COLON);
            case '/' -> {
                if(match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                    return;
                }
                if (match('*')) {
                    while (peek() != '*') advance();
                    advance();
                    if (!match('/')){Lox.error(line, "Unexpected character.");}
                    return;
                }
                addToken(TokenType.SLASH);
            }
            case ' ', '\t', '\r' -> {}
            case '\n'-> {
                line++;
            }
            case '"'-> {
                string();
            }
            default -> {
                if(isDigit(c)){number();}
                else if (isAlpha(c)) {identifier();}
                else {
                    Lox.error(line, "Unexpected character.");
                }
            }
        }
    }

    // consumes an identifier 
    private void identifier() {
        while(isAlphanumeric(peek())) advance();
        String text = source.substring(start,current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    // assert if a char is alphanumeric
    private boolean isAlphanumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    // assert if a char is alphabetic
    private boolean isAlpha(char c) {
        return  (c >= 'A'&& c <= 'Z') || (c >= 'a'&& c <= 'z') || (c=='_');
    }

    // check the current character
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    
    // moves the offset of the source by one
    private char advance() {
        current++;
        return  source.charAt(current-1); // picking the character at one step back
    }

    // perform a character lookahead 
    private  char lookAhead(){
        if (isAtEnd()) return '\0';
        return  source.charAt(getIdx().yield());
    }

    // matchchecks if current char is equal to the expected and advance if true.
    private boolean match(char expected) {
        if (isAtEnd()) return  false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private void addToken(TokenType type, Object literal) {
        String text =  source.substring(start, current);
        tokens.add(new Token(type,text,literal,line,start,start+text.length()));
    }

    private void addToken(TokenType type){
        addToken(type,null);
    }

    // consumes a string
    private void string(){
        while(peek() != '"' && !isAtEnd()){
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()){
            Lox.error(line, "Unterminated string");
            return;
        }

        advance();

        String value = source.substring(start+1,current -1);
        addToken(TokenType.STRING,value);
    }

    // asserts if the character is a digit
    private boolean isDigit(char c){
        return  c >= '0' && c <= '9';
    }

    // consumes a number 
    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume the "." (decimal point)
            while(isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.valueOf(source.substring(start,current)));
    }


    private char peekNext(){
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }
}


