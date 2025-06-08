package org.elijaxapps.libre8;

import java.io.File;
import java.io.PrintWriter;

public class MicroCodeV8GPT {

    private static final int WORD = (int) 36;
    private static final int MEM_COLS = 8;
    private static final int MEM_DIGITS = WORD / 4;
    private static final int LENGTH = (int) (0x800000);
    private static final int MEM_LEN = (MEM_COLS + 1) * (LENGTH);
    private static final int MEM_CELLS = MEM_LEN * MEM_DIGITS;

    private static final int TOTAL_CELLS = MEM_LEN * MEM_COLS;

    private static String[] memory = new String[LENGTH]; // [(int) word];

    private static int i = 0;
    private static int k = 0;

    private static final long LD = 0x1d00;

    private static final long LDA = 0x1a00;
    private static final long LDB = 0x1b00;
    private static final long LDC = 0x1c00;
    private static final long LDD = 0x1e00;

    private static final long LDIA = 0xda00;
    private static final long LDIB = 0xdb00;
    private static final long LDIC = 0xdc00;
    private static final long LDID = 0xdd00;

    // MOV A to X;
    private static final long MOV_AMem = 0xf000;
    private static final long MOV_MemA = 0xf100;
    private static final long MOV_AB = 0xf200;
    private static final long MOV_AC = 0xf300;
    private static final long MOV_AD = 0xf400;

    private static final long MOV_BMem = 0xf500;
    private static final long MOV_MemB = 0xf600;
    private static final long MOV_BA = 0xf700;
    private static final long MOV_BC = 0xf800;
    private static final long MOV_BD = 0xf900;

    private static final long MOV_CMem = 0xfa00;
    private static final long MOV_MemC = 0xfb00;
    private static final long MOV_CA = 0xfc00;
    private static final long MOV_CB = 0xfd00;
    private static final long MOV_CD = 0xfe00;
    private static final long MOV_DMem = 0xff00;

    private static final long MOV_SP_BP = 0x0100;
    private static final long MOV_DI_I = 0x0200;
    private static final long MOV_REG_BP = 0x0300;

    private static final long STA = 0x5a00;
    private static final long STB = 0x5b00;
    private static final long STC = 0x5c00;
    private static final long STD = 0x5d00;

    private static final long ADD = 0xaa00;
    private static final long SUB = 0xa500;
    private static final long MUL = 0xa200;
    private static final long DIV = 0xad00;

    private static final long DEC = 0xde00;
    private static final long DECE = 0xdf00;
    private static final long DECI = 0xd100;

    private static final long IADD = 0x6a00;
    private static final long ISUB = 0x6500;
    private static final long IMUL = 0x6200;
    private static final long IDIV = 0x6d00;

    private static final long POKE = 0x9900;

    private static final long POKX = 0x9a00;
    private static final long POKY = 0x9b00;
    private static final long PXYD = 0x9c00;

    private static final long PIKX = 0x9d00;
    private static final long PIKY = 0x9e00;
    private static final long PIYD = 0x9f00;

    private static final long OUTA = 0x0500;
    private static final long OUTB = 0x0600;
    private static final long OUTC = 0x0700;
    private static final long OUTD = 0x0800;

    private static final long PSAX = 0xc100;
    private static final long PSAH = 0xc200;
    private static final long PSAL = 0xc300;
    private static final long POPX = 0xc400;
    private static final long POPH = 0xc500;
    private static final long POPL = 0xc600;

    private static final long LDI = 0xde00;

    private static final long HLT = 0x9100;
    private static final long STO = 0x8600;

    private static final long NOP = 0x1100;

    private static final long JMP = 0xe100;
    private static final long JZ = 0xe200;
    private static final long JC = 0xe300;
    private static final long JNZ = 0xe400;
    private static final long JNC = 0xe500;
    private static final long JNB = 0xe600;
    private static final long JB = 0xe700;
    private static final long JP = 0xe800;
    private static final long JNP = 0xe900;

    private static final long B = 0x8000;
    private static final long BC = 0x8100;
    private static final long BNC = 0x8200;
    private static final long BZ = 0x8300;
    private static final long BNZ = 0x8400;
    private static final long BNB = 0x8500;
    private static final long BP = 0x8600;
    private static final long BNP = 0x8700;
    private static final long BB = 0x8800;

    private static final long BX = 0x8900;
    private static final long RST = 0x7700;
    private static final long PST = 0x7800;

