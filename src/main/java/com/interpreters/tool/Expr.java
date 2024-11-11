package com.interpreters.tool;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Expr {
    public static void main(String[] args) throws IOException, FormatterException {
        if (args.length != 1){
            System.out.println("Usage: generate_ast <output_directory>");
            System.exit(64);
        }
        String outputDir =  args[0];
        String baseName = "Expr";

        defineAst(outputDir,baseName, List.of(
                "Binary      : Expr left, Token operator, Expr right",
                "Grouping    : Expr expression",
                "Literal    : Object value",
                "Unary      : Token operator, Expr right"
        ));

        String generatedFilePath = outputDir + "/" + baseName + ".java";
        try (FileReader reader = new FileReader(generatedFilePath)){
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            List<String> lines = bufferedReader.lines().toList();
            for (String line : lines) {
                sb.append(line).append("\n");
            }
            System.out.println(sb.toString());
            new Formatter().formatSourceAndFixImports(sb.toString());
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
            for (String type: types) {
                String[] components = type.split(":");
                String className = components[0].trim();
                String fields = components[1].trim();
                defineType(writer, baseName, className,fields);
            }
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
        for (String field :  fields) {
            writer.printf("     final %s %s;\n",types.get(idx),paramNames.get(idx));
            idx+=1;
        }
        writer.println("    }");
    }
}
