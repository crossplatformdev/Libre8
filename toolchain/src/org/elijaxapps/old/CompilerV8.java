package org.elijaxapps.old;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.elijaxapps.libre8.AssemblerV8;

/**
 * Libre 8 C Compiler V3. Generates .as, using Libre 8 assembly language. (refer
 * to MicrocodeV3.java)
 *
 * @version 3.0
 *
 * LDA varName -> Load varName into A LDB varName -> Load varName into B LDC
 * varName -> Load varName into C LDD varName -> Load varName into D
 *
 * STA varName -> Store A into varName STB varName -> Store B into varName STC
 * varName -> Store C into varName STD varName -> Store D into varName
 *
 * OUT varName -> Output varName OUTA -> Output A OUTB -> Output B OUTC ->
 * Output C OUTD -> Output D
 *
 * DEC varName -> Input by keyboard without echo, store in A DECE -> Input with
 * echo, store in A
 *
 * ADD varName -> Add varName to A SUB varName -> Subtract varName from A MUL
 * varName -> Multiply A by varName DIV varName -> Divide A by varName
 *
 * JZ varName -> Jump to varName if A is zero JNZ varName -> Jump to varName if
 * A is not zero JMP varName -> Jump to varName unconditionally JC varName ->
 * Jump to varName if carry JNC varName -> Jump to varName if A if not carry
 *
 * B varName -> Branch to varName BX varName -> Return branch. Value is stored
 * in A is alwatys a byte. BZ varName -> Branch to varName if A is zero BC
 * varName -> Branch to varName if A is carry BNZ varName -> Branch to varName
 * if A is not zero BNC varName -> Branch to varName if A is not carry
 *
 * PSAX -> PUSH A (8Bits) POP -> POP stack and store in A (8Bits)
 *
 * ;; this is a comment .data ;; Variables and constants and function prototypes
 * must be declared here. var1 00001000h 'E' ;; var1 is a byte with value ff
 * var2 00001001h 'l' ;; var2 is a byte with value 00 var3 00001002h 'i' ;; var3
 * is a byte with value 01 helloWorld 00001003h arg1 arg2 arg3 ;; helloWorld is
 * a function with 3 arguments
 *
 * .code ;; this is the code section
 *
 * .helloWorld ;; this is a function POPX ;; pop the stack and store in A LDA
 * arg1 ;; load arg1 into A STA var1 ;; store A into var1 POPX ;; pop the stack
 * and store in A LDA arg2 ;; load arg2 into A STA var2 ;; store A into var2
 * POPX ;; pop the stack and store in A LDA arg3 ;; load arg3 into A STA var3 ;;
 * store A into var3 BX ;; return ;; Code must be written here.
 *
 * .main PUSH var1 PUSH var2 PUSH var3 B helloWorld JMP main
 */
public class CompilerV8 {

    static String cCode = "";
    static String token = "";
    static ArrayList<String> data = new ArrayList<String>();
    static ArrayList<String> main = new ArrayList<String>();
    static ArrayList<String> code = new ArrayList<String>();
    static ArrayList<String> asm = new ArrayList<String>();
    static ArrayList<String> includes = new ArrayList<String>();

    static long dataAddress = 0x00090000L;
    static ArrayList<String> tokens = new ArrayList<String>();
    static Long tokenCounter = 0L;
    //private static Iterator<String> tokenIterator = tokens.iterator();
    private static String hexValue;
    private static String varName;
    private static String varType;
    private static String funName;
    private static ArrayList<String> variableNames = new ArrayList<String>();
    private static ArrayList<String> functionNames = new ArrayList<String>();
    private static ArrayList<String> argNames = new ArrayList<String>();
    private static Long functionAddress = 0x00100000L;

    public CompilerV8() {

    }

    public static void main(String[] args) throws Exception {
        run(args);
    }

    public static void run(String[] args) throws Exception {
        readAndParseTokens(args);

        File file = new File("Libre8.asm");
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            asm.add(sc.nextLine() + "\n");
        }

        sc.close();

