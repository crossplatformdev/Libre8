package org.elijaxapps.libre8;

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

public class MiniCCompiler {

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

    private static final Map<String, Integer> variables = new LinkedHashMap<>();
    private static final Map<String, String> initialValues = new LinkedHashMap<>();
    private static final Map<String, String> strings = new LinkedHashMap<>();
    private static final Map<String, Integer> functions = new LinkedHashMap<>();
    private static final Map<String, List<String>> functionBodies = new LinkedHashMap<>();
    private static final Map<String, List<String>> ifBodies = new LinkedHashMap<>();
    private static final Map<String, List<String>> functionArgs = new LinkedHashMap<>();

    private static boolean noIfElse = false; // Flag to skip if-else parsing
    private static final Map<String, List<String>> conditions = new LinkedHashMap<>();
    public static void main(String[] args) throws IOException {
        String input = readFile("C_example.c");
        parseGlobals(input);
        parseFunctions(input);
        generateDataSection();
        generateCodeSection();
        saveToFile("main.as");
    }

    public static void run(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            filename = "C_example.c";
        }
        String input = readFile(filename);
        parseGlobals(input);
        parseFunctions(input);
        generateDataSection();
        generateCodeSection();
        saveToFile("main.as");
    }

    private static String readFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private static void parseGlobals(String src) {
        Pattern pattern = Pattern.compile("(int|char)\\s+(\\*?\\w+)\\s*=\\s*([^;]+);");
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            String type = matcher.group(1);
            String name = matcher.group(2);
            String value = matcher.group(3).trim();

            if (type.equals("char") && !name.startsWith("*")) {
                int val = value.startsWith("'") ? (int) value.charAt(1) : Integer.parseInt(value);
                variables.put(name, varOffset--);
                initialValues.put(name, val < 0 ? "00" : Integer.toString(val, 16));

            } else if ((type.equals("char") && name.startsWith("*")) || type.equals("char*") || name.endsWith("[]")) {
                if (name.startsWith("*")) {
                    name = name.substring(1);
                }
                while (variables.containsKey(name)) {
                    name += "_";
                }

                String cleanString = value.replaceAll("\"", "");
                strings.put(name, cleanString);
                for (int i = 0; i < cleanString.length(); i++) {
                    String varName = name + i;
                    while (variables.containsKey(varName)) {
                        varName += "_";
                    }
                    variables.put(varName, strOffset);
                    initialValues.put(varName, "'" + cleanString.charAt(i) + "'");
                    strOffset--;
                }

            } else if (type.equals("int")) {
                variables.put(name, varOffset--);
                if (value.matches("\\d+")) {
                    initialValues.put(name, Integer.toHexString(Integer.parseInt(value)));
                    //If value is not a number, assume it is an expression
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
                for (String arg : args.split(",")) {
                    argList.add(arg.trim().split(" ")[1]);
                }
            }
            functionArgs.put(name, argList);
            functionBodies.put(name, Arrays.asList(body.trim().split("\\n")));
            functions.put(name, funcOffset);
            dataSection.add(name + " " + String.format("%08xh", funcOffset) + " ;; Function offset");
            funcOffset -= 0x4000;
        }
    }

    private static void generateDataSection() {
        dataSection.add(";;;;;;;;;;;;;;;\n;; DATA BEGIN ;;\n;;;;;;;;;;;;;;;\n.data");
        dataSection.add(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n;; GLYPHS: _00 00600000h 00 - _ff 006000ffh ff ;;\n;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        for (int i = 0; i <= 255; i++) {
            dataSection.add(String.format("_%02x %08xh %02x", i, GLYPH_BASE_OFFSET + i, i));
        }
        dataSection.add(";;;;;;;;;;;;;;;\n;; GLYPHS END ;;\n;;;;;;;;;;;;;;;");
        dataSection.add("\n;;;;;;;;;;;;;;;\n;; VARIABLES ;;\n;;;;;;;;;;;;;;;");
        for (Map.Entry<String, Integer> entry : variables.entrySet()) {
            String name = entry.getKey();
            int offset = entry.getValue();
            String val = initialValues.getOrDefault(name, "0");
            dataSection.add(String.format("%s %08xh %s ;; variable initialized", name, offset, val));
        }
        dataSection.add("\n;;;;;;;;;;;;;;;\n;; VARIABLES END ;;\n;;;;;;;;;;;;;;;");
        dataSection.add("\n;;;;;;;;;;;;;;;\n;; STRINGS ;;\n;;;;;;;;;;;;;;;");
        for (Map.Entry<String, String> entry : strings.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            dataSection.add(String.format("%s %08xh \"%s\" ;; string", name, strOffset, value));
            strOffset -= value.length() + 1; // Adjust offset for next string
        }
        dataSection.add("\n;;;;;;;;;;;;;;;\n;; STRINGS END ;;\n;;;;;;;;;;;;;;;");
        dataSection.add("\n;;;;;;;;;;;;;;;\n;; FUNCTIONS ;;\n;;;;;;;;;;;;;;;");
        for (Map.Entry<String, Integer> entry : functions.entrySet()) {
            String fn = entry.getKey();
            String args = String.join(" ", functionArgs.get(fn));
            dataSection.add(String.format("%s %08xh %s;; function offset", fn, entry.getValue(), args));
        }
        dataSection.add("\n;;;;;;;;;;;;;;;\n;; DATA END ;;\n;;;;;;;;;;;;;;;");
    }

    private static void generateCodeSection() {
        codeSection.add(";;;;;;;;;;;;;;;\n"
                + ";; CODE BEGIN ;;\n"
                + ";;;;;;;;;;;;;;;\n"
                + ".code\n"
                + "B main ;; Branch to main function"
                + "\n");

        while (functionBodies.size() > 0) {
            Map.Entry<String, List<String>> entry = functionBodies.entrySet().iterator().next();
            generateCode(entry);
            functionBodies.remove(entry.getKey());
        }

        for (Map.Entry<String, List<String>> fn : ifBodies.entrySet()) {
            String[] parts = fn.getKey().split("\\+\\+\\+");
            String ifLabel = parts[0];
            String elseLabel = parts.length > 1 ? parts[1] : "ELSE_" + labelCounter++;
            codeSection.add(";; If-Else block: " + ifLabel + " else " + elseLabel);
            String condition = fn.getValue().toString().replace("[", "").replace("]", "").trim();
            generateConditionalBody(condition, ifLabel, elseLabel);
        }

    }

    public static void generateCode(Map.Entry<String, List<String>> fn) {
        String name = fn.getKey();
        List<String> body = fn.getValue();
        codeSection.add(";;;;;;;;;;;;;;;\n;; BEGIN " + name + " ;;");
        codeSection.add("." + name);

        if (!functionArgs.isEmpty()) {
            for (String arg : functionArgs.get(name)) {
                codeSection.add(";; Argument: " + arg);
            }
        }
        boolean endif = false;
        for (String line : body) {
            if (endif) {
                // Reset flag to allow if-else parsing
                endif = false;
                noIfElse = false; // Reset flag to allow if-else parsing
                continue;
            }
            String trim = line.trim();
            if (trim.isEmpty() || trim.startsWith("__asm") || trim.startsWith(";;")) {
                continue;
            }

            if (trim.startsWith("if(") || trim.startsWith("if (")) {
                //Guess if next trim is an function call or __asm
                String nextTrim = body.get(body.indexOf(line) + 1).trim();
                String functionName = "";
                if (nextTrim.startsWith("__asm")) {
                    // Handle inline assembly
                } else {
                    // Handle function call
                    functionName = nextTrim.substring(0, nextTrim.indexOf("(")).trim();
                }
                parseIf(trim, functionName);
                noIfElse = true;
                continue;
            }

            if (trim.startsWith("else if(") || trim.startsWith("else if (") || trim.startsWith("} else if (") || trim.startsWith("} else if(") || trim.startsWith("}else if (") || trim.startsWith("}else if(") || trim.startsWith("else{") || trim.startsWith("} else if (")) {
                //Guess if next trim is an function call or __asm
                String nextTrim = body.get(body.indexOf(line) + 1).trim();
                String functionName = "";
                if (nextTrim.startsWith("__asm")) {
                    // Handle inline assembly
                } else {
                    // Handle function call
                    functionName = nextTrim.substring(0, nextTrim.indexOf("(")).trim();
                }
                parseIf(trim, functionName);
                noIfElse = true;
                continue;
            }

            if (trim.startsWith("else{") || trim.startsWith("else {") || trim.startsWith("} else {") || trim.startsWith("} else{") || trim.startsWith("}else {") || trim.startsWith("}else{")) {
                                //Guess if next trim is an function call or __asm
                String nextTrim = body.get(body.indexOf(line) + 1).trim();
                String functionName = "";
                if (nextTrim.startsWith("__asm")) {
                    // Handle inline assembly
                    codeSection.add(";; Inline assembly");
                    int counter = 0;
                    while (!nextTrim.startsWith("}")) {
                        codeSection.add(nextTrim);
                        nextTrim = body.get(body.indexOf(line) + 2).trim();
                        counter++;
                    }
                } else {
                    // Handle function call
                    functionName = nextTrim.substring(0, nextTrim.indexOf("(")).trim();
                }
                parseElse(trim, functionName);
                endif = true; // Reset flag to allow if-else parsing
                continue;
            }

            if (trim.equals("break;")) {
                if (!breakStack.isEmpty()) {
                    codeSection.add("JMP " + breakStack.get(breakStack.size() - 1));
                }
                continue;
            }

            if (trim.equals("continue;")) {
                if (!continueStack.isEmpty()) {
                    codeSection.add("JMP " + continueStack.get(continueStack.size() - 1));
                }
                continue;
            }

            // WHILE
            if (trim.startsWith("while(")) {
                String condition = trim.substring(trim.indexOf("(") + 1, trim.lastIndexOf(")"));
                String loopStart = "__while_start_" + labelCounter;
                blockEndLabel.add("__while_end_" + labelCounter);
                //Address is 32b ending in h: ex 00000000h
                dataSection.add(loopStart + " " + String.format("%08xh", funcOffset) + "h ;; While loop start");
                funcOffset -= 0x4000; // Adjust function offset for next label
                labelCounter++;
                continue;
            }

            // FOR (assumes format for(init; condition; inc))
            if (trim.startsWith("for(")) {
                String content = trim.substring(trim.indexOf("(") + 1, trim.lastIndexOf(")"));
                String[] parts = content.split(";");
                String init = parts[0].trim(), cond = parts[1].trim(), inc = parts[2].trim();
                String loopStart = "__for_start_" + labelCounter;
                String loopEnd = "__for_end_" + labelCounter;
                String incLabel = "__for_inc_" + labelCounter;
                blockEndLabel.add(loopEnd);
                labelCounter++;

                continue;
            }

            // DO-WHILE
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

            // SWITCH & CASE (básico)
            if (trim.startsWith("switch(")) {
                // Implementar con análisis de case:
                // codeSection.add("LDA <switch_var>"); guardar estado actual para comparaciones posteriores
                continue;
            }
            if (trim.startsWith("case ")) {
                // codeSection.add("SUB <value>"); codeSection.add("JZ __case_label_<counter>");
                continue;
            }
            if (trim.equals("default:")) {
                // codeSection.add("__default_label_<counter>");
                continue;
            }

            if (trim.equals("}")) {
                if (!breakStack.isEmpty()) {
                    breakStack.remove(breakStack.size() - 1);
                }
                if (!continueStack.isEmpty()) {
                    continueStack.remove(continueStack.size() - 1);
                }

                /*
                    if (!blockEndLabel.isEmpty()) {
                        String endLabel = blockEndLabel.remove(blockEndLabel.size() - 1);
                        codeSection.add("BX _00 ;; Return to end of block");
                    }

                 */
                codeSection.add(";; End of block");
                continue;
            } else if (trim.contains("BX")) {
                codeSection.add("BX _00 ;; Return from function");
            } else if (trim.contains("POP")) {
                codeSection.add("POPX " + functionArgs.get(name).get(0) + " ;; Pop the argument from the stack");
            } else if (trim.startsWith("OUT ")) {
                String arg = trim.substring(4).trim();
                codeSection.add("OUT " + arg + " ;; Output var");
            } else if (trim.equals("OUTA") || trim.equals("OUTB") || trim.equals("OUTC") || trim.equals("OUTD")
                    || trim.equals("DEC") || trim.equals("DECE") || trim.equals("HLT") || trim.equals("NOP")
                    || trim.equals("RST") || trim.equals("PST") || trim.equals("PTRI") || trim.equals("PTRD")
                    || trim.equals("PTRL") || trim.equals("PTRS")) {
                codeSection.add(trim + " ;; Inline mnemonic");
            } else if (trim.startsWith("LDA") || trim.startsWith("LDB") || trim.startsWith("LDC") || trim.startsWith("LDD")
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
                    || trim.startsWith("NOP") || trim.startsWith("RST") || trim.startsWith("PST")) {

                codeSection.add(trim);
            } else if (trim.contains("=") && containsOp(trim)) {
                compileExpression(trim);
            } else if (trim.contains("=")) {
                String[] parts = trim.split("=");
                String left = parts[0].trim();
                String right = parts[1].replace(";", "").trim();
                codeSection.add("LDA  " + right + " ;; Load value into A");
                codeSection.add("STA  " + left + " ;; Store value from A into " + left);
            } else if (trim.contains("(") && !noIfElse) {
                String call = trim.split("\\(")[0].trim();
                String arg = trim.replaceAll(".*\\((.*)\\).*", "$1");
                if (!arg.isEmpty()) {
                    codeSection.add((isNumber(arg) ? "LDA _" : "LDA ") + arg);
                    codeSection.add("PSAX ;; Push the argument onto the stack");
                }
                codeSection.add("B " + call + " ;; Call the function");
            } else if (trim.startsWith(".")) {
                trim = trim.substring(1).trim(); // Remove leading dot
                codeSection.add(";; Label: " + trim);
                codeSection.add(trim);
                //dataSection.add(trim + " " + Integer.toHexString(funcOffset) + "h ;; Label offset");
                //funcOffset -= 0x4000; // Adjust function offset for next label
            }
        }

        if (name.equals("main")) {
            codeSection.add("BX _00 ;; Return from main");
        }

        codeSection.add(";;;;;;;;;;;;;;;\n;; END " + name + " ;;");
    }

    private static void parseIf(String trim, String functionName) {
        String condition = trim.substring(trim.indexOf("(") + 1, trim.lastIndexOf(")"));
        String labelIn = "IF_IN_" + labelCounter;
        String labelDest = functionName;

        //dataSection.add(labelIn + " " + String.format("%08xh", funcOffset) + " ;; If label");
        //funcOffset -= 0x4000; // Adjust function offset for next label
        //dataSection.add(labelDest + " " + String.format("%08xh", funcOffset) + " ;; DEST of if label");
        //funcOffset -= 0x4000; // Adjust function offset for next label
        generateConditionalCall(condition, labelIn, labelDest);
        conditions.put(labelDest, Arrays.asList(condition.split("\\n")));
        labelCounter++;
    }

    private static void parseElse(String trim, String functionName) {
        String labelEnd = functionName;
        String code = ";; Else block code for: " + labelEnd + "\n";
        codeSection.add(code);
        for(Map.Entry<String, List<String>> entry : conditions.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString().replace("[", "").replace("]", "").trim();

            if(!codeSection.contains(";; Else condition for: " + labelEnd)) {
                codeSection.add(";; Else condition for: " + labelEnd);
            
                if (value.contains("==")) {
                    // If the previous condition was ==, we need to check if they are different (always the opposite)
                    String[] parts = value.split("==");
                    codeSection.add("LDA " + parts[1].trim() + " ;; Load left side of condition");
                    codeSection.add("SUB " + parts[0].trim() + " ;; Subtract right side of condition");
                    codeSection.add("BNZ " + labelEnd + " ;; Branch to else label if not zero");
                } else if (value.contains("!=")) {
                    String[] parts = value.split("!=");
                    codeSection.add("LDA " + parts[1].trim() + " ;; Load left side of condition");
                    codeSection.add("SUB " + parts[0].trim() + " ;; Subtract right side of condition");
                    codeSection.add("BZ " + labelEnd + " ;; Branch to else label if zero");
                } else if (value.contains("<=")) {
                    String[] parts = value.split("<=");
                    codeSection.add("LDA " + parts[1].trim() + " ;; Load left side of condition");
                    codeSection.add("SUB " + parts[0].trim() + " ;; Subtract right side of condition");
                    codeSection.add("BB " + labelEnd + " ;; Branch to else label if less than or equal");
                } else if (value.contains(">=")) {
                    String[] parts = value.split(">=");
                    codeSection.add("LDA " + parts[1].trim() + " ;; Load right side of condition");
                    codeSection.add("SUB " + parts[0].trim() + " ;; Subtract left side of condition");
                    codeSection.add("BNB " + labelEnd + " ;; Branch to else label if greater than or equal");
                } else if (value.contains("<")) {
                    // If the previous condition was <, we need to check if the value is greater than or equal to the right side
                    String[] parts = value.split("<");
                    codeSection.add("LDA " + parts[1].trim() + " ;; Load right side of condition");
                    codeSection.add("SUB " + parts[0].trim() + " ;; Subtract left side of condition");
                    codeSection.add("BB " + labelEnd + " ;; Branch to else label if greater than or equal");
                } else if (value.contains(">")) {
                    //If the previous condition was >, we need to check if the value is less than or equal to the right side
                    String[] parts = value.split(">");
                    codeSection.add("LDA " + parts[1].trim() + " ;; Load left side of condition");
                    codeSection.add("SUB " + parts[0].trim() + " ;; Subtract right side of condition");
                    codeSection.add("BNB " + labelEnd + " ;; Branch to else label if less than or equal");
                } else {
                    throw new IllegalArgumentException("Condición no soportada: " + value);
                }

            }
            
        }
    }

    private static void generateConditionalCall(String condition, String ifLabel, String labelDest) {
        codeSection.add(";; If condition: " + condition);
        generateConditionalBody(condition, ifLabel, labelDest);
    }

    private static void generateConditionalBody(String condition, String ifLabel, String labelDest) {
        String code = ";; Conditional code for: " + condition + " \n";

        //code += ";; If label: " + ifLabel + " \n";
        // code += "." + labelDest + " ;; Destination label for condition\n";
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            code += "LDA " + parts[0].trim() + " ;; Load left side of condition\n";
            code += "SUB " + parts[1].trim() + " ;; Subtract right side of condition\n";
            code += "BZ " + labelDest + " ;; Branch to destination label if zero\n";
            //codeSection.add("JMP " + elseLabel);
        } else if (condition.contains("!=")) {
            String[] parts = condition.split("!=");
            code += "LDA " + parts[0].trim() + " ;; Load left side of condition\n";
            code += "SUB " + parts[1].trim() + " ;; Subtract right side of condition\n";
            code += "BNZ " + labelDest + " ;; Branch to destination label if not zero\n";
            //codeSection.add("JMP " + elseLabel);
        } else if (condition.contains("<")) {
            String[] parts = condition.split("<");
            code += "LDA " + parts[0].trim() + " ;; Load left side of condition\n";
            code += "SUB " + parts[1].trim() + " ;; Subtract right side of condition\n";
            code += "BB " + labelDest + " ;; Branch to destination label if less than\n";
            //codeSection.add("JMP " + elseLabel);
        } else if (condition.contains(">")) {
            String[] parts = condition.split(">");
            code += "LDA " + parts[0].trim() + " ;; Load right side of condition\n";
            code += "SUB " + parts[1].trim() + " ;; Subtract left side of condition\n";
            code += "BNB " + labelDest + " ;; Branch to destination label if greater than\n";
            //codeSection.add("JMP " + elseLabel);
        } else if (condition.contains("<=")) {
            String[] parts = condition.split("<=");
            code += "LDA " + parts[0].trim() + " ;; Load left side of condition\n";
            code += "SUB " + parts[1].trim() + " ;; Subtract right side of condition\n";
            code += "BB " + labelDest + " ;; Branch to destination label if less than or equal\n";
            //codeSection.add("JMP " + elseLabel);
        } else if (condition.contains(">=")) {
            String[] parts = condition.split(">=");
            code += "LDA " + parts[0].trim() + " ;; Load right side of condition\n";
            code += "SUB " + parts[1].trim() + " ;; Subtract left side of condition\n";
            code += "BNB " + labelDest + " ;; Branch to destination label if greater than or equal\n";
            //codeSection.add("JMP " + elseLabel);
        } else {
            throw new IllegalArgumentException("Condición no soportada: " + condition);
        }

        //code += "BX _00 ;; Return from function\n";
        //ifBodies.put(ifLabel, Arrays.asList(code.split("\\n")));
        //functionArgs.put(ifLabel, new ArrayList<>()); // No arguments for if statements
        //conditions.put(ifLabel, Arrays.asList(code.split("\\n")));
        codeSection.add(code);
    }

    private static boolean containsOp(String line) {
        return line.contains("+") || line.contains("-") || line.contains("*") || line.contains("/");
    }

    private static void compileExpression(String line) {
        String[] parts = line.split("=");
        String left = parts[0].replace("int", "").trim();
        String expr = parts[1].replace(";", "").trim();

        Pattern p = Pattern.compile("([\\w]+)\\s*([+\\-*/])\\s*([\\w]+)");
        Matcher m = p.matcher(expr);
        if (m.find()) {
            String lhs = m.group(1);
            String op = m.group(2);
            String rhs = m.group(3);
            if (isNumber(lhs)) {
                lhs = Integer.toHexString(Integer.parseInt(lhs));
                while (lhs.length() < 2) {
                    lhs = "0" + lhs; // Ensure at least two characters for char values
                }
                lhs = "_" + lhs; // Ensure numbers are prefixed with _
            }
            if (isNumber(rhs)) {
                rhs = Integer.toHexString(Integer.parseInt(rhs));
                while (rhs.length() < 2) {
                    rhs = "0" + rhs; // Ensure at least two characters for char values
                }
                rhs = "_" + rhs; // Ensure numbers are prefixed with _
            }
            codeSection.add((isNumber(lhs) ? "LDA _" : "LDA ") + lhs);
            switch (op) {
                case "+":
                    codeSection.add((isNumber(rhs) ? "ADD _" : "ADD ") + rhs);
                    break;
                case "-":
                    codeSection.add((isNumber(rhs) ? "SUB _" : "SUB ") + rhs);
                    break;
                case "*":
                    codeSection.add((isNumber(rhs) ? "MUL _" : "MUL ") + rhs);
                    break;
                case "/":
                    codeSection.add((isNumber(rhs) ? "DIV _" : "DIV ") + rhs);
                    break;
                default:
                    throw new IllegalArgumentException("Operador no soportado: " + op);
            }
            codeSection.add("STA " + left);
        }
    }

    private static String adaptGlyphs(String instr) {
        Matcher matcher = Pattern.compile("\\b(\\d+)\\b").matcher(instr);
        while (matcher.find()) {
            String num = matcher.group(1);

            if (!isNumber(num)) {
                continue; // Skip if not a number
            }

            while (num.length() < 2) {
                num = "0" + num; // Ensure at least two characters for char values
            }

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
        if (file.exists()) {
            file.delete();
        }
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
