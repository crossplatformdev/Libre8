package org.elijaxapps.old;

import java.io.PrintWriter;

public class MicroCode {

	public static int word = (int) 16;
	public static int length = (int) (1048576) + 16;
	public static String[][] memory = new String[65536][word];

	public static int i = 0;
	public static int k = 0;

	public static final int LDA = 0x1a00;
	public static final int LDB = 0x1b00;
	public static final int LDC = 0x1c00;
	public static final int LDD = 0x1d00;

	public static final int LDIA = 0xda00;
	public static final int LDIB = 0xdb00;
	public static final int LDIC = 0xdc00;
	public static final int LDID = 0xdd00;

	// MOV A to X;
	private static final int MOV_AMem = 0xa100;
	private static final int MOV_MemA = 0xa200;
	private static final int MOV_AB = 0xa300;
	private static final int MOV_AC = 0xa400;
	private static final int MOV_AD = 0xa500;

	private static final int MOV_BMem = 0xa600;
	private static final int MOV_MemB = 0xa700;
	private static final int MOV_BA = 0xa800;
	private static final int MOV_BC = 0xa900;
	private static final int MOV_BD = 0xaa00;

	private static final int MOV_CMem = 0xab00;
	private static final int MOV_MemC = 0xac00;
	private static final int MOV_CA = 0xad00;
	private static final int MOV_CB = 0xae00;
	private static final int MOV_CD = 0xaf00;
	private static final int MOV_DMem = 0xa000;
	
	private static final int MOV_SP_BP = 0xb000;
	private static final int MOV_DI_I = 0xb100;
	private static final int MOV_REG_BP = 0xb200;

	public static int STA = 0x5a00;
	public static int STB = 0x5b00;
	public static int STC = 0x5c00;
	public static int STD = 0x5d00;

	public static int ADD = 0xf100;
	public static int SUB = 0xf200;

	private static int DEC = 0xf300;
	private static int DECE = 0xf400;

	private static int IADD = 0xf500;
	private static int ISUB = 0xf600;

	public static int OUTA = 0x0500;
	public static int OUTB = 0x0600;
	public static int OUTC = 0x0700;
	public static int OUTD = 0x0800;

	private static int PSAX = 0xc100;
	private static int PSAH = 0xc200;
	private static int PSAL = 0xc300;
	private static int POPX = 0xc400;
	private static int POPH = 0xc500;
	private static int POPL = 0xc600;

	public static int LDI = 0xf700;

	public static int HLT = 0x9100;
	public static int STO = 0x8600;

	public static int NOP = 0x1100;

	public static int JMP = 0xe100;
	private static int JZ = 0xe200;
	private static int JC = 0xe300;
	private static int JNZ = 0xe400;
	private static int JNC = 0xe500;
	private static int JNB = 0xe600;
	private static int JB = 0xe700;
	private static int JP = 0xe800;
	private static int JNP = 0xe900;

	private static int B = 0x8000;
	private static int BC = 0x8100;
	private static int BNC = 0x8200;
	private static int BZ = 0x8300;
	private static int BNZ = 0x8400;
	private static int BNB = 0x8500;
	private static int BP = 0x8600;
	private static int BNP = 0x8700;
	private static int BB = 0x8800;
	
	private static int BX = 0x8900;
	
	private static int LDR = 0xaa00;

