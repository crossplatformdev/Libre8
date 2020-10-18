package org.elijaxapps.free8;

import java.io.PrintWriter;

public class MicroCode {

	public static int word = (int) 16;
	public static int length = (int) (16384 * 16) + 16;
	public static String[][] memory = new String[16384][word];

	public static int i = 0;
	public static int k = 0;

	public static final int LDA = 0x0000;
	public static final int LDB = 0x1000;
	public static final int LDC = 0x2000;
	public static final int LDD = 0x3000;

	// MOV A to X;
	private static final int MOV_AMem = 0x4000;
	private static final int MOV_MemA = 0x4100;
	private static final int MOV_AB = 0x5000;
	private static final int MOV_AC = 0x6000;
	private static final int MOV_AD = 0x7000;

	private static final int MOV_BMem = 0x8000;
	private static final int MOV_MemB = 0x8100;
	private static final int MOV_BA = 0x9000;
	private static final int MOV_BC = 0xa000;
	private static final int MOV_BD = 0xb000;

	private static final int MOV_CMem = 0xc000;
	private static final int MOV_MemC = 0xc100;
	private static final int MOV_CA = 0xd000;
	private static final int MOV_CB = 0xe000;
	private static final int MOV_CD = 0xf000;
	private static final int MOV_DMem = 0xf000;

	public static int STA = 0x7100;
	public static int STB = 0x7200;
	public static int STC = 0x7300;
	public static int STD = 0x7400;

	public static final int ADD = 0xf100;
	public static final int SUB = 0xf200;

	public static int OUTA = 0x8300;
	public static int OUTB = 0x8400;
	public static int OUTC = 0x8500;
	public static int OUTD = 0x8600;

	public static int LDI = 0xf400;

	public static int HLT = 0xf500;
	public static int STO = 0xf600;

	public static int NOP = 0xf700;
	public static int JMP = 0xf800;

	private static int JZ = 0x6800;
	private static int JC = 0x6900;

	private static int JNZ = 0x7900;
	private static int JNC = 0x7800;

	private static int B = 0x9100;
	private static int BC = 0x9200;
	private static int BNC = 0x9300;
	private static int BZ = 0x9400;
	private static int BNZ = 0x9500;

	private static int BX = 0x9600;

	private static int LDR = 0x9900;
	
	private static int DEC = 0x9700;

