/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elijaxapps.free8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author tooel
 */
public class Compiler {
	public static int LDA = 0x00;
	public static int LDB = 0x10;
	public static int LDC = 0x20;
	public static int LDD = 0x30;

	// MOV A to X;
	private static final int MOV_AMem = 0x40;
	private static final int MOV_MemA = 0x41;
	private static final int MOV_AB = 0x50;
	private static final int MOV_AC = 0x60;
	private static final int MOV_AD = 0x70;

	private static final int MOV_BMem = 0x80;
	private static final int MOV_MemB = 0x81;
	private static final int MOV_BA = 0x90;
	private static final int MOV_BC = 0xa0;
	private static final int MOV_BD = 0xb0;

	private static final int MOV_CMem = 0xc0;
	private static final int MOV_MemC = 0xc1;
	private static final int MOV_CA = 0xd0;
	private static final int MOV_CB = 0xe0;
	private static final int MOV_CD = 0xf0;
	private static final int MOV_DMem = 0xf0;

	public static int STA = 0x71;
	public static int STB = 0x72;
	public static int STC = 0x73;
	public static int STD = 0x74;

	public static int ADD = 0xf1;
	public static int SUB = 0xf2;

	public static int OUTA = 0x83;
	public static int OUTB = 0x84;
	public static int OUTC = 0x85;
	public static int OUTD = 0x86;

	public static int LDI = 0xf4;
	public static int DEC = 0x97;

	public static int HLT = 0xf5;
	public static int STO = 0xf6;

	public static int NOP = 0xf7;
	public static int JMP = 0xf8;

	private static int JZ = 0x68;
	private static int JC = 0x69;
	private static int JNZ = 0x79;
	private static int JNC = 0x78;

	private static int B = 0x91;
	private static int BC = 0x92;
	private static int BNC = 0x93;
	private static int BZ = 0x94;
	private static int BNZ = 0x95;

	private static int BX = 0x96;

	private static int LDR = 0x99;

	public static int size = 2097152 * 8;

	public static String[] mem0 = new String[size];

	private static boolean code = false;
	private static int offset;
	private static boolean data;

	/*
	 * Macro
	 * 
	 * vcounterh ; variable counter (addresses, n counter) pcounter ; ammount of
	 * parameters (variable) parametersh ; address of parameters (addresses, n
	 * counter) lch ; linecounter iflc ; in function linecounter flc ; function
	 * linecounter
	 */
	public static Integer vcounterh = 0;
	public static Integer pcounter = 0;
	public static Integer parametersh = 0;
	public static Integer lch = 0;
	public static Integer iflc = 0;
	public static Integer flc = 0;
	public static Integer fc = 0;
	private static List<String> lines = new ArrayList<String>();
	private static int vcounter;
	private static String[] variables = new String[size];

	static HashMap<String, String> varsMap = new HashMap<String, String>();
	static HashMap<String, String> fMap = new HashMap<String, String>();

