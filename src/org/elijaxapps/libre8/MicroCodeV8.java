package org.elijaxapps.libre8;

import java.io.PrintWriter;

public class MicroCodeV8 {

	private static int WORD = (int) 35;
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
	


	private static long STA = 0x5a00;
	private static long STB = 0x5b00;
	private static long STC = 0x5c00;
	private static long STD = 0x5d00;

	private static long ADD = 0xaa00;
	private static long SUB = 0xa500;
	private static long MUL = 0xa200;
	private static long DIV = 0xad00;

	private static long DEC = 0xde00;
	private static long DECE = 0xdf00;

	private static long IADD = 0x6a00;
	private static long ISUB = 0x6500;
	private static long IMUL = 0x6200;
	private static long IDIV = 0x6d00;

	private static final long POKE = 0x9900;
	private static final long POKX = 0x9a00;
	private static final long POKY = 0x9b00;
	private static final long PXYD = 0x9c00;
	
	private static long OUTA = 0x0500;
	private static long OUTB = 0x0600;
	private static long OUTC = 0x0700;
	private static long OUTD = 0x0800;

	private static long PSAX = 0xc100;
	private static long PSAH = 0xc200;
	private static long PSAL = 0xc300;
	private static long POPX = 0xc400;
	private static long POPH = 0xc500;
	private static long POPL = 0xc600;

	private static long LDI = 0xde00;

	private static long HLT = 0x9100;
	private static long STO = 0x8600;

	private static long NOP = 0x1100;

	private static long JMP = 0xe100;
	private static long JZ = 0xe200;
	private static long JC = 0xe300;
	private static long JNZ = 0xe400;
	private static long JNC = 0xe500;
	private static long JNB = 0xe600;
	private static long JB = 0xe700;
	private static long JP = 0xe800;
	private static long JNP = 0xe900;

	private static long B = 0x8000;
	private static long BC = 0x8100;
	private static long BNC = 0x8200;
	private static long BZ = 0x8300;
	private static long BNZ = 0x8400;
	private static long BNB = 0x8500;
	private static long BP = 0x8600;
	private static long BNP = 0x8700;
	private static long BB = 0x8800;

	private static long BX = 0x8900;
	private static long RST = 0x7700;
	private static long PST = 0x7800;

	private static long LDR = 0xaa00;
	private static int icuadrant;

