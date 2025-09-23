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

/**
 * Baremetal C Compiler for Libre8 Architecture
 * Supports hardware-specific programming without OS dependencies
 */
public class MiniCCompilerGPT {

    // Memory layout for Libre8 baremetal environment
    private static final int BASE_VAR_OFFSET = 0x00030000;  // Stack area
    private static final int BASE_STR_OFFSET = 0x00040000;  // String storage
    private static final int BASE_FUNC_OFFSET = 0x00050000; // Function code
    private static final int GLYPH_BASE_OFFSET = 0x000010ff; // Character glyphs

    private static int varOffset = BASE_VAR_OFFSET;
    private static int strOffset = BASE_STR_OFFSET;
    private static int funcOffset = BASE_FUNC_OFFSET;

    private static int labelCounter = 0;
    private static final List<String> breakStack = new ArrayList<>();
    private static final List<String> continueStack = new ArrayList<>();
    private static final ArrayList<String> blockEndLabel = new ArrayList<>();

    private static final List<String> dataSection = new ArrayList<>();
    private static final List<String> codeSection = new ArrayList<>();

    private static final Map<String, Integer> variables = new LinkedHashMap<>();
    private static final Map<String, String> initialValues = new LinkedHashMap<>();
    private static final Map<String, String> strings = new LinkedHashMap<>();
    private static final Map<String, Integer> functions = new LinkedHashMap<>();
    private static final Map<String, List<String>> functionBodies = new LinkedHashMap<>();
    private static final Map<String, List<String>> ifBodies = new LinkedHashMap<>();
    private static final Map<String, List<String>> functionArgs = new LinkedHashMap<>();

