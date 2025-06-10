package org.elijaxapps.old;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;


/**
 * Libre 8 C Compiler V3. Generates .as, using Libre 8 assembly language. (refer to MicrocodeV3.java)
 * 
 * @version 3.0
 * 
 * LDA varName -> Load varName into A
 * LDB varName -> Load varName into B
 * LDC varName -> Load varName into C
 * LDD varName -> Load varName into D
 * 
 * STA varName -> Store A into varName
 * STB varName -> Store B into varName
 * STC varName -> Store C into varName
 * STD varName -> Store D into varName
 * 
 * OUT varName -> Output varName
 * OUTA -> Output A
 * OUTB -> Output B
 * OUTC -> Output C
 * OUTD -> Output D
 * 
 * DEC varName -> Input by keyboard without echo, store in A
 * DECE -> Input with echo, store in A
 * 
 * ADD varName -> Add varName to A
 * SUB varName -> Subtract varName from A
 * MUL varName -> Multiply A by varName
 * DIV varName -> Divide A by varName
 * 
 * JMP varName -> Jump to varName
 * JEQ varName -> Jump to varName if A equals varName
 * JNE varName -> Jump to varName if A not equals varName
 * JGT varName -> Jump to varName if A greater than varName
 * JLT varName -> Jump to varName if A less than varName
 * JGE varName -> Jump to varName if A greater or equals varName
 * JLE varName -> Jump to varName if A less or equals varName
 * 
 * B varName -> Branch to varName
 * BX varName -> Return branch. Value is stored in A is alwatys a byte.
 * 
 * PSAX -> PUSH A (8Bits)
 * POP -> POP stack and store in A (8Bits)
 * 
 * ;; this is a comment
 * .data 
 * ;; Variables and constants and function prototypes must be declared here.
 * var1 00001000h 'E' ;; var1 is a byte with value ff
 * var2 00001001h 'l' ;; var2 is a byte with value 00
 * var3 00001002h 'i' ;; var3 is a byte with value 01
 * helloWorld 00001003h arg1 arg2 arg3 ;; helloWorld is a function with 3 arguments
 * 
 * .code ;; this is the code section
 * 
 * .helloWorld ;; this is a function
 * POPX ;; pop the stack and store in A
 * LDA arg1 ;; load arg1 into A
 * STA var1 ;; store A into var1
 * POPX ;; pop the stack and store in A
 * LDA arg2 ;; load arg2 into A
 * STA var2 ;; store A into var2
 * POPX ;; pop the stack and store in A
 * LDA arg3 ;; load arg3 into A
 * STA var3 ;; store A into var3
 * BX ;; return
 * ;; Code must be written here.
 * 
 * .main
 * PUSH var1
 * PUSH var2
 * PUSH var3
 * B helloWorld
 * JMP main
 */


public class CompilerV3 {
	static String cCode = "";
	static String token = "";
	static ArrayList<String> data = new ArrayList<String>();
	static ArrayList<String> code = new ArrayList<String>();
	static ArrayList<String> asm = new ArrayList<String>();
	static ArrayList<String> includes = new ArrayList<String>();

	static long dataAddress = 0x00777777;
	static ArrayList<String> tokens = new ArrayList<String>();
	static Long tokenCounter = 0L;
	//private static Iterator<String> tokenIterator = tokens.iterator();
	private static String hexValue;
	private static String varName;
	private static String varType;
	private static String funName;
	private static Long functionAddress = 0x00100000L;

	public CompilerV3() {

	}

	public static void main(String[] args) throws Exception {
		readAndParseTokens();

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
		
		while (!token.equals("EOF")) {
			if (!token.equals("")) {

				evalToken();

				System.out.println(token);
			} else {
				token = nextToken();
			}

		}
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

		asmTmp = asmTmp.replace("{{ data }}", dataTmp);
		asmTmp = asmTmp.replace("{{ code }}", codeTmp);

		////Write in file main.as
		
		File file2 = new File("main.as");
		java.io.FileWriter fileWriter = new java.io.FileWriter(file2);
		fileWriter.write(asmTmp);
		fileWriter.close();

		System.err.println(asmTmp);
		System.out.println("Compilation finished.");

			
	}

