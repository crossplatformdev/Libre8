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

public class AssemblerV8GPT {

    // Opcodes por mnemónico
    private static final Map<String, Long> OPCODES = new HashMap<>();

    // Memoria y constantes
    public static final int WORD_SIZE = 16;
    public static final int MEMORY_SIZE = 16384;
    public static final int TOTAL_SIZE = MEMORY_SIZE * WORD_SIZE + WORD_SIZE;
    public static final int FILE_SIZE = 16 * 1024 * 1024;

    public static String[][] memory = new String[MEMORY_SIZE][WORD_SIZE];
    public static String[] mem0 = new String[FILE_SIZE];
    public static String[] variables = new String[FILE_SIZE];

    private static final Map<String, String> varsMap = new HashMap<>();
    private static final Map<String, String> fMap = new HashMap<>();
    private static final Map<String, String> argsMap = new HashMap<>();
    private static final List<String> lines = new ArrayList<>();

    private static boolean inCodeSection = false;
    private static boolean inDataSection = false;
    private static int offset = 0;
    public static int vcounter = 0;
    public static int counter = 1;

    static {
        OPCODES.put("LDIA", 0xdaL);
        OPCODES.put("LDIB", 0xdbL);
        OPCODES.put("LDIC", 0xdcL);
        OPCODES.put("LDID", 0xddL);
        OPCODES.put("LDA", 0x1aL);
        OPCODES.put("LDB", 0x1bL);
        OPCODES.put("LDC", 0x1cL);
        OPCODES.put("LDD", 0x1eL);
        OPCODES.put("STA", 0x5aL);
        OPCODES.put("STB", 0x5bL);
        OPCODES.put("STC", 0x5cL);
        OPCODES.put("STD", 0x5dL);
        OPCODES.put("ADD", 0xaaL);
        OPCODES.put("SUB", 0xa5L);
        OPCODES.put("MUL", 0xa2L);
        OPCODES.put("DIV", 0xadL);
        OPCODES.put("IADD", 0x6aL);
        OPCODES.put("ISUB", 0x65L);
        OPCODES.put("IMUL", 0x62L);
        OPCODES.put("IDIV", 0x6dL);
        OPCODES.put("DEC", 0xdeL);
        OPCODES.put("DECE", 0xdfL);
        OPCODES.put("LDI", 0xdeL);
        OPCODES.put("NOP", 0x11L);
        OPCODES.put("PSAX", 0xc1L);
        OPCODES.put("PSAH", 0xc2L);
        OPCODES.put("PSAL", 0xc3L);
        OPCODES.put("POPX", 0xc4L);
        OPCODES.put("POPH", 0xc5L);
        OPCODES.put("POPL", 0xc6L);
        OPCODES.put("POKE", 0x99L);
        OPCODES.put("POKX", 0x9aL);
        OPCODES.put("POKY", 0x9bL);
        OPCODES.put("PXYD", 0x9cL);
        OPCODES.put("OUT", 0x05L);
        OPCODES.put("OUTA", 0x05L);
        OPCODES.put("OUTB", 0x06L);
        OPCODES.put("OUTC", 0x07L);
        OPCODES.put("OUTD", 0x08L);
        OPCODES.put("MOV_AMem", 0xf0L);
        OPCODES.put("MOV_MemA", 0xf1L);
        OPCODES.put("MOV_AB", 0xf2L);
        OPCODES.put("MOV_AC", 0xf3L);
        OPCODES.put("MOV_AD", 0xf4L);
        OPCODES.put("MOV_BMem", 0xf5L);
        OPCODES.put("MOV_MemB", 0xf6L);
        OPCODES.put("MOV_BA", 0xf7L);
        OPCODES.put("MOV_BC", 0xf8L);
        OPCODES.put("MOV_BD", 0xf9L);
        OPCODES.put("MOV_CMem", 0xfaL);
        OPCODES.put("MOV_MemC", 0xfbL);
        OPCODES.put("MOV_CA", 0xfcL);
        OPCODES.put("MOV_CB", 0xfdL);
        OPCODES.put("MOV_CD", 0xfeL);
        OPCODES.put("MOV_DMem", 0xffL);
        OPCODES.put("MOV_SP_BP", 0x01L);
        OPCODES.put("MOV_DI_I", 0x02L);
        OPCODES.put("MOV_REG_BP", 0x03L);
        OPCODES.put("JMP", 0xe1L);
        OPCODES.put("JZ", 0xe2L);
        OPCODES.put("JC", 0xe3L);
        OPCODES.put("JNZ", 0xe4L);
        OPCODES.put("JNC", 0xe5L);
        OPCODES.put("JNB", 0xe6L);
        OPCODES.put("JB", 0xe7L);
        OPCODES.put("JP", 0xe8L);
        OPCODES.put("JNP", 0xe9L);
        OPCODES.put("B", 0x80L);
        OPCODES.put("BC", 0x81L);
        OPCODES.put("BNC", 0x82L);
        OPCODES.put("BZ", 0x83L);
        OPCODES.put("BNZ", 0x84L);
        OPCODES.put("BNB", 0x85L);
        OPCODES.put("BP", 0x86L);
        OPCODES.put("BNP", 0x87L);
        OPCODES.put("BB", 0x88L);
        OPCODES.put("BX", 0x89L);
        OPCODES.put("RST", 0x77L);
        OPCODES.put("PST", 0x78L);
        OPCODES.put("PTRI", 0x79L);
        OPCODES.put("PTRD", 0x7aL);
        OPCODES.put("PTRL", 0x7bL);
        OPCODES.put("PTRS", 0x7cL);
        OPCODES.put("LDR", 0xaaL);
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java AssemblerV8GPT <filename>");
            System.out.println("Default filename is 'main.as'");
            args = new String[]{"main.as"};
        }
        run(args[0]);
    }

    public static void run(String filename) throws Exception {
        File inputFile = filename.length() == 0 ? new File("main.as") : new File(filename);
        Arrays.fill(mem0, "00");

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            System.out.println("Parsing file...");
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().replaceAll(";.*", "").replaceAll("//.*", "").trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }

        compileLines();
        String output = dump();
        String outputFile = "output/bin.hex";
        if (outputFile.equals(filename)) {
            outputFile += ".bin";
        }
        String hexDump = dump();
        PrintWriter out = new PrintWriter(outputFile);
        out.println(hexDump);
        System.out.println("\nSuccess compiling and writing binary!");
    }

    private static void compileLines() throws Exception {
        for (String line : lines) {
            if (line.startsWith(";;")) {
                continue;
            }

            if (line.startsWith(".data")) {
                inDataSection = true;
                inCodeSection = false;
                System.out.println("Compiling data...");
                continue;
            }

            if (line.startsWith(".code")) {
                inCodeSection = true;
                inDataSection = false;
                System.out.println("\nCompiling code...");
                continue;
            }

            if (line.startsWith(".")) {
                String label = line.substring(1);
                System.out.println("\nfunction(): " + label);
                fMap.putIfAbsent("Main", "000000");
                offset = Integer.parseInt(fMap.computeIfAbsent(label, k -> String.format("%06X", offset)), 16);
                continue;
            }

            if (inCodeSection && !line.startsWith("end")) {
                offset = parseInstruction(line, counter);
            } else if (inDataSection && !line.isEmpty()) {
                parseData(line, counter);
            }

            if (++counter % 24 == 0) {
                System.out.println();
            }
            System.out.print(".");
        }
    }

    private static int parseInstruction(String line, int lineNumber) throws Exception {
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9_]+)\\s?([0-9A-Fa-f]{8,72}h?|[\\w_]+)?$");
        Matcher matcher = pattern.matcher(line);

        if (!matcher.matches()) {
            throw new Exception("Error parsing line " + lineNumber + " -> " + line);
        }

        String mnemonic = matcher.group(1).toUpperCase();
        String operand = matcher.group(2);
        boolean isSingle = mnemonic.matches("OUT.*|MOV_.*|DEC|DECE|PSAX|POPX|BX|NOP|HLT|RST|PST");

        Long opcode = OPCODES.get(mnemonic);
        if (opcode == null) {
            throw new Exception("Unknown mnemonic: " + mnemonic + " at line " + lineNumber);
        }

        mem0[offset++] = String.format("%02x", opcode);

        if (operand != null && !isSingle) {
            operand = resolveOperand(operand.trim());
            Matcher hexMatch = Pattern.compile("([0-9A-Fa-f]{8})h?").matcher(operand);
            if (hexMatch.matches()) {
                for (int i = 0; i < 4; i++) {
                    mem0[offset++] = operand.substring(i * 2, (i + 1) * 2);
                }
            }
        }
        return offset;
    }

    private static void parseData(String line, int lineNumber) throws Exception {
        Pattern pattern = Pattern.compile("([\\w\\*]+)\\s+([0-9A-Fa-f]{1,8})([hbdo])?\\s*(.+)?");
        Matcher matcher = pattern.matcher(line);

        if (!matcher.matches()) {
            throw new Exception("Error parsing data at line " + lineNumber + " -> " + line);
        }

        String name = matcher.group(1);
        String addrStr = matcher.group(2);
        String radix = matcher.group(3);
        String value = matcher.group(4);

        int base = 16;
        if (radix != null) {
            switch (radix) {
                case "b":
                    base = 2;
                    break;
                case "o":
                    base = 8;
                    break;
                case "d":
                    base = 10;
                    break;
                default:
                    base = 16;
                    break;
            }
        }

        int address = Integer.parseInt(addrStr, base);
        String paddedAddr = String.format("%08X", address);

        if (value != null) {
            if (value.contains("'")) {
                char c = value.replace("'", "").charAt(0);
                value = String.format("%02x", (int) c);
            }

            try {
                if (Integer.parseInt(value, 16) <= 0xFF) {
                    variables[vcounter++] = name + " " + paddedAddr + " " + value;
                    varsMap.put(name, paddedAddr);
                    mem0[address] = value;
                    return;
                }
            } catch (Exception ignored) {
            }

            String[] args = value.contains(",") ? value.split(",") : value.split(" ");
            for (int i = 0; i < args.length; i++) {
                argsMap.put(args[i], String.format("%08X", address + i));
            }
            fMap.put(name, String.format("%08X", address + args.length));
        } else {
            fMap.put(name, paddedAddr);
        }
    }

    private static String resolveOperand(String operand) {
        if (operand == null) {
            return null;
        }
        if (fMap.containsKey(operand)) {
            return fMap.get(operand);
        }
        if (argsMap.containsKey(operand)) {
            return argsMap.get(operand);
        }
        if (varsMap.containsKey(operand)) {
            return varsMap.get(operand);
        }
        return operand.replace("*", "").trim();
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
}
