package com.interpreters.lox;

import java.util.List;

public class LoxFunction implements  LoxCallable{
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;
    LoxFunction(Stmt.Function declaration, Environment closure,
                boolean isInitializer) {
        this.isInitializer = isInitializer;
        this.declaration = declaration;
        this.closure = closure;
    }
    @Override
    public int arity() {
        return this.declaration.parameters.size();
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment,
                isInitializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.parameters.size(); i++)
            environment.define(declaration.parameters.get(i).lexeme,
                    arguments.get(i));

       try {
           interpreter.executeBlock(declaration.body, environment);
       } catch (Return returnValue){
           return  returnValue.value;
       }
        if (isInitializer) return closure.getAt(0, "this");
        return null;
    }

    @Override
    public String toString() {
        return "<fx "+this.declaration.functionName+ ">";
    }
}