    private static boolean noIfElse = false;
    private static final Map<String, List<String>> conditions = new LinkedHashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Libre8 Baremetal C Compiler - Generating Assembly");
        String input = readFile("C_example.c");
        input = preprocessSource(input);
        parseGlobals(input);
        parseFunctions(input);
        generateDataSection();
        generateCodeSection();
        saveToFile("main.as");
        System.out.println("Compilation complete. Output: main.as");
    }

    public static void run(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) filename = "C_example.c";
        String input = readFile(filename);
        input = preprocessSource(input);
        parseGlobals(input);
        parseFunctions(input);
        generateDataSection();
        generateCodeSection();
        saveToFile("main.as");
    }

    // Preprocess C source: remove comments, handle #defines, #includes
    private static String preprocessSource(String src) {
        // Remove single-line comments
        src = src.replaceAll("//.*", "");
        // Remove multi-line comments
        src = src.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        // Handle simple #define constants
        Pattern definePattern = Pattern.compile("#define\\s+(\\w+)\\s+(\\d+)");
        Matcher defineMatcher = definePattern.matcher(src);
        Map<String, String> defines = new LinkedHashMap<>();
        while (defineMatcher.find()) {
            defines.put(defineMatcher.group(1), defineMatcher.group(2));
        }
        // Replace #defines in source
        for (Map.Entry<String, String> def : defines.entrySet()) {
            src = src.replaceAll("\\b" + def.getKey() + "\\b", def.getValue());
        }
        // Remove preprocessor directives
        src = src.replaceAll("#.*", "");
        return src;
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
        // Parse only global variable declarations (not function declarations)
        Pattern pattern = Pattern.compile("^\\s*(int|char|uint8|uint16|uint32)\\s+(\\w+)\\s*=\\s*([^;]+);", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            String type = matcher.group(1);
            String name = matcher.group(2);
            String value = matcher.group(3).trim();

            // Handle baremetal types
            if (type.equals("char") || type.equals("uint8")) {
                int val = 0;
                if (value.startsWith("'")) {
                    val = (int) value.charAt(1);
                } else if (value.startsWith("0x")) {
                    val = Integer.parseInt(value.substring(2), 16);
                } else if (value.matches("\\d+")) {
                    val = Integer.parseInt(value);
                }
                variables.put(name, varOffset--);
                initialValues.put(name, String.valueOf(val & 0xFF));
            } else if (type.equals("int") || type.equals("uint16") || type.equals("uint32")) {
                variables.put(name, varOffset--);
                int val = 0;
                if (value.startsWith("0x")) {
                    val = Integer.parseInt(value.substring(2), 16);
                } else if (value.matches("\\d+")) {
                    val = Integer.parseInt(value);
                } else {
                    // Expression or reference - set to 0 initially
                    val = 0;
                }
                String decimalValue = String.valueOf(val & 0xFF);
                initialValues.put(name, decimalValue);
            } else if (name.startsWith("*") || name.endsWith("[]")) {
                // String or array handling
                if (name.startsWith("*")) name = name.substring(1);
                while (variables.containsKey(name)) name += "_";
                String cleanString = value.replaceAll("\"", "");
                strings.put(name, cleanString);
                for (int i = 0; i < cleanString.length(); i++) {
                    String varName = name + i;
                    while (variables.containsKey(varName)) varName += "_";
                    variables.put(varName, strOffset);
                    initialValues.put(varName, String.format("%02x", (int) cleanString.charAt(i)));
                    strOffset--;
                }
            }
        }
        
        // Parse variable declarations without initialization
        Pattern uninitPattern = Pattern.compile("(int|char|uint8|uint16|uint32)\\s+(\\w+);");
        Matcher uninitMatcher = uninitPattern.matcher(src);
        while (uninitMatcher.find()) {
            String name = uninitMatcher.group(2);
            if (!variables.containsKey(name)) {
                variables.put(name, varOffset--);
                initialValues.put(name, "00");
            }
        }
    }

    private static void parseFunctions(String src) {
        // Parse function definitions (supports baremetal types and void parameters)
        Pattern pattern = Pattern.compile("(int|void|char|uint8|uint16|uint32)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{(.*?)\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            String returnType = matcher.group(1);
            String name = matcher.group(2);
            String args = matcher.group(3);
            String body = matcher.group(4);

            List<String> argList = new ArrayList<>();
            if (!args.trim().isEmpty() && !args.trim().equals("void")) {
                for (String arg : args.split(",")) {
                    String[] parts = arg.trim().split("\\s+");
                    if (parts.length >= 2) {
                        argList.add(parts[1]); // Add argument name
                    }
                }
            }
            
            functionArgs.put(name, argList);
            // Split function body into lines, preserving structure
            List<String> bodyLines = new ArrayList<>();
            for (String line : body.split("\\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    bodyLines.add(trimmed);
                }
            }
            functionBodies.put(name, bodyLines);
            functions.put(name, funcOffset);
            funcOffset -= 0x1000; // Smaller function spacing for baremetal
            
            System.out.println("Parsed function: " + name + " (" + returnType + ") with " + argList.size() + " args");
        }
    }

    private static void generateDataSection() {
        addFormattedDataSection(";;;;;;;;;;;;;;;\n;; DATA BEGIN ;;\n;;;;;;;;;;;;;;;\n.data");
        addFormattedDataSection(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n;; GLYPHS: _00 00001100 00 - _ff 000011ff ff ;;\n;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        for (int i = 0; i <= 255; i++)
            addFormattedDataSection(String.format("_%02x %08x %d", i, GLYPH_BASE_OFFSET + i, i));
        addFormattedDataSection(";;;;;;;;;;;;;;;\n;; GLYPHS END ;;\n;;;;;;;;;;;;;;;");
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; VARIABLES ;;\n;;;;;;;;;;;;;;;");
        for (Map.Entry<String, Integer> entry : variables.entrySet()) {
            String name = entry.getKey();
            int offset = entry.getValue();
            String val = initialValues.getOrDefault(name, "0");
            addFormattedDataSection(String.format("%s %08x %s", name, offset, val));
        }
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; VARIABLES END ;;\n;;;;;;;;;;;;;;;");
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; STRINGS ;;\n;;;;;;;;;;;;;;;");
        for (Map.Entry<String, String> entry : strings.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            addFormattedDataSection(String.format("%s %08x \"%s\"", name, strOffset, value));
            strOffset -= value.length() + 1;
        }
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; STRINGS END ;;\n;;;;;;;;;;;;;;;");
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; FUNCTIONS ;;\n;;;;;;;;;;;;;;;");
        for (Map.Entry<String, Integer> entry : functions.entrySet()) {
            String fn = entry.getKey();
            String args = String.join(" ", functionArgs.get(fn));
            addFormattedDataSection(String.format("%s %08x %s", fn, entry.getValue(), args));
        }
        addFormattedDataSection("\n;;;;;;;;;;;;;;;\n;; DATA END ;;\n;;;;;;;;;;;;;;;");
    }

    private static void generateCodeSection() {
        addFormattedCodeSection(";;;;;;;;;;;;;;;;\n"
                + ";; CODE BEGIN ;;\n"
                + ";;;;;;;;;;;;;;;;\n"
                + ".code\n"
                + "B main ;; Branch to main function\n"
                + "JMP 00000000 ;; Jump to end of code section (placeholder for main function)\n");
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
            for (String arg : functionArgs.get(name)) addFormattedCodeSection(";; Argument: " + arg);
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
                String nextTrim = body.get(body.indexOf(line) + 1).trim();
                String functionName = "";
                if (nextTrim.startsWith("__asm")) {
                    functionName = "__asm_" + labelCounter;
                    labelCounter += 1;
                } else {
                    int parenIndex = nextTrim.indexOf("(");
                    if (parenIndex > 0) {
                        functionName = nextTrim.substring(0, parenIndex).trim();
                    } else {
                        functionName = "unknown_func_" + labelCounter;
                        labelCounter += 1;
                    }
                }
                addFormattedCodeSection(parseIf(trim, functionName));
                noIfElse = false;
                continue;
            }

            if (trim.startsWith("else if(") || trim.startsWith("else if (") || trim.startsWith("} else if (") || trim.startsWith("} else if(") || trim.startsWith("}else if (") || trim.startsWith("}else if(") || trim.startsWith("else{") || trim.startsWith("} else if (")) {
                String nextTrim = body.get(body.indexOf(line) + 1).trim();
                String functionName = "";
                if (nextTrim.startsWith("__asm")) {
                    functionName = "__asm_" + labelCounter;
                    labelCounter += 1;
                } else {
                    functionName = nextTrim.substring(0, nextTrim.indexOf("(")).trim();
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
                String nextTrim = body.get(body.indexOf(line) + 1).trim();
                String functionName = "";
                if (nextTrim.startsWith("__asm")) {
                    functionName = "__asm_" + labelCounter;
                    labelCounter += 1;
                } else {
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
                blockEndLabel.add("__while_end_" + labelCounter);
                addFormattedDataSection(loopStart + " " + String.format("%08x", funcOffset) + " ;; While loop start");
                funcOffset -= 0x4000;
                labelCounter++;
                continue;
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
                Integer topValValue = Integer.valueOf(cond.replaceAll("\\(", "").replaceAll("\\)", "").trim().split("<|>|==|!=|<=|>=|\\+|-|\\*|/")[1].trim());
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
                int lineIndex = body.indexOf(line);
                String nextTrim = "";
                if (lineIndex + counter < body.size()) {
                    nextTrim = body.get(lineIndex + counter).trim();
                    if (nextTrim.startsWith("__asm")) {
                        // Handle inline assembly
                        //addFormattedDataSection(loopStart + " " + String.format("%08xh", funcOffset) + "h ;; For loop start");
                        //funcOffset -= 0x4000; // Adjust function offset for next label
                        while (!nextTrim.equals("}") && lineIndex + counter < body.size()) {
                            nextTrim = body.get(lineIndex + counter).trim();
                            loopBody += nextTrim + "\n";
                            counter++;
                        }
                    } else {
                        loopBody = nextTrim.split("\\(")[0]; // Assuming the loop body is a single line for simplicity
                    }
                }

                while (value.length() < 2) {
                    value = "0" + value; // Ensure at least two characters for char values
                }

                addFormattedDataSection(topVal + " " + String.format("%08x", varOffset--) + " " + String.valueOf(topValValue) + " ;; For loop top value");
                if (inc.contains("++")) {
                    addFormattedDataSection(incVar + " " + String.format("%08x", varOffset--) + " 01 ;; For loop step");
                } else if (inc.contains("--")) {
                    addFormattedDataSection(incVar + " " + String.format("%08x", varOffset--) + " FF ;; For loop step");
                } else {
                    if (inc.contains("+=")) {
                        String incValue = inc.split("\\+=")[1].trim();
                        if (incValue.length() < 2) {
                            incValue = "0" + incValue; // Ensure at least two characters for char values
                        }
                        addFormattedDataSection(incVar + " " + String.format("%08x", varOffset--) + " " + String.valueOf(topValValue) + " ;; For loop step");
                    } else if (inc.contains("-=")) {
                        String incValue = inc.split("-=")[1].trim();
                        if (incValue.length() < 2) {
                            incValue = "0" + incValue; // Ensure at least two characters for char values
                        }
                        addFormattedDataSection(incVar + " " + String.format("%08x", varOffset--) + " " + String.valueOf(topValValue) + " ;; For loop step");
                    } else {
                        addFormattedDataSection(incVar + " " + String.format("%08x", varOffset--) + " 00 ;; For loop step");
                    }
                }

                addFormattedDataSection(loopEnd + " " + String.format("%08x", funcOffset) + " ;; For loop end");
                funcOffset -= 0x4000; // Adjust function offset for next label
                addFormattedDataSection(loopStart + " " + String.format("%08x", funcOffset) + " ;; For loop start");
                funcOffset -= 0x4000; // Adjust function offset for next label
                addFormattedDataSection(loopInit + " " + String.format("%08x", funcOffset) + " ;; For loop initialization");
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

                endif = true; // Set flag to skip next lines
                labelCounter++;

                continue;
            }

            if (trim.startsWith("do {")) {
                String label = "__do_while_start_" + labelCounter;
                blockEndLabel.add("__do_while_end_" + labelCounter);
                labelCounter++;
                continue;
            }
            if (trim.startsWith("} while(")) {
                String condition = trim.substring(trim.indexOf("(") + 1, trim.lastIndexOf(")"));
                String startLabel = continueStack.remove(continueStack.size() - 1);
                String endLabel = breakStack.remove(breakStack.size() - 1);
                blockEndLabel.add("__do_while_end_" + labelCounter);
                labelCounter++;
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
            } else if (trim.contains("BX")) {
                addFormattedCodeSection("BX _00 ;; Return from function");
            } else if (trim.contains("POP")) {
                addFormattedCodeSection("POPX " + functionArgs.get(name).get(0) + " ;; Pop the argument from the stack");
            } else if (trim.startsWith("OUT ")) {
                String arg = trim.substring(4).trim();
                addFormattedCodeSection("OUT " + arg + " ;; Output var");
            } else if (trim.equals("OUTA") || trim.equals("OUTB") || trim.equals("OUTC") || trim.equals("OUTD")
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
            } else if (trim.contains("=") && containsOp(trim)) {
                compileExpression(trim);
            } else if (trim.contains("=")) {
                String[] parts = trim.split("=");
                String left = parts[0].trim();
                String right = parts[1].replace(";", "").trim();
                
                // Remove type declarations from left side
                left = left.replaceAll("\\b(int|char|uint8|uint16|uint32)\\b\\s*", "");
                
                // Handle function calls on right side
                if (right.contains("()")) {
                    String funcName = right.replace("()", "");
                    addFormattedCodeSection("B " + funcName + " ;; Call function");
                    addFormattedCodeSection("STA " + left + " ;; Store return value");
                } else {
                    // Use formatOperand to handle all operand types properly
                    String rightOperand = formatOperand(right);
                    addFormattedCodeSection("LDA " + rightOperand + " ;; Load value");
                    addFormattedCodeSection("STA " + left + " ;; Store value");
                }
            } else if (trim.contains("(") && !noIfElse) {
                String call = trim.split("\\(")[0].trim();
                String arg = trim.replaceAll(".*\\((.*)\\).*", "$1");
                if (!arg.isEmpty()) {
                    addFormattedCodeSection((isNumber(arg) ? "LDA _" : "LDA ") + arg);
                    addFormattedCodeSection("PSAX ;; Push the argument onto the stack");
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
            String value = entry.getValue().toString().replace("[", "").replace("]", "").trim();
                       
            // Split on comparison operators, handle <= and >= first
            String[] parts;
            String operator = "";
            if (value.contains("<=")) {
                parts = value.split("<=");
                operator = "<=";
            } else if (value.contains(">=")) {
                parts = value.split(">=");
                operator = ">=";
            } else if (value.contains("==")) {
                parts = value.split("==");
                operator = "==";
            } else if (value.contains("!=")) {
                parts = value.split("!=");
                operator = "!=";
            } else if (value.contains("<")) {
                parts = value.split("<");
                operator = "<";
            } else if (value.contains(">")) {
                parts = value.split(">");
                operator = ">";
            } else if (value.contains("&&")) {
                parts = value.split("&&");
                operator = "&&";
            } else if (value.contains("||")) {
                parts = value.split("||");
                operator = "||";
            } else {
                code += ";; Invalid condition format\n";
                continue;
            }
            
            if (parts.length < 2) {
                code += ";; Invalid condition format\n";
                continue;
            }
            
            String left = parts[0].trim();
            String right = parts[1].trim();
            
            // Format operands correctly using formatOperand
            String leftOperand = formatOperand(left);
            String rightOperand = formatOperand(right);
            
            // Generate appropriate else condition (opposite of original)
            switch (operator) {
                case "==":
                    code += "LDA " + leftOperand + " ;; Load left side\n";
                    code += "SUB " + rightOperand + " ;; Subtract right side\n";
                    if (labelEnd.startsWith("__asm")) code += "JNZ " + labelEnd.replace("__asm", "") + " ;; Branch to else if not equal\n";
                    else code += "BNZ " + labelEnd + " ;; Branch to else if not equal\n";
                    break;
                case "!=":
                    code += "LDA " + leftOperand + " ;; Load left side\n";
                    code += "SUB " + rightOperand + " ;; Subtract right side\n";
                    if (labelEnd.startsWith("__asm")) code += "JZ " + labelEnd.replace("__asm", "") + " ;; Branch to else if equal\n";
                    else code += "BZ " + labelEnd + " ;; Branch to else if equal\n";
                    break;
                // For else conditions, we need the opposite logic
                default:
                    code += ";; Unsupported operator for else: " + operator + "\n";
                    break;
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

    private static String generateConditionalBody(String condition, String ifLabel, String labelDest) {
        String code = ";; Conditional code for: " + condition + " \n";
        
        // Handle complex conditions with && and ||
        if (condition.contains("&&") || condition.contains("||")) {
            // For now, just generate a simple comment for complex conditions
            code += ";; Complex condition - simplified\n";
            return code;
        }
        
        // Split on comparison operators, handle <= and >= first
        String[] parts;
        String operator = "";
        if (condition.contains("<=")) {
            parts = condition.split("<=");
            operator = "<=";
        } else if (condition.contains(">=")) {
            parts = condition.split(">=");
            operator = ">=";
        } else if (condition.contains("==")) {
            parts = condition.split("==");
            operator = "==";
        } else if (condition.contains("!=")) {
            parts = condition.split("!=");
            operator = "!=";
        } else if (condition.contains("<")) {
            parts = condition.split("<");
            operator = "<";
        } else if (condition.contains(">")) {
            parts = condition.split(">");
            operator = ">";
        } else {
            return code + ";; Invalid condition\n";
        }
        
        if (parts.length < 2) {
            return code + ";; Invalid condition format\n";
        }
        
        String left = parts[0].trim();
        String right = parts[1].trim();
        
        // Format operands correctly
        String leftOperand = formatOperand(left);
        String rightOperand = formatOperand(right);
        
        // Generate appropriate comparison
        String label = "if_" + (labelCounter++);
        switch (operator) {
            case "==":
                code += "LDA " + leftOperand + " ;; Load left side\n";
                code += "SUB " + rightOperand + " ;; Subtract right side\n";
                code += "BZ " + label + " ;; Branch if equal\n";
                break;
            case "!=":
                code += "LDA " + leftOperand + " ;; Load left side\n";
                code += "SUB " + rightOperand + " ;; Subtract right side\n";
                code += "BNZ " + label + " ;; Branch if not equal\n";
                break;
            case "<":
                code += "LDA " + leftOperand + " ;; Load left side\n";
                code += "SUB " + rightOperand + " ;; Subtract right side\n";
                code += "BB " + label + " ;; Branch if less than\n";
                break;
            case ">":
                code += "LDA " + rightOperand + " ;; Load right side\n";
                code += "SUB " + leftOperand + " ;; Subtract left side\n";
                code += "BB " + label + " ;; Branch if greater than\n";
                break;
            case "<=":
                code += "LDA " + rightOperand + " ;; Load right side\n";
                code += "SUB " + leftOperand + " ;; Subtract left side\n";
                code += "BNB " + label + " ;; Branch if less than or equal\n";
                break;
            case ">=":
                code += "LDA " + leftOperand + " ;; Load left side\n";
                code += "SUB " + rightOperand + " ;; Subtract right side\n";
                code += "BNB " + label + " ;; Branch if greater than or equal\n";
                break;
        }
        
        return code;
    }
    
    private static String formatOperand(String operand) {
        if (isNumber(operand)) {
            int num = Integer.parseInt(operand);
            return String.format("_%02x", num);
        } else if (variables.containsKey(operand)) {
            return operand;
        } else {
            return "_00"; // Default value
        }
    }

    private static boolean containsOp(String line) {
        return line.contains("+") || line.contains("-") || line.contains("*") || line.contains("/");
    }

    private static void compileExpression(String line) {
        String[] parts = line.split("=");
        String left = parts[0].replaceAll("\\b(int|uint8|char)\\b", "").trim();
        String expr = parts[1].replace(";", "").trim();

        Pattern p = Pattern.compile("([\\w]+)\\s*([+\\-*/]|&&|\\|\\|)\\s*([\\w]+)");
        Matcher m = p.matcher(expr);
        if (m.find()) {
            String lhs = m.group(1);
            String op = m.group(2);
            String rhs = m.group(3);
            
            // Format operands properly - use decimal values directly
            String lhsOperand = formatOperand(lhs);
            String rhsOperand = formatOperand(rhs);
            
            addFormattedCodeSection("LDA " + lhsOperand);
            switch (op) {
                case "+": addFormattedCodeSection("ADD " + rhsOperand); break;
                case "-": addFormattedCodeSection("SUB " + rhsOperand); break;
                case "*": addFormattedCodeSection("MUL " + rhsOperand); break;
                case "/": addFormattedCodeSection("DIV " + rhsOperand); break;
                case "&&": addFormattedCodeSection("AND " + rhsOperand); break;
                case "||": addFormattedCodeSection("OR " + rhsOperand); break;
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
        writer.close();
        System.out.println("Assembly code saved to " + filename);
    }
}
