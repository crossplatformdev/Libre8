package org.elijaxapps.free8;


public class Registers {

	public static Integer ASH = 0b1;
	public static Integer AC = 0b10;
	public static Integer AL = 0b100;
	public static Integer ASI = 0b1000;

	public static Integer BSH = 0b10000;
	public static Integer BC = 0b100000;
	public static Integer BL = 0b1000000;
	public static Integer BSI = 0b10000000;

	public static Integer CSH = 0b100000000;
	public static Integer CC = 0b1000000000;
	public static Integer CL = 0b10000000000;
	public static Integer CSI = 0b100000000000;

	public static Integer DSH = 0b1000000000000;
	public static Integer DC = 0b10000000000000;
	public static Integer DL = 0b100000000000000;
	public static Integer DSI = 0b1000000000000000;

	public static void main(String[] args) {


		// B to
		// ->D: BC + DC + DL
		// NOT USED NEED IDLE REG MOD
		print(0); //IDLE
		// Mem
		print(BC); // BMEM
		// A
		print(BC + AL); //BTOA
		// C
		print(BC + AC + AL); //BTOC.old (SUM) <-- SUM OPERATION
		println();
		
		// A to AXXX
		// Mem
		print(AC); // MOV A MEM
		// B
		print(AC + BL); // MOV A B // ATOB
		// C
		print(AC + CL); // MOV A C // ATOC
		// D
		print(AC + DL); // MOV A D // ATOD
		println();

		
		// Mem to MEMX
		// A
		print(AL); //LDA // MEMA
		// B
		print(BL); //LDB // MEMB
		// C
		print(CL); //LDC // MEMC
		// D
		print(DL); //LDD // MEMD
		println();
		
		// C to
		// Mem
		print(CC); // CMEM
		// A
		print(CC + AL); // CTOA
		// B
		print(CC + BL); // CTOB
		// D
		print(DC); // DMEM
		println();
	}

	private static void print(Integer string) {
		// TODO Auto-generated method stub
		System.out.print(Integer.toHexString(string)+ " ");
	}

	private static void println() {
		// TODO Auto-generated method stub
		System.out.println();
	}

}