        for (String include : includes) {
            String includeName = "";
            try {
                File includeFile = new File(includeName);
                Scanner sc2 = new Scanner(includeFile);
                while (sc2.hasNextLine()) {
                    if (sc2.nextLine() != null) {
                        data.add(sc.nextLine() + "\n");
                    }
                }

                sc2.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            data.add(include);
        }
        // ;; <name> <address>

        while (!token.equals("FOE")) {
            if (!token.equals("")) {
                System.out.println(token);
                evalToken();
            }
            token = nextToken();
        }

        String gliphs = ";;; GLIPHS\n";
        for (int i = 0; i < 256; i++) {
            String hex = Integer.toHexString(0xffffff - i - 1);
            while (hex.length() < 6) {
                hex = "0" + hex;
            }
            hex = hex + "h";
            gliphs += "_" + i + " " + hex + " " + i + "\n";
        }
        gliphs += ";;; END GLIPHS\n";
        String asmTmp = "";
        for (String as : asm) {
            asmTmp += as;
        }

        String dataTmp = "";
        for (String d : data) {
            dataTmp += d;
        }

        String codeTmp = "";
        for (String c : code) {
            codeTmp += c;
        }

        codeTmp += "\n{ main }\n";

        String mainTmp = "";
        for (String m : main) {
            mainTmp += m;
        }

        asmTmp = asmTmp.replace("{ gliphs }", gliphs);
        asmTmp = asmTmp.replace("{ data }", dataTmp);
        codeTmp = codeTmp.replace("{ main }", mainTmp);
        asmTmp = asmTmp.replace("{ code }", codeTmp);

        ////Write in file main.as
		


		File file2 = new File("main.as");
        java.io.FileWriter fileWriter = new java.io.FileWriter(file2);
        fileWriter.write(asmTmp);
        fileWriter.close();

        System.err.println(asmTmp);
        System.out.println("Compiling L8 ASSY.");

        AssemblerV8.run(new String[]{"main.as"});
    }

    private static void evalToken() {
        //Must be able to evaluate C code from main_test.c: declarations, assignments, functions, etc. ALL C89 CODE WITH LIBRE 8 ASM
        final String ISNAME = name2(token);
        if (ISNAME != null && name(ISNAME)) {
            //Check if it is variable name or function name. must have been declared before
            token = ISNAME;
            if (functionNames.contains(token)) {
                //Function call
                String openParenthesis = nextToken();
                if (openParenthesis(openParenthesis)) {
                    String args = "";
                    String funStr = "";
                    token = nextToken();
                    while (!closeParenthesis(token)) {
                        if (!token.equals(",")) {
                            args += token + " ";
                            funStr += "LDA " + token + "\n";
                            funStr += "PSAX " + token + "\n";
                        }
                        token = nextToken();
                    }
                    funName = ISNAME;
                    funStr += "B " + funName + "\n";
                    if (funName.equals("main")) {
                        main.add(funStr + "\n");
                    } else {
                        code.add(funStr + "\n");
                    }
                }
            } else {
                if (variableNames.contains(token)) {
                    //Variable
                    String varName = token;
                    token = nextToken();
                    if (assignment(token)) {
                        String value = nextToken();
                        if (value.startsWith("0x")) {
                            value = value.replaceAll("0x", "");
                        }
                        if (value(value)) {
                            String varStr = "";
                            while (!semicolon(token)) {
                                if (arithmethic(token)) {
                                    String operator = token;
                                    varStr = makeArithmethic(varName, operator);
                                }
                                token = nextToken();
                            }

                            if (funName == null || funName.equals("main")) {
                                main.add(varStr);
                            } else {
                                code.add(varStr);
                            }
                        }
                    } else if (arithmethic(token)) {
                        String operator = token;
                        String varStr = makeArithmethic(varName, operator);

                        if (funName == null || funName.equals("main")) {
                            main.add(varStr);
                        } else {
                            code.add(varStr);
                        }
                    }
                }
            }
        } else {
            switch (token) {
                case "__asm":
                    String brace = nextToken();
                    String asmLine = "";
                    if (openBrace(brace)) {
                        token = nextToken();
                        while (!closeBrace(token)) {
                            asmLine += token + " ";
                            token = nextToken();
                        }
                    }
                    if (asmLine.length() > 0) {

                        function(funName);

                        if (funName.equals("main")) {
                            main.add(asmLine + "\n");
                        } else {
                            code.add(asmLine + "\n");
                        }
                    }

                    token = nextToken();
                    break;
                case "*":
                    String next = nextToken();
                    if (name(next)) {
                        //Pointer
                        varName = next;
                        token = nextToken();
                        if (assignment(token)) {
                            String value = nextToken();
                            if (value.startsWith("0x")) {
                                value = value.replaceAll("0x", "");
                            }
                            //Value is an address, appeng "varName <address>"" to data
                            addAddress(value);
                        }
                    }
                case "+":
                case "-":

                case "/":
                    String operator = token;
                    String varStr = makeArithmethic(varName, operator);

                    if (funName == null || funName.equals("main")) {
                        main.add(varStr);
                    } else {
                        code.add(varStr);
                    }
                    break;
                /*
			case "if":
				String openParenthesis = nextToken();
				if(openParenthesis(openParenthesis)){
					String[] condition = new String[256];
					token = nextToken();
					while(!closeParenthesis(token)){
						if(name(token)){
							condition[0] = token;
						} else if(operator(token)){
							condition[1] = token;
						} 
						token = nextToken();
					}

				}
				break;
                 */
                case "int":
                case "char":
                case "byte":
                case "bool":
                case "void":
                    varType = token;
                    String name = nextToken();
                    if (name(name)) {
                        if (!nextTokenIs("(")) {
                            varName = name;
                            while (variableNames.contains(varName)) {
                                varName = "_" + varName;
                            }
                            variableNames.add(varName);
                            //Check if it is an assignment (=)

                            String token = nextToken();

                            if (value(token)) {
                                if (token.startsWith("0x")) {
                                    token = token.replaceAll("0x", "");
                                }
                                addValue(token);
                            } else {
                                String value = "00";
                                addValue(value);
                            }
                        } else {
                            funName = name;
                            while (functionNames.contains(funName)) {
                                funName = "_" + name;
                            }
                            functionNames.add(funName);
                        }
                    }
                    break;
                case "return":
                    String returnValue = nextToken();
                    String returnStr = "";
                    if (value(returnValue)) {
                        returnStr += "LDA " + returnValue + "\n";
                        returnStr += "BX " + returnValue + "\n"; //Return value
                    } else {
                        returnStr += "BX 00000000h\n"; //Return 0
                    }
                    if ("main".equals(funName)) {
                        main.add(returnStr + "\n");
                        main.add(";;;;;;;;;;;" + funName + ";;;;;;;;;;\n");
                        main.add("\n");
                    } else {
                        code.add(returnStr + "\n");
                        code.add(";;;;;;;;;;;" + funName + ";;;;;;;;;;\n");
                        code.add("\n");
                    }
                    break;
            }
        }
    }