	public static void main(String[] args) throws Exception {
		// Registers

		format();
		k = 0;
		i = 0;

		// trainASCII();

		for (int icuadrant = 0;

				icuadrant <=

						Signals.PARITY_FLAG1 + Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1 + Signals.BORROW_FLAG1

				; icuadrant += Signals.CARRY_FLAG1) {
			// LDA
			setOffset(PSAL, icuadrant);
			push24b(icuadrant, PSAL);
			setOffset(PSAX, icuadrant);
			push24b(icuadrant, PSAX);
			setOffset(POPX, icuadrant);
			pop24b(icuadrant, POPX);
			setOffset(POPH, icuadrant);
			pop24b(icuadrant, POPH);
			setOffset(POPL, icuadrant);
			pop24b(icuadrant, POPL);

			createLD(icuadrant, LDA, Signals.MEMA);
			createLD(icuadrant, LDB, Signals.MEMB);
			createLD(icuadrant, LDC, Signals.MEMC);
			createLD(icuadrant, LDD, Signals.MEMD);
			
			createLDI(icuadrant, LDIA, Signals.MEMA);
			createLDI(icuadrant, LDIB, Signals.MEMB);
			createLDI(icuadrant, LDIC, Signals.MEMC);
			createLDI(icuadrant, LDID, Signals.MEMD);

			createArithmetic(icuadrant, ADD, Signals.RO + Signals.MEMB, Signals.FI + Signals.ALU_EOUT + Signals.SUM);
			createArithmetic(icuadrant, SUB, Signals.RO + Signals.MEMB,
					Signals.FI + Signals.ALU_EOUT + Signals.SUM + Signals.ALU_SUB);

			createIArithmetic(icuadrant, IADD, Signals.SUM);
			createIArithmetic(icuadrant, ISUB, Signals.SUM + Signals.ALU_SUB);

			createOUTput(icuadrant, OUTA, Signals.AMEM);
			createOUTput(icuadrant, OUTB, Signals.BMEM);
			createOUTput(icuadrant, OUTC, Signals.CMEM);
			createOUTput(icuadrant, OUTD, Signals.DMEM);

			createINput(icuadrant, DEC, false);
			createINput(icuadrant, DECE, true);

			createBX(icuadrant, BX);

			jump(icuadrant, JMP);
			
			jump(icuadrant, JNZ);
			notJump(icuadrant, JNC);
			jump(icuadrant, JNB);
			notJump(icuadrant, JNP);
			
			notJump(icuadrant, JC);
			notJump(icuadrant, JZ);
			notJump(icuadrant, JP);
			notJump(icuadrant, JB);

			call(icuadrant, B);
			
			call(icuadrant, BNZ);
			notCall(icuadrant, BNC);
			call(icuadrant, BNB);
			notCall(icuadrant, BNP);
			
			notCall(icuadrant, BC);
			notCall(icuadrant, BZ);
			notCall(icuadrant, BP);			
			notCall(icuadrant, BB);

			if (((float) icuadrant / Signals.CARRY_FLAG1) >= 1.0) {
				notCall(icuadrant, BNC);
				call(icuadrant, BC);
				notJump(icuadrant, JNC);
				jump(icuadrant, JC);
			}

			if (((float) icuadrant / Signals.ZERO_FLAG1) >= 1.0) {
				notCall(icuadrant, BNZ);
				call(icuadrant, BZ);
				notJump(icuadrant, JNZ);
				jump(icuadrant, JZ);
			}

			if (((float) icuadrant / Signals.BORROW_FLAG1) >= 1.0) {
				call(icuadrant, BB);
				notCall(icuadrant, BNB);
				jump(icuadrant, JB);
				notJump(icuadrant, JNB);
			}

			if (((float) icuadrant / Signals.PARITY_FLAG1) >= 1.0) {
				notCall(icuadrant, BNC);
				call(icuadrant, BC);
				notJump(icuadrant, JNC);
				jump(icuadrant, JC);
				
				call(icuadrant, BB);
				notCall(icuadrant, BNB);
				jump(icuadrant, JB);
				notJump(icuadrant, JNB);
				
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

	private static void trainASCII() throws Exception {

		for (int i = length - 512; i < length; i++) {
			write(i);
		}
	}

	private static void createINput(int icuadrant, int instruction, boolean withEcho) {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.KBI + Signals.KBO + Signals.MEMA + Signals.HALT);

		if (withEcho) {
			write(Signals.AMEM + Signals.COUT);
		}

	}

	private static void call(int icuadrant, int instruction) {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		bit24Indirection(true, true, false);
		write(Signals.CO + Signals.clpcr);
	}

	private static void pushStack() {
		
		write(Signals.CMM + Signals.LRS);
		write(Signals.AMEM + Signals.LR0);
		write(Signals.CPP + Signals.LRS);
		
		write(Signals.CO + Signals.J2 + Signals.J0 + Signals.MEMA);
		write(Signals.AMEM + Signals.LR2 + Signals.LR0);
		
		write(Signals.CO + Signals.J2 + Signals.MEMA);
		write(Signals.AMEM + Signals.LR2);
		
		write(Signals.CO + Signals.J0 + Signals.MEMA);
		write(Signals.AMEM + Signals.LR0);	
	}

	private static void createMov_SP_BP(int icuadrant) {
		setOffset(MOV_SP_BP, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.LRS + Signals.MEMA);
		write(Signals.AMEM + Signals.CPP + Signals.CMM);
		write(Signals.CO + Signals.clpcr);
	}
	
	private static void createMov_REG_BP(int icuadrant, int registerToMem) {
		setOffset(MOV_REG_BP, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.LRS + Signals.CMM + Signals.CPP + registerToMem);
		write(Signals.CO + Signals.clpcr);
	}
	
	private static void createMov_DI_I(int icuadrant) {
		setOffset(MOV_DI_I, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.LRS + Signals.MEMA);
		write(Signals.AMEM + Signals.CPP + Signals.CMM);
		write(Signals.CO + Signals.clpcr);
	}
	
	private static void push24b(int icuadrant, int instruction) {
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.CMM + Signals.LRS);

		write(Signals.BMEM + Signals.LRS + Signals.LR2 + Signals.LR0);
		write(Signals.CMEM + Signals.LRS + Signals.LR2);
		write(Signals.DMEM + Signals.LRS + Signals.LR0);
		
		
		write(Signals.CO + Signals.clpcr);
	}

	private static void pop24b(int icuadrant, int instruction) {
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
//		write(Signals.CMM + Signals.CPP + Signals.LRS + Signals.MEMA);

     	//Put breadcrumb
		write(Signals.CPP);
		pushStack();
	
		//Copy LRS Address to regs
		write(Signals.LRS + Signals.LR2 + Signals.LR0 + Signals.MEMB);
		write(Signals.LRS + Signals.LR2 + Signals.MEMC);
		write(Signals.LRS + Signals.LR0 + Signals.MEMD);

		//Decrement LRS
		write(Signals.CPP + Signals.LRS);

		
		//LRS to J
		write(Signals.J0 + Signals.J2  + Signals.BMEM);
		write(Signals.J2 + Signals.CMEM);
		write(Signals.J0 + Signals.DMEM);
		// Point RAM to J
		//write(Signals.CO + Signals.CE);
		write(Signals.CO + Signals.MII);
		
		
		
		write(Signals.RO + Signals.MEMA);
		
		popStack();
		write(Signals.CMM);
		
		//write(Signals.CO + Signals.CMM);
		write(Signals.CO + Signals.clpcr);
	}

	private static void notCall(int icuadrant, int instruction) throws Exception {
		// setOffset(instruction, icuadrant);
		notJump(icuadrant, instruction);
	}

	private static void createST(int icuadrant, int instruction, int signal) throws Exception {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		bit24Indirection(true, true, true, signal + Signals.RI);
		write(Signals.CO + Signals.clpcr);
	}

	private static void popStack() {
		write(Signals.LR2 + Signals.LR0 + Signals.J0 + Signals.J2);
		write(Signals.LR2 + Signals.J2);
		write(Signals.LR0 + Signals.J0);
		write(Signals.CO + Signals.MII);
	}

	private static void popStackAddress(int... operation) {
		write(Signals.CMM);
		write(Signals.LR2 + Signals.LR0 + operation[0]);
		write(Signals.LR2 + operation[1]);
		write(Signals.LR0 + operation[2]);
	}

	private static void createNOP(int icuadrant) throws Exception {
		setOffset(NOP, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.CO + Signals.clpcr);
	}

	private static void createHLT(int icuadrant) throws Exception {
		setOffset(HLT, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.HALT);
		//write(Signals.CO + Signals.clpcr);
	}

	
	private static void createMOVfromMEM(int icuadrant, int instruction, int signal) throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(signal + Signals.RO);
		write(Signals.CO + Signals.clpcr);
	}