	private static void evalToken(){
		//Must be able to evaluate C code from main_test.c: declarations, assignments, functions, etc. ALL C89 CODE WITH LIBRE 8 ASM
		
		switch (token) {
		case "_int_":
		case "_char_":
		case "_byte_":
		case "_bool_":
		case "_void_":
			varType = token;
			token = nextToken();
			if (name()) {
				varName = token;
				token = nextToken();
				if (assignment()) {
					token = nextToken();
					if (value()) {
						hexValue = Integer.toHexString(Integer.parseInt(token));
						data.add(varName + " " + hexValue + "h\n");
						token = nextToken();
						if (semicolon()) {
							token = nextToken();
						}
					}
				}
			}
			break;
		case "_return_":
			token = nextToken();
			if (value()) {
				code.add("POPX\n");
				code.add("LDA " + token + "\n");
				code.add("BX\n");
				token = nextToken();
				if (semicolon()) {
					token = nextToken();
				}
			}
			break;
		case "_asm_":
			token = nextToken();
			if (openBrace()) {
				token = nextToken();
				while (!closeBrace()) {
					asm.add(token + "\n");
					token = nextToken();
				}
				token = nextToken();
			}
			break;
		default:
			//Is a name of a function or a variable
			if (name()) {
				funName = token;
				token = nextToken();
				if (openParenthesis()) {
					token = nextToken();
					while (!closeParenthesis()) {
						if (type()) {
							token = nextToken();
							if (name()) {
								token = nextToken();
								if (comma()) {
									token = nextToken();
								}
							}
						}
					}
		
				}
			}
		}
	
		token = nextToken();
	}

	private static boolean type() {
		if (token.equals("int") || token.equals("char") || token.equals("byte") || token.equals("bool") || token.equals("void")) {
			return true;
		}
		return false;
	}

	private static boolean name() {
		if (Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", token)) {
			return true;
		}
		return false;
	}

	private static boolean value() {
		if (Pattern.matches("[0-9]+", token)) {
			return true;
		}
		return false;
	}

	private static boolean operator() {
		if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("==")
				|| token.equals("!=") || token.equals(">") || token.equals("<") || token.equals(">=")
				|| token.equals("<=") || token.equals("&&") || token.equals("||") || token.equals("!")) {
			return true;
		}
		return false;
	}

	private static boolean assignment() {
		if (token.equals("=")) {
			return true;
		}
		return false;
	}

	private static boolean semicolon() {
		if (token.equals(";")) {
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

	private static boolean openParenthesis() {
		if (token.equals("(")) {
			return true;
		}
		return false;
	}

	private static boolean closeParenthesis() {
		if (token.equals(")")) {
			return true;
		}
		return false;
	}

	private static boolean openBrace() {
		if (token.equals("{")) {
			return true;
		}
		return false;
	}

	private static boolean closeBrace() {
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

	
	private static boolean nextTokenIs(String token) {		
		if (nextToken().equals(token)) {
			tokenCounter-=1;
			return true;
		}
		return false;
	}
	


	private static String nextToken() {
		String ret = tokens.get(tokenCounter.intValue());
		tokenCounter+=1;
		token = ret;
		return ret;
	}


	private static void readAndParseTokens() throws FileNotFoundException {
		File file = new File("main_test.c");
		Scanner sc = new Scanner(file);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			
			//Replace comments // with ;;//
			line = line.replaceAll("//", ";;");
			line = line.replaceAll(";", " ;; ");
			line = line.replaceAll("\\(", "_(_");
			line = line.replaceAll("\\)", "_)_");
			line = line.replaceAll("\\{", "_{_");
			line = line.replaceAll("\\}", "_}_");
			
			line = line.replaceAll("int", "_int_");
			line = line.replaceAll("char", "_char_");
			line = line.replaceAll("byte", "_byte_");
			line = line.replaceAll("bool", "_bool_");
			line = line.replaceAll("void", "_void_");

			line = line.replaceAll("return", "_return_");

			line = line.replace("__asm", "___asm_");

			line = line.replaceAll("=", "_=_");
			
			
			String[] lineTokens = line.split("\\s*");
			for (String token : lineTokens) {
				if (token != null && !token.equals("")) {
					tokens.add(token.trim());
				}
			}
		}
		tokens.add("EOF");
		sc.close();
		tokenCounter = 0L;
	}
}