    public static String makeArithmethic(String varName, String operator) {
        String bOperand = nextToken();
        String varStr = "";
        varStr += "LDA " + varName + "\n";

        switch (operator) {
            case "+":
                varStr += "ADD _" + bOperand + "\n";
                break;
            case "-":
                varStr += "SUB _" + bOperand + "\n";
                break;
            case "*":
                varStr += "MUL _" + bOperand + "\n";
                break;
            case "/":
                varStr += "DIV _" + bOperand + "\n";
                break;
            default:
                break;
        }

        varStr += "STA " + varName + "\n";

        return varStr;
    }

    private static void function(String name) {

        String header = "";
        header += ";;;;;;;;;;;" + name + ";;;;;;;;;;\n";
        header += "." + name + "\n";
        String args = "";

        if (!code.contains(header)) {
            code.add(header);

            //Function prototype
            while (!closeParenthesis(token)) {
                String argName = nextToken();
                if (type(token)) {
                    if (argNames.contains(argName) || variableNames.contains(argName) && !type(argName)) {
                        argName = "_" + argName;
                        args += argName + " ";
                    }

                    code.add("POPX\n");
                    code.add("STA " + argName + "\n");

                }
                token = argName;
            }

            /*
			String openBrace = nextToken();
			if(openBrace(openBrace)){
				token = nextToken();
				while(!closeBrace(token)){							
					evalToken();
					token = nextToken();
				}
			} else {


				String addStr = Long.toHexString(functionAddress);
				while (addStr.length() < 8) {
					addStr = "0" + addStr;
				}
				addStr += "h";

				String funStr = "";
				funStr +=  funName + " " + addStr + " ";
				funStr += args;
				funStr = funStr.trim();
				
				data.add(funStr + "\n");
				functionAddress+=4096;	
			}

             */
        }

        String funAddress = Long.toHexString(functionAddress);
        String funDeclaration = name + " " + funAddress + "h " + args + "\n";
        if (!data.contains(funDeclaration)) {
            data.add(funDeclaration);
            functionAddress += 4096;
        }

    }

    private static void addValue(String value) {
        //Test if the sentence is a variable declaration
        String addStr = Long.toHexString(dataAddress);
        while (addStr.length() < 8) {
            addStr = "0" + addStr;
        }
        addStr += "h";

        String varStr = "";
        varStr += varName + " " + addStr + " ";
        varStr += value + " ";

        data.add(varStr + "\n");
        dataAddress--;
    }

    private static void addAddress(String addStr) {
        //Test if the sentence is a variable declaration
        while (addStr.length() < 8) {
            addStr = "0" + addStr;
        }
        if (!addStr.contains("h")) {
            addStr += "h";
        }

        String varStr = "";
        varStr += varName + " " + addStr + " ";

        data.add(varStr + "\n");
        dataAddress--;
    }