	private static void createMOVBetweenRegisters(int icuadrant, int instruction, int signal) throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(signal);
		write(Signals.CO + Signals.clpcr);
	}

	private static void createLD(int icuadrant, int instruction, int operation) throws Exception {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		bit24Indirection(true, true, true, operation + Signals.RO);
		write(Signals.CO + Signals.clpcr);
	}

	private static void createLDI(int icuadrant, int instruction, int operation) {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + operation + Signals.CE);
		write(Signals.CO + Signals.clpcr);
	}

	private static void createLDR(int icuadrant, int instruction, int operation) throws Exception {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		bit24Indirection(true, false, true, operation);
		write(Signals.CO + Signals.clpcr);
	}

	private static void createBX(int icuadrant, int instruction) throws Exception {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		
		//write(Signals.RO + Signals.CE + Signals.MEMB);
		//write(Signals.CO + Signals.MII);
		//write(Signals.RO + Signals.CE + Signals.MEMC);
		//write(Signals.CO + Signals.MII);
		//write(Signals.RO + Signals.CE + Signals.MEMD);

		//write(Signals.BMEM + Signals.MI2 + Signals.MI0);
		//write(Signals.CMEM + Signals.MI2);
		//write(Signals.DMEM + Signals.MI0);

		bit24Indirection(true, false, true,
				Signals.MEMA + Signals.RO);
		
		//write(Signals.MEMA + Signals.RO);
		//write(Signals.CMM);
		popStack();
		
		write(Signals.CO + Signals.clpcr);
	}

	private static void createOUTput(int icuadrant, int instruction, int operation1) throws Exception {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.COUT + operation1); // Something Failed Here...s
		write(Signals.CO + Signals.clpcr);
	}

	private static void createLDDirect(int icuadrant, int instruction, int operation) throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + operation);
		write(Signals.CO + Signals.clpcr);
	}

	private static void createMOVtoMem(int icuadrant, int instruction, int operation) throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.RI + operation);
		write(Signals.CO + Signals.clpcr);
	}

	private static void jump(int icuadrant, int instruction) {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		bit24Indirection(true, false, false);
		write(Signals.CO + Signals.clpcr);
	}

	private static void notJump(int icuadrant, int instruction) {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.CO + Signals.CE);
		write(Signals.CO + Signals.CE);
		write(Signals.CO + Signals.CE);
		write(Signals.CO + Signals.clpcr);
	}

	private static void createArithmetic(int icuadrant, int instruction, int operation1, int operation2)
			throws Exception {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		bit24Indirection(true, true, true, operation1, operation2);
		write(Signals.CO + Signals.clpcr);
	}
	
	private static void createIArithmetic(int icuadrant, int instruction, int operation1)
			throws Exception {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		//bit24Indirection(true, true, true, operation1, operation2);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.MEMB + Signals.CE);
		write(Signals.FI + Signals.ALU_EOUT + operation1);
		write(Signals.CO + Signals.clpcr);
	}

	private static void bit24Indirection(boolean withJump, boolean withBreadcrumb, boolean withReturn,
			int... operations) {

		if (withJump) {

			// Copy the 3 next bytes to the registers
			write(Signals.CO + Signals.MII);
			write(Signals.RO + Signals.CE + Signals.MEMB);
			write(Signals.CO + Signals.MII);
			write(Signals.RO + Signals.CE + Signals.MEMC);
			write(Signals.CO + Signals.MII);
			write(Signals.RO + Signals.CE + Signals.MEMD);
			
			// Put a breadcrumb
			if (withBreadcrumb) {
				pushStack();
				write(Signals.CPP);
			} else {
				//Is BX				
//				write(Signals.CPP);
			}

			// JMP
			write(Signals.BMEM + Signals.J0 + Signals.J2);
			write(Signals.CMEM + Signals.J2);
			write(Signals.DMEM + Signals.J0);

			
			// Point to RAM loaded address
			write(Signals.CO + Signals.MII);
			
			for (int op : operations) {
				write(op);
			}

			if (withReturn) {
				// Pop with jump, same as popStack
				write(Signals.CMM);
				popStack();
			}

		}
	}

	private static String dump() {
		StringBuilder sb = new StringBuilder();
		for (int fff = 0; fff < memory.length; fff++) {
			for (int c = 0; c < 16; c++) {

				sb.append(memory[fff][c]);
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	private static void setOffset(int height, int cuadrant) {
		int count = 0;
		height += cuadrant;
		all: for (int l = 0; l < length; l++) {
			for (int m = 0; m < 16; m++) {
				if (count == height) {
					i = l;
					k = m;

					break all;
				}
				
				count += 1;
			}
		}

		// System.err.println(count);
	}

	public static void write(int val) {
		String chunck = Integer.toHexString(val);

		while (chunck.length() < 4 || chunck == null) {
			chunck = "0" + chunck;
		}

		if (i < memory.length) {
			memory[i][k] = chunck;
			memory[i][k] += " ";
			k += 1;
		}

		if (k >= 16) {
			if (i < memory.length) {
				i += 1;
			}

			k = 0;
		}

	}

	public static int fetch(int address) throws Exception {
		write(Signals.CO + Signals.MII + Signals.CMM + Signals.LRS);
		write(Signals.RO + Signals.CE + Signals.II);

		return address;
	}

	public static int readOneNotWrite(int address) throws Exception {
		write(Signals.CO + Signals.MI0);
		write(Signals.RO + Signals.CE);
		return address;
	}

	public static int clear(int address) throws Exception {
		return fetch(address);
	}

	public static void format() throws Exception {
		for (int ii = 0; ii < length; ii++) {
			clear(ii);
		}
	}

	public static class Signals {

		public static int r0 = 						0b1;
		public static int r1 = 						0b10;
		public static int r2 = 						0b100;
		public static int r3 = 						0b1000;

		public static int IDLE = 0 + 0 + 0 + 0; // 0
		public static int BMEM = 0 + 0 + 0 + r0; // 1
		public static int BTOA = 0 + 0 + r1 + 0; // 2
		public static int SUM = 0 + 0 + r1 + r0; // 3

		public static int AMEM = 0 + r2 + 0 + 0; // 4
		public static int ATOB = 0 + r2 + 0 + r0; // 5
		public static int ATOC = 0 + r2 + r1 + 0; // 6
		public static int ATOD = 0 + r2 + r1 + r0; // 7

		public static int MEMA = r3 + 0 + 0 + 0; // 8
		public static int MEMB = r3 + 0 + 0 + r0; // 9
		public static int MEMC = r3 + 0 + r1 + 0; // a
		public static int MEMD = r3 + 0 + r1 + r0; // b

		public static int CMEM = r3 + r2 + 0 + 0; // c
		public static int CTOA = r3 + r2 + 0 + r0; // d
		public static int CTOB = r3 + r2 + r1 + 0; // e
		public static int DMEM = r3 + r2 + r1 + r0; // f

		public static int MEM(String reg) {
			switch (reg) {
			case "A":
				return AMEM;
			case "B":
				return BMEM;
			case "C":
				return CMEM;
			}
			return 0;
		}

		// public static int OUTPUT_ENABLE = 0b100;
		// public static int WRITE_ENABLE = 0b1000;
		// public static int REG_CLEAR = 0b10000;
		// public static int REG_SHIFT = 0b100000;
		// public static int SHIFT_IN = 0b1000000;

		public static int FI = 					0b10000;
		public static int MI0 = 				0b100000;
		public static int MI2 = 				0b1000000;

		public static int HALT = 				0b10000000;
		public static int ALU_SUB = 			0b100000000;
		public static int ALU_EOUT = 			0b1000000000;
		public static int COUT = 				0b10000000000;
		public static int II = 					0b100000000000;
		public static int J0 = 					0b1000000000000;
		public static int J2 = 					0b10000000000000;
		public static int RI = 					0b100000000000000;

		public static int RO = 0b1000000000000000;

		public static int clpcr = 0b10000000000000000;

		public static int CE = 0b100000000000000000;
		public static int CO = 0b1000000000000000000;

		public static int LR0 = 0b10000000000000000000;
		public static int LR2 = 0b100000000000000000000;
		
		
		
		// public static int WE = 0b1000000000000000000000;

		public static int LRS = 0b1000000000000000000000;

		public static int MII = 0b10000000000000000000000;
		public static int CPP = 0b100000000000000000000000;
		public static int CMM = 0b1000000000000000000000000;

		public static int SBP = CPP + CMM;
		
		public static int KBO = 0b10000000000000000000000000;
		public static int KBI = 0b100000000000000000000000000;

		public static final int CARRY_FLAG0 =  0x00000;
		public static final int CARRY_FLAG1 =  0x10000;

		public static final int ZERO_FLAG0 =   0x00000;
		public static final int ZERO_FLAG1 =   0x20000;

		public static final int BORROW_FLAG0 = 0x00000;
		public static final int BORROW_FLAG1 = 0x40000;

		public static final int PARITY_FLAG0 = 0x00000;
		public static final int PARITY_FLAG1 = 0x80000;

	}

}