    private static final long PTRI = 0x7900; //Pointer increment
    private static final long PTRD = 0x7a00; //Pointer decrement
    private static final long PTRL = 0x7b00; //Pointer load (indirection to the pointer, once there. is load the next 4 bytes, with the values of registers A, B, C, D)
    private static final long PTRS = 0x7c00; //Pointer store (indirection to the pointer, once there. is store the next 4 bytes, with the values of registers A, B, C, D)

    private static final long LDR = 0xaa00;
    private static int icuadrant;

    public static void main(String[] args) throws Exception {
        // Registers
        System.out.println("Formatting...");
        format();
        System.out.println("Formatted!");

        int ccount = 0;
        for (icuadrant = 0; icuadrant <= (Signals.GREATER_FLAG1 * 2); icuadrant += Signals.PARITY_FLAG1) {
            System.out.println("Cuadrant count: " + ccount + " | Cuadrant: " + icuadrant);
            ccount += 1;

            push24b(icuadrant, PSAX);
            pop24b(icuadrant, POPX);

            pushRst(icuadrant, RST);
            popRst(icuadrant, PST);

            createLD(icuadrant, LD, Signals.MEMA);
            createLD(icuadrant, LDA, Signals.MEMA);
            createLD(icuadrant, LDB, Signals.MEMB);
            createLD(icuadrant, LDC, Signals.MEMC);
            createLD(icuadrant, LDD, Signals.MEMD);

            createLDI(icuadrant, LDIA, Signals.MEMA);
            createLDI(icuadrant, LDIB, Signals.MEMB);
            createLDI(icuadrant, LDIC, Signals.MEMC);
            createLDI(icuadrant, LDID, Signals.MEMD);

            createArithmetic(icuadrant, ADD, Signals.RO + Signals.MEMB, Signals.SUM + Signals.ALU_EOUT + Signals.FI);
            createArithmetic(icuadrant, SUB, Signals.RO + Signals.MEMB, Signals.SUM + Signals.ALU_SUB + Signals.FI);
            createArithmetic(icuadrant, MUL, Signals.RO + Signals.MEMB, Signals.SUM + Signals.ALU_DIV + Signals.FI);
            createArithmetic(icuadrant, DIV, Signals.RO + Signals.MEMB, Signals.SUM + Signals.ALU_DIV + Signals.ALU_SUB + Signals.FI);

            createIArithmetic(icuadrant, IADD, Signals.SUM);
            createIArithmetic(icuadrant, ISUB, Signals.SUM + Signals.ALU_SUB);

            createOUTput(icuadrant, OUTA, Signals.AMEM);
            createOUTput(icuadrant, OUTB, Signals.BMEM);
            createOUTput(icuadrant, OUTC, Signals.CMEM);
            createOUTput(icuadrant, OUTD, Signals.DMEM);

            createINput(icuadrant, DEC, false);
            createINput(icuadrant, DECE, true);
            createIINput(icuadrant, DECI, false);

            jump(icuadrant, JMP);

            // ✅ Fixed jump logic
            jump(icuadrant, JNZ);    // jump if not zero
            jump(icuadrant, JZ);  // do nothing if zero

            jump(icuadrant, JNC);    // jump if not carry
            jump(icuadrant, JC);  // do nothing if carry

            jump(icuadrant, JNB);    // jump if not borrow
            jump(icuadrant, JB);  // do nothing if borrow

            jump(icuadrant, JNP);    // jump if not parity
            jump(icuadrant, JP);  // do nothing if parity

            createBX(icuadrant, BX);

            call(icuadrant, B);
            call(icuadrant, BNZ);
            call(icuadrant, BNC);
            call(icuadrant, BNB);
            call(icuadrant, BNP);
            call(icuadrant, BC);
            call(icuadrant, BZ);
            call(icuadrant, BP);
            call(icuadrant, BB);

            if ((icuadrant % Signals.CARRY_FLAG1) == 0) {
                notCall(icuadrant, BNC);
                call(icuadrant, BC);
                notJump(icuadrant, JNC);
                jump(icuadrant, JC);
            }

            if ((icuadrant % Signals.ZERO_FLAG1) == 0) {
                notCall(icuadrant, BNZ);
                call(icuadrant, BZ);
                notJump(icuadrant, JNZ);
                jump(icuadrant, JZ);
            }

            if ((icuadrant % Signals.BORROW_FLAG1) == 0) {
                call(icuadrant, BB);
                notCall(icuadrant, BNB);
                jump(icuadrant, JB);
                notJump(icuadrant, JNB);
            }

            if ((icuadrant % Signals.PARITY_FLAG1) == 0) {
                call(icuadrant, BP);
                notCall(icuadrant, BNP);
                jump(icuadrant, JP);
                notJump(icuadrant, JNP);
            }

            createMOVtoMem(icuadrant, MOV_AMem, Signals.AMEM);
            createMOVtoMem(icuadrant, MOV_BMem, Signals.BMEM);
            createMOVtoMem(icuadrant, MOV_CMem, Signals.CMEM);
            createMOVtoMem(icuadrant, MOV_DMem, Signals.DMEM);

            createMOVBetweenRegisters(icuadrant, MOV_AB, Signals.ATOB);
            createMOVBetweenRegisters(icuadrant, MOV_AD, Signals.ATOD);
            createMOVBetweenRegisters(icuadrant, MOV_BA, Signals.BTOA);
            createMOVBetweenRegisters(icuadrant, MOV_BC, Signals.SUM);
            createMOVBetweenRegisters(icuadrant, MOV_CA, Signals.CTOA);
            createMOVBetweenRegisters(icuadrant, MOV_AC, Signals.ATOC);
            createMOVBetweenRegisters(icuadrant, MOV_CB, Signals.CTOB);

            createMOVfromMEM(icuadrant, MOV_MemA, Signals.MEMA);
            createMOVfromMEM(icuadrant, MOV_MemB, Signals.MEMB);
            createMOVfromMEM(icuadrant, MOV_MemC, Signals.MEMC);

            createMov_SP_BP(icuadrant);
            createHLT(icuadrant);
            createNOP(icuadrant);

            createST(icuadrant, STA, Signals.AMEM + Signals.RW);
            createST(icuadrant, STB, Signals.BMEM + Signals.RW);
            createST(icuadrant, STC, Signals.CMEM + Signals.RW);
            createST(icuadrant, STD, Signals.DMEM + Signals.RW);

            createPOKE(icuadrant, POKE, Signals.REG_C + Signals.RO + Signals.POKE);

            createPXYDirect(icuadrant, POKX, Signals.RO + Signals.MEMA, Signals.AMEM + Signals.POKE);
            createPXYDirect(icuadrant, POKY, Signals.RO + Signals.MEMB, Signals.BMEM + Signals.POKE);
            createPXYDirect(icuadrant, PXYD, Signals.RO + Signals.MEMC, Signals.CMEM + Signals.POKE);

            createPXYIndirect(icuadrant, PIKX, Signals.RO + Signals.MEMA, Signals.AMEM + Signals.POKE);
            createPXYIndirect(icuadrant, PIKY, Signals.RO + Signals.MEMB, Signals.BMEM + Signals.POKE);
            createPXYIndirect(icuadrant, PIYD, Signals.RO + Signals.MEMC, Signals.CMEM + Signals.POKE);

            createPTRI(icuadrant, PTRI);
            createPTRL(icuadrant, PTRL);
            createPTRS(icuadrant, PTRS);
        }

        String str = dump();
        String outputFile = "./output/microcode.hex";
        File file = new File(outputFile);
        if (file.exists()) {
            file.delete();
            System.out.println("File already exists, deleting: " + outputFile);
        }
        PrintWriter writer = new PrintWriter(file);
        writer.println(str);
        writer.close();
        System.out.println("Microcode written to: " + outputFile);
    }

