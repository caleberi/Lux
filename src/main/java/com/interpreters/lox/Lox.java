package com.interpreters.lox;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;



public class Lox {

    private static final Interpreter interpreter = new Interpreter();
    static   Boolean hasError = false;
    static  Boolean hasRuntimeError = false;

    public static void main(String[] args) throws  IOException{
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(1);
        } else  if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }


    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if(hasError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input =  new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;){
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hasError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for(Token token: tokens){
            System.out.println(token);
        }

        Parser parser =  new Parser(tokens);
        List<Stmt> statements = parser.parse();

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        if (hasError) return;
        if (hasRuntimeError) System.exit(79);

        interpreter.interpret(statements);
    }

    // ERROR HANDLING
    private static void report(int line,String where,String message) {
        System.err.println("[line "+line+"]: Error: "+where + ":"+ message);
        hasError = true;
    }

    static void error(int  line, String message ){
        report(line," ",message);
    }

    public static void runtimeError(RuntimeError error) {
        System.out.println(
                error.getMessage() +
                        "\nline["+error.token.line.toString() + "]\n[start:"+
                        error.token.startOffset.toString() +
                        "- end:" +error.token.endOffset.toString()+ "]\n");
        hasError = true;
    }
}