	public static void main(String[] args) throws IOException {

		// Format hex
		for (int i = 0; i < mem0.length; i++) {
			mem0[i] = new String("00");
		}

		File file;
		if (args.length == 0) {
			file = new File("./main.as");
		} else {
			file = new File(args[0]);
		}

		BufferedReader br = new BufferedReader(new FileReader(file));

		String line;
		int counter = 1;
		System.out.println("Parsing file...");
		while ((line = br.readLine()) != null) {
			String clean = "";
			// Cleanup
			if (line.contains(";")) {
				clean = line.trim().replaceAll("\t", "");
			}

			clean = line.trim();

			if (clean == null || clean.equals("")) {
				continue;
			}

			lines.add(clean);
		}
		br.close();

		try {
			runCompiler(counter);
			System.out.println();
			System.out.println("Success compiling program!");
			String strr = dump();

			if(args.length == 0) {
				PrintWriter out = new PrintWriter("./output/bin.hex");
				out.println(strr);
				out.close();
			} else {
				PrintWriter out = new PrintWriter(args[0]);
				out.println(strr);
				out.close();
			}

			System.out.println("Success writing binary!");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	private static void runCompiler(int counter) throws Exception {
		String line;
		Iterator<String> it = lines.iterator();
		while (it.hasNext()) {
			String clean = it.next();

			if (clean.startsWith(";;")) {
				continue;
			}

			if (clean.startsWith(".data")) {
				data = true;
				System.out.println("Compiling data...");
				line = "";
				continue;
			}

			if (clean.startsWith(".code")) {
				System.out.println();
				System.out.println("Compiling code...");
				data = false;
				code = true;
				continue;
			}

			if (clean.startsWith(".")) {
				clean = clean.replace(".", "");
				System.out.println();
				System.out.println("function(): " + clean);
				if (fMap.containsKey(clean)) {
					offset = Integer.valueOf(fMap.get(clean), 16);
				} else {
					fMap.put(clean, Integer.toHexString(offset));
				}
				continue;
			}

			if (code) {
				if (!clean.startsWith("end") || !clean.equals("")) {
					offset = parseInstruction(clean, counter);
				}
			} else if (data) {
				parseData(clean, counter);
			}

			if (counter % 24 == 0) {
				System.out.println();
			}

			counter += 1;
			System.out.print(".");
		}
	}

	private static String dump() {
		StringBuilder sb = new StringBuilder();
		int counter = 0;
		for (int fff = 0; fff < size; fff++) {
			String str = new String(mem0[counter] + " ");
			sb.append(str);
			counter += 1;
			if (counter % 8 == 0) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	private static int parseInstruction(String line, int counter) throws Exception {
		String patternString = "^([a-z|A-Z|0-9]{1,4})\\s*([a-z|A-Z|0-9]+|[a-f|A-f|0-9]h)?\\s*$";
		Pattern pattern = Pattern.compile(patternString);
		if (line == null || line == "")
			return offset;
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches()) {
			String instruction = matcher.group(1);
			String instructionHex = "";
			String replacedVar = matcher.group(2);

			if (replacedVar != null) {
				if (varsMap.containsKey(matcher.group(2))) {
					replacedVar = varsMap.get(matcher.group(2));
				}

				if (fMap.containsKey(matcher.group(2))) {
					replacedVar = fMap.get(matcher.group(2));
				}
			}
			boolean isSingleToken = false;
			// LD
			if (instruction.equals("JMP")) {
				instructionHex = Integer.toHexString(JMP);
			} else if (instruction.equals("DEC")) {
				instructionHex = Integer.toHexString(DEC);
			} else if (instruction.equals("B")) {
				instructionHex = Integer.toHexString(B);
			} else if (instruction.equals("BC")) {
				instructionHex = Integer.toHexString(BC);
			} else if (instruction.equals("BNZ")) {
				instructionHex = Integer.toHexString(BNZ);
			} else if (instruction.equals("BNC")) {
				instructionHex = Integer.toHexString(BNC);
			} else if (instruction.equals("BZ")) {
				instructionHex = Integer.toHexString(BZ);
			} else if (instruction.equals("BX")) {
				instructionHex = Integer.toHexString(BX);
			} else if (instruction.equals("LDR")) {
				instructionHex = Integer.toHexString(LDR);
			} else if (instruction.equals("JC")) {
				instructionHex = Integer.toHexString(JC);
			} else if (instruction.equals("JNZ")) {
				instructionHex = Integer.toHexString(JNZ);
			} else if (instruction.equals("JNC")) {
				instructionHex = Integer.toHexString(JNC);
			} else if (instruction.equals("JZ")) {
				instructionHex = Integer.toHexString(JZ);
			} else if (instruction.equals("LDA")) {
				instructionHex = Integer.toHexString(LDA);
			} else if (instruction.equals("LDB")) {
				instructionHex = Integer.toHexString(LDB);
			} else if (instruction.equals("LDC")) {
				instructionHex = Integer.toHexString(LDC);
			} else if (instruction.equals("LDD")) {
				instructionHex = Integer.toHexString(LDD);
			} else if (instruction.equals("STA")) {
				instructionHex = Integer.toHexString(STA);
			} else if (instruction.equals("STB")) {
				instructionHex = Integer.toHexString(STB);
			} else if (instruction.equals("STC")) {
				instructionHex = Integer.toHexString(STC);
			} else if (instruction.equals("STD")) {
				instructionHex = Integer.toHexString(STD);
			} else if (instruction.equals("ADD")) {
				instructionHex = Integer.toHexString(ADD);
			} else if (instruction.equals("SUB")) {
				instructionHex = Integer.toHexString(SUB);
			} else if (instruction.equals("OUTA")) {
				isSingleToken = true;
				instructionHex = Integer.toHexString(OUTA);
			} else if (instruction.equals("OUTB")) {
				isSingleToken = true;
				instructionHex = Integer.toHexString(OUTB);
			} else if (instruction.equals("OUTC")) {
				isSingleToken = true;
				instructionHex = Integer.toHexString(OUTC);
			} else if (instruction.equals("OUTD")) {
				isSingleToken = true;
				instructionHex = Integer.toHexString(OUTD);
			} else if (instruction.equals("LDI")) {
				instructionHex = Integer.toHexString(LDI);
			} else if (instruction.equals("HLT")) {
				instructionHex = Integer.toHexString(HLT);
			} else if (instruction.equals("MOV")) {
				String slug = matcher.group(2).replaceAll("\\s", "").replaceAll(",", "");
				if (slug == null) {
					String msg = "Can't find MOV slug.";
					System.err.println(msg);
					throw new Exception(msg);
				}
				switch (slug) {
				case "AB":
					instructionHex = Integer.toHexString(MOV_AB);
					isSingleToken = true;
					break;
				case "AC":
					instructionHex = Integer.toHexString(MOV_AC);
					isSingleToken = true;
					break;
				case "AD":
					instructionHex = Integer.toHexString(MOV_AD);
					isSingleToken = true;
					break;
				case "BA":
					instructionHex = Integer.toHexString(MOV_BA);
					isSingleToken = true;
					break;
				case "BC":
					instructionHex = Integer.toHexString(MOV_BC);
					isSingleToken = true;
					break;
				case "CA":
					instructionHex = Integer.toHexString(MOV_CA);
					isSingleToken = true;
					break;
				case "CB":
					instructionHex = Integer.toHexString(MOV_CB);
					isSingleToken = true;
					break;
				case "CD":
					instructionHex = Integer.toHexString(MOV_CD);
					isSingleToken = true;
				case "A":
					instructionHex = Integer.toHexString(MOV_AD);
					break;
				default:
					if (matcher.group(5) != null) {
						String var = matcher.group(5);
						if (varsMap.containsKey(var)) {
							String rpl = varsMap.get(var);
							var = rpl;
						}
					} else {
						String msg = "Could not read MOV variable";
						System.err.println(msg);
						throw new Exception(msg);
					}
				}
			} else if (instruction.equals("STO")) {
				instructionHex = Integer.toHexString(STO);
			} else if (instruction.equals("NOP")) {
				instructionHex = Integer.toHexString(NOP);
			} else {
				String msg = "Unknown mnemonic: " + instruction + " in line " + counter;
				System.err.println(msg);
				throw new Exception(msg);
			}

			if (offset < mem0.length) {

				mem0[offset] = instructionHex;
				offset += 1;

				if (replacedVar != null && !isSingleToken) {

					while (replacedVar.length() < 6) {
						replacedVar = "0" + replacedVar;
					}
					
					Pattern mb = Pattern.compile(
							"([0-9|a-f|A-F][0-9|a-f|A-F])?([0-9|a-f|A-F][0-9|a-f|A-F])?([0-9|a-f|A-F]?[0-9|a-f|A-F])h?");

					Matcher bm = mb.matcher(replacedVar);
					if (bm.matches()) {
						if (bm.groupCount() != 3) {
							String msg = "Address malformed";
							System.err.println(msg);
							throw new Exception(msg);
						}

						for (int mi = 1; mi <= bm.groupCount(); mi += 1) {
							mem0[offset] = bm.group(mi);
							offset += 1;
						}
					}
				}

			} else {
				String msg = "Out of memory";
				System.err.println(msg);
				throw new Exception(msg);
			}

		} else {
			String msg = "Error parsing line " + counter + " -> " + line;
			System.err.println(msg);
			throw new Exception(msg);
		}

		return offset;
	}

	private static void parseData(String line, int counter) throws Exception {
		String patternString = "([\\-|0-9|a-z|A-Z]+)\\s+([0123456789ABCDEFabcdef]{1,6})h\\s*(([0123456789ABCDEFabcdef]{2})|([\\']{1}[\\w|\\W]{1}[\\']{1}))?";
		Pattern pattern = Pattern.compile(patternString);
		if (line == null)
			return;
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches()) {
			String nombre = matcher.group(1);
			String address = matcher.group(2);
			String value = matcher.group(3);
			if (value != null) {
				
				if(value.contains("'")) {
					value = value.replaceAll("'", "");
					value = Integer.toHexString((int)value.charAt(0));
				}
				
				variables[vcounter] = nombre + " " + address + " " + value;
				Integer cell = Integer.parseInt(address, 16);
				varsMap.put(nombre, address);
				mem0[cell] = value;
			} else {
				fMap.put(nombre, address);
			}
			// }
		} else {
			String msg = "Error parsing data at line: " + counter + " -> " + line;
			System.err.println(msg);
			throw new Exception(msg);
		}
	}
}
