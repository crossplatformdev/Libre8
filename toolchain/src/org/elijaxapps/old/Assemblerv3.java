/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elijaxapps.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
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
public class Assemblerv3 {

	public static int word = (int) 16;
	public static int length = (int) (16384 * 16) + 16;
	public static String[][] memory = new String[16384][word];

	public static int i = 0;
	public static int k = 0;

	private static final long LD = 0x1d;

	private static final long LDA = 0x1a;
	private static final long LDB = 0x1b;
	private static final long LDC = 0x1c;
	private static final long LDD = 0x1e;

	private static final long LDIA = 0xda;
	private static final long LDIB = 0xdb;
	private static final long LDIC = 0xdc;
	private static final long LDID = 0xdd;

	// MOV A to X;
	private static final long MOV_AMem = 0xf0;
	private static final long MOV_MemA = 0xf1;
	private static final long MOV_AB = 0xf2;
	private static final long MOV_AC = 0xf3;
	private static final long MOV_AD = 0xf4;

	private static final long MOV_BMem = 0xf5;
	private static final long MOV_MemB = 0xf6;
	private static final long MOV_BA = 0xf7;
	private static final long MOV_BC = 0xf8;
	private static final long MOV_BD = 0xf9;

	private static final long MOV_CMem = 0xfa;
	private static final long MOV_MemC = 0xfb;
	private static final long MOV_CA = 0xfc;
	private static final long MOV_CB = 0xfd;
	private static final long MOV_CD = 0xfe;
	private static final long MOV_DMem = 0xff;

	private static final long MOV_SP_BP = 0x01;
	private static final long MOV_DI_I = 0x02;
	private static final long MOV_REG_BP = 0x03;

	private static long STA = 0x5a;
	private static long STB = 0x5b;
	private static long STC = 0x5c;
	private static long STD = 0x5d;

	private static long ADD = 0xaa;
	private static long SUB = 0xa5;
	private static long MUL = 0xa2;
	private static long DIV = 0xad;

	private static long DEC = 0xde;
	private static long DECE = 0xdf;

	private static long IADD = 0x6a;
	private static long ISUB = 0x65;
	private static long IMUL = 0x62;
	private static long IDIV = 0x6d;

	private static long OUTA = 0x05;
	private static long OUTB = 0x06;
	private static long OUTC = 0x07;
	private static long OUTD = 0x08;

	private static long PSAX = 0xc1;
	private static long PSAH = 0xc2;
	private static long PSAL = 0xc3;
	private static long POPX = 0xc4;
	private static long POPH = 0xc5;
	private static long POPL = 0xc6;

	private static long LDI = 0xde;

	private static long HLT = 0x91;
	private static long STO = 0x86;

	private static long NOP = 0x11;

	private static long JMP = 0xe1;
	private static long JZ = 0xe2;
	private static long JC = 0xe3;
	private static long JNZ = 0xe4;
	private static long JNC = 0xe5;
	private static long JNB = 0xe6;
	private static long JB = 0xe7;
	private static long JP = 0xe8;
	private static long JNP = 0xe9;

	private static long B = 0x80;
	private static long BC = 0x81;
	private static long BNC = 0x82;
	private static long BZ = 0x83;
	private static long BNZ = 0x84;
	private static long BNB = 0x85;
	private static long BP = 0x86;
	private static long BNP = 0x87;
	private static long BB = 0x88;

	private static long BX = 0x89;

	private static long LDR = 0xbb;
	private static long RST = 0x77;
	private static long PST = 0x78;

	public static long size = 4294967296L;

	public static ArrayMap<Long, String> mem0 = new ArrayMap<Long, String>(size);

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
	static HashMap<String, String> argsMap = new HashMap<String, String>();
	private static String clean;

	public static void main(String[] args) throws Exception {

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

			if (args.length == 0) {
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
			throw e;
		}

	}

