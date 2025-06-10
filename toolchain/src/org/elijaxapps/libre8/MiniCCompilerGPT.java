// MiniCCompilerGPT.java — Reescrito y verificado para Java 17
package org.elijaxapps.libre8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniCCompilerGPT {

    private final List<String> output = new ArrayList<>();
    private final Map<String, String> dataSection = new LinkedHashMap<>();
    private int stringCounter = 0;
    private int varCounter = 0;

    public void compile(File inputFile, File outputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile)); PrintWriter writer = new PrintWriter(outputFile)) {

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }

            output.add(".data");
            parseGlobals(lines);
            output.addAll(dataSection.values());
            output.add(".code");
            output.add("B __MAIN__");
            parseCode(lines);

            for (String out : output) {
                writer.println(out);
            }
        }
    }

    private void parseGlobals(List<String> lines) {
        for (String line : lines) {
            if (line.startsWith("int") || line.startsWith("char")) {
                String[] tokens = line.split("\\s*[=;\\s]\\s*");
                if (tokens.length >= 2) {
                    String var = tokens[1];
                    String value = "00";
                    if (line.contains("=")) {
                        value = resolveValue(tokens[tokens.length - 1]);
                    }
                    String addr = generateAddress();
                    dataSection.put(var, var + " " + addr + " " + value);
                }
            } else if (line.contains("\"")) {
                handleStringLiteral(line);
            }
        }
    }

    private void handleStringLiteral(String line) {
        Matcher matcher = Pattern.compile("\\\"(.*?)\\\"").matcher(line);
        if (matcher.find()) {
            String literal = matcher.group(1);
            for (int i = 0; i < literal.length(); i++) {
                char ch = literal.charAt(i);
                String key = "str" + (stringCounter++);
                String addr = generateAddress();
                dataSection.put(key, key + " " + addr + " " + String.format("%02x", (int) ch));
            }
        }
    }

    private void parseCode(List<String> lines) {
        boolean insideAsm = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.contains("__asm")) {
                insideAsm = true;
                continue;
            }
            if (insideAsm) {
                if (line.contains("}")) {
                    insideAsm = false;
                    continue;
                }
                String asmLine = line.replaceAll("[{}]", "").trim();
                if (!asmLine.isEmpty()) {
                    output.add(asmLine);
                }
                continue;
            }

            if (line.startsWith("int ") && line.contains("=")) {
                String[] tokens = line.split("\\s*[=;\\s]\\s*");
                if (tokens.length >= 3) {
                    String var = tokens[1];
                    String val = resolveValue(tokens[2]);
                    output.add("LDA " + val);
                    output.add("STA " + var);
                }
            }

            if (line.contains("=") && line.contains("+")) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String lhs = parts[0].trim();
                    String rhs = parts[1].trim();
                    String[] operands = rhs.split("\\+");
                    if (operands.length == 2) {
                        output.add("LDA " + operands[0].trim());
                        output.add("ADD " + operands[1].trim());
                        output.add("STA " + lhs);
                    }
                }
            }

            if (line.matches("\\w+\\(.*\\);")) {
                Matcher callMatcher = Pattern.compile("(\\w+)\\((.*?)\\);?").matcher(line);
                if (callMatcher.find()) {
                    String func = callMatcher.group(1);
                    String args = callMatcher.group(2).trim();
                    if (!args.isEmpty()) {
                        output.add("LDIA " + args);
                        output.add("PSAX");
                    }
                    output.add("B " + func);
                }
            }
        }
    }

    private String resolveValue(String token) {
        token = token.trim();
        try {
            int val = Integer.parseInt(token);
            return "_" + String.format("%02x", val);
        } catch (NumberFormatException e) {
            return token;
        }
    }

    private String generateAddress() {
        int base = 0x00090000;
        return String.format("%08Xh", base - varCounter++);
    }

    public static void main(String[] args) throws IOException {
        MiniCCompilerGPT compiler = new MiniCCompilerGPT();
        File input = new File("C_example.c");
        File output = new File("main.as");
        compiler.compile(input, output);
        System.out.println("Compilation complete.");
    }
}
