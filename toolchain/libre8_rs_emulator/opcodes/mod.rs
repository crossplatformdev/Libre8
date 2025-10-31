#![allow(dead_code)]

#[allow(dead_code)]
pub struct Opcodes;
// Instruction Opcodes
pub const LDA: u8 = 0x1a;
pub const LDB: u8 = 0x1b;
pub const LDC: u8 = 0x1c;
pub const LDD: u8 = 0x1e;
pub const LDIA: u8 = 0xda;
pub const LDIB: u8 = 0xdb;
pub const LDIC: u8 = 0xdc;
pub const LDID: u8 = 0xdd;

// MOV Instructions
pub const MOV_AMEM: u8 = 0xf0;
pub const MOV_MEM_A: u8 = 0xf1;
pub const MOV_AB: u8 = 0xf2;
pub const MOV_AC: u8 = 0xf3;
pub const MOV_AD: u8 = 0xf4;
pub const MOV_BMEM: u8 = 0xf5;
pub const MOV_MEM_B: u8 = 0xf6;
pub const MOV_BA: u8 = 0xf7;
pub const MOV_BC: u8 = 0xf8;
pub const MOV_BD: u8 = 0xf9;
pub const MOV_CMEM: u8 = 0xfa;
pub const MOV_MEM_C: u8 = 0xfb;
pub const MOV_CA: u8 = 0xfc;
pub const MOV_CB: u8 = 0xfd;
pub const MOV_CD: u8 = 0xfe;
pub const MOV_DMEM: u8 = 0xff;
pub const MOV_SP_BP: u8 = 0x01;
pub const MOV_DI_I: u8 = 0x02;
pub const MOV_REG_BP: u8 = 0x03;

// ALU Instructions
pub const STA: u8 = 0x5a;
pub const STB: u8 = 0x5b;
pub const STC: u8 = 0x5c;
pub const STD: u8 = 0x5d;
pub const ADD: u8 = 0xaa;
pub const SUB: u8 = 0xa5;
pub const MUL: u8 = 0xa2;
pub const DIV: u8 = 0xad;
pub const DEC: u8 = 0xde;
pub const DECE: u8 = 0xdf;
pub const DECI: u8 = 0xd1;

pub const IADD: u8 = 0x6a;
pub const ISUB: u8 = 0x65;
pub const IMUL: u8 = 0x62;
pub const IDIV: u8 = 0x6d;

// I/O Instructions
pub const POKE: u8 = 0x95;
pub const POKX: u8 = 0x9a;
pub const POKY: u8 = 0x9b;
pub const PXYD: u8 = 0x9c;
pub const PIKX: u8 = 0x9d;
pub const PIKY: u8 = 0x9e;
pub const PIYD: u8 = 0x9f;
pub const OUTA: u8 = 0x05;
pub const OUTB: u8 = 0x06;
pub const OUTC: u8 = 0x07;
pub const OUTD: u8 = 0x08;

// Stack and Pointer Operations
pub const PSAX: u8 = 0xc1;
pub const PSAH: u8 = 0xc2;
pub const PSAL: u8 = 0xc3;
pub const POPX: u8 = 0xc4;
pub const POPH: u8 = 0xc5;
pub const POPL: u8 = 0xc6;

// Miscellaneous
pub const LDI: u8 = 0x1d;
pub const HLT: u8 = 0x91;
pub const STO: u8 = 0x86;
pub const NOP: u8 = 0x11;

// Jump and Branch
pub const JMP: u8 = 0xe1;
pub const JZ: u8 = 0xe2;
pub const JC: u8 = 0xe3;
pub const JNZ: u8 = 0xe4;
pub const JNC: u8 = 0xe5;
pub const JNB: u8 = 0xe6;
pub const JB: u8 = 0xe7;
pub const JP: u8 = 0xe8;
pub const JNP: u8 = 0xe9;

pub const B: u8 = 0x80;
pub const BC: u8 = 0x81;
pub const BNC: u8 = 0x82;
pub const BZ: u8 = 0x83;
pub const BNZ: u8 = 0x84;
pub const BNB: u8 = 0x85;
pub const BP: u8 = 0x86;
pub const BNP: u8 = 0x87;
pub const BB: u8 = 0x88;
pub const BX: u8 = 0x89;

pub const RST: u8 = 0x77;
pub const PST: u8 = 0x78;

pub const PTRI: u8 = 0x79;
pub const PTRD: u8 = 0x7a;
pub const PTRL: u8 = 0x7b;
pub const PTRS: u8 = 0x7c;
pub const OUTT: u8 = 0x7d;
pub const OUTM: u8 = 0x7e;

pub const LDR: u8 = 0xaa;