	private static void runCompiler(int counter) throws Exception {
		String line;
		Iterator<String> it = lines.iterator();

		while (it.hasNext()) {
			clean = it.next();

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

				if (!fMap.containsKey("")) {
					// fMap.put("Main", "000000");
				}

				if (fMap.containsKey(clean)) {
					offset = Integer.valueOf(fMap.get(clean), 16);
				} else {
					fMap.put(clean, Long.toHexString(offset));
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
		String patternString = "^([a-z|A-Z|0-9]{1,4})\\s?([a|b|c|d|r0|r1|r2|r3|0123456789ABCDEFabcdef|a-z|A-Z|0-9|_|,|\\s]{1,256}){1,2}?$";
		Pattern pattern = Pattern.compile(patternString);
		if (line == null || line == "")
			return offset;
		Matcher matcher = pattern.matcher(line.toLowerCase());
		if (matcher.matches()) {
			String instruction = new String(matcher.group(1)).toUpperCase();
			String instructionHex = "";
			String replacedVar = matcher.group(2);
			boolean isSingleToken = false;
			boolean isDoubleToken = false;
			String[] vars = null;
			if (replacedVar != null) {
				String functionName = replacedVar;
				if (replacedVar.contains(",")) {

					vars = replacedVar.split(",");
					for (int i = 0; i < vars.length; i++) {
						isDoubleToken = true;

						if (fMap.containsKey(vars[i])) {
							replacedVar = fMap.get(vars[i]);
						}

						if (varsMap.containsKey(vars[i])) {
							replacedVar = varsMap.get(vars[i]);
						}

						if (argsMap.containsKey(replacedVar)) {
							replacedVar = argsMap.get(vars[i]);
						}

					}
				} else {
					vars = replacedVar.split("");
				}
			}

			// LD
			if (instruction.equals("JMP")) {
				instructionHex = Long.toHexString(JMP);
			} else if (instruction.equals("LD")) {
				instructionHex = Long.toHexString(LD);
				isDoubleToken = true;
			} else if (instruction.equals("RST")) {
				instructionHex = Long.toHexString(RST);
				isSingleToken = true;
			} else if (instruction.equals("PST")) {
				instructionHex = Long.toHexString(PST);
				isSingleToken = true;
			} else if (instruction.equals("DEC")) {
				instructionHex = Long.toHexString(DEC);
				isSingleToken = true;
			} else if (instruction.equals("DECE")) {
				instructionHex = Long.toHexString(DECE);
				isSingleToken = true;
			} else if (instruction.equals("PSAX")) {
				instructionHex = Long.toHexString(PSAX);
				isSingleToken = true;
			} else if (instruction.equals("PSAH")) {
				instructionHex = Long.toHexString(PSAH);
			} else if (instruction.equals("PSAL")) {
				instructionHex = Long.toHexString(PSAL);
			} else if (instruction.equals("POPX")) {
				instructionHex = Long.toHexString(POPX);
				isSingleToken = true;
			} else if (instruction.equals("POPH")) {
				instructionHex = Long.toHexString(POPH);
			} else if (instruction.equals("POPL")) {
				instructionHex = Long.toHexString(POPL);
			} else if (instruction.equals("B")) {
				instructionHex = Long.toHexString(B);
			} else if (instruction.equals("BC")) {
				instructionHex = Long.toHexString(BC);
			} else if (instruction.equals("BNZ")) {
				instructionHex = Long.toHexString(BNZ);
			} else if (instruction.equals("BNB")) {
				instructionHex = Long.toHexString(BNB);
			} else if (instruction.equals("BP")) {
				instructionHex = Long.toHexString(BP);
			} else if (instruction.equals("BNP")) {
				instructionHex = Long.toHexString(BNP);
			} else if (instruction.equals("BNC")) {
				instructionHex = Long.toHexString(BNC);
			} else if (instruction.equals("BZ")) {
				instructionHex = Long.toHexString(BZ);
			} else if (instruction.equals("BB")) {
				instructionHex = Long.toHexString(BB);
			} else if (instruction.equals("BX")) {
				instructionHex = Long.toHexString(BX);
			} else if (instruction.equals("LDR")) {
				instructionHex = Long.toHexString(LDR);
			} else if (instruction.equals("JC")) {
				instructionHex = Long.toHexString(JC);
			} else if (instruction.equals("JB")) {
				instructionHex = Long.toHexString(JB);
			} else if (instruction.equals("JNZ")) {
				instructionHex = Long.toHexString(JNZ);
			} else if (instruction.equals("JNB")) {
				instructionHex = Long.toHexString(JNB);
			} else if (instruction.equals("JB")) {
				instructionHex = Long.toHexString(JB);
			} else if (instruction.equals("JP")) {
				instructionHex = Long.toHexString(JP);
			} else if (instruction.equals("JNP")) {
				instructionHex = Long.toHexString(JNP);
			} else if (instruction.equals("JNC")) {
				instructionHex = Long.toHexString(JNC);
			} else if (instruction.equals("JZ")) {
				instructionHex = Long.toHexString(JZ);
			} else if (instruction.equals("LDA")) {
				instructionHex = Long.toHexString(LDA);
			} else if (instruction.equals("LDB")) {
				instructionHex = Long.toHexString(LDB);
			} else if (instruction.equals("LDC")) {
				instructionHex = Long.toHexString(LDC);
			} else if (instruction.equals("LDD")) {
				instructionHex = Long.toHexString(LDD);
			} else if (instruction.equals("LDIA")) {
				instructionHex = Long.toHexString(LDIA);
			} else if (instruction.equals("LDIB")) {
				instructionHex = Long.toHexString(LDIB);
			} else if (instruction.equals("LDIC")) {
				instructionHex = Long.toHexString(LDIC);
			} else if (instruction.equals("LDID")) {
				instructionHex = Long.toHexString(LDID);
			} else if (instruction.equals("STA")) {
				instructionHex = Long.toHexString(STA);
			} else if (instruction.equals("STB")) {
				instructionHex = Long.toHexString(STB);
			} else if (instruction.equals("STC")) {
				instructionHex = Long.toHexString(STC);
			} else if (instruction.equals("STD")) {
				instructionHex = Long.toHexString(STD);
			} else if (instruction.equals("ADD")) {
				instructionHex = Long.toHexString(ADD);
			} else if (instruction.equals("SUB")) {
				instructionHex = Long.toHexString(SUB);
			} else if (instruction.equals("IADD")) {
				instructionHex = Long.toHexString(IADD);
			} else if (instruction.equals("ISUB")) {
				instructionHex = Long.toHexString(ISUB);
			} else if (instruction.equals("OUT")) {
				isSingleToken = true;
				instructionHex = Long.toHexString(OUTA);
			} else if (instruction.equals("OUTA")) {
				isSingleToken = true;
				instructionHex = Long.toHexString(OUTA);
			} else if (instruction.equals("OUTB")) {
				isSingleToken = true;
				instructionHex = Long.toHexString(OUTB);
			} else if (instruction.equals("OUTC")) {
				isSingleToken = true;
				instructionHex = Long.toHexString(OUTC);
			} else if (instruction.equals("OUTD")) {
				isSingleToken = true;
				instructionHex = Long.toHexString(OUTD);
			} else if (instruction.equals("LDI")) {
				instructionHex = Long.toHexString(LDI);
			} else if (instruction.equals("HLT")) {
				instructionHex = Long.toHexString(HLT);
			} else if (instruction.equals("MOV")) {
				String slug = matcher.group(4).replaceAll("\\s", "").replaceAll(",", "").toUpperCase();
				if (slug == null) {
					String msg = "Can't find MOV slug.";
					System.err.println(msg);
					throw new Exception(msg);
				}
				switch (slug) {
				case "RBPA":
					instructionHex = Long.toHexString(MOV_REG_BP);
					isSingleToken = true;
					break;
				case "RBPRSP":
					instructionHex = Long.toHexString(MOV_SP_BP);
					isSingleToken = true;
					break;
				case "AB":
					instructionHex = Long.toHexString(MOV_AB);
					isSingleToken = true;
					break;
				case "AC":
					instructionHex = Long.toHexString(MOV_AC);
					isSingleToken = true;
					break;
				case "AD":
					instructionHex = Long.toHexString(MOV_AD);
					isSingleToken = true;
					break;
				case "BA":
					instructionHex = Long.toHexString(MOV_BA);
					isSingleToken = true;
					break;
				case "BC":
					instructionHex = Long.toHexString(MOV_BC);
					isSingleToken = true;
					break;
				case "CA":
					instructionHex = Long.toHexString(MOV_CA);
					isSingleToken = true;
					break;
				case "CB":
					instructionHex = Long.toHexString(MOV_CB);
					isSingleToken = true;
					break;
				case "CD":
					instructionHex = Long.toHexString(MOV_CD);
					isSingleToken = true;
				case "A":
					instructionHex = Long.toHexString(MOV_AD);
					break;
				default:
					String msg = "Could not read MOV variable";
					System.err.println(msg);
					throw new Exception(msg);
				}
			} else if (instruction.equals("STO")) {
				instructionHex = Long.toHexString(STO);
			} else if (instruction.equals("NOP")) {
				instructionHex = Long.toHexString(NOP);
			} else {
				String msg = "Unknown mnemonic: " + instruction + " in line " + counter;
				System.err.println(msg);
				throw new Exception(msg);
			}

			if (offset < mem0.length) {

				mem0[offset] = instructionHex;
				offset += 1;

				if (replacedVar != null && (!isSingleToken || isDoubleToken)) {
					for (int j = vars.length-1; j >=0; j--) {
						replacedVar = vars[j];

						Pattern mb = Pattern.compile("([0123456789ABCDEFabcdef]{1,8})");

						Matcher bm = mb.matcher(replacedVar);
						if (bm.matches()) {
							String hex = "";
							for (int i = 1; i <= bm.groupCount(); i += 1) {
								hex = bm.group(i) == null ? "0" : bm.group(i);

							}
							while (hex.length() < 8) {
								hex = "0" + hex;
							}
							for (int i = 0; i < hex.length(); i += 2) {
								mem0[offset] = hex.substring(i, i + 2);
								offset += 1;
							}

						} else {
							Pattern mbyte = Pattern.compile("([0-255]|[0-f][0-f])");
							if (mbyte.matcher(replacedVar).matches()) {
								mem0[offset] = replacedVar;
								offset += 1;
							}
						}
					}
				}

			} else {
				String msg = "Out of memory";
				System.err.println(msg);
				throw new Exception(msg);
			}

		} else

		{
			String msg = "Error parsing line " + counter + " -> " + line;
			System.err.println(msg);
			throw new Exception(msg);
		}

		return offset;
	}

	private static void parseData(String line, int counter) throws Exception {
		String patternString = "([\\-|0-9|a-z|A-Z|_]+)\\s+([0123456789ABCDEFabcdef]{1,8})([h|b|d|o])?\\s*([0123456789ABCDEFabcdef]{2}|.*)?";
		Pattern pattern = Pattern.compile(patternString);
		if (line == null)
			return;
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String address = Long.toHexString(Integer.valueOf(matcher.group(2), 16));
			String value = matcher.groupCount() == 4 ? matcher.group(4) : null;
			String radix = matcher.groupCount() == 4 ? matcher.group(3) : null;

			while (address.length() < 8) {
				address = "0" + address;
			}

			int cell = 0;
			int base = 16;
			if ("h".equals(radix)) {
				base = 16;
				cell = Integer.parseInt(address, base);
			}
			if ("b".equals(radix)) {
				base = 2;
				cell = Integer.parseInt(address, base);
			}
			if ("o".equals(radix)) {
				base = 8;
				cell = Integer.parseInt(address, base);
			}
			if ("d".equals(radix)) {
				base = 10;
				cell = Integer.parseInt(address, base);
			}

			// address = Integer.valueOf(address, base).toString();
			boolean isVar = false;
			if (value != null) {
				isVar = true;
				boolean isByte = false;
				try {
					isByte = Integer.parseInt(value, 16) <= 0xff;
				} catch (Exception e) {
					isByte = false;
				}
				if (value.contains("'")) {
					value = value.replaceAll("'", "");
					value = Long.toHexString((int) value.charAt(0));
				} else if (isByte) {
					//
				} else {
					isVar = false;
					String[] arguments = null;
					if (value.contains(" ")) {
						// value is argument list
						arguments = value.split("\\s");
					} else {
						arguments = new String[] { value };
					}

					Integer ctr = 0; // Here at least we got 1 arg, plus min offset 1 (1+1)
					String addr = "";
					for (String arg : arguments) {
						addr = Long.toHexString(
								Integer.valueOf(address, 16).intValue() + Integer.valueOf("" + ctr, 16).intValue())
								.toString();
						while (addr.length() < 8) {
							addr = "0" + addr;
						}
						varsMap.put(arg, addr);
						ctr += 1;
					}
					addr = Long
							.toHexString(
									Integer.valueOf(address, 16).intValue() + Integer.valueOf("" + ctr, 16).intValue())
							.toString();
					while (addr.length() < 6) {
						addr = "0" + addr;
					}
					fMap.put(name, addr);

				}

				if (isVar) {
					variables[vcounter] = name + " " + address + " " + value;
					varsMap.put(name, address);
					mem0[cell] = value;
				}

			} else {
				fMap.put(name, address);
			}

			// }
		} else {
			String msg = "Error parsing data at line: " + counter + " -> " + line;
			System.err.println(msg);
			throw new Exception(msg);
		}
	}
}
