package com.interpreters.tool;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateAst {
    private final static  Map<String,List<String>>  astMap = new HashMap<>();
    private final static  Map<String,String>  outputFileName = new HashMap<>();

    static {
        outputFileName.put("Stmt","/Stmt.java");
        outputFileName.put("Expr","/Expr.java");
    }

    static {
        astMap.put("Expr", List.of(
            "Binary     : Expr left, Token operator, Expr right",
            "Call       : Expr callee, Token paren, List<Expr> arguments",
            "Grouping   : Expr expression",
            "Get        : Expr object, Token name",
            "Set        : Expr object, Token name, Expr value",
            "Super      : Token keyword, Token method",
            "This       : Token keyword",
            "Literal    : Object value",
            "Logical    : Expr left, Token operator, Expr right",
            "Unary      : Token operator, Expr right",
            "Ternary    : Expr expression, Token question_mark, Expr truth_side, Token colon_operator, Expr false_side",
            "Variable   : Token name",
            "Assign     : Token name, Expr right"
        ));

        astMap.put("Stmt",List.of(
            "Expression : Expr expression",
            "Print      : Expr expression",
            "Var        : Token name, Expr initializer",
            "Block      : List<Stmt> statements",
            "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
            "While      : Expr condition, Stmt body",
            "Function   : Token functionName, List<Token> parameters, List<Stmt> body",
            "Return     : Token keyword, Expr value",
            "Class      : Token name, Expr.Variable superclass, List<Stmt.Function> methods"
        ));
    }

    
    public static void main(String[] args) throws IOException, FormatterException {
        if (args.length != 1){
            System.out.println("Usage: generate_ast <output_directory>");
            System.exit(64);
        }
        String outputDir =  args[0];
        for (Map.Entry<String, List<String>> en : astMap.entrySet()) {
            String fileName = outputFileName.get(en.getKey());
            String generatedFilePath = outputDir + fileName;
            File outputFile = new File(generatedFilePath);
            if (!outputFile.exists()){
                try( FileWriter fileWriter = new FileWriter(outputFile,false)){
                    fileWriter.flush();
                }
            }
            defineAst(outputDir, en.getKey(), en.getValue());
            formatOutput(generatedFilePath);
        }

    }

    private static void formatOutput(String generatedFilePath) throws IOException, FormatterException {
        StringBuilder sb = new StringBuilder();
        try (FileReader reader = new FileReader(generatedFilePath)){
            BufferedReader bufferedReader = new BufferedReader(reader);
            List<String> lines = bufferedReader.lines().toList();
            for (String line : lines) {
                sb.append(line).append("\n");
            }
        }

        String out = new Formatter().formatSourceAndFixImports(sb.toString());
        try (FileWriter writer = new FileWriter(generatedFilePath,false)) {
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            PrintWriter printWriter = new PrintWriter(bufferedWriter);
            printWriter.println(out);
            printWriter.flush();
        }
    }

    public  static void defineAst(String outputDir, String baseName, List<String> types) throws  IOException{
        String path = outputDir + "/" + baseName + ".java";
        try (PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
            writer.println("package com.interpreters.lox;");
            writer.println();
            writer.println("import java.util.List;");
            writer.println();
            writer.printf("abstract class %s {\n", baseName);
            defineVisitor(writer,baseName,types);
            for (String type: types) {
                String[] components = type.split(":");
                String className = components[0].trim();
                String fields = components[1].trim();
                defineType(writer, baseName, className,fields);
            }

            writer.println();
            writer.println(" abstract <R> R accept(Visitor<R> visitor);");
            writer.println("}");
        }
    }

    public  static void defineType(PrintWriter writer, String baseName, String className, String fieldList){
        writer.printf(" static class %s extends %s {\n",className,baseName);
        writer.printf("     %s (%s){\n",className,fieldList);
        String[] fields = fieldList.split(", ");
        List<String> types = new ArrayList<>();
        List<String> paramNames = new ArrayList<>();
        for (String field :  fields) {
            String[] components = field.split(" ");
            String type = components[0].trim();
            String paramName = components[1].trim();
            writer.printf("         this.%s = %s;\n",paramName,paramName);
            {
                types.add(type);
                paramNames.add(paramName);
            }
        }
        writer.println("    }");
        int idx = 0;
        for (@SuppressWarnings("unused")String field :  fields) {
            writer.printf("     final %s %s;\n",types.get(idx),paramNames.get(idx));
            idx+=1;
        }

        writer.println();
        writer.println(" @Override");
        writer.println(" <R> R accept(Visitor<R> visitor) {");
        writer.println(" return visitor.visit" +
                className + baseName + "(this);");
        writer.println(" }");
        writer.println("    }");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println(" interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(" R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println(" }");
    }
}