    private static void createPTRL(int icuadrant, long ptrl) throws Exception {
        setOffset(ptrl, icuadrant);
        fetch();

        long[] operations = {};

        bit24Indirection(true, true, false, operations);

        write(Signals.clpcr);
    }

    private static void createPTRS(int icuadrant, long ptrs) throws Exception {
        setOffset(ptrs, icuadrant);
        fetch();

        long[] operations = {
            Signals.AMEM + Signals.RW,
            Signals.CO + Signals.MI + Signals.RO + Signals.CE,};

        bit24Indirection(true, true, true, operations);

        write(Signals.clpcr);
    }

    private static void createPTRI(int icuadrant, long ptri) throws Exception {
        setOffset(ptri, icuadrant);
        fetch();

        // Advance RAM point
        for (int l = 0; l < 3; l++) {
            readOneNotWrite();
            write(Signals.RO + Signals.MEMD);
            write(Signals.SHIN + Signals.DMEM);
        }

        write(Signals.CO);
        write(Signals.SHOUT + Signals.LR0 + Signals.LRW);
        write(Signals.SHOUT + Signals.LR2 + Signals.LRW);
        write(Signals.SHOUT + Signals.LR0 + Signals.LR2 + Signals.LRW);
        write(Signals.SHOUT);

        write(Signals.CPP);

        //Add 1 to the byte in LR0 + LR2
        long index = write(Signals.LR2 + Signals.LR0 + Signals.MEMA);
        write(Signals.RO + Signals.MEMB);
        write(Signals.FI + Signals.ALU_EOUT + Signals.SUM);
        write(Signals.clpcr);
    }

