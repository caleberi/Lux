package com.interpreters.lox;


class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    final int startOffset;



    final int endOffset;

    Token(TokenType type, String lexeme, Object literal, int line, int startOffset, int endOffset) {
        this.endOffset =  endOffset;
        this.startOffset =  startOffset;
        this.type =  type;
        this.line = line;
        this.lexeme = lexeme;
        this.literal = literal;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", lexeme='" + lexeme + '\'' +
                ", literal=" + literal +
                ", line=" + line +
                ", startOffset=" + startOffset +
                ", endOffset=" + endOffset +
                '}';
    }

}


