package org.elijaxapps.libre8.ucode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MicroCodeV8GPT {

    // Constants
    private static final int WORD = 32; // 32 bits
    private static final int MEM_COLS = 8;
    private static final int MEM_DIGITS = WORD / 4;
    private static final int LENGTH = 0x100000;
    private static final int MEM_LEN = (MEM_COLS + 1) * LENGTH;
    private static final int MEM_CELLS = MEM_LEN * MEM_DIGITS;
    private static final int TOTAL_CELLS = MEM_LEN * MEM_COLS;

    private static String[] memory = new String[LENGTH];

    private static int i = 0;
    private static int k = 0;

    // Instruction codes (all atomic)
    private static final long LDA = 0x1a00;
    private static final long LDB = 0x1b00;
    private static final long LDC = 0x1c00;
    private static final long LDD = 0x1e00;
    private static final long LDIA = 0xda00;
    private static final long LDIB = 0xdb00;
    private static final long LDIC = 0xdc00;
    private static final long LDID = 0xdd00;
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
    private static final long AND = 0xa300;
    private static final long OR = 0xa400;
    private static final long DIV = 0xad00;
    private static final long DEC = 0xde00;
    private static final long DECE = 0xdf00;
    private static final long IADD = 0x6a00;
    private static final long ISUB = 0x6500;
    private static final long IMUL = 0x6200;
    private static final long IDIV = 0x6d00;
    private static final long POKE = 0x9500;
    private static final long POKX = 0x9a00;
    private static final long POKY = 0x9b00;
    private static final long PXYD = 0x9c00;
    private static final long OUTA = 0x0500;
    private static final long OUTB = 0x0600;
    private static final long OUTC = 0x0700;
    private static final long OUTD = 0x0800;
    private static final long OUTS = 0x0900;
    private static final long PSAX = 0xc100;
    private static final long PSAH = 0xc200;
    private static final long PSAL = 0xc300;
    private static final long POPX = 0xc400;
    private static final long POPH = 0xc500;
    private static final long POPL = 0xc600;
    private static final long LDI = 0x1d00;
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
    private static final long PTRI = 0x7900;
    private static final long PTRD = 0x7a00;
    private static final long PTRL = 0x7b00;
    private static final long PTRS = 0x7c00;
    private static final long LDR = 0xaa00;
    private static final long OUTT = 0x7d00; 

    private static int icuadrant;

    public static void main(String[] args) throws Exception {
        System.out.println("Formatting...");
        format();
        System.out.println("Formatted!");
        int ccount = 0;
        Long max = (Signals.PARITY_FLAG1 + Signals.ZERO_FLAG1 + Signals.CARRY_FLAG1 + Signals.BORROW_FLAG1
                /*+ Signals.LESSER_FLAG1 + Signals.GREATER_FLAG1 + Signals.EQUAL_FLAG1*/);
        for (icuadrant = 0; icuadrant <= max; icuadrant += Signals.PARITY_FLAG1) {
            // All atomic operations and stubs preserved
            push8b(icuadrant, PSAX, Signals.AMEM + Signals.RW);
            pop8b(icuadrant, POPX, Signals.MEMA + Signals.RO);
            pushRst(icuadrant, RST);
            popRst(icuadrant, PST);
            createLD(icuadrant, LDA, Signals.MEMA);
            createLD(icuadrant, LDB, Signals.MEMB);
            createLD(icuadrant, LDC, Signals.MEMC);
            createLD(icuadrant, LDD, Signals.MEMD);
            createLDI(icuadrant, LDIA, Signals.MEMA);
            createLDI(icuadrant, LDIB, Signals.MEMB);
            createLDI(icuadrant, LDIC, Signals.MEMC);
            createLDI(icuadrant, LDID, Signals.MEMD);
            createArithmetic(icuadrant, ADD, Signals.RO + Signals.MEMB, Signals.SUM, Signals.AMEM);
            createArithmetic(icuadrant, SUB, Signals.RO + Signals.MEMB, Signals.SUB, Signals.AMEM);
            createArithmetic(icuadrant, MUL, Signals.RO + Signals.MEMB, Signals.MUL, Signals.AMEM);
            createArithmetic(icuadrant, DIV, Signals.RO + Signals.MEMB, Signals.DIV, Signals.AMEM);
            createLogic(icuadrant, AND, Signals.RO + Signals.MEMB, Signals.AND, Signals.AMEM);
            createLogic(icuadrant, OR, Signals.RO + Signals.MEMB, Signals.OR, Signals.AMEM);
            createIArithmetic(icuadrant, IADD, Signals.SUM, Signals.AMEM);
            createIArithmetic(icuadrant, ISUB, Signals.SUB, Signals.AMEM);
            createIArithmetic(icuadrant, IDIV, Signals.DIV, Signals.AMEM);
            createIArithmetic(icuadrant, IMUL, Signals.MUL, Signals.AMEM);
            createOUTput(icuadrant, OUTA, Signals.AMEM);
            createOUTput(icuadrant, OUTB, Signals.BMEM);
            createOUTput(icuadrant, OUTC, Signals.CMEM);
            createOUTput(icuadrant, OUTD, Signals.DMEM);
            createOUTS(icuadrant, OUTS, Signals.AMEM);
            createINput(icuadrant, DEC, false);
            createINput(icuadrant, DECE, true);
            jump(icuadrant, JMP);
            createBX(icuadrant, BX);
            call(icuadrant, B);

            
            if ((icuadrant % Signals.PARITY_FLAG1) == Signals.PARITY_FLAG0 && icuadrant >= Signals.PARITY_FLAG1) {
                call(icuadrant, BP);
                notCall(icuadrant, BNP);
                jump(icuadrant, JP);
                notJump(icuadrant, JNP);
                /*
                    jump(icuadrant, JNZ);
                    jump(icuadrant, JNC);
                    jump(icuadrant, JNB);
                    notJump(icuadrant, JC);
                    notJump(icuadrant, JZ);
                    notJump(icuadrant, JB);
                    call(icuadrant, BNZ);
                    call(icuadrant, BNC);
                    call(icuadrant, BNB);
                    notCall(icuadrant, BC);
                    notCall(icuadrant, BZ);
                    notCall(icuadrant, BB);
                 */
            } else {
                notCall(icuadrant, BP);
                call(icuadrant, BNP);
                notJump(icuadrant, JP);
                jump(icuadrant, JNP);
            }

            if (icuadrant >= Signals.BORROW_FLAG1 && ((icuadrant % (Signals.BORROW_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.BORROW_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.BORROW_FLAG1 + Signals.ZERO_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.BORROW_FLAG1 + Signals.ZERO_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.BORROW_FLAG1 + Signals.CARRY_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.BORROW_FLAG1 + Signals.ZERO_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.BORROW_FLAG1 + Signals.CARRY_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.BORROW_FLAG1 + Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.BORROW_FLAG1 + Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0)) {
                call(icuadrant, BB);
                notCall(icuadrant, BNB);
                jump(icuadrant, JB);
                notJump(icuadrant, JNB);
            } else {
                notCall(icuadrant, BB);
                call(icuadrant, BNB);
                notJump(icuadrant, JB);
                jump(icuadrant, JNB);
            }

            if (icuadrant >= Signals.ZERO_FLAG1 && ((icuadrant % (Signals.ZERO_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.ZERO_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.ZERO_FLAG1 + Signals.BORROW_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.ZERO_FLAG1 + Signals.CARRY_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.ZERO_FLAG1 + Signals.BORROW_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.ZERO_FLAG1 + Signals.CARRY_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.ZERO_FLAG1 + Signals.CARRY_FLAG1 + Signals.BORROW_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.ZERO_FLAG1 + Signals.CARRY_FLAG1 + Signals.BORROW_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0)) {
                notCall(icuadrant, BNZ);
                call(icuadrant, BZ);
                notJump(icuadrant, JNZ);
                jump(icuadrant, JZ);
            } else {
                call(icuadrant, BNZ);
                notCall(icuadrant, BZ);
                jump(icuadrant, JNZ);
                notJump(icuadrant, JZ);
            }

            if (icuadrant >= Signals.CARRY_FLAG1 && ((icuadrant % Signals.CARRY_FLAG1) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.CARRY_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.CARRY_FLAG1 + Signals.BORROW_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.CARRY_FLAG1 + Signals.BORROW_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1 + Signals.BORROW_FLAG1)) == Signals.PARITY_FLAG0
                    || (icuadrant % (Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1 + Signals.BORROW_FLAG1 + Signals.PARITY_FLAG1)) == Signals.PARITY_FLAG0)) {
                notCall(icuadrant, BNC);
                call(icuadrant, BC);
                notJump(icuadrant, JNC);
                jump(icuadrant, JC);
            } else {
                call(icuadrant, BNC);
                notCall(icuadrant, BC);
                jump(icuadrant, JNC);
                notJump(icuadrant, JC);
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
            createPXYD(icuadrant, POKX, Signals.REG_A + Signals.RO + Signals.POKE);
            createPXYD(icuadrant, POKY, Signals.REG_B + Signals.RO + Signals.POKE);
            createPXYD(icuadrant, PXYD, Signals.REG_C + Signals.RO + Signals.POKE);
            createPTRI(icuadrant, PTRI);
            createPTRL(icuadrant, PTRL);
            createPTRS(icuadrant, PTRS);
            createOUTtelnet(icuadrant, OUTT,0);
        }
        saveToFile("./output/microcode.hex", dump());
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
            Signals.CO + Signals.MI + Signals.RO + Signals.CE
        };
        bit24Indirection(true, true, true, operations);
        write(Signals.clpcr);
    }

    private static void createPTRI(int icuadrant, long ptri) throws Exception {
        setOffset(ptri, icuadrant);
        fetch();
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
        long index = write(Signals.LR2 + Signals.LR0 + Signals.MEMA);
        write(Signals.RO + Signals.MEMB);
        write(Signals.FI + Signals.ALU_EOUT + Signals.SUM);
        write(Signals.clpcr);
    }

    private static void createPOKE(int icuadrant, long poke, long... operations) throws Exception {
        setOffset(poke, icuadrant);
        fetch();
        for(int zzz = 63; zzz > 0; zzz--) {
            readOneNotWrite();
            write(Signals.RO + Signals.REG_C + Signals.POKE);            
        }
        write(Signals.clpcr);
    }

    private static void createPXYD(int icuadrant, long pxyd, long operations) throws Exception {
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
        write(Signals.KBI + Signals.MEMA + Signals.FI + Signals.HALT + (withEcho ? Signals.KBO + Signals.COUT: 0));

        write(Signals.clpcr);
    }

    private static void call(long icuadrant, long instruction) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();
        bit24Indirection(true, true, false, Signals.MEMA + Signals.RO);
        write(Signals.clpcr);
    }

    private static void pushProgramCounterToStack() {
        write(Signals.CMM + Signals.LRS);
        write(Signals.AMEM + Signals.LR0 + Signals.LRW + Signals.LRS);
        write(Signals.BMEM + Signals.LR2 + Signals.LRW + Signals.LRS);
        write(Signals.CMEM + Signals.LR0 + Signals.LR2 + Signals.LRW + Signals.LRS);
        write(Signals.clpcr);
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

    private static void push8b(long icuadrant, long ins, long operation) throws Exception {
        createST(icuadrant, ins, operation);
    }

    private static void pop8b(long icuadrant, long ins, long operation) throws Exception {
        createLD(icuadrant, ins, operation);
    }

    private static void pop24b(long icuadrant, long ins) throws Exception {
        setOffset(ins, icuadrant);
        fetch();
        write(Signals.CPP + Signals.LRS);
        write(Signals.MEMA + Signals.LR0 + Signals.LRS);
        write(Signals.MEMB + Signals.LR2 + Signals.LRS);
        write(Signals.MEMC + Signals.LR2 + Signals.LR0 + Signals.LRS);
        write(Signals.LRS);
        write(Signals.clpcr);
    }

    private static void popRst(long icuadrant, long ins) throws Exception {
        setOffset(ins, icuadrant);
        fetch();
        write(Signals.LRS + Signals.LR2);
        write(Signals.SHIN);
        write(Signals.SHIN);
        write(Signals.SHIN);
        write(Signals.SHIN);
        write(Signals.CPP + Signals.LRS);
    }


    private static void notCall(long icuadrant, long instruction) throws Exception {
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
        write(Signals.clpcr);
    }

    private static void createOUTput(long icuadrant, long instruction, long dMEM) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();
        write(Signals.COUT + dMEM + Signals.ALU_EOUT); // Something Failed Here...s
        write(Signals.clpcr);
    }
    
    private static void createOUTS(long icuadrant, long instruction, long dMEM) throws Exception {
        setOffset(instruction, icuadrant);
        //If icuadrant is 0, never stop, but if ZERO_FLAG1 is set, stop after one iteration
        fetch();
        
        write(Signals.COUT + Signals.MEMB + Signals.KBO); // Something Failed Here...s
        
            
    }

    private static void createOUTtelnet(long icuadrant, long instruction, long dMEM) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();
        write(Signals.COUT + Signals.MEMA + Signals.KBO); // Something Failed Here...s
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

    private static void createLogic(long icuadrant, long instruction, long... e) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();
        bit24Indirection(true, true, true, e);
        write(Signals.clpcr);
    }

    private static void createIArithmetic(long icuadrant, long instruction, long d, long e) throws Exception {
        setOffset(instruction, icuadrant);
        fetch();
        readOneNotWrite();
        write(Signals.RO + Signals.MEMB + Signals.MEMA + Signals.FI + Signals.ALU_EOUT);        
        write(Signals.clpcr);
    }

    private static void bit24Indirection(boolean withIndirection, boolean withJumpTo, boolean withReturn,
            long... operations) throws Exception {
        if (withIndirection) {
            readOneNotWrite();
            readOneNotWrite();
            write(Signals.RO + Signals.MEMB);
            readOneNotWrite();
            write(Signals.RO + Signals.MEMC);
            readOneNotWrite();
            write(Signals.RO + Signals.MEMD);
            write(Signals.SHIN + Signals.DMEM);
            write(Signals.SHIN + Signals.CMEM);
            write(Signals.SHIN + Signals.BMEM);
            write(Signals.SHIN + Signals.BMEM);
        }
        if (withJumpTo) {
            write(Signals.CO);
            write(Signals.SHOUT + Signals.LR0 + Signals.LRW);
            write(Signals.SHOUT + Signals.LR2 + Signals.LRW);
            write(Signals.SHOUT + Signals.LR0 + Signals.LR2 + Signals.LRW);
            write(Signals.SHOUT + Signals.LRW);
            write(Signals.CPP);
        }
        write(Signals.JMP + Signals.RO);
        write(Signals.CO + Signals.MI);
        for (long op : operations) {
            write(op);
        }
        if (withReturn) {
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
        if ((address <= POKE + 2 || address > POKE + 128)) {
            fetch();
        }
        return address;
    }

    private static void format() throws Exception {
        long total = TOTAL_CELLS;
        k = 0;
        i = 0;
        for (int ii = 0; ii < total; ii += 2) {
            clear(ii);
        }
        k = 0;
        i = 0;
        System.out.print("\n");
    }

    private static void saveToFile(String filename, String content) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
        System.out.println("Firmware code saved to " + filename);
    }

    // Signals class preserved
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
        private static final long REG_A = 1L;
        private static final long REG_B = 1L << 1;
        private static final long REG_C = 1L << 2;
        private static final long REG_D = 1L << 3;
        private static final long LD = 1L << 4;
        private static final long CL = 1L << 5;
        private static final long SIN = 1L << 6;
        private static final long S0 = 1L << 7;
        private static final long MI = 1L << 8;
        private static final long RW = 1L << 9;
        private static final long RO = 1L << 10;
        private static final long JMP = 1L << 11;
        private static final long RST = 1L << 12;
        private static final long CO = 1L << 13;
        private static final long CE = 1L << 14;
        private static final long SHIN = 1L << 15;
        private static final long LRW = 1L << 16;
        private static final long LR0 = 1L << 17;
        private static final long LR2 = 1L << 18;
        private static final long LRS = 1L << 19;
        private static final long CPP = 1L << 20;
        private static final long CMM = 1L << 21;
        private static final long ALU_DIV = 1L << 22;
        private static final long ALU_SUB = 1L << 23;
        private static final long ALU_EOUT = 1L << 24;
        private static final long KBI = 1L << 25;
        private static final long KBO = 1L << 26;
        private static final long SHOUT = 1L << 27;
        private static final long II = 1L << 28;
        private static final long O0 = 1L << 29;
        private static final long FI = 1L << 30;
        private static final long COUT = 1L << 31;
        private static final long HALT = 1L << 32;
        private static final long POKE = 1L << 33;
        private static final long clpcr = 1L << 34;
        private static final long ANDOR = 0b1_0000_0000_0000_0000_0000_0000_0000_0000_000L;
        private static final long PARITY_FLAG0 = 0b0_0000;
        private static final long PARITY_FLAG1 = 0b10000000000000000L;
        private static final long BORROW_FLAG0 = 0b00000;
        private static final long BORROW_FLAG1 = 0b100000000000000000L;
        private static final long ZERO_FLAG0 = 0b00000;
        private static final long ZERO_FLAG1 = 0b1000000000000000000L;
        private static final long CARRY_FLAG0 = 0b00000;
        private static final long CARRY_FLAG1 = 0b10000000000000000000L;
        private static final long LESSER_FLAG0 = 0b00000;
        private static final long LESSER_FLAG1 = 0b100000000000000000000L;
        private static final long EQUAL_FLAG0 = 0b00000;
        private static final long EQUAL_FLAG1 = 0b1000000000000000000000L;
        private static final long GREATER_FLAG0 = 0b00000;
        private static final long GREATER_FLAG1 = 0b10000000000000000000000L;
        private static final long IDLE = 0;
        private static final long BMEM = REG_B + Signals.O0;
        private static final long BTOA = REG_B + REG_A + Signals.LD + Signals.O0;
        private static final long SUM = BTOA + Signals.FI + Signals.ALU_EOUT;
        private static final long SUB = BTOA + Signals.FI + Signals.ALU_SUB;
        private static final long DIV = BTOA + Signals.FI + Signals.ALU_DIV;
        private static final long MUL = BTOA + Signals.FI + Signals.ALU_DIV + Signals.ALU_EOUT;
        private static final long AND = BTOA + Signals.FI + Signals.ANDOR;
        private static final long OR = BTOA + Signals.FI + Signals.ANDOR + Signals.ALU_EOUT;

        private static final long AMEM = 0 + REG_A + 0 + 0 + Signals.O0;
        private static final long ATOB = 0 + REG_A + 0 + 0 + Signals.O0 + REG_B;
        private static final long ATOC = 0 + REG_A + 0 + 0 + Signals.O0 + REG_C;
        private static final long ATOD = 0 + REG_A + 0 + 0 + Signals.O0 + REG_D;
        private static final long MEMA = REG_A + 0 + 0 + 0 + Signals.LD;
        private static final long MEMB = REG_B + 0 + 0 + 0 + Signals.LD;
        private static final long MEMC = REG_C + 0 + 0 + 0 + Signals.LD;
        private static final long MEMD = REG_D + 0 + 0 + 0 + Signals.LD;
        private static final long CMEM = REG_C + Signals.O0;
        private static final long CTOA = REG_C + 0 + REG_A + Signals.LD + Signals.O0;
        private static final long CTOB = REG_C + REG_B + Signals.LD + Signals.O0;
        private static final long DMEM = REG_D + Signals.O0;
    }
}