    private static void createPOKE(int icuadrant, long poke, long... operations) throws Exception {
        setOffset(poke, icuadrant);
        fetch();
//		bit24Indirection(true, true, true, operations);
        for (int m = 72; m > 0; --m) {
            readOneNotWrite();
            if (m != 1) {
                write(Signals.RO + Signals.REG_C + Signals.POKE);
            } else {
                write(Signals.RO + Signals.REG_C + Signals.POKE + Signals.clpcr);
            }
            //write(Signals.CO + Signals.MI + Signals.RO + Signals.REG_C + Signals.POKE);
            //write(Signals.CE);

        }
    }

    private static void createPXYDirect(int icuadrant, long pxyd, long... operations) throws Exception {
        setOffset(pxyd, icuadrant);
        fetch();
//		bit24Indirection(true, true, true, operations);
        readOneNotWrite();
        for (long operation : operations) {
            write(operation);
        }
        write(Signals.clpcr);
    }

    private static void createPXYIndirect(int icuadrant, long pxyd, long... operations) throws Exception {
        setOffset(pxyd, icuadrant);
        fetch();
        bit24Indirection(true, true, true, operations);
        write(Signals.clpcr);
    }

    private static void trainASCII() {

        for (int i = (int) (TOTAL_CELLS - 512); i < TOTAL_CELLS; i++) {
            write((long) i);
        }
    }

    private static void createINput(long icuadrant, long instruction, boolean withEcho) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();

        write(Signals.KBI + Signals.MEMA + Signals.HALT);

