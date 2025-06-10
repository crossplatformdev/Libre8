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
 * LIBRE 8 ASSEMBLY LANGUAGE:
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
 * 
 * ```Example Hello World Code, in C:
		char arg1 = 'H';
		char arg2 = 'i';
		char arg3 = '!';

		void helloWorld(char arg1, char arg2, char arg3);

		void helloWorld(char arg1, char arg2, char arg3) {
			//output with OUT instruction when its parsed by CompilerV3
			__asm {
				LDA arg1
				OUT
				LDA arg2
				OUT
				LDA arg3
				OUT
			}	

			return;
		}

		int main() {
			//output with OUT instruction when its parsed by CompilerV3
			//Declare label .main
			helloWorld(arg1, arg2, arg3);
			
			__asm {
				JMP .main
			}
			
		}
 * ```:
 * 
 * MUST PRODUCE THE BELOW CODE (!!!):
 * 
 * ```Assembly	
	;; EXAMPLE HELLO WORLD CODE, CALLING A FUNCTION:
	;; THIS CODE CALLS A FUNCTION THAT PRINTS "Eli".
	.data 
	;; Variables and constants and function prototypes must be declared here.
	var1 00001000h 'H' ;; My assembler converts the char to hex automatically
	var2 00001001h 'i' ;; 
	var3 00001002h '!' ;; 
	helloWorld 07000000h arg1 arg2 arg3	;; this is the function prototype function with 3 arguments. 
	;; Arguments are stored in the stack, using LDA <var> and PSAX. Then they are POPed using POPX, inside the function body.
	
	.code ;; this is the code section
	
	.helloWorld ;; this is a function
	POPX ;; pop the stack and store in A
	LDA arg1 ;; load arg1 into A
	STA var1 ;; store A into var1
	POPX ;; pop the stack and store in A
	LDA arg2 ;; load arg2 into A
	STA var2 ;; store A into var2
	POPX ;; pop the stack and store in A
	LDA arg3 ;; load arg3 into A
	STA var3 ;; store A into var3
	BX ;; return
	;; Code must be written here.
	
	.main
	PUSH var1
	PUSH var2
	PUSH var3
	B helloWorld
	JMP main
 ```
 */


public class CompilerV5 {
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

	private static String tmpCodeString = "";
	private static ArrayList<String> tmpCode = new ArrayList<>();
	/**
	 * This function cleans the C code and gets the tokens.
	 * @throws FileNotFoundException
	 */
	private static void readAndParseTokens() throws FileNotFoundException {
		File file = new File("main_test.c");
		Scanner sc = new Scanner(file);	
		String codeTXT = "";
		while (sc.hasNextLine()) {
			String line = sc.nextLine();

			String sentences[] = line.split(";");
			for (String s : sentences) {
				tmpCode.add(s);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		readAndParseTokens();		
		for (String sentence : tmpCode){
			System.out.println(sentence);
			Pattern p = Pattern.compile(".*__asm.*");

			String[] words = sentence.split(" ");

			if (p.matcher(sentence).matches()) {
				tmpCodeString = "";
				for (String s : tmpCode) {
					tmpCodeString += s;
				}
				cCode += tmpCodeString;
			}

			//Test if the sentence is a function prototype
			p = Pattern.compile("(void|int|char|byte)\\s*(.+)\\s*\\(((void|int|char|byte)\\s*([0-9a-zA-Z]*)\\s*,?\\s?)*\\)*");
			if (p.matcher(sentence).matches()) {
				for(String word : words) {
					if (word.equals("void") || word.equals("int") || word.equals("char") || word.equals("byte")) {
						funType = word;
					} else {
						if (word.contains("(")) {
							name = word.substring(0, word.indexOf("("));
						} else {
							if (word.contains(")")) {
								word = word.replace(")", "");
							}
							if (word.contains(",")) {
								word = word.replace(",", "");
							}
							functionArgs.add(word);
						}
					}
				}
				//Format the assy line
				String addStr = Long.toHexString(functionAddress);
				while (addStr.length() < 8) {
					addStr = "0" + addStr;
				}
				addStr += "h";

				String funStr = "";
				funStr +=  name + " " + addStr + " ";
				for (String arg : functionArgs) {
					funStr += arg + " ";
				}
				funStr = funStr.trim();
				
				data.add(funStr);				
			}

			//Test if the sentence is a variable declaration
			p = Pattern.compile("(void|int|char|byte)\\s*(.+)=\\s*(\\'?.+\\'?)\\s*");
			if (p.matcher(sentence).matches()) {
				type = p.matcher(sentence).group(1);
				varName = p.matcher(sentence).group(2);
				hexValue = p.matcher(sentence).group(3);
				//Format the assy line
				String addStr = Long.toHexString(dataAddress);
				while (addStr.length() < 8) {
					addStr = "0" + addStr;
				}
				addStr += "h";

				String varStr = "";
				varStr += varName + " " + addStr + " ";
				if (type.equals("char")) {
					varStr += varName.charAt(0) + " ";
				} else {
					varStr += varName + " ";
				}

				data.add(varStr);
				dataAddress++;
			}
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

}

