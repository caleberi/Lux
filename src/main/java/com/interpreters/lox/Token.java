package com.interpreters.lox;


class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final Integer line;
    final Integer startOffset;
    final Integer endOffset;

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


