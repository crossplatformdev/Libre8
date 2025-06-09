// Refactored AssemblerV8.java for readability and structure
// Functionality is preserved exactly as in the original version
package org.elijaxapps.libre8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssemblerV8 {

    // Instruction Opcodes
    private static final long LD = 0x1d;
    private static final long LDA = 0x1a;
    private static final long LDB = 0x1b;
    private static final long LDC = 0x1c;
    private static final long LDD = 0x1e;
    private static final long LDIA = 0xda;
    private static final long LDIB = 0xdb;
    private static final long LDIC = 0xdc;
    private static final long LDID = 0xdd;

    // MOV Instructions
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

    // ALU Instructions
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
    private static final long DECI = 0xd1;

    private static long IADD = 0x6a;
    private static long ISUB = 0x65;
    private static long IMUL = 0x62;
    private static long IDIV = 0x6d;

    // I/O Instructions
    private static long POKE = 0x99;
    private static final long POKX = 0x9a;
    private static final long POKY = 0x9b;
    private static final long PXYD = 0x9c;
    private static final long PIKX = 0x9d;
    private static final long PIKY = 0x9e;
    private static final long PIYD = 0x9f;
    private static long OUTA = 0x05;
    private static long OUTB = 0x06;
    private static long OUTC = 0x07;
    private static long OUTD = 0x08;

    // Stack and Pointer Operations
    private static long PSAX = 0xc1;
    private static long PSAH = 0xc2;
    private static long PSAL = 0xc3;
    private static long POPX = 0xc4;
    private static long POPH = 0xc5;
    private static long POPL = 0xc6;

    // Miscellaneous
    private static long LDI = 0xde;
    private static long HLT = 0x91;
    private static long STO = 0x86;
    private static long NOP = 0x11;

    // Jump and Branch
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

    private static long RST = 0x77;
    private static long PST = 0x78;

    private static long PTRI = 0x79;
    private static long PTRD = 0x7a;
    private static long PTRL = 0x7b;
    private static long PTRS = 0x7c;

    private static long LDR = 0xaa;

    // Memory Constants
    public static final int WORD_SIZE = 16;
    public static final int MEMORY_SIZE = 16384;
    public static final int TOTAL_SIZE = (MEMORY_SIZE * WORD_SIZE) + WORD_SIZE;
    public static final int FILE_SIZE = 16 * 1024 * 1024;

    public static String[][] memory = new String[MEMORY_SIZE][WORD_SIZE];
    public static String[] mem0 = new String[FILE_SIZE];
    public static String[] variables = new String[FILE_SIZE];

    // State and bookkeeping
    private static List<String> lines = new ArrayList<>();
    private static final Map<String, String> varsMap = new HashMap<>();
    private static final Map<String, String> fMap = new HashMap<>();
    private static final Map<String, String> argsMap = new HashMap<>();

    private static boolean code = false;
    private static boolean data = false;
    private static int offset;

    public static int vcounter = 0;
    public static int counter = 1;

    private static String clean;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("No input file specified. Using default: main.as");
            args = new String[]{"main.as"};
        }
        run(args[0]);
    }

    public static void run(String filename) throws Exception {
        Arrays.fill(mem0, "00");

        File inputFile = (filename.length() == 0) ? new File("main.as") : new File(filename);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(inputFile));
            System.out.println("Parsing file...");
            String line;
            while ((line = br.readLine()) != null) {
                clean = line.trim();
                if (!clean.isEmpty()) {
                    clean = line.trim().replaceAll(";.*", "").trim();
                    if (!clean.isEmpty()) {
                        lines.add(clean);
                    }
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        runCompiler();
        System.out.println("\n\nCompilation complete. Writing binary...");
        String hexDump = dump();
        String outputFile = "output/bin.hex";
        if (outputFile.equals(filename)) {
            outputFile += ".bin";
        }
        File file = new File(outputFile);
        if (file.exists()) {
            file.delete();
            System.out.println("Overwriting existing file: " + outputFile);
        }
        PrintWriter writer = new PrintWriter(file);
        writer.println(hexDump);
        writer.close();
        System.out.println("\nBinary written to: " + outputFile);
        System.out.println("\nSuccess compiling and writing binary!");
    }

    private static void runCompiler() throws Exception {
        for (String line : lines) {
            clean = line;
            if (clean.startsWith(";;") || clean.startsWith("//") || clean.startsWith("#")) {
                continue;
            }

            if (clean.startsWith("/*")) {
                while (!clean.endsWith("*/")) {
                    clean = lines.get(++counter).trim();
                }
                continue;
            }

            if (clean.contains(";") || clean.contains("//")) {
                clean = clean.split(";;|;|//")[0].trim();
            }

            if (clean.startsWith(".data")) {
                data = true;
                System.out.println("Compiling data...");
                continue;
            }
            if (clean.startsWith(".code")) {
                data = false;
                code = true;
                System.out.println("\nCompiling code...");
                continue;
            }

            if (clean.startsWith(".")) {
                String label = clean.replace(".", "");
                System.out.println("\nfunction(): " + label);
                fMap.putIfAbsent("Main", "000000");
                offset = Integer.parseInt(fMap.computeIfAbsent(label, k -> Integer.toHexString(offset)), 16);
                offset = Integer.parseInt(varsMap.computeIfAbsent(label, k -> Integer.toHexString(offset)), 16);
                continue;
            }

            if (code) {
                if (!clean.startsWith("end") && !clean.isEmpty()) {
                    offset = parseInstruction(clean, counter);
                }
            } else if (data) {
                if (clean.isEmpty() || clean.startsWith(";")) {
                    continue;
                }
                parseData(clean, counter);
                parseData(clean, counter);
            }

            if (counter % 24 == 0) {
                System.out.println();
            }
            counter++;
            System.out.print(".");
        }
    }

    private static String dump() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < FILE_SIZE; i++) {
            sb.append(mem0[i]).append(" ");
            if ((i + 1) % 8 == 0) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static int parseInstruction(String line, int counter) throws Exception {
        String patternString = "^([a-zA-Z0-9]{1,4})\\s?([0-9A-Fa-f]{8,72}h?|[\\w_]{1,256}|.*)$";
        Pattern pattern = Pattern.compile(patternString);
        if (line == null || line.isEmpty()) {
            return offset;
        }

        Matcher matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            throw new Exception("Error parsing line " + counter + " -> " + line);
        }

        String instruction = matcher.group(1);
        String operand = matcher.group(2);
        boolean isSingleToken = false;
        String instructionHex;

        // Replace known operand aliases
        if (operand != null) {
            operand = operand.trim();
            if (fMap.containsKey(operand)) {
                operand = fMap.get(operand);
            } else if (argsMap.containsKey(operand)) {
                operand = argsMap.get(operand);
            } else if (varsMap.containsKey(operand)) {
                operand = varsMap.get(operand);
            }
        }

        switch (instruction) {
            case "JMP":
                instructionHex = Long.toHexString(JMP);
                break;
            case "JZ":
                instructionHex = Long.toHexString(JZ);
                break;
            case "JC":
                instructionHex = Long.toHexString(JC);
                break;
            case "JNZ":
                instructionHex = Long.toHexString(JNZ);
                break;
            case "JNC":
                instructionHex = Long.toHexString(JNC);
                break;
            case "JNB":
                instructionHex = Long.toHexString(JNB);
                break;
            case "JB":
                instructionHex = Long.toHexString(JB);
                break;
            case "JP":
                instructionHex = Long.toHexString(JP);
                break;
            case "JNP":
                instructionHex = Long.toHexString(JNP);
                break;
            case "LDA":
                instructionHex = Long.toHexString(LDA);
                break;
            case "LDB":
                instructionHex = Long.toHexString(LDB);
                break;
            case "LDC":
                instructionHex = Long.toHexString(LDC);
                break;
            case "LDD":
                instructionHex = Long.toHexString(LDD);
                break;
            case "LDIA":
                instructionHex = Long.toHexString(LDIA);
                break;
            case "LDIB":
                instructionHex = Long.toHexString(LDIB);
                break;
            case "LDIC":
                instructionHex = Long.toHexString(LDIC);
                break;
            case "LDID":
                instructionHex = Long.toHexString(LDID);
                break;
            case "STA":
                instructionHex = Long.toHexString(STA);
                break;
            case "STB":
                instructionHex = Long.toHexString(STB);
                break;
            case "STC":
                instructionHex = Long.toHexString(STC);
                break;
            case "STD":
                instructionHex = Long.toHexString(STD);
                break;
            case "ADD":
                instructionHex = Long.toHexString(ADD);
                break;
            case "SUB":
                instructionHex = Long.toHexString(SUB);
                break;
            case "MUL":
                instructionHex = Long.toHexString(MUL);
                break;
            case "DIV":
                instructionHex = Long.toHexString(DIV);
                break;
            case "DEC":
                instructionHex = Long.toHexString(DEC);
                isSingleToken = true;
                break;
            case "DECE":
                instructionHex = Long.toHexString(DECE);
                isSingleToken = true;
                break;
            case "DECI":
                instructionHex = Long.toHexString(DECI);
                isSingleToken = true;
                break;
            case "IADD":
                instructionHex = Long.toHexString(IADD);
                break;
            case "ISUB":
                instructionHex = Long.toHexString(ISUB);
                break;
            case "IMUL":
                instructionHex = Long.toHexString(IMUL);
                break;
            case "IDIV":
                instructionHex = Long.toHexString(IDIV);
                break;
            case "POKE":
                instructionHex = Long.toHexString(POKE);
                break;
            case "POKX":
                instructionHex = Long.toHexString(POKX);
                break;
            case "POKY":
                instructionHex = Long.toHexString(POKY);
                break;
            case "PXYD":
                instructionHex = Long.toHexString(PXYD);
                break;
            case "PIKX":
                instructionHex = Long.toHexString(PIKX);
                break;
            case "PIKY":
                instructionHex = Long.toHexString(PIKY);
                break;
            case "PIYD":
                instructionHex = Long.toHexString(PIYD);
                break;
            case "OUT":
                instructionHex = Long.toHexString(OUTA);
                isSingleToken = true;
                break;
            case "OUTA":
                instructionHex = Long.toHexString(OUTA);
                isSingleToken = true;
                break;
            case "OUTB":
                instructionHex = Long.toHexString(OUTB);
                isSingleToken = true;
                break;
            case "OUTC":
                instructionHex = Long.toHexString(OUTC);
                isSingleToken = true;
                break;
            case "OUTD":
                instructionHex = Long.toHexString(OUTD);
                isSingleToken = true;
                break;
            case "PSAX":
                instructionHex = Long.toHexString(PSAX);
                isSingleToken = true;
                break;
            case "PSAH":
                instructionHex = Long.toHexString(PSAH);
                break;
            case "PSAL":
                instructionHex = Long.toHexString(PSAL);
                break;
            case "POPX":
                instructionHex = Long.toHexString(POPX);
                isSingleToken = true;
                break;
            case "POPH":
                instructionHex = Long.toHexString(POPH);
                break;
            case "POPL":
                instructionHex = Long.toHexString(POPL);
                break;
            case "LDI":
                instructionHex = Long.toHexString(LDI);
                break;
            case "HLT":
                instructionHex = Long.toHexString(HLT);
                break;
            case "NOP":
                instructionHex = Long.toHexString(NOP);
                break;
            case "MOV_AB":
                instructionHex = Long.toHexString(MOV_AB);
                isSingleToken = true;
                break;
            case "MOV_AC":
                instructionHex = Long.toHexString(MOV_AC);
                isSingleToken = true;
                break;
            case "MOV_AD":
                instructionHex = Long.toHexString(MOV_AD);
                isSingleToken = true;
                break;
            case "MOV_BA":
                instructionHex = Long.toHexString(MOV_BA);
                isSingleToken = true;
                break;
            case "MOV_BC":
                instructionHex = Long.toHexString(MOV_BC);
                isSingleToken = true;
                break;
            case "MOV_BD":
                instructionHex = Long.toHexString(MOV_BD);
                isSingleToken = true;
                break;
            case "MOV_CA":
                instructionHex = Long.toHexString(MOV_CA);
                isSingleToken = true;
                break;
            case "MOV_CB":
                instructionHex = Long.toHexString(MOV_CB);
                isSingleToken = true;
                break;
            case "MOV_CD":
                instructionHex = Long.toHexString(MOV_CD);
                isSingleToken = true;
                break;
            case "MOV_SP_BP":
                instructionHex = Long.toHexString(MOV_SP_BP);
                isSingleToken = true;
                break;
            case "MOV_REG_BP":
                instructionHex = Long.toHexString(MOV_REG_BP);
                isSingleToken = true;
                break;
            case "MOV_AMem":
                instructionHex = Long.toHexString(MOV_AMem);
                break;
            case "MOV_BMem":
                instructionHex = Long.toHexString(MOV_BMem);
                break;
            case "MOV_CMem":
                instructionHex = Long.toHexString(MOV_CMem);
                break;
            case "MOV_DMem":
                instructionHex = Long.toHexString(MOV_DMem);
                break;
            case "MOV_MemA":
                instructionHex = Long.toHexString(MOV_MemA);
                break;
            case "MOV_MemB":
                instructionHex = Long.toHexString(MOV_MemB);
                break;
            case "MOV_MemC":
                instructionHex = Long.toHexString(MOV_MemC);
                break;
            case "B":
                instructionHex = Long.toHexString(B);
                break;
            case "BC":
                instructionHex = Long.toHexString(BC);
                break;
            case "BNC":
                instructionHex = Long.toHexString(BNC);
                break;
            case "BZ":
                instructionHex = Long.toHexString(BZ);
                break;
            case "BNZ":
                instructionHex = Long.toHexString(BNZ);
                break;
            case "BNB":
                instructionHex = Long.toHexString(BNB);
                break;
            case "BP":
                instructionHex = Long.toHexString(BP);
                break;
            case "BNP":
                instructionHex = Long.toHexString(BNP);
                break;
            case "BB":
                instructionHex = Long.toHexString(BB);
                break;
            case "BX":
                instructionHex = Long.toHexString(BX);
                break;
            case "RST":
                instructionHex = Long.toHexString(RST);
                break;
            case "PST":
                instructionHex = Long.toHexString(PST);
                break;
            case "PTRI":
                instructionHex = Long.toHexString(PTRI);
                break;
            case "PTRD":
                instructionHex = Long.toHexString(PTRD);
                break;
            case "PTRL":
                instructionHex = Long.toHexString(PTRL);
                break;
            case "PTRS":
                instructionHex = Long.toHexString(PTRS);
                break;
            case "LDR":
                instructionHex = Long.toHexString(LDR);
                break;
            default:
                throw new Exception("Unknown mnemonic: " + instruction + " in line " + counter);
        }

        if (offset >= mem0.length) {
            throw new Exception("Out of memory");
        }
        mem0[offset++] = instructionHex;

        if (operand != null && !isSingleToken) {
            Matcher mHex = Pattern.compile("([0-9A-Fa-f]{2}){4}h?").matcher(operand);

            if (mHex.matches()) {
                for (int i = 0; i < 4; i++) {
                    mem0[offset++] = operand.substring(i * 2, (i + 1) * 2);
                }
            } else {
                Matcher mByte = Pattern.compile("([0-9A-Fa-f]{2}){1,72}").matcher(operand);
                if (mByte.matches()) {
                    for (int i = 0; i < operand.length(); i += 2) {
                        mem0[offset++] = operand.substring(i, i + 2);
                    }
                }
            }
        }
        return offset;
    }

    private static void parseData(String line, int counter) throws Exception {
        Pattern pattern = Pattern.compile("([\\w\\*]+)\\s+([0-9A-Fa-f]{1,8})([hbdo])?\\s*([0-9A-Fa-f]{2}|'.*'|.*)?");
        if (line == null) {
            return;
        }

        Matcher matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            throw new Exception("Error parsing data at line: " + counter + " -> " + line);
        }

        String name = matcher.group(1);
        String addressHex = matcher.group(2);
        String radix = matcher.group(3);
        String value = matcher.group(4);

        int base = 16;
        switch (radix != null ? radix : "") {
            case "b":
                base = 2;
                break;
            case "o":
                base = 8;
                break;
            case "d":
                base = 10;
                break;
        }

        int address = Integer.parseInt(addressHex, base);
        String paddedAddress = String.format("%08X", address);

        if (value != null) {
            if (value.contains("'")) {
                char c = value.replace("'", "").charAt(0);
                value = String.format("%02x", (int) c); // <-- FIXED: always 2-digit hex
            }

            boolean isByte = false;
            try {
                isByte = Integer.parseInt(value, 16) <= 0xff;
            } catch (Exception ignored) {
            }

            if (isByte) {
                variables[vcounter] = name + " " + paddedAddress + " " + value;
                varsMap.put(name, paddedAddress);
                mem0[address] = value;
            } else {
                String[] args = value.contains(",") ? value.split(";*")[0].split(",") : value.split(";*")[0].split(" ");
                for (int i = 0; i < args.length; i++) {
                    argsMap.put(args[i], String.format("%08X", address + i));
                }
                fMap.put(name, String.format("%08X", address + args.length));
            }
        } else {
            fMap.put(name, paddedAddress);
        }
    }
}
