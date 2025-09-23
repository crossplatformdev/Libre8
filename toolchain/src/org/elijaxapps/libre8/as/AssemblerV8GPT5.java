package org.elijaxapps.libre8.as;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssemblerV8GPT5 {
    // Sparse memory map that returns 0 for missing keys
    private static final Map<Long, Byte> memory = new HashMap<Long, Byte>() {
        @Override
        public Byte get(Object key) {
            Byte value = super.get(key);
            return value != null ? value : (byte) 0;
        }

        @Override
        public Byte put(Long key, Byte value) {
            if (value == null || value.byteValue() == 0) {
                // don't store zeros, keep memory sparse
                return super.remove(key);
            }
            return super.put(key, value);
        }
    };

    // ===== Instruction Opcodes (stored as int; bytes are written to mem) =====
    private static final int LD = 0x1d;
    private static final int LDA = 0x1a;
    private static final int LDB = 0x1b;
    private static final int LDC = 0x1c;
    private static final int LDD = 0x1e;
    private static final int LDIA = 0xda;
    private static final int LDIB = 0xdb;
    private static final int LDIC = 0xdc;
    private static final int LDID = 0xdd;

    private static final int MOV_AMem = 0xf0;
    private static final int MOV_MemA = 0xf1;
    private static final int MOV_AB = 0xf2;
    private static final int MOV_AC = 0xf3;
    private static final int MOV_AD = 0xf4;
    private static final int MOV_BMem = 0xf5;
    private static final int MOV_MemB = 0xf6;
    private static final int MOV_BA = 0xf7;
    private static final int MOV_BC = 0xf8;
    private static final int MOV_BD = 0xf9;
    private static final int MOV_CMem = 0xfa;
    private static final int MOV_MemC = 0xfb;
    private static final int MOV_CA = 0xfc;
    private static final int MOV_CB = 0xfd;
    private static final int MOV_CD = 0xfe;
    private static final int MOV_DMem = 0xff;
    private static final int MOV_SP_BP = 0x01;
    private static final int MOV_DI_I = 0x02;
    private static final int MOV_REG_BP = 0x03;

    private static final int STA = 0x5a;
    private static final int STB = 0x5b;
    private static final int STC = 0x5c;
    private static final int STD = 0x5d;
    private static final int ADD = 0xaa;
    private static final int SUB = 0xa5;
    private static final int MUL = 0xa2;
    private static final int DIV = 0xad;
    private static final int DEC = 0xde;
    private static final int DECE = 0xdf;
    private static final int DECI = 0xd1;

    private static final int IADD = 0x6a;
    private static final int ISUB = 0x65;
    private static final int IMUL = 0x62;
    private static final int IDIV = 0x6d;

    private static final int POKE = 0x97;
    private static final int POKX = 0x9a;
    private static final int POKY = 0x9b;
    private static final int PXYD = 0x9c;
    private static final int PIKX = 0x9d;
    private static final int PIKY = 0x9e;
    private static final int PIYD = 0x9f;
    private static final int OUTA = 0x05;
    private static final int OUTB = 0x06;
    private static final int OUTC = 0x07;
    private static final int OUTD = 0x08;

    private static final int PSAX = 0xc1;
    private static final int PSAH = 0xc2;
    private static final int PSAL = 0xc3;
    private static final int POPX = 0xc4;
    private static final int POPH = 0xc5;
    private static final int POPL = 0xc6;

    private static final int LDI = 0xde;
    private static final int HLT = 0x91;
    private static final int STO = 0x86;
    private static final int NOP = 0x11;

    private static final int JMP = 0xe1;
    private static final int JZ = 0xe2;
    private static final int JC = 0xe3;
    private static final int JNZ = 0xe4;
    private static final int JNC = 0xe5;
    private static final int JNB = 0xe6;
    private static final int JB = 0xe7;
    private static final int JP = 0xe8;
    private static final int JNP = 0xe9;

    private static final int B = 0x80;
    private static final int BC = 0x81;
    private static final int BNC = 0x82;
    private static final int BZ = 0x83;
    private static final int BNZ = 0x84;
    private static final int BNB = 0x85;
    private static final int BP = 0x86;
    private static final int BNP = 0x87;
    private static final int BB = 0x88;
    private static final int BX = 0x89;

    private static final int RST = 0x77;
    private static final int PST = 0x78;

    private static final int PTRI = 0x79;
    private static final int PTRD = 0x7a;
    private static final int PTRL = 0x7b;
    private static final int PTRS = 0x7c;
    private static final int OUTT = 0x7d;
    private static final int OUTM = 0x7e;

    private static final int LDR = 0xaa;

    // ===== Memory Constants (unchanged) =====
    public static final int WORD_SIZE = 16;
    public static final long MEMORY_SIZE = 1L << 32;
    public static final long FILE_SIZE = 4L * 1024 * 1024 * 1024;
    public static final long TOTAL_SIZE = FILE_SIZE;

    // ===== Fast instruction lookup tables =====
    private static final Map<String, Integer> INSTRUCTION_MAP;
    static {
        INSTRUCTION_MAP = new HashMap<>();
        INSTRUCTION_MAP.put("JMP", JMP); INSTRUCTION_MAP.put("JZ", JZ); INSTRUCTION_MAP.put("JC", JC); 
        INSTRUCTION_MAP.put("JNZ", JNZ); INSTRUCTION_MAP.put("JNC", JNC); INSTRUCTION_MAP.put("JNB", JNB); 
        INSTRUCTION_MAP.put("JB", JB); INSTRUCTION_MAP.put("JP", JP); INSTRUCTION_MAP.put("JNP", JNP);
        INSTRUCTION_MAP.put("LDA", LDA); INSTRUCTION_MAP.put("LDB", LDB); INSTRUCTION_MAP.put("LDC", LDC); 
        INSTRUCTION_MAP.put("LDD", LDD); INSTRUCTION_MAP.put("LDIA", LDIA); INSTRUCTION_MAP.put("LDIB", LDIB); 
        INSTRUCTION_MAP.put("LDIC", LDIC); INSTRUCTION_MAP.put("LDID", LDID);
        INSTRUCTION_MAP.put("STA", STA); INSTRUCTION_MAP.put("STB", STB); INSTRUCTION_MAP.put("STC", STC); 
        INSTRUCTION_MAP.put("STD", STD); INSTRUCTION_MAP.put("ADD", ADD); INSTRUCTION_MAP.put("SUB", SUB); 
        INSTRUCTION_MAP.put("MUL", MUL); INSTRUCTION_MAP.put("DIV", DIV);
        INSTRUCTION_MAP.put("DEC", DEC); INSTRUCTION_MAP.put("DECE", DECE); INSTRUCTION_MAP.put("DECI", DECI);
        INSTRUCTION_MAP.put("IADD", IADD); INSTRUCTION_MAP.put("ISUB", ISUB); INSTRUCTION_MAP.put("IMUL", IMUL); 
        INSTRUCTION_MAP.put("IDIV", IDIV); INSTRUCTION_MAP.put("POKE", POKE); INSTRUCTION_MAP.put("POKX", POKX); 
        INSTRUCTION_MAP.put("POKY", POKY); INSTRUCTION_MAP.put("PXYD", PXYD); INSTRUCTION_MAP.put("PIKX", PIKX); 
        INSTRUCTION_MAP.put("PIKY", PIKY); INSTRUCTION_MAP.put("PIYD", PIYD);
        INSTRUCTION_MAP.put("OUT", OUTA); INSTRUCTION_MAP.put("OUTA", OUTA); INSTRUCTION_MAP.put("OUTB", OUTB); 
        INSTRUCTION_MAP.put("OUTC", OUTC); INSTRUCTION_MAP.put("OUTD", OUTD); INSTRUCTION_MAP.put("PSAX", PSAX); 
        INSTRUCTION_MAP.put("PSAH", PSAH); INSTRUCTION_MAP.put("PSAL", PSAL); INSTRUCTION_MAP.put("POPX", POPX); 
        INSTRUCTION_MAP.put("POPH", POPH); INSTRUCTION_MAP.put("POPL", POPL);
        INSTRUCTION_MAP.put("LDI", LDI); INSTRUCTION_MAP.put("HLT", HLT); INSTRUCTION_MAP.put("NOP", NOP);
        INSTRUCTION_MAP.put("MOV_AB", MOV_AB); INSTRUCTION_MAP.put("MOV_AC", MOV_AC); INSTRUCTION_MAP.put("MOV_AD", MOV_AD);
        INSTRUCTION_MAP.put("MOV_BA", MOV_BA); INSTRUCTION_MAP.put("MOV_BC", MOV_BC); INSTRUCTION_MAP.put("MOV_BD", MOV_BD);
        INSTRUCTION_MAP.put("MOV_CA", MOV_CA); INSTRUCTION_MAP.put("MOV_CB", MOV_CB); INSTRUCTION_MAP.put("MOV_CD", MOV_CD);
        INSTRUCTION_MAP.put("MOV_SP_BP", MOV_SP_BP); INSTRUCTION_MAP.put("MOV_REG_BP", MOV_REG_BP);
        INSTRUCTION_MAP.put("MOV_AMem", MOV_AMem); INSTRUCTION_MAP.put("MOV_BMem", MOV_BMem); 
        INSTRUCTION_MAP.put("MOV_CMem", MOV_CMem); INSTRUCTION_MAP.put("MOV_DMem", MOV_DMem);
        INSTRUCTION_MAP.put("MOV_MemA", MOV_MemA); INSTRUCTION_MAP.put("MOV_MemB", MOV_MemB); 
        INSTRUCTION_MAP.put("MOV_MemC", MOV_MemC);
        INSTRUCTION_MAP.put("B", B); INSTRUCTION_MAP.put("BC", BC); INSTRUCTION_MAP.put("BNC", BNC); 
        INSTRUCTION_MAP.put("BZ", BZ); INSTRUCTION_MAP.put("BNZ", BNZ); INSTRUCTION_MAP.put("BNB", BNB); 
        INSTRUCTION_MAP.put("BP", BP); INSTRUCTION_MAP.put("BNP", BNP); INSTRUCTION_MAP.put("BB", BB); 
        INSTRUCTION_MAP.put("BX", BX); INSTRUCTION_MAP.put("RST", RST); INSTRUCTION_MAP.put("PST", PST); 
        INSTRUCTION_MAP.put("PTRI", PTRI); INSTRUCTION_MAP.put("PTRD", PTRD); INSTRUCTION_MAP.put("PTRL", PTRL); 
        INSTRUCTION_MAP.put("PTRS", PTRS); INSTRUCTION_MAP.put("LDR", LDR); INSTRUCTION_MAP.put("OUTT", OUTT); 
        INSTRUCTION_MAP.put("OUTM", OUTM);
    }

    // ===== Single-token instructions (no operands) =====
    private static final Set<String> SINGLE_TOKEN_INSTRUCTIONS;
    static {
        SINGLE_TOKEN_INSTRUCTIONS = new HashSet<>();
        SINGLE_TOKEN_INSTRUCTIONS.add("DEC"); SINGLE_TOKEN_INSTRUCTIONS.add("DECE"); 
        SINGLE_TOKEN_INSTRUCTIONS.add("DECI"); SINGLE_TOKEN_INSTRUCTIONS.add("OUT"); 
        SINGLE_TOKEN_INSTRUCTIONS.add("OUTA"); SINGLE_TOKEN_INSTRUCTIONS.add("OUTB"); 
        SINGLE_TOKEN_INSTRUCTIONS.add("OUTC"); SINGLE_TOKEN_INSTRUCTIONS.add("OUTD");
        SINGLE_TOKEN_INSTRUCTIONS.add("PSAX"); SINGLE_TOKEN_INSTRUCTIONS.add("POPX"); 
        SINGLE_TOKEN_INSTRUCTIONS.add("MOV_AB"); SINGLE_TOKEN_INSTRUCTIONS.add("MOV_AC"); 
        SINGLE_TOKEN_INSTRUCTIONS.add("MOV_AD"); SINGLE_TOKEN_INSTRUCTIONS.add("MOV_BA"); 
        SINGLE_TOKEN_INSTRUCTIONS.add("MOV_BC"); SINGLE_TOKEN_INSTRUCTIONS.add("MOV_BD"); 
        SINGLE_TOKEN_INSTRUCTIONS.add("MOV_CA"); SINGLE_TOKEN_INSTRUCTIONS.add("MOV_CB"); 
        SINGLE_TOKEN_INSTRUCTIONS.add("MOV_CD"); SINGLE_TOKEN_INSTRUCTIONS.add("MOV_SP_BP"); 
        SINGLE_TOKEN_INSTRUCTIONS.add("MOV_REG_BP"); SINGLE_TOKEN_INSTRUCTIONS.add("OUTT"); 
        SINGLE_TOKEN_INSTRUCTIONS.add("OUTM");
    }

    // Other structures - use ArrayList with initial capacity for better performance
    public static final ArrayList<String> variables = new ArrayList<>(1000);
    private static final List<String> lines = new ArrayList<>(10000);
    private static final Map<String, String> varsMap = new HashMap<>(1000);
    private static final Map<String, String> fMap = new HashMap<>(1000);
    private static final Map<String, String> argsMap = new HashMap<>(1000);

    private static boolean code = false;
    private static boolean data = false;
    private static int offset = 1;

    public static int vcounter = 0;
    public static int counter = 1;

    private static String clean;

    // ===== Precompiled regex patterns to avoid recompilation =====
    private static final Pattern INSTR_PATTERN =
            Pattern.compile("^([a-zA-Z0-9]{1,4})\\s?([0-9A-Fa-f]{8,256}h?|[\\w_]{1,256}|.*)$");
    private static final Pattern HEX4_PATTERN = Pattern.compile("([0-9A-Fa-f]{2}){4}h?");
    private static final Pattern BYTES_PATTERN = Pattern.compile("([0-9A-Fa-f]{2}){1,256}");
    private static final Pattern DATA_PATTERN =
            Pattern.compile("([\\w\\*]+)\\s+([0-9A-Fa-f]{1,8})([hbdo])?\\s*([0-9A-Fa-f]{2}|'.*'|.*)?");

    public static void main(String[] args) throws Exception {
        System.out.println("Libre8 Assembler V8GPT - Compiling Assembly to Binary");
        if (args.length == 0) {
            System.out.println("No input file specified. Using default: main.as");
            args = new String[]{"main.as"};
        }
        run(args[0]);
    }

    public static void run(String filename) throws Exception {
        File inputFile = (filename.length() == 0) ? new File("main.as") : new File(filename);
        
        // Optimized file reading with try-with-resources
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
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
        }

        runCompiler();
        System.out.println("\n\nCompilation complete. Writing binary...");
        String outputFile = "output/bin.hex";
        if (outputFile.equals(filename)) {
            outputFile += ".bin";
        }
        File file = new File(outputFile);
        if (file.exists()) {
            file.delete();
            System.out.println("Overwriting existing file: " + outputFile);
        }
        // Stream the dump directly to disk to avoid holding it in memory
        writeHexDump(file);
        
        System.out.println("\nBinary written to: " + outputFile);
        System.out.println("\nSuccess compiling and writing binary!");
    }

    // Stream the same dump format directly to disk (identical to dump())
    private static void writeHexDump(File file) throws Exception {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        final char[] HEX = "0123456789ABCDEF".toCharArray();
        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), java.nio.charset.StandardCharsets.US_ASCII),
                1 << 20)) { // 1 MiB buffer
            for (long i = 0; i < FILE_SIZE; i++) {
                if (i % 48 == 0 && i != 0) {
                    out.write('\n');
                } else if (i > 0) {
                    out.write("  "); // two spaces before non-first byte on a line
                }
                int v = memory.get(i) & 0xFF;
                out.write(HEX[(v >>> 4) & 0xF]);
                out.write(HEX[v & 0xF]);
                out.write(' ');
            }
            // No trailing trim() in streamed version; dump() trims, but file content remains identical
            out.write('\n');
        }
    }

    // Faster hex dump, preserves exact same format, optimized for sparse memory
    public static String dump() {
        StringBuilder sb = new StringBuilder();
        for (long i = 0; i < 4L*1024*1024*1024; i++) {
            if (i % 48 == 0 && i != 0) {
                sb.append("\n");
            } else if (i > 0) {
                sb.append("  ");
            }
            // Use optimized format with sparse memory - get() returns 0 for missing keys
            byte value = memory.get(i);
            sb.append(String.format("%02X ", value & 0xFF));
        }
        return sb.toString().trim();
    }

    private static void runCompiler() throws Exception {
        // Remove expensive 4GB memory initialization - SparseMemory handles defaults
        
        for (String l : lines) {
            clean = l;
            if (clean.startsWith(";;") || clean.startsWith("//") || clean.startsWith("#")) {
                continue;
            }

            if (clean.startsWith("/*")) {
                while (!clean.endsWith("*/")) {
                    clean = lines.get(counter++).trim();
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
            }

            if (counter % 102400 == 0) {
                System.out.println();
            } else {
                if (counter % 102400 == 1) {
                    System.out.println("Line " + counter);
                }
            }
            counter++;
        }
    }

    private static int parseInstruction(String line, int counter) throws Exception {
        if (line == null || line.isEmpty()) {
            return offset;
        }
        Matcher matcher = INSTR_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new Exception("Error parsing line " + counter + " -> " + line);
        }

        String instruction = matcher.group(1);
        String operand = matcher.group(2);

        // Fast lookup using HashMap instead of switch
        Integer opcode = INSTRUCTION_MAP.get(instruction);
        if (opcode == null) {
            throw new Exception("Unknown mnemonic: " + instruction + " in line " + counter);
        }

        boolean isSingleToken = SINGLE_TOKEN_INSTRUCTIONS.contains(instruction);

        if (operand != null) {
            operand = operand.trim();
            // Optimize map lookups by avoiding multiple containsKey calls
            String mappedOperand = fMap.get(operand);
            if (mappedOperand == null) {
                mappedOperand = argsMap.get(operand);
                if (mappedOperand == null) {
                    mappedOperand = varsMap.get(operand);
                }
            }
            if (mappedOperand != null) {
                operand = mappedOperand;
            }
        }

        // Write opcode directly without string conversion
        memory.put((long) offset++, (byte) opcode.intValue());

        if (operand != null && !isSingleToken) {
            // Fast hex parsing - try direct parsing first, fall back to regex
            if (operand.length() == 8 || (operand.length() == 9 && operand.endsWith("h"))) {
                // 4-byte hex operand
                String hexStr = operand.endsWith("h") ? operand.substring(0, 8) : operand;
                try {
                    for (int i = 0; i < 4; i++) {
                        int v = Integer.parseInt(hexStr.substring(i * 2, (i + 1) * 2), 16);
                        memory.put((long) offset++, (byte) v);
                    }
                } catch (NumberFormatException e) {
                    // Fall back to regex if fast parsing fails
                    Matcher mHex = HEX4_PATTERN.matcher(operand);
                    if (mHex.matches()) {
                        for (int i = 0; i < 4; i++) {
                            int v = Integer.parseInt(operand.substring(i * 2, (i + 1) * 2), 16);
                            memory.put((long) offset++, (byte) v);
                        }
                    }
                }
            } else {
                // Variable length byte operand
                Matcher mByte = BYTES_PATTERN.matcher(operand);
                if (mByte.matches()) {
                    for (int i = 0; i < operand.length(); i += 2) {
                        int v = Integer.parseInt(operand.substring(i, i + 2), 16);
                        memory.put((long) offset++, (byte) v);
                    }
                }
            }
        }
        return offset;
    }

    private static void parseData(String line, int counter) throws Exception {
        if (line == null) return;

        Matcher matcher = DATA_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new Exception("Error parsing data at line: " + counter + " -> " + line);
        }

        String name = matcher.group(1);
        String addressHex = matcher.group(2);
        String radix = matcher.group(3);
        String value = matcher.group(4);

        int base = 16;
        switch (radix != null ? radix : "") {
            case "b": base = 2; break;
            case "o": base = 8; break;
            case "d": base = 10; break;
        }

        int address = Integer.parseInt(addressHex, base);
        String paddedAddress = String.format("%08X", address);

        boolean isByte = false;
        if (value != null) {
            if (value.contains("'")) {
                char c = value.replace("'", "").charAt(0);
                value = String.format("%02x", (int) c);
            }

            try {
                isByte = Integer.parseInt(value, 16) <= 0xff;
            } catch (NumberFormatException ignored) { 
                // Not a valid hex number, treat as non-byte
            }

            if (isByte) {
                variables.add(name + " " + paddedAddress + " " + value);
                varsMap.put(name, paddedAddress);
                memory.put((long) address, (byte) Integer.parseInt(value, 16));
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
