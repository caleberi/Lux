package com.interpreters.lox;

import java.util.List;

abstract class Expr {
  interface Visitor<R> {
    R visitBinaryExpr(Binary expr);

    R visitCallExpr(Call expr);

    R visitGroupingExpr(Grouping expr);

    R visitLiteralExpr(Literal expr);

    R visitLogicalExpr(Logical expr);

    R visitUnaryExpr(Unary expr);

    R visitTernaryExpr(Ternary expr);

    R visitVariableExpr(Variable expr);

    R visitAssignExpr(Assign expr);
  }

  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    final Expr left;
    final Token operator;
    final Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
  }

  static class Call extends Expr {
    Call(Expr callee, Token paren, List<Expr> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    final Expr callee;
    final Token paren;
    final List<Expr> arguments;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }
  }

  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    final Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    final Object value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    final Expr left;
    final Token operator;
    final Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }
  }

  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    final Token operator;
    final Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }

  static class Ternary extends Expr {
    Ternary(
        Expr expression,
        Token question_mark,
        Expr truth_side,
        Token colon_operator,
        Expr false_side) {
      this.expression = expression;
      this.question_mark = question_mark;
      this.truth_side = truth_side;
      this.colon_operator = colon_operator;
      this.false_side = false_side;
    }

    final Expr expression;
    final Token question_mark;
    final Expr truth_side;
    final Token colon_operator;
    final Expr false_side;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTernaryExpr(this);
    }
  }

  static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    final Token name;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }
  }

  static class Assign extends Expr {
    Assign(Token name, Expr right) {
      this.name = name;
      this.right = right;
    }

    final Token name;
    final Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }
  }

  abstract <R> R accept(Visitor<R> visitor);
}

