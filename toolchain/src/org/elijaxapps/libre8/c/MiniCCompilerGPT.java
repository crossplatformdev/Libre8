package org.elijaxapps.libre8.c;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniCCompilerGPT {

    private static final int BASE_VAR_OFFSET = 0x001f0000;
    private static final int BASE_STR_OFFSET = 0x003f0000;
    private static final int BASE_FUNC_OFFSET = 0x006f0000;
    private static final int GLYPH_BASE_OFFSET = 0x000010ff;

    private static int varOffset = BASE_VAR_OFFSET;
    private static int strOffset = BASE_STR_OFFSET;
    private static int funcOffset = BASE_FUNC_OFFSET;

    private static int labelCounter = 0;
    private static final List<String> breakStack = new ArrayList<>();
    private static final List<String> continueStack = new ArrayList<>();
    private static final ArrayList<String> blockEndLabel = new ArrayList<>();

    private static final List<String> dataSection = new ArrayList<>();
    private static final List<String> codeSection = new ArrayList<>();
    private static final List<String> printfCodeSection = new ArrayList<>();

    private static final Map<String, Integer> variables = new LinkedHashMap<>();
    private static final Map<String, String> initialValues = new LinkedHashMap<>();
    private static final Map<String, String> strings = new LinkedHashMap<>();
    private static final Map<String, Integer> functions = new LinkedHashMap<>();
    private static final Map<String, List<String>> functionBodies = new LinkedHashMap<>();
    private static final Map<String, List<String>> ifBodies = new LinkedHashMap<>();
    private static final Map<String, List<String>> functionArgs = new LinkedHashMap<>();

    private static boolean noIfElse = false;
    private static final Map<String, List<String>> conditions = new LinkedHashMap<>();
    private static int printfCounter = 0;
    private static int printfTempCounter = 0;
    public static void main(String[] args) throws IOException {
        String input = readFile("C_example.c");
        parseGlobals(input);
        parseFunctions(input);
        //parseVariables(input);
        generateDataSection();
        generateCodeSection();
        saveToFile("main.as");
    }

    public static void run(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) filename = "C_example.c";
        String input = readFile(filename);
        parseGlobals(input);
        parseFunctions(input);
        //parseVariables(input);
        generateDataSection();
        generateCodeSection();
        saveToFile("main.as");
    }

    private static String readFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private static void parseGlobals(String src) {
        Pattern pattern = Pattern.compile("(int|char)\\s+(\\*?\\w+\\[\\d*\\])\\s*=\\s*([^;]+);");
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            String type = matcher.group(1);
            String name = matcher.group(2);
            String value = matcher.group(3).trim();

            
            //If is array...
            if (name.endsWith("[]")) {
                Integer arr_length = name.contains("[") ? Integer.parseInt(name.substring(name.indexOf("[") + 1, name.indexOf("]"))) : 0;
                for (int i = 0; i < arr_length; i++) {
                    String varName = name.substring(0, name.indexOf("[")) + "_" + i;
                    while (variables.containsKey(varName)) varName += "_";
                    variables.put(varName, varOffset--);
                    initialValues.put(varName, "00");
                }
            } else if (type.equals("char") && !name.startsWith("*")) {
                int val = value.startsWith("'") ? (int) value.charAt(1) : Integer.parseInt(value);
                variables.put(name, varOffset--);
                initialValues.put(name, val < 0 ? "00" : Integer.toString(val, 16));
            } else if ((type.equals("char") && name.startsWith("*")) || type.equals("char*") || name.endsWith("[]")) {
                if (name.startsWith("*")) name = name.substring(1);
                while (variables.containsKey(name)) name += "_";
                String cleanString = value.replaceAll("\"", "");
                strings.put(name, cleanString);
                for (int i = 0; i < cleanString.length(); i++) {
                    String varName = name + i;
                    while (variables.containsKey(varName)) varName += "_";
                    variables.put(varName, strOffset);
                    initialValues.put(varName, "'" + cleanString.charAt(i) + "'");
                    strOffset--;
                }
            } else if (type.equals("int")) {
                variables.put(name, varOffset--);
                if (value.matches("\\d+")) {
                    String hexValue = Integer.toHexString(Integer.parseInt(value));
                    while (hexValue.length() < 2) hexValue = "0" + hexValue;
                    initialValues.put(name, hexValue);
                } else if (value.matches("\\d+\\s*[-+*/]\\s*\\d+")) {
                    initialValues.put(name, "00");
                } else if (variables.containsKey(name)) {
                    initialValues.put(name, "00");
                } else {
                    throw new IllegalArgumentException("Unsupported value expression for: " + name);
                }
            } else {
                throw new IllegalArgumentException("Unsupported type: " + type + " for variable: " + name);
            }
        }
    }

    private static void parseFunctions(String src) {
        Pattern pattern = Pattern.compile("(int|void)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{(.*?)\\n}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            String name = matcher.group(2);
            String args = matcher.group(3);
            String body = matcher.group(4);

            List<String> argList = new ArrayList<>();
            if (!args.trim().isEmpty()) {
                for (String arg : args.split(",")) argList.add(arg.trim().split(" ")[1]);
            }
            functionArgs.put(name, argList);
            functionBodies.put(name, Arrays.asList(body.trim().split("\\n")));
            functions.put(name, funcOffset);
            funcOffset -= 0x4000;
        }
    }

    private static void generateDataSection() {
        addFormattedDataSection(";;;;;;;;;;;;;;;\n;; DATA BEGIN ;;\n;;;;;;;;;;;;;;;\n.data");
        addFormattedDataSection(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n;; GLYPHS: _00 00600000h 00 - _ff 006000ffh ff ;;\n;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        for (int i = 0; i <= 255; i++)
            addFormattedDataSection(String.format("_%02x %08xh %02x", i, GLYPH_BASE_OFFSET + i, i));
        addFormattedDataSection(";;;;;;;;;;;;;;;\n;; GLYPHS END ;;\n;;;;;;;;;;;;;;;");
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; VARIABLES ;;\n;;;;;;;;;;;;;;;");
        for (Map.Entry<String, Integer> entry : variables.entrySet()) {
            String name = entry.getKey();
            int offset = entry.getValue();
            String val = initialValues.getOrDefault(name, "0");
            addFormattedDataSection(String.format("%s %08xh %s ;; variable initialized", name, offset, val));
        }
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; VARIABLES END ;;\n;;;;;;;;;;;;;;;");
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; STRINGS ;;\n;;;;;;;;;;;;;;;");
        for (Map.Entry<String, String> entry : strings.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            addFormattedDataSection(String.format("%s %08xh \"%s\" ;; string", name, strOffset, value));
            strOffset -= value.length() + 1;
        }
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; STRINGS END ;;\n;;;;;;;;;;;;;;;");
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; FUNCTIONS ;;\n;;;;;;;;;;;;;;;");
        for (Map.Entry<String, Integer> entry : functions.entrySet()) {
            String fn = entry.getKey();
            String args = String.join(" ", functionArgs.get(fn));
            addFormattedDataSection(String.format("%s %08xh %s;; function offset", fn, entry.getValue(), args));
        }
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; DATA END ;;\n;;;;;;;;;;;;;;;");
    }

    private static void generateCodeSection() {
        addFormattedCodeSection(";;;;;;;;;;;;;;;;\n"
                + ";; CODE BEGIN ;;\n"
                + ";;;;;;;;;;;;;;;;\n"
                + ".code\n"
                + "B main ;; Branch to main function\n"
                + "JMP 00000000h ;; Jump to end of code section (placeholder for main function)\n");
        for (Map.Entry<String, List<String>> entry : functionBodies.entrySet()) {
            generateCode(entry);
        }
        for (Map.Entry<String, List<String>> fn : ifBodies.entrySet()) {
            String[] parts = fn.getKey().split("\\+\\+\\+");
            String ifLabel = parts[0];
            String elseLabel = parts.length > 1 ? parts[1] : "ELSE_" + labelCounter++;
            addFormattedCodeSection(";; If-Else block: " + ifLabel + " else " + elseLabel);
            String condition = fn.getValue().toString().replace("[", "").replace("]", "").trim();
            generateConditionalBody(condition, ifLabel, elseLabel);
        }
    }

    public static void generateCode(Map.Entry<String, List<String>> fn) {
        String name = fn.getKey();
        List<String> body = fn.getValue();
        addFormattedCodeSection(";;;;;;;;;;;;;;;\n;; BEGIN " + name + " ;;");
        addFormattedCodeSection("." + name);

        if (!functionArgs.isEmpty()) {
            for (String arg : functionArgs.get(name)) {                
                addFormattedDataSection(name + "_" + arg + " " + String.format("%08xh", varOffset--));
                addFormattedCodeSection(";; Argument: " + arg);
                addFormattedCodeSection("POPX " + name + "_" + arg + " ;; Pop the argument from the stack");
                addFormattedCodeSection("STA " + name + "_" + arg + " ;; Store argument in variable");
            }
        }
        boolean endif = false;
        for (String line : body) {
            if (endif) {
                endif = false;
                noIfElse = true;
                continue;
            }
            String trim = line.trim();
            trim = trim.replaceAll("//", ";;");
            if (trim.isEmpty() || trim.startsWith("__asm") || trim.startsWith(";;")) continue;

            if (trim.startsWith("if(") || trim.startsWith("if (")) {
                trim = trim.replace("if\\s\\(", "");
                String nextTrim = body.get(body.indexOf(line) + 1).trim();
                String functionName = "";
                if (nextTrim.startsWith("__asm")) {
                    functionName = "__asm_" + labelCounter;
                    labelCounter += 1;
                } else if (nextTrim.indexOf("(") > 0) {
                    functionName = nextTrim.substring(0, nextTrim.indexOf("(")).trim();
                }
                addFormattedCodeSection(parseIf(trim, functionName));
                noIfElse = false;
                continue;
            }

            if (trim.startsWith("else if(") || trim.startsWith("else if (") || trim.startsWith("} else if (") || trim.startsWith("} else if(") || trim.startsWith("}else if (") || trim.startsWith("}else if(") || trim.startsWith("else{") || trim.startsWith("} else if (")) {
                trim = trim.replaceAll("else\\s*if\\s*\\(", "");
                String nextTrim = body.get(body.indexOf(line) + 1).trim();
                String functionName = "";
                if (nextTrim.startsWith("__asm")) {
                    functionName = "__asm_" + labelCounter;
                    labelCounter += 1;
                }
                addFormattedCodeSection(parseIf(trim, functionName));
                nextTrim = body.get(body.indexOf(line) + 1).trim();
                if (nextTrim.startsWith("__asm")) {
                    int counter = 2;
                    nextTrim = body.get(body.indexOf(line) + counter).trim();
                    while (!nextTrim.equals("}")) {
                        addFormattedCodeSection(nextTrim);
                        counter++;
                        nextTrim = body.get(body.indexOf(line) + counter).trim();
                    }
                    addFormattedCodeSection(";; End of inline assembly");
                }
                noIfElse = false;
                continue;
            }

            if (trim.startsWith("else{") || trim.startsWith("else {") || trim.startsWith("} else {") || trim.startsWith("} else{") || trim.startsWith("}else {") || trim.startsWith("}else{")) {
                trim = trim.replaceAll("else\\s*\\{", "");
                String nextTrim = body.get(body.indexOf(line) + 1).trim();
                String functionName = "";
                if (nextTrim.startsWith("__asm")) {
                    functionName = "__asm_" + labelCounter;
                    labelCounter += 1;
                } else if (nextTrim.indexOf("(") > 0) {
                    functionName = nextTrim.substring(0, nextTrim.indexOf("(")).trim();
                }
                addFormattedCodeSection(parseElse(trim, functionName));
                if (nextTrim.startsWith("__asm")) {
                    nextTrim = body.get(body.indexOf(line) + 2).trim();
                    int counter = 3;
                    while (!nextTrim.equals("}")) {
                        addFormattedCodeSection(nextTrim);
                        counter++;
                        nextTrim = body.get(body.indexOf(line) + counter).trim();
                    }
                    addFormattedCodeSection(";; End of inline assembly");
                }
                noIfElse = false;
                continue;
            }

            if (trim.equals("break;")) {
                if (!breakStack.isEmpty()) addFormattedCodeSection("JMP " + breakStack.get(breakStack.size() - 1));
                continue;
            }

            if (trim.equals("continue;")) {
                if (!continueStack.isEmpty()) addFormattedCodeSection("JMP " + continueStack.get(continueStack.size() - 1));
                continue;
            }

            if (trim.startsWith("while(")) {
                String condition = trim.substring(trim.indexOf("(") + 1, trim.lastIndexOf(")"));
                String loopStart = "__while_start_" + labelCounter;
                String loopEnd = "__while_end_" + labelCounter;
                blockEndLabel.add(loopEnd);
                addFormattedDataSection(loopStart + " " + String.format("%08xh", funcOffset) + "h ;; While loop start");
                funcOffset -= 0x4000;
                addFormattedDataSection("__while_body_" + labelCounter + " " + String.format("%08xh", funcOffset) + "h ;; While loop body");
                funcOffset -= 0x4000;
                addFormattedDataSection(loopEnd + " " + String.format("%08xh", funcOffset) + "h ;; While loop end");
                funcOffset -= 0x4000;

                String functionName = "";
                String nextTrim = body.get(body.indexOf(line) + 1).trim();
                if (nextTrim.startsWith("__asm")) {
                    functionName = "__asm_" + labelCounter;
                    labelCounter += 1;
                } else if (nextTrim.indexOf("(") > 0) {
                    functionName = nextTrim.substring(0, nextTrim.indexOf("(")).trim();
                }

                addFormattedCodeSection("B " + loopStart + " ;; Branch to while loop start");
                addFormattedCodeSection(";; While loop: " + loopStart);
                addFormattedCodeSection("." + loopStart);
                
                addFormattedCodeSection(parseIf(condition, functionName));
                addFormattedCodeSection(parseElse(condition, loopEnd));
                addFormattedCodeSection(";; While loop body");

                continueStack.add(loopStart);
                breakStack.add(loopEnd);
                labelCounter++;
            }

            if (trim.startsWith("for(") || trim.startsWith("for (")) {
                String content = trim.substring(trim.indexOf("(") + 1, trim.lastIndexOf(")"));
                String[] parts = content.split(";");
                String varname = parts[0].split("=")[0].replaceAll("int", "").trim();
                String value = parts[0].split("=")[1].trim();
                String cond = "(" + parts[1].trim() + ")";
                String inc = parts[2].trim();
                String incVar = "__for_inc_" + labelCounter;
                String loopInit = "__for_init_" + labelCounter;
                String loopStart = "__for_start_" + labelCounter;
                String loopEnd = "__for_end_" + labelCounter;
                String topVal = "__for_top_" + labelCounter;
                Integer topValValue = Integer.valueOf(value);
                String loopBody = "";

                if (topValValue.equals(varname)) {
                    topValValue = Integer.valueOf(cond.replaceAll("\\(", "").replaceAll("\\)", "").trim().split("<|>|==|!=|<=|>=|\\+|-|\\*|/")[0].trim());
                }

                boolean isSame = !varname.equals(topVal);

                if (cond.contains("<") && isSame) {
                    cond = '(' + varname + " < " + topVal + ')';
                } else if (cond.contains(">") && isSame) {
                    cond = '(' + varname + " > " + topVal + ')';
                } else if (cond.contains("==") && isSame) {
                    cond = '(' + varname + " == " + topVal + ')';
                } else if (cond.contains("!=") && isSame) {
                    cond = '(' + varname + " != " + topVal + ')';
                } else if (cond.contains("<=") && isSame) {
                    cond = '(' + varname + " <= " + topVal + ')';
                } else if (cond.contains(">=") && isSame) {
                    cond = '(' + varname + " >= " + topVal + ')';
                }

                int counter = 1;
                String nextTrim = body.get(body.indexOf(line) + counter).trim();
                if (nextTrim.startsWith("__asm")) {
                    // Handle inline assembly
                    //addFormattedDataSection(loopStart + " " + String.format("%08xh", funcOffset) + "h ;; For loop start");
                    //funcOffset -= 0x4000; // Adjust function offset for next label
                    loopBody = "__asm_" + labelCounter;
                    counter++;
                    nextTrim = body.get(body.indexOf(line) + counter).trim();
                    while (!nextTrim.equals("}")) {
                        addFormattedCodeSection(nextTrim);
                        counter++;
                        nextTrim = body.get(body.indexOf(line) + counter).trim();
                    }                    
                } else {
                    loopBody = nextTrim.split("\\(")[0]; // Assuming the loop body is a single line for simplicity
                }

                while (value.length() < 2) {
                    value = "0" + value; // Ensure at least two characters for char values
                }

                addFormattedDataSection(topVal + " " + String.format("%08xh", varOffset--) + " " + String.format("%02x", topValValue) + ";; For loop top value");
                if (inc.contains("++")) {
                    addFormattedDataSection(incVar + " " + String.format("%08xh", varOffset--) + " 01;; For loop step");
                } else if (inc.contains("--")) {
                    addFormattedDataSection(incVar + " " + String.format("%08xh", varOffset--) + " FF;; For loop step");
                } else {
                    if (inc.contains("+=")) {
                        String incValue = inc.split("\\+=")[1].trim();
                        if (incValue.length() < 2) {
                            incValue = "0" + incValue; // Ensure at least two characters for char values
                        }
                        addFormattedDataSection(incVar + " " + String.format("%08xh", varOffset--) + " " + String.format("%02x", topValValue) + ";; For loop step");
                    } else if (inc.contains("-=")) {
                        String incValue = inc.split("-=")[1].trim();
                        if (incValue.length() < 2) {
                            incValue = "0" + incValue; // Ensure at least two characters for char values
                        }
                        addFormattedDataSection(incVar + " " + String.format("%08xh", varOffset--) + " " + String.format("%02x", topValValue) + ";; For loop step");
                    } else {
                        addFormattedDataSection(incVar + " " + String.format("%08xh", varOffset--) + " 00;; For loop step");
                    }
                }

                addFormattedDataSection(loopEnd + " " + String.format("%08xh", funcOffset) + " ;; For loop end");
                funcOffset -= 0x4000; // Adjust function offset for next label
                addFormattedDataSection(loopStart + " " + String.format("%08xh", funcOffset) + " ;; For loop start");
                funcOffset -= 0x4000; // Adjust function offset for next label
                addFormattedDataSection(loopInit + " " + String.format("%08xh", funcOffset) + " ;; For loop initialization");
                funcOffset -= 0x4000; // Adjust function offset for next label
            

                String functionName = loopBody;
                addFormattedCodeSection(";; ENTERING FOR LOOP: " + functionName);
                addFormattedCodeSection("B " + loopInit + " ;; Branch to for loop start");
                addFormattedCodeSection(";; For loop: " + loopStart);
                addFormattedCodeSection(";; Initialization: " + varname + " = " + value);
                addFormattedCodeSection(";; Condition: " + cond);
                addFormattedCodeSection(";; Increment: " + inc);
                addFormattedCodeSection(";; Loop end: " + loopEnd);
                addFormattedCodeSection("." + loopInit);
                addFormattedCodeSection("LDA _" + String.format("%02x", Integer.valueOf(value)) + " ;; Load the variable value");
                addFormattedCodeSection("STA " + varname + " ;; Store the updated value back to the variable");
                addFormattedCodeSection("JMP " + loopStart + " ;; Jump to loop start");
                addFormattedCodeSection("." + loopStart);
                addFormattedCodeSection("LDA " + varname + " ;; Load the variable value for condition check");
                addFormattedCodeSection("ADD " + incVar + " ;; Subtract condition value");
                addFormattedCodeSection("STA " + varname + " ;; Jump to loop end if condition is zero");
                addFormattedCodeSection(parseIf(cond, functionName));
                addFormattedCodeSection(parseElse(cond, "__asm "+loopEnd));
                addFormattedCodeSection("." + loopEnd);
                addFormattedCodeSection("BX _00 ;; Return from function");

                continueStack.add(loopStart);
                breakStack.add(loopEnd);

                endif = true; // Set flag to skip next lines
                labelCounter++;

                continue;
            }

            if (trim.startsWith("do {")) {
                String label = "__do_while_start_" + labelCounter;
                addFormattedDataSection(label + " " + String.format("%08xh", funcOffset) + "h ;; Do-While loop start");
                funcOffset -= 0x4000; // Adjust function offset for next label
                continueStack.add(label);
                breakStack.add("__do_while_end_" + labelCounter);
                labelCounter++;
                continue;
            }
            if (trim.startsWith("} while(")) {
                String condition = trim.substring(trim.indexOf("(") + 1, trim.lastIndexOf(")"));
                String startLabel = continueStack.remove(continueStack.size() - 1);
                String endLabel = breakStack.remove(breakStack.size() - 1);
                addFormattedCodeSection("B " + startLabel + " ;; Branch to do-while loop start");
                addFormattedCodeSection(parseIf(condition, startLabel));
                addFormattedCodeSection(parseElse(condition, endLabel));                
                continue;
            }

            if (trim.startsWith("switch(")) continue;
            if (trim.startsWith("case ")) continue;
            if (trim.equals("default:")) continue;

            if (trim.equals("}")) {
                if (!breakStack.isEmpty()) breakStack.remove(breakStack.size() - 1);
                if (!continueStack.isEmpty()) continueStack.remove(continueStack.size() - 1);
                addFormattedCodeSection(";; End of block");
                if (noIfElse) noIfElse = false;
                continue;
            } else if (trim.startsWith("return ")) {
                String returnValue = trim.split(";")[0].replace("return", "").trim();
                if (!returnValue.isEmpty()) {
                    if (isNumber(returnValue)) {
                        if (returnValue.length() < 8) returnValue = "0" + returnValue;
                        addFormattedCodeSection("BX _" + returnValue + " ;; Return");
                    } else {
                        addFormattedCodeSection("BX " + returnValue + " ;; Return");
                    }
                } else {
                    addFormattedCodeSection("LDA _00 ;; Load zero as default return value");
                }
            } else if (trim.startsWith("break;")) {
                if (!breakStack.isEmpty()) addFormattedCodeSection("JMP " + breakStack.get(breakStack.size() - 1) + " ;; Break to label");
            } else if (trim.startsWith("continue;")) {
                if (!continueStack.isEmpty()) addFormattedCodeSection("JMP " + continueStack.get(continueStack.size() - 1) + " ;; Continue to label");
            } else if (trim.startsWith("printf(")) {
                addFormattedCodeSection(".print_f_" + (printfCounter++) + " ;; Handling printf statement");
                // Extract arguments from printf: printf("Hello, %d + %d = %d %s %c", _40, _02, _42, "World!", '☺');
                String argsContent = trim.substring(trim.indexOf("(") + 1, trim.lastIndexOf(")"));
                String[] args = argsContent.split("\",", 2);
                String formatString = args[0].trim().replaceAll("^\"|\"$", ""); // Remove surrounding quotes
                String formatArgsPart = args.length > 1 ? args[1].trim() :
                        "";
                String[] formatArgs = formatArgsPart.isEmpty() ? new String[0] : formatArgsPart.split(",");
                List<String> argList = new ArrayList<>();
                for (String fa : formatArgs) {
                    fa = fa.replaceAll(",", "").trim();
                    if (!fa.isEmpty()) argList.add(fa);
                }

                for (long i = 0; i < formatString.length(); i++) {
                    char ch = formatString.charAt((int)i);
                    if(ch == '\\' && i + 1 < formatString.length()) {
                        char nextChar = formatString.charAt((int)(i + 1));
                        switch (nextChar) {
                            case 'n':
                                addFormattedCodeSection("LDA _0a ;; Load newline character");
                                addFormattedCodeSection("OUT ;; Output newline");
                                break;
                            case 't':
                                addFormattedCodeSection("LDA _09 ;; Load tab character");
                                addFormattedCodeSection("OUT ;; Output tab");
                                break;
                            case '\\':
                                addFormattedCodeSection("LDA _5c ;; Load backslash character");
                                addFormattedCodeSection("OUT ;; Output backslash");
                                break;
                            case '\"':
                                addFormattedCodeSection("LDA _22 ;; Load double quote character");
                                addFormattedCodeSection("OUT ;; Output double quote");
                                break;
                            case '\'':
                                addFormattedCodeSection("LDA _27 ;; Load single quote character");
                                addFormattedCodeSection("OUT ;; Output single quote");
                                break;
                            default:
                                addFormattedCodeSection("LDA _" + Integer.toHexString((int) nextChar) + " ;; Load literal character");
                                addFormattedCodeSection("OUT ;; Output literal character");
                                break;
                        }
                        i++; // Skip the next character as it's part of the escape sequence
                    } else if (ch == '%') {
                        i++;
                        if (i < formatString.length()) {
                            char spec = formatString.charAt((int)i);
                            switch (spec) {
                                case 'i':
                                case 'd':
                                    if (!argList.isEmpty()) {
                                        //ASSUME 8-BIT INT - MUST PRINT 0-255
                                        String var = argList.remove(0);
                                        String operand = preparePrintfOperand(var);
                                        addFormattedCodeSection("LDA " + operand + " ;; Load int argument");
                                        addFormattedCodeSection("OUTD ;; Output int");                                        
                                    }   break;
                                
                                case 'c':
                                    if (!argList.isEmpty()) {
                                        String var = argList.remove(0);
                                        String operand = preparePrintfOperand(var);
                                        addFormattedCodeSection("LDA " + operand + " ;; Load char argument");
                                        addFormattedCodeSection("OUTC ;; Output char");
                                    }   break;
                                case 's':
                                    if (!argList.isEmpty()) {
                                        String var = argList.remove(0);
                                        for (int j = 0; j < strings.get(var).length(); j++) {
                                            char strChar = strings.get(var).charAt(j);
                                            addFormattedCodeSection("LDA _" + Integer.toHexString((int) strChar) + " ;; Load string character");
                                            addFormattedCodeSection("OUTA ;; Output string character");
                                        }
                                    }   break;
                                default:
                                    addFormattedCodeSection("LDA _" + Integer.toHexString((int) spec) + " ;; Load literal character");
                                    addFormattedCodeSection("OUT ;; Output literal character");
                                    break;
                            }
                        }
                    } else {
                        addFormattedCodeSection("LDA _" + Integer.toHexString((int) ch) + " ;; Load literal character");
                        addFormattedCodeSection("OUT ;; Output literal character");
                    }
                }
            } else if (trim.startsWith("__asm")) {
                addFormattedCodeSection(";; Begin inline assembly");
            } else if (trim.contains("BX")) {
                addFormattedCodeSection("BX _00 ;; Return from function");
            } else if (trim.contains("POP")) {
                addFormattedCodeSection("POPX " + functionArgs.get(name).get(0) + " ;; Pop the argument from the stack");
            } else if (trim.startsWith("OUT ")) {
                String arg = trim.substring(4).trim();
                addFormattedCodeSection("OUT " + arg + " ;; Output var");
            } else if (trim.equals("OUT") || trim.equals("OUTB") || trim.equals("OUTC") || trim.equals("OUTD")
                    || trim.equals("DEC") || trim.equals("DECE") || trim.equals("HLT") || trim.equals("NOP")
                    || trim.equals("RST") || trim.equals("PST") || trim.equals("PTRI") || trim.equals("PTRD")
                    || trim.equals("PTRL") || trim.equals("PTRS") || (trim.startsWith("LDA") || trim.startsWith("LDB") || trim.startsWith("LDC") || trim.startsWith("LDD")
                    || trim.startsWith("LDIA") || trim.startsWith("LDIB") || trim.startsWith("LDIC") || trim.startsWith("LDID")
                    || trim.startsWith("STA") || trim.startsWith("STB") || trim.startsWith("STC") || trim.startsWith("STD")
                    || trim.startsWith("ADD") || trim.startsWith("SUB") || trim.startsWith("MUL") || trim.startsWith("DIV")
                    || trim.startsWith("IADD") || trim.startsWith("ISUB") || trim.startsWith("IMUL") || trim.startsWith("IDIV")
                    || trim.startsWith("MOV_") || trim.startsWith("LD") || trim.startsWith("STO")
                    || trim.startsWith("JMP") || trim.startsWith("JZ") || trim.startsWith("JC") || trim.startsWith("JNZ")
                    || trim.startsWith("JNC") || trim.startsWith("JNB") || trim.startsWith("JB") || trim.startsWith("JP") || trim.startsWith("JNP")
                    || trim.startsWith("B") || trim.startsWith("BC") || trim.startsWith("BZ") || trim.startsWith("BNZ") || trim.startsWith("BNB")
                    || trim.startsWith("BP") || trim.startsWith("BNP") || trim.startsWith("BB")
                    || trim.startsWith("POKX") || trim.startsWith("POKY") || trim.startsWith("POKE")
                    || trim.startsWith("PXYD") || trim.startsWith("PIKX") || trim.startsWith("PIKY") || trim.startsWith("PIYD")
                    || trim.startsWith("OUT") || trim.startsWith("PSAX") || trim.startsWith("PSAH") || trim.startsWith("PSAL")
                    || trim.startsWith("POPX") || trim.startsWith("POPH") || trim.startsWith("POPL")
                    || trim.startsWith("DEC") || trim.startsWith("DECE") || trim.startsWith("HLT")
                    || trim.startsWith("NOP") || trim.startsWith("RST") || trim.startsWith("PST") || trim.startsWith("PTRI") || trim.startsWith("OUTT"))) {
                addFormattedCodeSection(trim);
            } else if(trim.contains("[") && trim.contains("]")) {
                String arrayName = trim.replaceAll("\\s*=.*", "").trim();
                String indexPart = trim.replaceAll(".*\\[(.*)\\].*", "$1").trim();
                String valuePart = trim.contains("=") ? trim.split("=")[1].replace(";", "").trim() : null;

                // Names of the array will be i.e myArray[3] myArray_3
                if (indexPart != null && !indexPart.isEmpty()) {
                    addFormattedCodeSection("B " + arrayName + "_" + indexPart + " ;; Load array element");
                }

            } else if (trim.contains("=") && containsOp(trim)) {
                compileExpression(trim);
            } else if (trim.contains("=")) {
                String[] parts = trim.split("=");
                String left = parts[0].trim();
                String right = parts[1].replace(";", "").trim();
                addFormattedCodeSection("LDA _" + Integer.toHexString((int) right.replace("'", "").charAt(0)) + " ;; Load the argument");
                addFormattedCodeSection("STA  " + left + " ;; Store value from A into " + left);
            } else if (trim.contains("(") && !noIfElse) {
                String call = trim.split("\\(")[0].trim();
                String arg = trim.replaceAll(".*\\((.*)\\).*", "$1");
                if (!arg.isEmpty()) {
                    String[] args = arg.split(",");
                    for (String a : args) {
                        a = a.trim();
                        if (isNumber(a)) {
                            while (a.length() < 2) a = "0" + a;
                            addFormattedCodeSection("LDA _" + a + " ;; Load the argument");
                            addFormattedCodeSection("PSAX ;; Push the argument onto the stack");
                        } else if (a.startsWith("'") && a.endsWith("'") && a.length() == 3) {
                            addFormattedCodeSection("LDA _" + Integer.toHexString((int) a.charAt(1)) + " ;; Load the argument");
                            addFormattedCodeSection("PSAX ;; Push the argument onto the stack");
                        } else if (a.startsWith("\"") && a.endsWith("\"")) {
                            for (int i = 0; i < a.length(); i++) {
                                char ch = a.charAt(i);
                                addFormattedCodeSection("LDA _" + Integer.toHexString((int) ch) + " ;; Load the argument");
                                addFormattedCodeSection("PSAX ;; Push the argument onto the stack");
                            }                            
                        } else {
                            addFormattedCodeSection("LDA " + a + " ;; Load the argument");
                            addFormattedCodeSection("PSAX ;; Push the argument onto the stack");
                        }
                    }                    
                }
                addFormattedCodeSection("B " + call + " ;; Call the function");
            } else if (trim.startsWith(".")) {
                trim = trim.substring(1).trim();
                addFormattedCodeSection(";; Label: " + trim);
                addFormattedCodeSection(trim);
            }
        }
        if (name.equals("main")) addFormattedCodeSection("BX _00 ;; Return from main");
        addFormattedCodeSection(";;;;;;;;;;;;;;;\n;; END " + name + " ;;");
    }

    private static void addFormattedCodeSection(String code) {
        if (code == null || code.isEmpty()) return;
        code = code.trim();
        codeSection.add(formatLine(code));
    }

    private static void addFormattedDataSection(String code) {
        if (code == null || code.isEmpty()) return;
        code = code.trim();
        dataSection.add(formatLine(code));
    }

    private static String formatLine(String line) {
        if(!line.contains(";;;") &&line.contains(";;")) {
            //Space the instruction and the coment so them appear aligned
            //Comments must appear always at the char 32
            String[] parts = line.split(";;");
            String instruction = parts[0].trim();
            String comment = "";
            
            for (int i = 1; i < parts.length; i++) {
                comment += ";; " + parts[i]; // Rebuild the comment
            }
            
            if (instruction.length() < 8) {
                instruction = String.format("%-8s", instruction); // Pad instruction to 8 characters
            }

            // Add spaces right after the instruction to align the comment
            if(instruction.length() < 32) {
                instruction = String.format("%-32s", instruction); // Pad instruction to 32 characters
            } else if (instruction.length() == 0) {
                comment = String.format("%-32s", comment) + comment; // Pad comment to 32 characters
            }
            line = instruction;
            if(!comment.isEmpty()){
                line += comment;
            }   
        }
        return line;
    }

    private static String parseIf(String trim, String functionName) {
        String condition = trim.substring(trim.indexOf("(") + 1, trim.lastIndexOf(")"));
        String labelIn = "IF_IN_" + labelCounter;
        String labelDest = functionName;
        String code = generateConditionalCall(condition, labelIn, labelDest);
        conditions.put(labelDest, Arrays.asList(condition.split("\\n")));
        labelCounter++;
        return code;
    }

    private static String parseElse(String trim, String functionName) {
        String labelEnd = functionName;
        String code = ";; Else block code for: " + labelEnd + "\n";
        for (Map.Entry<String, List<String>> entry : conditions.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString().replace("[", "").replace("]", "").trim();
            String[] parts = value.split("==|!=|<|>|<=|>=");
            if (isNumber(parts[0].trim())) {
                if (parts[0].trim().length() < 2) parts[0] = "0" + parts[0].trim();
                parts[0] = "_" + parts[0].trim();
            } else {
                parts[0] = parts[0].trim();
            }
            if (isNumber(parts[1].trim())) {
                if (parts[1].trim().length() < 2) parts[1] = "0" + parts[1].trim();
                parts[1] = "_" + parts[1].trim();
            } else {
                parts[1] = parts[1].trim();
            }

            String[] trimmedLabelParts = labelEnd.split("=");

            String labelStart = "";
            if (trimmedLabelParts.length == 2) {
                labelStart = trimmedLabelParts[0].trim();
                labelEnd = trimmedLabelParts[1].trim();
            }


            if (value.contains("==")) {
                code += ("LDA " + parts[1] + " ;; Load left side of condition\n");
                code += ("SUB " + parts[0] + " ;; Subtract right side of condition\n");
                if (labelEnd.startsWith("__asm")) code += ("JNZ " + labelEnd.replace("__asm", "") + " ;; Branch to else label if not zero\n");
                else code += ("BNZ " + labelEnd + " ;; Branch to else label if zero\n");
            } else if (value.contains("!=")) {
                code += ("LDA " + parts[1] + " ;; Load left side of condition\n");
                code += ("SUB " + parts[0] + " ;; Subtract right side of condition\n");
                if (labelEnd.startsWith("__asm")) code += ("JZ " + labelEnd.replace("__asm", "") + " ;; Branch to else label if zero\n");
                else code += ("BZ " + labelEnd + " ;; Branch to else label if zero\n");
            } else if (value.contains("<=")) {
                code += ("LDA " + parts[1] + " ;; Load left side of condition\n");
                code += ("SUB " + parts[0] + " ;; Subtract right side of condition\n");
                if (labelEnd.startsWith("__asm")) code += ("JB " + labelEnd.replace("__asm", "") + " ;; Branch to else label if less than or equal\n");
                else code += ("BB " + labelEnd + " ;; Branch to else label if less than or equal\n");
            } else if (value.contains(">=")) {
                code += ("LDA " + parts[1] + " ;; Load right side of condition\n");
                code += ("SUB " + parts[0] + " ;; Subtract left side of condition\n");
                if (labelEnd.startsWith("__asm")) code += ("JNB " + labelEnd.replace("__asm", "") + " ;; Branch to else label if greater than or equal\n");
                else code += ("BNB " + labelEnd + " ;; Branch to else label if greater than or equal\n");
            } else if (value.contains("<")) {
                code += ("LDA " + parts[1] + " ;; Load right side of condition\n");
                code += ("SUB " + parts[0] + " ;; Subtract left side of condition\n");
                if (labelEnd.startsWith("__asm")) code += ("JB " + labelEnd.replace("__asm", "") + " ;; Branch to else label if less than\n");
                else code += ("BB " + labelEnd + " ;; Branch to else label if less than\n");
            } else if (value.contains(">")) {
                code += ("LDA " + parts[1] + " ;; Load left side of condition\n");
                code += ("SUB " + parts[0] + " ;; Subtract right side of condition\n");
                if (labelEnd.startsWith("__asm")) code += ("JNB " + labelEnd.replace("__asm", "") + " ;; Branch to else label if greater than\n");
                else code += ("BNB " + labelEnd + " ;; Branch to else label if greater than\n");
            } else {
                throw new IllegalArgumentException("Condición no soportada: " + value);
            }
            break;            
        }
        conditions.clear();
        return code;
    }

    private static String generateConditionalCall(String condition, String ifLabel, String labelDest) {
        addFormattedCodeSection(";; If condition: " + condition);
        return generateConditionalBody(condition, ifLabel, labelDest);
    }

    private static String generateConditionalBody(String condition, String ifLabel, String labelDest) throws IllegalArgumentException {
        String code = ";; Conditional code for: " + condition + " \n";
        String[] parts = condition.split("==|!=|<|>|<=|>=");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Condición inválida: " + condition);
        }
        if (isNumber(parts[0].trim())) {
            if (parts[0].trim().length() < 2) parts[0] = "0" + parts[0].trim();
            parts[0] = "_" + parts[0].trim();
        } else {
            parts[0] = parts[0].trim();
        }
        if (isNumber(parts[1].trim())) {
            if (parts[1].trim().length() < 2) parts[1] = "0" + parts[1].trim();
            parts[1] = "_" + parts[1].trim();
        } else {
            parts[1] = parts[1].trim();
        }
        if (parts[0].contains("(")) {
            String funcName = parts[0].replaceAll(".*\\((.*)\\).*", "$1").trim();
            String funArgs = parts[0].replaceAll("(.*)\\((.*)\\).*", "$2").trim();
            String[] args = funArgs.split(",");
            for (String a : args) {
                code += "LDA " + a.trim() + " ;; Load function argument\n";
                code += "STA " + funcName + "_arg ;; Store argument for function\n";
            }
            code += "B " + funcName + " ;; Call function to get left side value\n";
        } else {
            code += "LDA " + parts[0] + " ;; Load left side of condition\n";
        }            

        if (condition.contains("==")) {       
            code += "SUB " + parts[1] + " ;; Subtract right side of condition\n";                        
            if (!labelDest.startsWith("__asm")) code += "BZ " + labelDest + " ;; Branch to destination label if zero\n";
        } else if (condition.contains("!=")) {            
            code += "SUB " + parts[1] + " ;; Subtract right side of condition\n";
            if (!labelDest.startsWith("__asm")) code += "BNZ " + labelDest + " ;; Branch to destination label if not zero\n";
        } else if (condition.contains("<")) {            
            code += "SUB " + parts[1] + " ;; Subtract right side of condition\n";
            if (!labelDest.startsWith("__asm")) code += "BB " + labelDest + " ;; Branch to destination label if less than\n";
        } else if (condition.contains(">")) {
            code += "SUB " + parts[1] + " ;; Subtract left side of condition\n";
            if (!labelDest.startsWith("__asm")) code += "BNB " + labelDest + " ;; Branch to destination label if greater than\n";
        } else if (condition.contains("<=")) {
            code += "SUB " + parts[1] + " ;; Subtract right side of condition\n";
            if (!labelDest.startsWith("__asm")) code += "BB " + labelDest + " ;; Branch to destination label if less than or equal\n";
        } else if (condition.contains(">=")) {
            code += "SUB " + parts[1] + " ;; Subtract left side of condition\n";
            if (!labelDest.startsWith("__asm")) code += "BNB " + labelDest + " ;; Branch to destination label if greater than or equal\n";
        } else {
            throw new IllegalArgumentException("Condición no soportada: " + condition);
        }
        return code;
    }

    private static boolean containsOp(String line) {
        return line.contains("+") || line.contains("-") || line.contains("*") || line.contains("/");
    }

    private static void compileExpression(String line) {
        String[] parts = line.split("=");
        String left = parts[0].replace("int", "").trim();
        String expr = parts[1].replace(";", "").trim();

        Pattern p = Pattern.compile("([\\w]+)\\s*([+\\-*/%])\\s*([\\w]+)");
        Matcher m = p.matcher(expr);
        if (m.find()) {
            String lhs = m.group(1);
            String op = m.group(2);
            String rhs = m.group(3);
            if (isNumber(lhs)) {
                lhs = Integer.toHexString(Integer.parseInt(lhs));
                while (lhs.length() < 2) lhs = "0" + lhs;
                lhs = "_" + lhs;
            }
            if (isNumber(rhs)) {
                rhs = Integer.toHexString(Integer.parseInt(rhs));
                while (rhs.length() < 2) rhs = "0" + rhs;
                rhs = "_" + rhs;
            }
            addFormattedCodeSection((isNumber(lhs) ? "LDA _" : "LDA ") + lhs);
            switch (op) {
                case "+": addFormattedCodeSection((isNumber(rhs) ? "ADD _" : "ADD ") + rhs); break;
                case "-": addFormattedCodeSection((isNumber(rhs) ? "SUB _" : "SUB ") + rhs); break;
                case "*": addFormattedCodeSection((isNumber(rhs) ? "MUL _" : "MUL ") + rhs); break;
                case "/": addFormattedCodeSection((isNumber(rhs) ? "DIV _" : "DIV ") + rhs); break;
                case "%": addFormattedCodeSection((isNumber(rhs) ? 
                // DO SUCCESSIVE SUBTRACTION FOR MODULO
                        "SUB _" : "SUB ") + rhs + "\n"
                        + "JNB __mod_loop_end_" + labelCounter + " ;; If A < 0, jump to end\n"
                        + "JMP __mod_loop_start_" + labelCounter + " ;; Repeat subtraction\n"
                        + "__mod_loop_start_" + labelCounter + ":\n"
                        + (isNumber(rhs) ? "SUB _" : "SUB ") + rhs + "\n"
                        + "JNB __mod_loop_end_" + labelCounter + " ;; If A < 0, jump to end\n"
                        + "JMP __mod_loop_start_" + labelCounter + " ;; Repeat subtraction\n"
                        + "__mod_loop_end_" + labelCounter + ":\n"
                        + "ADD " + rhs + " ;; Add back the last subtracted value to get the remainder");
                        labelCounter++;
                        break;
                default: throw new IllegalArgumentException("Operador no soportado: " + op);
            }
            addFormattedCodeSection("STA " + left);
        }
    }

    private static String adaptGlyphs(String instr) {
        Matcher matcher = Pattern.compile("\\b(\\d+)\\b").matcher(instr);
        while (matcher.find()) {
            String num = matcher.group(1);
            if (!isNumber(num)) continue;
            while (num.length() < 2) num = "0" + num;
            return "_" + num;
        }
        return instr;
    }

    private static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String preparePrintfOperand(String argument) {
        if (argument == null) return "";
        String trimmed = stripOuterParentheses(argument.trim());
        if (trimmed.isEmpty()) return trimmed;

        if (trimmed.startsWith("_") && trimmed.length() == 3) return trimmed;

        if (isCharLiteral(trimmed)) {
            int value = parseCharLiteralValue(trimmed);
            if (value >= 0) return formatImmediateHex(value);
        }

        if (isNumber(trimmed)) {
            return formatImmediateHex(Integer.parseInt(trimmed));
        }

        if (!hasTopLevelOperator(trimmed)) {
            return trimmed;
        }

        String tempName = createPrintfTemp();
        emitExpressionCode(tempName, trimmed);
        return tempName;
    }

    private static boolean isCharLiteral(String token) {
        if (token == null || token.length() < 3) return false;
        if (!token.startsWith("'") || !token.endsWith("'")) return false;
        return true;
    }

    private static int parseCharLiteralValue(String token) {
        if (!isCharLiteral(token)) return -1;
        if (token.length() == 3) {
            return token.charAt(1);
        }
        if (token.length() == 4 && token.charAt(1) == '\\') {
            char escaped = token.charAt(2);
            switch (escaped) {
                case 'n': return '\n';
                case 't': return '\t';
                case '0': return '\0';
                case '\\': return '\\';
                case '\'': return '\'';
                case '"': return '"';
                default: return escaped;
            }
        }
        return -1;
    }

    private static String formatImmediateHex(int value) {
        int normalized = value & 0xff;
        return "_" + String.format("%02x", normalized);
    }

    private static String createPrintfTemp() {
        String tempName = "__printf_tmp_" + (printfTempCounter++);
        addFormattedDataSection(tempName + " " + String.format("%08xh", varOffset--) + " 00 ;; printf temp storage");
        return tempName;
    }

    private static void emitExpressionCode(String target, String expr) {
        String sanitized = stripOuterParentheses(expr.trim());
        if (sanitized.isEmpty()) return;

        int opIndex = findTopLevelOperatorIndex(sanitized);
        if (opIndex == -1) {
            String operand = normalizeOperandToken(sanitized);
            addFormattedCodeSection(";; Compute printf argument literal: " + sanitized);
            addFormattedCodeSection("LDA " + operand + " ;; Load printf literal");
            addFormattedCodeSection("STA " + target + " ;; Store printf argument value");
            return;
        }

        char operator = sanitized.charAt(opIndex);
        String leftExpr = sanitized.substring(0, opIndex);
        String rightExpr = sanitized.substring(opIndex + 1);

        String leftOperand = operandFromExpression(leftExpr);
        String rightOperand = operandFromExpression(rightExpr);

        if (operator == '%') {
            emitModuloSequence(target, leftOperand, rightOperand, sanitized);
            return;
        }

        addFormattedCodeSection(";; Compute printf argument: " + sanitized);
        addFormattedCodeSection("LDA " + leftOperand + " ;; Load left operand");
        switch (operator) {
            case '+':
                addFormattedCodeSection("ADD " + rightOperand + " ;; Add right operand");
                break;
            case '-':
                addFormattedCodeSection("SUB " + rightOperand + " ;; Subtract right operand");
                break;
            case '*':
                addFormattedCodeSection("MUL " + rightOperand + " ;; Multiply by right operand");
                break;
            case '/':
                addFormattedCodeSection("DIV " + rightOperand + " ;; Divide by right operand");
                break;
            default:
                return;
        }
        addFormattedCodeSection("STA " + target + " ;; Store printf argument value");
    }

    private static void emitModuloSequence(String target, String leftOperand, String rightOperand, String expr) {
        String loopId = "__printf_mod_" + (printfTempCounter++);
        addFormattedCodeSection(";; Compute printf argument (mod): " + expr);
        addFormattedCodeSection("LDA " + leftOperand + " ;; Load left operand");
        addFormattedCodeSection(loopId + "_start:");
        addFormattedCodeSection("SUB " + rightOperand + " ;; Subtract divisor");
        addFormattedCodeSection("JNB " + loopId + "_end ;; Jump when result negative");
        addFormattedCodeSection("JMP " + loopId + "_start ;; Continue modulo loop");
        addFormattedCodeSection(loopId + "_end:");
        addFormattedCodeSection("ADD " + rightOperand + " ;; Restore remainder");
        addFormattedCodeSection("STA " + target + " ;; Store printf argument value");
    }

    private static String operandFromExpression(String expr) {
        String trimmed = stripOuterParentheses(expr.trim());
        if (trimmed.isEmpty()) return trimmed;

        if (hasTopLevelOperator(trimmed)) {
            String temp = createPrintfTemp();
            emitExpressionCode(temp, trimmed);
            return temp;
        }

        return normalizeOperandToken(trimmed);
    }

    private static String normalizeOperandToken(String token) {
        String trimmed = stripOuterParentheses(token.trim());
        if (trimmed.isEmpty()) return trimmed;

        if (trimmed.startsWith("_") && trimmed.length() == 3) return trimmed;

        if (isCharLiteral(trimmed)) {
            int value = parseCharLiteralValue(trimmed);
            if (value >= 0) return formatImmediateHex(value);
        }

        if (isNumber(trimmed)) {
            return formatImmediateHex(Integer.parseInt(trimmed));
        }

        return trimmed;
    }

    private static boolean hasTopLevelOperator(String expr) {
        return findTopLevelOperatorIndex(expr) != -1;
    }

    private static int findTopLevelOperatorIndex(String expr) {
        if (expr == null) return -1;
        int depth = 0;
        for (int i = 0; i < expr.length(); i++) {
            char ch = expr.charAt(i);
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                if (depth > 0) depth--;
            } else if (depth == 0 && (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%')) {
                if ((ch == '+' || ch == '-') && (i == 0 || isUnaryContext(expr, i))) continue;
                return i;
            }
        }
        return -1;
    }

    private static boolean isUnaryContext(String expr, int index) {
        for (int i = index - 1; i >= 0; i--) {
            char ch = expr.charAt(i);
            if (Character.isWhitespace(ch)) continue;
            return ch == '(' || ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%';
        }
        return true;
    }

    private static String stripOuterParentheses(String expr) {
        if (expr == null) return "";
        String result = expr.trim();
        while (result.startsWith("(") && result.endsWith(")")) {
            int depth = 0;
            boolean matches = true;
            for (int i = 0; i < result.length(); i++) {
                char ch = result.charAt(i);
                if (ch == '(') depth++;
                else if (ch == ')') {
                    depth--;
                    if (depth == 0 && i < result.length() - 1) {
                        matches = false;
                        break;
                    }
                }
            }
            if (matches && depth == 0) {
                result = result.substring(1, result.length() - 1).trim();
            } else {
                break;
            }
        }
        return result;
    }

    private static void saveToFile(String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) file.delete();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (String line : dataSection) {
            writer.write(line + "\n");
            System.out.println(line);
        }
        writer.newLine();
        for (String line : codeSection) {
            writer.write(line + "\n");
            System.out.println(line);
        }
        for (String line : printfCodeSection) {
            writer.write(line + "\n");
            System.out.println(line);
        }
        writer.close();
        System.out.println("Assembly code saved to " + filename);
    }
}