    private static boolean newLine(String token) {
        if (token.equals("nl\n")) {
            return true;
        }
        return false;
    }

    private static boolean type(String token) {
        if (token.equals("int") || token.equals("char") || token.equals("byte") || token.equals("bool") || token.equals("void")) {
            return true;
        }
        return false;
    }

    private static boolean asm(String token) {
        if (token.equals("__asm")) {
            return true;
        }
        return false;
    }

    private static boolean keyword(String token) {
        if (token.equals("return")) {
            return true;
        }
        return false;
    }

    private static boolean name(String token) {
        if (!type(token) && !asm(token) && !keyword(token) && Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", token)) {
            return true;
        }
        return false;
    }

    private static String name2(String token) {

        if (Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", token)) {
            final String ret = token;
            return ret;
        }
        return null;
    }

    private static boolean comment(String token) {
        if (Pattern.matches(";;.*", token)) {
            return true;
        }
        return false;
    }

    private static boolean value(String token) {
        if (!comment(token) && Pattern.matches("(0x)?\"?'?.*+'?\"?", token)) {
            return true;
        }
        return false;
    }

    private static boolean operator(String token) {
        if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("==")
                || token.equals("!=") || token.equals(">") || token.equals("<") || token.equals(">=")
                || token.equals("<=") || token.equals("&&") || token.equals("||") || token.equals("!")) {
            return true;
        }
        return false;
    }

    private static boolean assignment(String token) {
        if (token.equals("=")) {
            return true;
        }
        return false;
    }

    private static boolean semicolon(String token) {
        if (token.equals(";;")) {
            return true;
        }
        return false;
    }

    private static boolean comma() {
        if (token.equals(",")) {
            return true;
        }
        return false;
    }

    private static boolean openParenthesis(String token) {
        if (token.equals("(")) {
            return true;
        }
        return false;
    }

    private static boolean closeParenthesis(String token) {
        if (token.equals(")")) {
            return true;
        }
        return false;
    }

    private static boolean openBrace(String token) {
        if (token.equals("{")) {
            return true;
        }
        return false;
    }

    private static boolean closeBrace(String token) {
        if (token.equals("}")) {
            return true;
        }
        return false;
    }

    private static boolean openBracket() {
        if (token.equals("[")) {
            return true;
        }
        return false;
    }

    private static boolean closeBracket() {
        if (token.equals("]")) {
            return true;
        }
        return false;
    }

    private static boolean arithmethic(String token) {
        if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) {
            return true;
        }
        return false;
    }

    private static boolean nextTokenIs(String token) {
        if (nextToken().equals(token)) {
            tokenCounter -= 1;
            return true;
        }
        return false;
    }

    private static String nextToken() {
        Long index = tokenCounter;
        if (index >= tokens.size()) {
            return "FOE";
        }
        String ret = tokens.get(index.intValue());
        tokenCounter += 1;
        token = ret;
        return ret;
    }

    private static void readAndParseTokens(String[] args) throws FileNotFoundException {
        File file = new File(args[0] != null ? args[0] : "main_test.c");
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            //Replace comments // with ;;//
            line = line.replace(";", " ;; ");
            line = line.replace("//", ";;");
            //line = line.replace(" ; ", ";;");

            line = line.replace("(", " ( ");
            line = line.replace(")", " ) ");
            line = line.replace("{", " { ");
            line = line.replace("}", " } ");
            line = line.replace("[", " [ ");
            line = line.replace("]", " ] ");
            line = line.replace(",", " , ");
            line = line.replace("+", " + ");
            line = line.replace("-", " - ");
            line = line.replace("*", " * ");
            line = line.replace("/", " / ");
            line = line.replace("==", " == ");
            line = line.replace("!=", " != ");
            line = line.replace(">", " > ");
            line = line.replace("<", " < ");
            line = line.replace(">=", " >= ");
            line = line.replace("<=", " <= ");

            line = line.replaceAll("return", " return ");
            line = line.replace("__asm", " __asm ");
            line = line.replaceAll("=", " = ");
            line = line.replace(" ; ; ", " ;; ");
            String[] lineTokens = line.split(" ");
            for (String token : lineTokens) {
                if (token != null && !token.equals("")) {
                    tokens.add(token.trim());
                }
            }
            tokens.add("\n"); // Add a new line token to separate lines
        }
        tokens.add("FOE");
        sc.close();
        tokenCounter = 0L;
    }
}
