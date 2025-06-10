package org.elijaxapps.old;

import java.io.PrintWriter;

public class MicroCodev2 {

	public static int word = (int) 18;
	public static int length = (int) (256 * 1024 * 18) + 16;
	public static String[][] memory = new String[length][word];

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
			// Stack operations. Store 24b addres on LRS Stack
			
			push24b(icuadrant, PSAX);
			pop24b(icuadrant, POPX);
			
			
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
		
		push8b(Signals.AMEM , Signals.LR0);
		
		write(Signals.CO + Signals.J2 + Signals.J0 + Signals.MEMA);
		write(Signals.AMEM + Signals.LR2 + Signals.LR0);
		
		write(Signals.CO + Signals.J2 + Signals.MEMA);
		write(Signals.AMEM + Signals.LR2);
		
		write(Signals.CO + Signals.J0 + Signals.MEMA);
		write(Signals.AMEM + Signals.LR0);	
		
		pop8b(Signals.MEMA,  Signals.LR0);
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
	
	private static void push24b(int icuadrant, int ins) {
		setOffset(ins, icuadrant);

		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
		write(Signals.CMM + Signals.LRS);

		write(Signals.BMEM + Signals.LRS + Signals.LR2 + Signals.LR0);
		write(Signals.CMEM + Signals.LRS + Signals.LR2);
		write(Signals.DMEM + Signals.LRS + Signals.LR0);
		
		
		write(Signals.CO + Signals.clpcr);
	}
	
	
	private static void push8b(int REG, int DST) {
		write(Signals.CMM + Signals.LRS);
		write(REG + DST);		
	}

	private static void pop24b(int icuadrant, int ins) {
		setOffset(ins, icuadrant);
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.II + Signals.CE);
	
		//Copy LRS Address to regs
		write(Signals.LRS + Signals.LR2 + Signals.LR0 + Signals.MEMB);
		write(Signals.LRS + Signals.LR2 + Signals.MEMC);
		write(Signals.LRS + Signals.LR0 + Signals.MEMD);

		//Decrement LRS
		write(Signals.CPP + Signals.LRS);
	}

	private static void pop8b(int ORIG, int DST) {
		write(ORIG + DST);
		write(Signals.CPP + Signals.LRS);
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
		write(Signals.CMM);
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
		
		bit24Indirection(true, false, true,
				Signals.MEMA + Signals.RO);
		
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
		bit24Indirection(true, true, false);
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

	private static void bit24Indirection(boolean withIndirection, boolean withJtoStack, boolean WithStack2J,
			 int... operations) {
		// We're going to rewrite this funtion ----
		
		// So this is going to be the function which takes care of 
		// a) Fetch indirections . . . FOR LDx Operations. (Register Operations)
		//    [Breadcrumb, LDA]
		// b) Calls [Breadcrumb, Push]
		// c) Jumps [No breadcrumb, Push]
		// d) Returns [No breadcrumb, Pop, LDA]
		
		//Breadcrumb
		
		//We need to store the Computer current execution offset. 
		// It is stored in J and we need it to move it To Stack
		
		//Assuming There is connectivity from J to LRS... which actually there isn' t. :((
		// We need to activate LRW Signal, which is now activated when there is a REGister to Bus operation...
		
		// Actually lets keep it Reg wise
		
	
		// Here we're or should be in this scenario.
		// A) Instruction have been fetch and RAM pointer is at it.
		// B) Next 3 bytes are an address
		// C) A operation is need to be done on destination
		if(withIndirection) {
			// Advance RAM pointer
			write(Signals.CO + Signals.MII);
			// Copy the RAM cell content to a register. Keep it wise. WE NEED 3 because cant do PC operations mixed with RAM 
			write(Signals.MEMB + Signals.RO + Signals.CE);
			
			write(Signals.CO + Signals.MII);
			write(Signals.MEMC + Signals.RO + Signals.CE);
			
			write(Signals.CO + Signals.MII);
			write(Signals.MEMD + Signals.RO + Signals.CE);
		
			// We should, first of all, let the breadcrumb.
			// That will let us make use of REG A.
			if(withJtoStack) {
				
				//We need to temporary store A in stack and recover it after increment.
				push8b(Signals.AMEM, Signals.LR0 + Signals.LRS);
				
				write(Signals.CO + Signals.J2 + Signals.J0 + Signals.MEMA);
				write(Signals.AMEM + Signals.LR0 + Signals.LR2);
				
				write(Signals.CO + Signals.J2 + Signals.MEMA);
				write(Signals.AMEM + Signals.LR2);
				
				write(Signals.CO + Signals.J0 + Signals.MEMA);
				write(Signals.AMEM + Signals.LR0);
				
				// AFTER! a Push, we have to increment Stack pointer...
				write(Signals.CPP);
				
				pop8b(Signals.MEMA, Signals.LR0 + Signals.LRS);
			}
			
			write(Signals.BMEM + Signals.J0 + Signals.J2);
			write(Signals.CMEM + Signals.J2);
			write(Signals.DMEM + Signals.J0);
			
			write(Signals.CO + Signals.MII);
			
			for (int op : operations) {
				//Write the operation after the indirection.
				write(op);				
			}
		}		

		// Here we must copy back from the Stack to the J (Program counter)
		// At least, the counter wise operation is direct, as we do not need
		// LRW signal ^^
		if(WithStack2J) {
			
			// BEFORE a pop, we have to Decrement Stack pointer...
			write(Signals.CMM);
			
			write(Signals.LR0 + Signals.J0 + Signals.LR2 + Signals.J2);		
			write(Signals.LR2 + Signals.J2);		
			write(Signals.LR0 + Signals.J0);		
						
			write(Signals.CO + Signals.MII);
	
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

	public static int fetchOnce(int address) throws Exception {
		write(Signals.CO + Signals.MII + Signals.CMM + Signals.LRS);
		write(Signals.RO + Signals.CE + Signals.II);

		return address;
	}
	
	public static int fetch(int address) throws Exception {
		write(Signals.CO + Signals.MII);
		write(Signals.RO + Signals.CE + Signals.II);

		return address;
	}

	public static int readOneNotWrite(int address) throws Exception {
		write(Signals.CO + Signals.MI0);
		write(Signals.RO + Signals.CE);
		return address;
	}

	public static int clear(int address) throws Exception {
		return address == 0 ? fetchOnce(address) : fetch(address);
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

		public static int FI = 					0b1000;
		public static int MI0 = 				0b10000;
		public static int MI2 = 				0b100000;

		public static int HALT = 				0b1000000;
		public static int ALU_SUB = 			0b10000000;
		public static int ALU_EOUT = 			0b100000000;
		public static int COUT = 				0b1000000000;
		public static int II = 					0b10000000000;
		public static int J0 = 					0b100000000000;
		public static int J2 = 					0b1000000000000;
		public static int RI = 					0b10000000000000;

		public static int RO = 0b100000000000000;

		public static int clpcr = 0b1000000000000000;

		public static int CE  = 0b10000000000000000;
		public static int CO  = 0b100000000000000000;

		public static int LR0 = 0b1000000000000000000;
		public static int LR2 = 0b10000000000000000000;
		
		
		
		public static int WE  = 0b100000000000000000000;

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