        if (withEcho) {
            write(Signals.AMEM + Signals.KBO);
        }
        write(Signals.clpcr);
    }

    private static void createIINput(long icuadrant, long instruction, boolean withEcho) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();

        write(Signals.KBI + Signals.MEMA);

        if (withEcho) {
            write(Signals.AMEM + Signals.KBO);
        }
        write(Signals.clpcr);
    }

    private static void call(long icuadrant, long instruction) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();
        bit24Indirection(true, true, false, Signals.MEMA + Signals.RO);
        write(Signals.clpcr);
    }

    private static void pushProgramCounterToStack() {

        push8b(Signals.AMEM, Signals.LR0);

        write(Signals.CO);
        write(Signals.CO + Signals.REG_A + Signals.LD);
        write(Signals.REG_A + Signals.O0 + Signals.LR0);

        write(Signals.CO + Signals.REG_A + Signals.LD);
        write(Signals.REG_A + Signals.O0 + Signals.LR2);

        write(Signals.CO + Signals.REG_A + Signals.LD);
        write(Signals.REG_A + Signals.O0 + Signals.LR0 + Signals.LR2);

        pop8b(Signals.MEMA, Signals.LR0);
    }

    private static void createMov_SP_BP(long icuadrant) throws Exception {
        setOffset(MOV_SP_BP, icuadrant);
        fetch();

        write(Signals.LRS + Signals.MEMA);
        write(Signals.AMEM + Signals.CPP + Signals.CMM);
        write(Signals.clpcr);
    }

    private static void createMov_REG_BP(long icuadrant, long registerToMem) throws Exception {
        setOffset(MOV_REG_BP, icuadrant);
        fetch();

        write(Signals.LRS + Signals.CMM + Signals.CPP + registerToMem);
        write(Signals.clpcr);
    }

    private static void createMov_DI_I(long icuadrant) throws Exception {
        setOffset(MOV_DI_I, icuadrant);
        fetch();

        write(Signals.LRS + Signals.MEMA);
        write(Signals.AMEM + Signals.CPP + Signals.CMM);
        write(Signals.clpcr);
    }

    private static void push24b(long icuadrant, long ins) throws Exception {
        setOffset(ins, icuadrant);

        fetch();

        write(Signals.CMM + Signals.LRS);

        write(Signals.AMEM + Signals.LR0 + Signals.LRW + Signals.LRS);
        write(Signals.BMEM + Signals.LR2 + Signals.LRW + Signals.LRS);
        write(Signals.CMEM + Signals.LR0 + Signals.LR2 + Signals.LRW + Signals.LRS);

        write(Signals.clpcr);
    }

    private static void pushRst(long icuadrant, long ins) throws Exception {
        setOffset(ins, icuadrant);

        fetch();

        write(Signals.CMM + Signals.LRS);

        write(Signals.CO + Signals.LRS + Signals.LR0);
        write(Signals.CO + Signals.LRS + Signals.LR0);
        write(Signals.CO + Signals.LRS + Signals.LR0);
        write(Signals.CO + Signals.LRS + Signals.LR0);

        write(Signals.LRS + Signals.LRW);

        write(Signals.clpcr);
    }

    private static void push8b(long aMEM, long lR0) {
        write(Signals.CMM + Signals.LRS);
        write(aMEM + lR0);
    }

    private static void pop24b(long icuadrant, long ins) throws Exception {
        setOffset(ins, icuadrant);
        fetch();

        // Copy LRS Address to regs
        write(Signals.CPP + Signals.LRS);

        write(Signals.MEMA + Signals.LR0 + Signals.LRS);
        write(Signals.MEMB + Signals.LR2 + Signals.LRS);
        write(Signals.MEMC + Signals.LR2 + Signals.LR0 + Signals.LRS);
        //write(Signals.MEMD + Signals.LR0 + Signals.LRS);

        // Decrement LRS
        write(Signals.LRS);
    }

    private static void popRst(long icuadrant, long ins) throws Exception {
        setOffset(ins, icuadrant);
        fetch();

        // Copy LRS Address to regs
        write(Signals.LRS + Signals.LR2);
        write(Signals.SHIN);
        write(Signals.SHIN);
        write(Signals.SHIN);
        write(Signals.SHIN);

        // Decrement LRS
        write(Signals.CPP + Signals.LRS);
    }

    private static void pop8b(long mEMA, long lR0) {
        write(mEMA + lR0);
        write(Signals.CPP + Signals.LRS);
    }

    private static void notCall(long icuadrant, long instruction) throws Exception {
        // setOffset(instruction, icuadrant);
        notJump(icuadrant, instruction);
    }

    private static void createST(long icuadrant, long instruction, long register) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();

        bit24Indirection(true, true, true, register);
        write(Signals.clpcr);
    }

    private static void popStack() {
        write(Signals.CMM);
        write(Signals.LR2);
        write(Signals.SHIN);
        write(Signals.SHIN);
        write(Signals.SHIN);
        write(Signals.SHIN);
        write(Signals.JMP);
        write(Signals.clpcr);
    }

    private static void popStackAddress(long... operation) {
        write(Signals.CMM);
        write(Signals.LR2);
        write(operation[0]);
        write(operation[1]);
        write(operation[2]);
        write(operation[3]);
    }

    private static void createNOP(long icuadrant) throws Exception {
        setOffset(NOP, icuadrant);
        fetch();

        write(Signals.clpcr);
    }

    private static void createHLT(long icuadrant) throws Exception {
        setOffset(HLT, icuadrant);

        fetch();

        write(Signals.HALT);
        // write(Signals.clpcr);
    }

    private static void createMOVfromMEM(long icuadrant, long instruction, long mEMC) throws Exception {
        setOffset(instruction, icuadrant);

        fetch();

        write(mEMC + Signals.RO);
        write(Signals.clpcr);
    }

    private static void createMOVBetweenRegisters(long icuadrant, long instruction, long aTOB) throws Exception {
        setOffset(instruction, icuadrant);

        fetch();

        write(aTOB);
        write(Signals.clpcr);
    }

    private static void createLD(long icuadrant, long lda2, long receptorRegister) throws Exception {
        setOffset(lda2, icuadrant);
        fetch();
        bit24Indirection(true, true, true, receptorRegister + Signals.RO);
        //bit24Indirection(true, true, true, receptorRegister + Signals.RW);
        write(Signals.clpcr);
    }

    private static void createLDI(long icuadrant, long ldia2, long mEMA) throws Exception {
        setOffset(ldia2, icuadrant);
        fetch();

        write(Signals.CO + Signals.MI);
        write(Signals.RO + mEMA + Signals.CE);
        write(Signals.clpcr);
    }

    private static void createLDR(long icuadrant, long instruction, long operation) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();

        bit24Indirection(true, false, true, operation);
        write(Signals.clpcr);
    }

    private static void createBX(long icuadrant, long instruction) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();

        bit24Indirection(true, false, true, Signals.MEMA + Signals.RO);

        //write(Signals.CMM);
        /* 
		write(Signals.SHIN + Signals.LR0);
		write(Signals.SHIN + Signals.LR2);
		write(Signals.SHIN + Signals.LR0 + Signals.LR2);
		write(Signals.SHIN + Signals.LR0 + Signals.LR2);

		write(Signals.JMP + Signals.RO);
         */
        write(Signals.clpcr);
    }

    private static void createOUTput(long icuadrant, long instruction, long dMEM) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();
        write(Signals.COUT + dMEM);
        write(Signals.clpcr);
    }

    private static void createLDDirect(long icuadrant, long instruction, long operation) throws Exception {
        setOffset(instruction, icuadrant);

        fetch();

        write(Signals.CO + Signals.MI);
        write(Signals.RO + operation);
        write(Signals.clpcr);
    }

    private static void createMOVtoMem(long icuadrant, long instruction, long dMEM) throws Exception {
        setOffset(instruction, icuadrant);

        fetch();

        write(Signals.RW + dMEM);
        write(Signals.clpcr);
    }

    private static void jump(long icuadrant, long JMP) throws Exception {
        setOffset(JMP, icuadrant);
        fetch();

        readOneNotWrite();
        write(Signals.RO + Signals.MEMA);
        readOneNotWrite();
        write(Signals.RO + Signals.MEMB);
        readOneNotWrite();
        write(Signals.RO + Signals.MEMC);
        readOneNotWrite();
        write(Signals.RO + Signals.MEMD);

        write(Signals.SHIN + Signals.DMEM);
        write(Signals.SHIN + Signals.CMEM);
        write(Signals.SHIN + Signals.BMEM);
        write(Signals.SHIN + Signals.AMEM);

        write(Signals.JMP + Signals.RO);
        write(Signals.clpcr);
    }

    private static void notJump(long icuadrant, long instruction) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();

        write(Signals.CO + Signals.CE);
        write(Signals.CO + Signals.CE);
        write(Signals.CO + Signals.CE);
        write(Signals.CO + Signals.CE);
        write(Signals.clpcr);
    }

    private static void createArithmetic(long icuadrant, long instruction, long... e) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();
        bit24Indirection(true, true, true, e);
        write(Signals.clpcr);
    }

    private static void createIArithmetic(long icuadrant, long instruction, long d) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();

        // bit24Indirection(true, true, true, operation1, operation2);
        readOneNotWrite();
        write(Signals.RO + Signals.MEMB);
        write(Signals.FI + Signals.ALU_EOUT + d);
        write(Signals.clpcr);
    }

    private static void bit24Indirection(boolean withIndirection, boolean withJumpTo, boolean withReturn,
            long... operations) throws Exception {
        // We're going to rewrite this funtion ----

        // So this is going to be the function which takes care of
        // a) Fetch indirections . . . FOR LDx Operations. (Register Operations)
        // [Breadcrumb, LDA]
        // b) Calls [Breadcrumb, Push]
        // c) Jumps [No breadcrumb, Push]
        // d) Returns [No breadcrumb, Pop, LDA]
        // Breadcrumb
        // We need to store the Computer current execution offset.
        // It is stored in J and we need it to move it To Stack
        // Assuming There is connectivity from J to LRS... which actually there isn' t.
        // :((
        // We need to activate LRW Signal, which is now activated when there is a
        // REGister to Bus operation...
        // Actually lets keep it Reg wise
        // Here we're or should be in this scenario.
        // A) Instruction have been fetch and RAM polonger is at it.
        // B) Next 3 bytes are an address
        // C) A operation is need to be done on destination
        if (withIndirection) {
            // Copy the RAM cell content to a register. Keep it wise. WE NEED 3 because cant
            // do PC operations mixed with RAM			
            readOneNotWrite();
            // PLACEHOLDER
            readOneNotWrite();
            // END PLACEHOLDER
            write(Signals.RO + Signals.MEMB);
            readOneNotWrite();
            write(Signals.RO + Signals.MEMC);
            readOneNotWrite();
            write(Signals.RO + Signals.MEMD);

            // Shift in the values to the registers
            write(Signals.SHIN + Signals.DMEM);
            write(Signals.SHIN + Signals.CMEM);
            write(Signals.SHIN + Signals.BMEM);
            // PLACEHOLDER2
            write(Signals.SHIN + Signals.BMEM);
            // END PLACEHOLDER2

            // We should, first of all, let the breadcrumb.
            // That will let us make use of REG A.
            if (withJumpTo) {
                write(Signals.CO);
                write(Signals.SHOUT + Signals.LR0 + Signals.LRW);
                write(Signals.SHOUT + Signals.LR2 + Signals.LRW);
                write(Signals.SHOUT + Signals.LR0 + Signals.LR2 + Signals.LRW);
                write(Signals.SHOUT + Signals.LRW);

                //write(Signals.LRW);
                // AFTER! a Push, we have to increment Stack polonger...
                write(Signals.CPP);

                // pop8b(Signals.MEMA, Signals.LR0 + Signals.LRS);
            }

            write(Signals.JMP + Signals.RO);
            write(Signals.CO + Signals.MI);

            for (long op : operations) {
                // Write the operation after the indirection.
                write(op);
            }
        }

        // Here we must copy back from the Stack to the J (Program counter)
        // At least, the counter wise operation is direct, as we do not need
        // LRW signal ^^
        if (withReturn) {

            // BEFORE a pop, we have to Decrement Stack polonger...
            write(Signals.CMM);

            write(Signals.SHIN + Signals.LR0);
            write(Signals.SHIN + Signals.LR2);
            write(Signals.SHIN + Signals.LR0 + Signals.LR2);
            write(Signals.SHIN + Signals.LR0 + Signals.LR2);

            write(Signals.JMP + Signals.RO);
            write(Signals.CO + Signals.MI);
        }

    }

    private static String dump() {

        StringBuilder sb = new StringBuilder();

        for (int fff = 1; fff <= memory.length; fff++) {
            int digits = WORD / 4;
            CharSequence value = new String(memory[fff - 1] + " ");
            sb.append(value);
            if (fff % MEM_COLS == 0) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private static void setOffset(long instruction, long cuadrant) {
        i = (int) ((instruction + cuadrant)) / MEM_COLS;
        k = (int) ((instruction + cuadrant) % MEM_COLS);
    }

    private static Long write(Long instruction) {
        String chunk = instruction.toHexString(instruction);

        long digits = WORD / 4;
        long index = (i * MEM_COLS) + k;
        while (chunk.length() < digits) {
            chunk = "0" + chunk;
        }

        if (index < memory.length) {
            memory[(i * MEM_COLS) + k] = new String(chunk + " ");

            k += 1;
        }

        if (k >= MEM_COLS) {
            if (i < LENGTH) {
                i += 1;
            }

            k = 0;
        }

        return (long) index;
    }

    private static void fetchOnce() throws Exception {
        write(Signals.CO + Signals.MI + Signals.CMM + Signals.LRS + Signals.LR0);
        write(Signals.RO + Signals.II);
    }

    private static void fetch() throws Exception {
        write(Signals.CO + Signals.MI + Signals.RO);
        write(Signals.RO + Signals.CE + Signals.II);
    }

    private static void readOneNotWrite() throws Exception {
        write(Signals.CO + Signals.MI + Signals.RO + Signals.CE);
    }

    private static long clear(int address) throws Exception {
        if ((address <= POKE + 2 || address > POKE + 255)) {
            fetch();
        }
//		} else {
//			memory[(int) address] = Long.toHexString(address) + " ";
//		}
        return address;
    }

    private static void format() throws Exception {
        long total = TOTAL_CELLS;
        for (int pp = 0; pp < 2; pp++) {
            k = 0;
            i = 0;
            System.out.println("Phase " + pp);
            for (int ii = 0; ii < total; ii++) {
                clear(ii);

//				} else {
//					memory[ii] = Long.toHexString(ii) + " ";
//				}
                /*
				 * if(ii % 0x1000 == 0) { if (ii % 120 != 0) { System.out.print("."); } else {
				 * System.out.print("\n"); } }
                 */
            }
        }

        k = 0;
        i = 0;

        System.out.print("\n");

    }

    private static class Signals {

        private static long MEM(String reg) {
            switch (reg) {
                case "A":
                    return AMEM;
                case "B":
                    return BMEM;
                case "C":
                    return CMEM;
                case "D":
                    return DMEM;
            }
            return 0;
        }

        // private static long OUTPUT_ENABLE = 0b100;
        // private static long WRITE_ENABLE = 0b1000;
        // private static long REG_CLEAR = 0b10000;
        // private static long REG_SHIFT = 0b100000;
        // private static long SHIFT_IN = 0b1000000;
        private static final long REG_A = 1L;			//Activate Register A (0b1)
        private static final long REG_B = 1L << 1;			//Activate Register B (0b10)
        private static final long REG_C = 1L << 2;			//Activate Register C (0b100)
        private static final long REG_D = 1L << 3;		//Activate Register D (0b1000)

        private static final long LD = 1L << 4;		//Load 
        private static final long CL = 1L << 5;		//Clear
        private static final long SIN = 1L << 6;		//Shift In
        private static final long S0 = 1L << 7;		//Shift Out 0

        private static final long MI = 1L << 8;		//Memory In
        private static final long RW = 1L << 9;		//Memory Read / Write
        private static final long RO = 1L << 10;	//Memory Read
        private static final long JMP = 1L << 11;	//Jump

        private static final long RST = 1L << 12;	//Reset
        private static final long CO = 1L << 13;	//Counter Out
        private static final long CE = 1L << 14;	//Counter Enable
        private static final long SHIN = 1L << 15;	//Shifter In

        private static final long LRW = 1L << 16;	//Write enable for stack registers
        private static final long LR0 = 1L << 17;	//Stack Register 0
        private static final long LR2 = 1L << 18;	//Stack Register 2
        private static final long LRS = 1L << 19;	//Stack Register Selector

        private static final long CPP = 1L << 20;	//Increment Stack Pointer
        private static final long CMM = 1L << 21;	//Decrement Stack Pointer
        private static final long ALU_DIV = 1L << 22;          // ALU DIV
        private static final long ALU_SUB = 1L << 23; // 24	// ALU SUB

        private static final long ALU_EOUT = 1L << 24;		// ALU EOUT
        private static final long KBI = 1L << 25;			// Keyboard Input
        private static final long KBO = 1L << 26;			// Keyboard Output
        private static final long SHOUT = 1L << 27;		// Shifter Out
        private static final long II = 1L << 28;		// Instruction In

        private static final long O0 = 1L << 29;		// Activate Output Register A

        private static final long FI = 1L << 30; // 34 //Flag In
        private static final long COUT = 1L << 31; // 35 // Print Output

        //private static final long MII =  0b1000_0000_0000_0000_0000_0000_0000_0000_0000L;
        //private static final long MO = 0b1000_0000_0000_0000_0000_0000_0000_0000_0000_0L;
        private static final long HALT = 1L << 32; // 36 // HALT
        private static final long POKE = 1L << 33; // 37 // POKE INSTRUCTION
        private static final long clpcr = 1L << 34; // 38 // Clock Pulse

        private static final long ANDOR = 0b1_0000_0000_0000_0000_0000_0000_0000_0000_000L; // 39 // AND OR

        private static final long PARITY_FLAG0 = 0b0_0000;
        private static final long PARITY_FLAG1 = 0b1_0000_0000_0000_0000L;
        private static final long BORROW_FLAG0 = 0b00000;
        private static final long BORROW_FLAG1 = 0b10_0000_0000_0000_0000L;
        private static final long ZERO_FLAG0 = 0b0_0000;
        private static final long ZERO_FLAG1 = 0b100_0000_0000_0000_0000L;
        private static final long CARRY_FLAG0 = 0b0_0000;
        private static final long CARRY_FLAG1 = 0b1000_0000_0000_0000_0000L;

        private static final long LESSER_FLAG0 = 0b0_0000;
        private static final long LESSER_FLAG1 = 0b1_0000_0000_0000_0000_0000L;

        private static final long EQUAL_FLAG0 = 0b0_0000;
        private static final long EQUAL_FLAG1 = 0b10_0000_0000_0000_0000_0000L;

        private static final long GREATER_FLAG0 = 0b0_0000;
        private static final long GREATER_FLAG1 = 0b100_0000_0000_0000_0000_0000L;

        private static final long IDLE = 0; // 0
        private static final long BMEM = REG_B + Signals.O0; // 1
        private static final long BTOA = REG_B + REG_A + Signals.LD + Signals.O0; // 2
        private static final long SUM = BTOA; // 3

        private static final long AMEM = 0 + REG_A + 0 + 0 + Signals.O0; // 4
        private static final long ATOB = 0 + REG_A + 0 + 0 + Signals.O0 + REG_B; // 5
        private static final long ATOC = 0 + REG_A + 0 + 0 + Signals.O0 + REG_C;
        private static final long ATOD = 0 + REG_A + 0 + 0 + Signals.O0 + REG_D;

        private static final long MEMA = REG_A + 0 + 0 + 0 + Signals.LD; // 8
        private static final long MEMB = REG_B + 0 + 0 + 0 + Signals.LD; // 9
        private static final long MEMC = REG_C + 0 + 0 + 0 + Signals.LD;
        private static final long MEMD = REG_D + 0 + 0 + 0 + Signals.LD; // b

        private static final long CMEM = REG_C + Signals.O0; // c
        private static final long CTOA = REG_C + 0 + REG_A + Signals.LD + Signals.O0; // d
        private static final long CTOB = REG_C + REG_B + Signals.LD + Signals.O0; // e
        private static final long DMEM = REG_D + Signals.O0; // f
    }

}
