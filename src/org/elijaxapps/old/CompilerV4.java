package org.elijaxapps.old;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
 * ;; THIS CODE CALLS A FUNCTION THAT PRINTS "Eli".
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


public class CompilerV4 {
	static String cCode = "";
	static String token = "";
	static ArrayList<String> data = new ArrayList<>();
	static ArrayList<String> code = new ArrayList<>();
	static ArrayList<String> includes = new ArrayList<>();

	static long dataAddress = 0x00777777;
	static ArrayList<String> tokens = new ArrayList<>();
	static Long tokenCounter = 0L;
	//private static Iterator<String> tokenIterator = tokens.iterator();
	private static String hexValue;
	private static String varName;
	private static String type;
	private static String name;
	private static String funType;
	private static ArrayList<String> functionArgs = new ArrayList<>();
	private static Long functionAddress = 0x00100000L;
	private static Long bracketCounter = 0L;
	private static Long parenthesisCounter = 0L;
	
	private static void readAndParseTokens() throws FileNotFoundException {
		File file = new File("main_test.c");
		Scanner sc = new Scanner(file);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			System.out.println(line);
			//Replace comments // with ;;//
			line = line.replaceAll("//", ";;//");
			line = line.replaceAll(";", " ; ");
			line = line.replaceAll("\\(", " ( ");
			line = line.replaceAll("\\)", " ) ");
			line = line.replaceAll("\\{", " { ");
			line = line.replaceAll("\\}", " } ");
			
			line = line.replaceAll("int", "_int_");
			line = line.replaceAll("char", "_char_");
			line = line.replaceAll("byte", "_byte_");
			line = line.replaceAll("bool", "_bool_");
			line = line.replaceAll("void", "_void_");

			line = line.replaceAll("return", "_return_");

			line = line.replace("__asm", "___asm_");

			line = line.replaceAll("=", "_=_");
			line = line.replaceAll("'", "_'_");
			line = line.replaceAll(",", " , ");
			String[] lineTokens = line.split(" ");
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

	public static void main(String[] args) throws Exception {
		readAndParseTokens();		
		while (!token.equals("EOF")) {
			if (!token.equals("")) {

				evalToken();
			} 			
			token = nextToken();			
		}

		String dataTmp = "";
		for (String d : data) {
			dataTmp += d;
		}

		String codeTmp = "";
		for (String c : code) {
			codeTmp += c;
		}
		String asmTmp = "";
		asmTmp += ".data\n";
		asmTmp += "{{ data }}";
		asmTmp += "\n";
		asmTmp += ".code\n";
		asmTmp += "{{ code }}";

		asmTmp = asmTmp.replace("{{ data }}", dataTmp);
		asmTmp = asmTmp.replace("{{ code }}", codeTmp);

		////Write in file main.as
		
		File file2 = new File("main.as");
		java.io.FileWriter fileWriter = new java.io.FileWriter(file2);
		fileWriter.write(asmTmp);
		fileWriter.close();

		System.out.println(asmTmp);
			
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

	private static boolean semicolon() {
		return token.equals(" ; ");
	}

	private static boolean comma() {
		return token.equals(" , ");
	}

	private static boolean openParenthesis() {
		return token.equals(" ( ");
	}

	private static boolean closeParenthesis() {
		return token.equals(" ) ");
	}

	private static boolean openBrace() {
		return token.equals(" { ");
	}

	private static boolean closeBrace() {
		if(token.equals(" } ")) {
			bracketCounter--;
			if(bracketCounter == 0) {
				return true;
			}
		}
		return false;
	}

	private static boolean name() {
		return Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", token);
	}

	private static boolean type() {
		return token.equals("_int_") || token.equals("_char_") || token.equals("_byte_") || token.equals("_bool_") || token.equals("_void_");
	}

	private static boolean isByte() {
		//Example token "_00_" "_FF_" "_aa_"
		return Pattern.matches("_[0-9a-fA-F]{2}_", token);
	}

	private static boolean isChar() {
		//Example token "_'A'_"
		return Pattern.matches("_'_.{1}_'_", token);
	}

	private static boolean isInt() {
		//Example token "_1234_" "_0001_" 
		return Pattern.matches("_[0-9]+_", token);
	}

	private static boolean value() {
		return isByte() || isChar() || isInt();
	}

	private static boolean main() {
		return token.equals("_main_") && nextTokenIs(" ( ");
	}

	private static boolean functionPrototype() {
		// void| int| char| byte| bool| void funName(/* Can have arguments, |int|bool|char|byte|void arg1, */);
		// Set funName and funArgs
		if(type()) {
			funType = token;
			token = nextToken();
			if(name()) {
				name = token;
				token = nextToken();
				if(openParenthesis()) {
					while(!closeParenthesis()) {
						if(type()) {
							functionArgs.add(token);
							token = nextToken();
							if(value()) {
								token = nextToken();


						}
					}


					return true;
				}
			}
		}
	}

	private static boolean functionBody() {
		// '{' 

		return false;
	}



	private static boolean assignment() {
		return token.equals("_=_");
	}

	private static void evalToken() throws Exception{
		//Must be able to evaluate C code from main_test.c: declarations, assignments, functions, etc. ALL C89 CODE WITH LIBRE 8 ASM
		System.out.println(token);
		// type varName; -> data.add(varName + " " + <address_32b_formatted = 07000000-07FFFFFF, increments of 0x1> + "h 00")
		// type varName = hexValue; -> data.add(varName + " " + <address_32b_formatted = 07000000-07FFFFFF, increments of 0x1> + "h " + <byte value of hexValue>)
		if (type()) {
			type = token;
			token = nextToken();
			if (name()) {
				name = token;
				token = nextToken();
				if (semicolon()) {
					data.add(name + " " + String.format("%08X", dataAddress) + "h 00\n");
					dataAddress++;
				}

				// Has assignment?
				if (assignment()) {
					token = nextToken();
					if (value()) {
						token = token.replace("_", "");
						if(token.contains("'")){
							token = token.replace("'", "");
							hexValue = Long.toHexString(token.charAt(0));
						} else {
							hexValue = token;
						}
						data.add(name + " " + String.format("%08X", dataAddress) + "h " + hexValue + "\n");
						dataAddress++;
					}
				}
			}
		}


		// type funName(args); -> data.add(funName + " " + <address_32b_formatted = 07000000-07FFFFFF, increments of 0x400> + "h ")

		if(functionPrototype()){
			funType = type;
			name = token;
			token = nextToken();
			while(!closeParenthesis()) {
				if(type()) {
					functionArgs.add(token);
					token = nextToken();
					if(comma()) {
						token = nextToken();
					}
				}
			}
			data.add(name + " " + String.format("%08X", functionAddress) + "h " + functionArgs + "\n");
			functionAddress += 0x400;
		}

		// type funName(args){...} -> 
		//		.funName arg1, ...
		//		POPX
		//		LDA arg1
		//		STA arg1

		if(functionBody()){
			code.add("." + name + "\n");
			code.add("POPX\n");
			for(String arg : functionArgs) {
				code.add("LDA " + arg + "\n");
				code.add("STA " + arg + "\n");
			}
			
		}		


		// __asm_ -> 
		//	token

		if(token.equals("___asm_")) {
			token = nextToken();
			code.add(token + "\n");
		}

		// _return_ ->
		//	BX
		if(token.equals("_return_")) {
			code.add("BX\n");
		}
	}
}