	public static void main(String[] args) throws Exception {
		// Registers

		format();
		k = 0;
		i = 0;

		for (int icuadrant = 0; icuadrant <= 0x40000; icuadrant += Signals.CARRY_FLAG1) {
			// LDA
			createLD(icuadrant, LDA, Signals.MEMA);
			createLD(icuadrant, LDB, Signals.MEMB);
			createLD(icuadrant, LDC, Signals.MEMC);
			createLD(icuadrant, LDD, Signals.MEMD);

			createArithmetic(icuadrant, ADD, Signals.MEMB, Signals.SUM + Signals.FI + Signals.ALU_EOUT);
			createArithmetic(icuadrant, SUB, Signals.MEMB, Signals.SUM + Signals.FI + Signals.ALU_SUB);

			createOUTput(icuadrant, OUTA, Signals.AMEM);
			createOUTput(icuadrant, OUTB, Signals.BMEM);
			createOUTput(icuadrant, OUTC, Signals.CMEM);
			createOUTput(icuadrant, OUTD, Signals.DMEM);

			createINput(icuadrant, DEC);

			createBX(icuadrant, BX, Signals.MEMA);

			setOffset(B, icuadrant);
			call(icuadrant, B + icuadrant);

			if (icuadrant == Signals.ZERO_FLAG0) {
				setOffset(BZ, Signals.ZERO_FLAG0);
				notCall(icuadrant, BZ + Signals.ZERO_FLAG0);

				setOffset(BC, Signals.ZERO_FLAG0);
				notCall(icuadrant, BC + Signals.ZERO_FLAG0);

				setOffset(BNZ, Signals.ZERO_FLAG0);
				call(icuadrant, BNZ + Signals.ZERO_FLAG0);
				
				setOffset(BNC, Signals.ZERO_FLAG0);
				notCall(icuadrant, BNC + Signals.ZERO_FLAG0);

			}
			if (icuadrant == Signals.ZERO_FLAG1) {

				setOffset(BNZ, Signals.ZERO_FLAG1);
				notCall(icuadrant, BNZ + Signals.ZERO_FLAG1);

				setOffset(BNC, Signals.ZERO_FLAG1);
				call(icuadrant, BNC + Signals.ZERO_FLAG1);

				setOffset(BZ, Signals.ZERO_FLAG1);
				call(icuadrant, BZ + Signals.ZERO_FLAG1);

				setOffset(BC, Signals.ZERO_FLAG1);
				notCall(icuadrant, BC + Signals.ZERO_FLAG1);

			}
			
			if (icuadrant == Signals.CARRY_FLAG0) {

				setOffset(BNC, Signals.CARRY_FLAG0);
				call(icuadrant, BNC + Signals.CARRY_FLAG0);
				
				setOffset(BNZ, Signals.CARRY_FLAG0);
				notCall(icuadrant, BNZ + Signals.CARRY_FLAG0);

				setOffset(BZ, Signals.CARRY_FLAG0);
				notCall(icuadrant, BZ + Signals.CARRY_FLAG0);

				setOffset(BC, Signals.CARRY_FLAG0);
				notCall(icuadrant, BC + Signals.CARRY_FLAG0);

			}

			if (icuadrant == Signals.CARRY_FLAG1) {

				setOffset(BNC, Signals.CARRY_FLAG1);
				notCall(icuadrant, BNC + Signals.CARRY_FLAG1);

				setOffset(BC, Signals.CARRY_FLAG1);
				call(icuadrant, BC + Signals.CARRY_FLAG1);

				setOffset(BNZ, Signals.CARRY_FLAG1);
				call(icuadrant, BNZ + Signals.CARRY_FLAG1);

				setOffset(BZ, Signals.CARRY_FLAG1);
				notCall(icuadrant, BZ + Signals.CARRY_FLAG1);

			}
			
			if (icuadrant == Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1) {

				setOffset(BNC, Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);
				notCall(icuadrant, BNC + Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);

				setOffset(BC, Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);
				call(icuadrant, BC + Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);

				setOffset(BNZ, Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);
				call(icuadrant, BNZ + Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);

				setOffset(BZ, Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);
				call(icuadrant, BZ + Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);

			}

			
			if (icuadrant == Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0) {

				setOffset(BNZ, Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);
				call(icuadrant, BNZ + Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);

				setOffset(BC, Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);
				notCall(icuadrant, BC + Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);

				setOffset(BZ, Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);
				notCall(icuadrant, BZ + Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);

				setOffset(BNC, Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);
				jump(icuadrant, BNC + Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);

			}
			
			setOffset(JMP, icuadrant);
			jump(icuadrant, JMP + icuadrant);

			if (icuadrant == Signals.ZERO_FLAG0) {
				setOffset(JZ, Signals.ZERO_FLAG0);
				notJump(icuadrant, JZ + Signals.ZERO_FLAG0);

				setOffset(JC, Signals.ZERO_FLAG0);
				notJump(icuadrant, JC + Signals.ZERO_FLAG0);

				setOffset(JNZ, Signals.ZERO_FLAG0);
				jump(icuadrant, JNZ + Signals.ZERO_FLAG0);

				setOffset(JNC, Signals.ZERO_FLAG0);
				notJump(icuadrant, JNC + Signals.ZERO_FLAG0);

			}
			if (icuadrant == Signals.ZERO_FLAG1) {

				setOffset(JNZ, Signals.ZERO_FLAG1);
				notJump(icuadrant, JNZ + Signals.ZERO_FLAG1);

				setOffset(JZ, Signals.ZERO_FLAG1);
				jump(icuadrant, JZ + Signals.ZERO_FLAG1);

				setOffset(JC, Signals.ZERO_FLAG1);
				notJump(icuadrant, JC + Signals.ZERO_FLAG1);

				setOffset(JNC, Signals.ZERO_FLAG1);
				jump(icuadrant, JNC + Signals.ZERO_FLAG1);

			}

			if (icuadrant == Signals.CARRY_FLAG0) {

				setOffset(JNZ, Signals.CARRY_FLAG0);
				jump(icuadrant, JNZ + Signals.CARRY_FLAG0);

				setOffset(JC, Signals.CARRY_FLAG0);
				notJump(icuadrant, JC + Signals.CARRY_FLAG0);

				setOffset(JZ, Signals.CARRY_FLAG0);
				notJump(icuadrant, JZ + Signals.CARRY_FLAG0);

				setOffset(JNC, Signals.CARRY_FLAG0);
				jump(icuadrant, JNC + Signals.CARRY_FLAG0);
			}

			if (icuadrant == Signals.CARRY_FLAG1) {

				setOffset(JNZ, Signals.CARRY_FLAG1);
				jump(icuadrant, JNZ + Signals.CARRY_FLAG1);

				setOffset(JC, Signals.CARRY_FLAG1);
				jump(icuadrant, JC + Signals.CARRY_FLAG1);

				setOffset(JZ, Signals.CARRY_FLAG1);
				notJump(icuadrant, JZ + Signals.CARRY_FLAG1);

				setOffset(JNC, Signals.CARRY_FLAG1);
				notJump(icuadrant, JNC + Signals.CARRY_FLAG1);

			}
			
			if (icuadrant == Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1) {

				setOffset(JNZ, Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);
				jump(icuadrant, JNZ + Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);

				setOffset(JC, Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);
				jump(icuadrant, JC + Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);

				setOffset(JZ, Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);
				jump(icuadrant, JZ + Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);

				setOffset(JNC, Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);
				notJump(icuadrant, JNC + Signals.CARRY_FLAG1 + Signals.ZERO_FLAG1);

			}
			
			if (icuadrant == Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0) {

				setOffset(JNZ, Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);
				jump(icuadrant, JNZ + Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);

				setOffset(JC, Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);
				notJump(icuadrant, JC + Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);

				setOffset(JZ, Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);
				notJump(icuadrant, JZ + Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);

				setOffset(JNC, Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);
				jump(icuadrant, JNC + Signals.CARRY_FLAG0 + Signals.ZERO_FLAG0);

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

	private static void createINput(int icuadrant, int instruction) {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.KBI + Signals.HALT);
		write(Signals.KBO + Signals.MEMA);
		write(Signals.clpcr);
	}

	private static void call(int icuadrant, int instruction) {
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		bit24Indirection(instruction, true, true);
		write(Signals.clpcr);
	}

	private static void pushStack() {
		write(Signals.CO + Signals.J2 + Signals.J0 + Signals.LR2 + Signals.LR0);
		write(Signals.CO + Signals.J2 + Signals.LR2);
		write(Signals.CO + Signals.J0 + Signals.LR0);
		write(Signals.CPP);
	}

	private static void notCall(int icuadrant, int instruction) throws Exception {
		notJump(icuadrant, instruction);
	}

	private static void createST(int icuadrant, int instruction, int signal) throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);				
		
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.CE + Signals.MEMB);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.CE + Signals.MEMC);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.CE + Signals.MEMD);
		
		write(Signals.BMEM + Signals.MI2 + Signals.MI0);
		write(Signals.CMEM + Signals.MI2);
		write(Signals.DMEM + Signals.MI0);
		write(signal + Signals.RI);
		
		//popStack();
		
		write(Signals.clpcr);
	}

	private static void popStack() {
		write(Signals.CMM);
		write(Signals.LRO + Signals.LR2 + Signals.LR0 + Signals.J0 + Signals.J2);
		write(Signals.LRO + Signals.LR2 + Signals.J2);
		write(Signals.LRO + Signals.LR0 + Signals.J0);
	}

	private static void createNOP(int icuadrant) throws Exception {
		setOffset(HLT, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.clpcr);
	}

	private static void createHLT(int icuadrant) throws Exception {
		setOffset(HLT, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.HALT);
		write(Signals.clpcr);
	}

	private static void createMOVfromMEM(int icuadrant, int instruction, int signal) throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(signal + Signals.RO);
		write(Signals.clpcr);
	}

	private static void createMOVBetweenRegisters(int icuadrant, int instruction, int signal) throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(signal);
		write(Signals.clpcr);
	}

	private static void createLD(int icuadrant, int instruction, int operation) throws Exception {
		setOffset(instruction, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		bit24Indirection(instruction, false, false);
		// pushStack();
		write(Signals.RO + operation);
		write(Signals.CO + Signals.MII);
		write(Signals.clpcr);
	}

	private static void createLDR(int icuadrant, int instruction, int operation) throws Exception {
		setOffset(instruction, icuadrant);
		pushStack();
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		bit24Indirection(instruction, false, false);
		write(Signals.RO + operation);
		popStack();
		write(Signals.clpcr);
	}

	private static void createBX(int icuadrant, int instruction, int operation) throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);				
		
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.CE + Signals.MEMB);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.CE + Signals.MEMC);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.CE + Signals.MEMD);
		
		write(Signals.BMEM + Signals.MI2 + Signals.MI0);
		write(Signals.CMEM + Signals.MI2);
		write(Signals.DMEM + Signals.MI0);
		write(Signals.MEMA + Signals.RO);
				
		popStack();
		
		write(Signals.clpcr);
	}

	private static void createOUTput(int icuadrant, int instruction, int operation1) throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.COUT + operation1);
		write(Signals.clpcr);
	}

	private static void createLDDirect(int icuadrant, int instruction, int operation) throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + operation);

		write(Signals.CO + Signals.MII);

		write(Signals.clpcr + Signals.CE);
	}

	private static void createMOVtoMem(int icuadrant, int instruction, int operation) throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.RI + operation);
		write(Signals.clpcr);
	}

	private static void jump(int icuadrant, int instruction) {

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		bit24Indirection(instruction, true, false);
		write(Signals.clpcr);
	}

	private static void notJump(int icuadrant, int instruction) {
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.CO + Signals.J0 + Signals.CE);
		write(Signals.CO + Signals.J0 + Signals.CE);
		write(Signals.CO + Signals.J0 + Signals.CE);
		write(Signals.CO + Signals.MII);
		write(Signals.clpcr);
	}

	private static void createArithmetic(int icuadrant, int instruction, int operation1, int operation2)
			throws Exception {
		setOffset(instruction, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);

		bit24Indirection(instruction, false, false);
		write(Signals.RO + operation1);
		write(operation2);

		write(Signals.clpcr);
	}

	private static void bit24Indirection(int instruction, boolean withJump, boolean withPush) {
	
		if(withJump) {
			write(Signals.CO + Signals.MII);
			write(Signals.RO + Signals.CE + Signals.MEMB);
			write(Signals.CO + Signals.MII);
			write(Signals.RO + Signals.CE + Signals.MEMC);
			write(Signals.CO + Signals.MII);
			write(Signals.RO + Signals.CE + Signals.MEMD);
			
			if(withPush) {
				pushStack();
			}
			
			write(Signals.BMEM + Signals.J2 + Signals.J0);
			write(Signals.CMEM + Signals.J2);
			write(Signals.DMEM + Signals.J0);
			//if(withPush) {
				//write(Signals.CO + Signals.MII + Signals.CPP);			
			//} else {
				write(Signals.CO + Signals.MII);
			//}
		} else {
			write(Signals.CO + Signals.MII);
			write(Signals.RO + Signals.CE + Signals.MEMB);
			write(Signals.CO + Signals.MII);
			write(Signals.RO + Signals.CE + Signals.MEMC);
			write(Signals.CO + Signals.MII);
			write(Signals.RO + Signals.CE + Signals.MEMD);
			
			write(Signals.BMEM + Signals.MI2 + Signals.MI0);
			write(Signals.CMEM + Signals.MI2);
			write(Signals.DMEM + Signals.MI0);		
		}
	

	}

	private static String dump() {
		StringBuilder sb = new StringBuilder();

		for (int fff = 0; fff < 16384; fff++) {
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
		all: for (int l = 0; l < memory.length; l++) {
			for (int m = 0; m < 16; m++) {

				count += 1;

				if (count - 1 == height) {
					i = l;
					k = m;

					// System.err.println("L"+l+":"+"M"+m);
					break all;
				}
			}
		}

		// System.err.println(count);
	}

	public static void write(int val) {
		String chunck = Integer.toHexString(val);

		while (chunck.length() < 4 || chunck == null) {
			chunck = "0" + chunck;
		}

		if (i < 16384) {
			memory[i][k] = chunck;
			memory[i][k] += " ";
			k += 1;
		}

		if (k >= 16) {
			if (i < 32768) {
				i += 1;
			}

			k = 0;
		}

	}

	public static int fetch(int address) throws Exception {
		write(Signals.CO + Signals.MI0);
		write(Signals.RO + Signals.CE + Signals.II);

		return address;
	}

	public static int readOneNotWrite(int address) throws Exception {
		write(Signals.CO + Signals.MI0);
		write(Signals.RO + Signals.CE);
		return address;
	}

	public static int clear(int address) throws Exception {
		// fetch(address);
		write(0b00);
		return address;
	}

	public static void format() throws Exception {
		for (int ii = 0; ii < length; ii++) {
			clear(ii);
		}
	}

	public static class Signals {

		public static int r0 = 0b1;
		public static int r1 = 0b10;
		public static int r2 = 0b100;
		public static int r3 = 0b1000;

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
		
		public static int FI = 0b10000;
		public static int MI0 = 0b100000;
		public static int MI2 = 0b1000000;

		public static int HALT = 0b10000000;
		public static int ALU_SUB = 0b100000000;
		public static int ALU_EOUT = 0b1000000000;
		public static int COUT = 0b10000000000;
		public static int II = 0b100000000000;
		public static int J0 = 0b1000000000000;
		public static int J2 = 0b10000000000000;
		public static int RI = 0b100000000000000;

		public static int RO = 0b1000000000000000;

		public static int clpcr = 0b10000000000000000;

		public static int CE = 0b100000000000000000;
		public static int CO = 0b1000000000000000000;

		public static int LR0 = 0b10000000000000000000;
		public static int LR2 = 0b100000000000000000000;
		public static int LRO = 0b1000000000000000000000;

		public static int MII = 0b10000000000000000000000;
		public static int CPP = 0b100000000000000000000000;
		public static int CMM = 0b1000000000000000000000000;

		public static int KBO = 0b10000000000000000000000000;
		public static int KBI = 0b100000000000000000000000000;

		public static final int CARRY_FLAG0 = 0x000000;
		public static final int CARRY_FLAG1 = 0x10000;

		public static final int ZERO_FLAG0 = 0x00000;
		public static final int ZERO_FLAG1 = 0x20000;

	}

}