	public static void main(String[] args) throws Exception {
		// Registers
		System.out.println("Formatting...");
		format();
		System.out.println("Formatted!");
		// trainASCII();
		int ccount = 0;
		for (icuadrant = 0;

				icuadrant <= (Signals.GREATER_FLAG1 * 2)  

				; icuadrant += Signals.PARITY_FLAG1) {
			// Stack operations. Store 24b addres on LRS Stack
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

			createArithmetic(icuadrant, ADD, Signals.RO + Signals.MEMB,
					Signals.ALU_EOUT + Signals.SUM + Signals.FI);
			createArithmetic(icuadrant, SUB, Signals.RO + Signals.MEMB,
					Signals.ALU_EOUT + Signals.SUM + Signals.ALU_SUB + Signals.FI);

			createArithmetic(icuadrant, MUL, Signals.RO + Signals.MEMB,
					Signals.ALU_EOUT + Signals.SUM + Signals.ALU_DIV + Signals.FI);
			createArithmetic(icuadrant, DIV, Signals.RO + Signals.MEMB,
					Signals.ALU_EOUT + Signals.ALU_DIV + Signals.ALU_SUB + Signals.SUM + Signals.FI);

			createIArithmetic(icuadrant, IADD, Signals.SUM);
			createIArithmetic(icuadrant, ISUB, Signals.SUM + Signals.ALU_SUB);

			createOUTput(icuadrant, OUTA, Signals.AMEM);
			createOUTput(icuadrant, OUTB, Signals.BMEM);
			createOUTput(icuadrant, OUTC, Signals.CMEM);
			createOUTput(icuadrant, OUTD, Signals.DMEM);

			createINput(icuadrant, DEC, false);
			createINput(icuadrant, DECE, true);

			jump(icuadrant, JMP);

			jump(icuadrant, JNZ);
			notJump(icuadrant, JNC);
			jump(icuadrant, JNB);
			notJump(icuadrant, JNP);

			notJump(icuadrant, JC);
			notJump(icuadrant, JZ);
			notJump(icuadrant, JP);
			notJump(icuadrant, JB);

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

			createST(icuadrant, STA, Signals.AMEM);
			createST(icuadrant, STB, Signals.BMEM);
			createST(icuadrant, STC, Signals.CMEM);
			createST(icuadrant, STD, Signals.DMEM);

			
			createPOKE(icuadrant, POKX, Signals.REG_A + Signals.RO + Signals.POKE);
			createPOKE(icuadrant, POKY, Signals.REG_B + Signals.RO + Signals.POKE);
			createPOKE(icuadrant, POKE, Signals.REG_C + Signals.RO + Signals.POKE);
			createPXYD(icuadrant, PXYD);
		}

		String str = dump();

		try (PrintWriter out = new PrintWriter("./output/microcode.hex")) {
			out.println(str);
			out.close();
			System.out.println("Success generating microcode.hex");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void createPOKE(int icuadrant, long poke, long... operations) throws Exception {
		setOffset(poke, icuadrant);
		fetch();
//		bit24Indirection(true, true, true, operations);
		for(int m = 72; m > 0; --m)
		{
			readOneNotWrite();
			if(m!=1){
				write(Signals.RO + Signals.REG_C + Signals.POKE);
			}else{
				write(Signals.RO + Signals.REG_C + Signals.POKE + Signals.clpcr);
			}
			//write(Signals.CO + Signals.MI + Signals.RO + Signals.REG_C + Signals.POKE);
			//write(Signals.CE);
			
		}
	}

	private static void createPXYD(int icuadrant, long pxyd) throws Exception {
		setOffset(pxyd, icuadrant);
		fetch();
		
		readOneNotWrite();
		write(Signals.RO + Signals.REG_D + Signals.POKE);
		readOneNotWrite();
		write(Signals.RO + Signals.REG_A + Signals.POKE);
		readOneNotWrite();
		write(Signals.RO + Signals.REG_B + Signals.POKE);
		readOneNotWrite();
		write(Signals.RO + Signals.REG_C + Signals.POKE);
				
		write(Signals.clpcr);
	}

	private static void trainASCII() {

		for (int i = (int) (TOTAL_CELLS - 512); i < TOTAL_CELLS; i++) {
			write((long)i);
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

	private static void call(long icuadrant, long instruction) throws Exception {
		setOffset(instruction, icuadrant);
		fetch();
		bit24Indirection(true, true, false);
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

		bit24Indirection(true, true, true, register + Signals.RW);
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


		bit24Indirection(true, true, true, Signals.MEMA + Signals.RO);
		
		write(Signals.CMM);

		write(Signals.LR0 + Signals.SHIN);
		write(Signals.LR2 + Signals.SHIN);
		write(Signals.LR0 + Signals.LR2 + Signals.SHIN);

		write(Signals.JMP);
		
		write(Signals.clpcr);
	}

	private static void createOUTput(long icuadrant, long instruction, long dMEM) throws Exception {
		setOffset(instruction, icuadrant);
		fetch();
		write(Signals.COUT + dMEM + Signals.ALU_EOUT); // Something Failed Here...s
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

	private static void jump(long icuadrant, long jMP2) throws Exception {
		setOffset(jMP2, icuadrant);
		fetch();

		bit24Indirection(true, true, false);
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
		write(Signals.CO + Signals.MI);
		write(Signals.RO + Signals.MEMB + Signals.CE);
		write(Signals.FI + Signals.ALU_EOUT + d);
		write(Signals.clpcr);
	}

	private static void bit24Indirection(boolean withIndirection, boolean withJtoStack, boolean WithStack2J,
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

			// Advance RAM point
			readOneNotWrite();
			write(Signals.RO + Signals.SHIN);
			readOneNotWrite();
			write(Signals.RO + Signals.SHIN);
			readOneNotWrite();
			write(Signals.RO + Signals.SHIN);
			readOneNotWrite();
			write(Signals.RO + Signals.SHIN);
			
			// We should, first of all, let the breadcrumb.
			// That will let us make use of REG A.
			if (withJtoStack) {
				
				write(Signals.CO);
				write(Signals.SHOUT + Signals.LR0 + Signals.LRW);
				write(Signals.SHOUT + Signals.LR2 + Signals.LRW);
				write(Signals.SHOUT + Signals.LR0 + Signals.LR2 + Signals.LRW);
				write(Signals.SHOUT);
				
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
		if (WithStack2J) {

			// BEFORE a pop, we have to Decrement Stack polonger...
			write(Signals.CMM);
			
			write(Signals.SHIN + Signals.LR0 + Signals.LR2);
			write(Signals.SHIN + Signals.LR0 + Signals.LR2);
			write(Signals.SHIN + Signals.LR2);
			write(Signals.SHIN + Signals.LR0);


			write(Signals.JMP + Signals.RO);
			write(Signals.CO + Signals.MI);
		}

	}

	private static String dump() {

		StringBuilder sb = new StringBuilder();

		for (int fff = 1; fff <= memory.length; fff++) {
			int digits = WORD / 4;
			CharSequence value = new String(memory[fff-1] + " ");
			sb.append(value);
			if(fff%MEM_COLS==0) {
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

		long digits = WORD/4;
		long index = (i*MEM_COLS)+k;
		while (chunk.length() < digits) {
			chunk = "0" + chunk;
		}

		if (index < memory.length) {
			memory[(i*MEM_COLS)+k] = new String(chunk + " ");

			k += 1;
		}

		if (k >= MEM_COLS) {
			if (i < LENGTH) {
				i += 1;
			}

			k = 0;
		}

		return (long)index;
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
		write(Signals.CO + Signals.MI + +Signals.RO + Signals.CE);
	}

	private static long clear(int address) throws Exception {
		if((address <= POKE + 2 || address > POKE+255)) {
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

				
		
		private static final long REG_A = 	0b1L;			//Activate Register A
		private static final long REG_B = 	0b10L;			//Activate Register B
		private static final long REG_C = 	0b100L;			//Activate Register C
		private static final long REG_D = 	0b1000L;		//Activate Register D

		private static long LD 	=  		  	0b1000_0L;		//Load
		private static long CL 	=  			0b1000_00L;		//Clear
		private static long SIN =  			0b1000_000L;		//Shift In Enable
		private static long S0 	=  			0b1000_0000L; // 8 //Shift in value (0 or 1)

		private static long MI 	= 			0b1000_0000_0L;	//Memory In
		private static long RW 	= 			0b1000_0000_00L;	//Read/Write
		private static long RO 	= 			0b1000_0000_000L;	//Read Out
		private static long JMP = 			0b1000_0000_0000L;//Jump

		private static long RST = 			0b1000_0000_0000_0L;		//Right / Left Stack Selector
		private static long CO 	= 			0b1000_0000_0000_00L;		//PC counter out
		private static long CE 	= 			0b1000_0000_0000_000L;	//PC counter enable
		private static long SHIN= 			0b1000_0000_0000_0000L; // 16	//Shift for the stack (4 phases)

		private static long LRW = 			0b1000_0000_0000_0000_0L;			//Write enable Stack
		private static long LR0 = 			0b1000_0000_0000_0000_00L;		//LSB of STACK
		private static long LR2 = 			0b1000_0000_0000_0000_000L;		//MSB of STACK
		private static long LRS = 			0b1000_0000_0000_0000_0000L;		//Half of Stack selector

		private static long CPP = 			0b1000_0000_0000_0000_0000_0L;	//Increment Stack Pointer
		private static long CMM = 			0b1000_0000_0000_0000_0000_00L;	//Decrement Stack Pointer
		private static long ALU_DIV = 		0b1000_0000_0000_0000_0000_000L;          // ALU DIV
		private static long ALU_SUB = 		0b1000_0000_0000_0000_0000_0000L; // 24	// ALU SUB

		private static long ALU_EOUT = 		0b1000_0000_0000_0000_0000_0000_0L;		// ALU EOUT
		private static long KBI = 			0b1000_0000_0000_0000_0000_0000_00L;			// Keyboard Input
		private static long KBO = 			0b1000_0000_0000_0000_0000_0000_000L;			// Keyboard Output
		private static long SHOUT = 		0b1000_0000_0000_0000_0000_0000_0000L;		// Shifter Out
		private static long II = 			0b1000_0000_0000_0000_0000_0000_0000_0L;		// Instruction In

		private static final long O0 = 		0b1000_0000_0000_0000_0000_0000_0000_00L;		// Activate Output Register A

		private static long FI = 			0b1_0000_0000_0000_0000_0000_0000_0000_00L; // 34 //Flag In
		private static long COUT =   		0b1_0000_0000_0000_0000_0000_0000_0000_000L; // 35 // Print Output
		
		
		//private static long MII =  0b1000_0000_0000_0000_0000_0000_0000_0000_0000L;
		//private static long MO = 0b1000_0000_0000_0000_0000_0000_0000_0000_0000_0L;
		
		
		private static long HALT =   		0b1_0000_0000_0000_0000_0000_0000_0000_0000L; // 36 // HALT
		private static long POKE = 			0b1_0000_0000_0000_0000_0000_0000_0000_0000_0L; // 37 // POKE INSTRUCTION
		private static long clpcr = 		0b1_0000_0000_0000_0000_0000_0000_0000_0000_00L; // 38 // Clock Pulse


		private static long PARITY_FLAG0 = 0b0_0000;
		private static long PARITY_FLAG1 = 0b1_0000_0000_0000_0000L;
		private static long BORROW_FLAG0 = 0b00000;
		private static long BORROW_FLAG1 = 0b10_0000_0000_0000_0000L;
		private static long ZERO_FLAG0 =   0b0_0000;
		private static long ZERO_FLAG1 =   0b100_0000_0000_0000_0000L;		
		private static long CARRY_FLAG0 =  0b0_0000;
		private static long CARRY_FLAG1 =  0b1000_0000_0000_0000_0000L;



		private static long LESSER_FLAG0 = 0b0_0000;
		private static long LESSER_FLAG1 = 0b1_0000_0000_0000_0000_0000L;

		private static long EQUAL_FLAG0 =   0b0_0000;
		private static long EQUAL_FLAG1 =   0b10_0000_0000_0000_0000_0000L;

		private static long GREATER_FLAG0 = 0b0_0000;
		private static long GREATER_FLAG1 = 0b100_0000_0000_0000_0000_0000L;

		private static final long IDLE = 0; // 0
		private static final long BMEM = REG_B + Signals.O0; // 1
		private static final long BTOA = 0 + 0 + /*REG_B +*/ REG_A + Signals.LD + Signals.O0; // 2
		private static final long SUM = BTOA + Signals.O0; // 3

		private static long AMEM = 0 + REG_A + 0 + 0 + Signals.O0; // 4
		private static long ATOB = 0 + REG_A + 0 + 0 + Signals.O0 + REG_B; // 5
		private static long ATOC = 0 + REG_A + 0 + 0 + Signals.O0 + REG_C;
		private static long ATOD = 0 + REG_A + 0 + 0 + Signals.O0 + REG_D;

		

		private static long MEMA = REG_A + 0 + 0 + 0 + Signals.LD; // 8
		private static long MEMB = REG_B + 0 + 0 + 0 + Signals.LD; // 9
		private static long MEMC = REG_C + 0 + 0 + 0 + Signals.LD;
		private static long MEMD = REG_D + 0 + 0 + 0 + Signals.LD; // b

		private static long CMEM = REG_C + 0 + 0 + 0 + Signals.O0; // c
		private static long CTOA = REG_C + 0 + REG_A + Signals.LD + Signals.O0; // d
		private static long CTOB = REG_C + REG_B + Signals.LD + Signals.O0; // e
		private static long DMEM = REG_D + Signals.O0; // f
	}

}
