package org.elijaxapps.old;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.anarres.cpp.CppReader;
import org.anarres.cpp.CppTask;
import org.anarres.cpp.FileLexerSource;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.Token;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileNameMapper;

public class Compiler {

	public static Preprocessor pp = new Preprocessor();
	public static CppReader cppj = new CppReader(pp);
	public static CppTask cppt = new CppTask();

	private static int offset;	
	private static HashMap<Integer, String> offsets = new HashMap<Integer, String>();
	private static String code;
	private static boolean include;
	private static boolean hash;

	public static final int AND_EQ = 257;
	public static final int ARROW = 258;
	public static final int CHARACTER = 259;
	public static final int CCOMMENT = 260;
	public static final int CPPCOMMENT = 261;
	public static final int DEC = 262;
	public static final int DIV_EQ = 263;
	public static final int ELLIPSIS = 264;
	public static final int EOF = 265;
	public static final int EQ = 266;
	public static final int GE = 267;
	public static final int HASH = 268;
	public static final int HEADER = 269;
	public static final int IDENTIFIER = 270;
	public static final int INC = 271;
	public static final int NUMBER = 272;
	public static final int LAND = 273;
	public static final int LAND_EQ = 274;
	public static final int LE = 275;
	public static final int LITERAL = 276;
	public static final int LOR = 277;
	public static final int LOR_EQ = 278;
	public static final int LSH = 279;
	public static final int LSH_EQ = 280;
	public static final int MOD_EQ = 281;
	public static final int MULT_EQ = 282;
	public static final int NE = 283;
	public static final int NL = 284;
	public static final int OR_EQ = 285;
	public static final int PASTE = 286;
	public static final int PLUS_EQ = 287;
	public static final int RANGE = 288;
	public static final int RSH = 289;
	public static final int RSH_EQ = 290;
	public static final int SQSTRING = 291;
	public static final int STRING = 292;
	public static final int SUB_EQ = 293;
	public static final int WHITESPACE = 294;
	public static final int XOR_EQ = 295;
	public static final int M_ARG = 296;
	public static final int M_PASTE = 297;
	public static final int M_STRING = 298;
	public static final int P_LINE = 299;
	public static final int INVALID = 300;

	public static void main(String[] args) throws IOException, LexerException {
		Compiler.compile(args);
	}
	
	public static String compile(String[] args) throws IOException, LexerException {
        Path fileName = Path.of("_"+args[0]+".as");
		FileLexerSource f = new FileLexerSource(new File(args[0]));
		
		String contentData  = ".data \n";
				
		contentData +="LibreRT	133700h \n";
		for(int i = 0; i < 256; i++) {
			if(i>32 && i<127) {
				contentData +=	"_"+(char)i+" "+((int)133600+i)+"h "+"'"+(char)i+"'"+ " \n";
			} else {
				contentData +=	"_"+i+" "+((int)133600+i)+"h "+Integer.toHexString(i)+" \n";	
			}
		}
		
		
		String contentCode  = ".code\n";
		contentCode +=".Main";
		contentCode+=" JMP LibreRT";
		contentCode+=" LDA _E";
		contentCode+=" OUTA";
		contentCode+=" LDA _r";
		contentCode+=" OUTA";
		contentCode+=" LDA _r";
		contentCode+=" OUTA";
		contentCode+=" LDA _o";
		contentCode+=" OUTA";
		contentCode+=" LDA _r";
		contentCode+=" OUTA";
		contentCode+=" LDA _!";
		contentCode+=" OUTA";
				
		String content = "";
		Token token = null;
		while ((token = f.token()).getText() != null) {
			System.out.println("Type: " + token.getTokenName(token.getType()) + " Text: " + token.getText()
					+ " Column: " + token.getColumn() + " Line: " + token.getLine() + " Value: " + token.getValue());
			if("<eof>".equals(f.token().getText())){
				break;
			}

			if (token.getType() <= 256) {
				// Is char
			} else {
				switch (token.getType()) {
				case AND_EQ:
					break;
				case ARROW:
					break;
				case CHARACTER:
					break;
				case CCOMMENT:
					contentData= ";; "+token.getText();				
					contentCode = ";; "+token.getText();					
					break;
				case CPPCOMMENT:
					contentData= ";; "+token.getText();				
					contentCode = ";; "+token.getText();					
					break;
				case DEC:
					break;
				case DIV_EQ:
					break;
				case ELLIPSIS:
					break;
				case EOF:
					break;
				case EQ:
					break;
				case GE:
					break;
				case HASH:
					//preprocessor directive
					hash = true;
					break;
				case HEADER:
					break;
				case IDENTIFIER:
					
					switch(token.getText()) {
					case "include":
						// Go to file recursively
						include = true;
						break;
					case "int":
						contentData = "";
						break;
					case "char":
						contentData = "";
						break;
					case "struct":
						contentData = "";
						offset += 1;
						break;
					default:
						//Is name
						offsets.put(offset, token.getText());						
						code += "";	
						contentData = " "+ token.getText() + " ";
						contentCode = "."+ token.getText() + " ";;
						
						break;
					}
					
					break;
				case INC:
					break;
				case NUMBER:
					break;
				case LAND:
					break;
				case LAND_EQ:
					break;
				case LE:
					break;
				case LITERAL:
					break;
				case LOR:
					break;
				case LOR_EQ:
					break;
				case LSH:
					break;
				case LSH_EQ:
					break;
				case MOD_EQ:
					break;
				case MULT_EQ:
					break;
				case NE:
					break;
				case NL:
					contentCode = "\n";
					break;
				case OR_EQ:
					break;
				case PASTE:
					break;
				case PLUS_EQ:
					break;
				case RANGE:
					break;
				case RSH:
					break;
				case RSH_EQ:
					break;
				case SQSTRING:
					break;
				case STRING:
					if(include) {
						// Go to file recursively
					}
					break;
				case SUB_EQ:
					break;
				case WHITESPACE:
					contentCode = " ";
					break;
				case XOR_EQ:
					break;
				case M_ARG:
					break;
				case M_PASTE:
					break;
				case M_STRING:
					break;
				case P_LINE:
					break;
				case INVALID:
					break;
				}
			}
		}
		content = " \n\n ;; DATA ;; \n\n" + contentData +" \n\n ;; CODE ;; \n\n" + contentCode;
		Files.writeString(fileName, content);
		return content;
	}

	/*
	 * static String Lexicon(String token) { switch(token) { case "+": return "ADD";
	 * case "-": return "SUB"; case "*": return "MUL"; case "/": return "DIV";
	 * default: return "NOP"; } }
	 * 
	 * static String Function(String token) {
	 * 
	 * 
	 * 
	 * 
	 * Pattern p = Pattern.compile("/^(.*)\\((.*,{0,1})*)\\)/"); Matcher m =
	 * p.matcher(token); if(m.matches()) { String function = m.group(1);
	 * List<String> args = new ArrayList<String>(); for(int i = 2; i <=
	 * m.groupCount(); i++) { args.add("" + m.group(i)); } } else {
	 * System.out.println("Function call malformed: incorrect syntax"); } }
	 * 
	 * static String Variable(String token) {
	 * 
	 * }
	 */
}
