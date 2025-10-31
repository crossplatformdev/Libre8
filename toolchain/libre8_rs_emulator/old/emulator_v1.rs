use anyhow::Result;
use minifb::{Key, Window, WindowOptions};
use std::fs;
use std::path::Path;
// Import the necessaary to create a secondary terminal
use std::io::{self, BufRead, BufReader, Write};
use std::process::{Child, ChildStderr, ChildStdin, ChildStdout, Command, Stdio};
use std::thread;

const COL_BG: u32 = 0x101218;
const COL_GRID: u32 = 0x2a2f3a;
const COL_TEXT: u32 = 0xE6E6E6;
const COL_ACCENT: u32 = 0x4D7CFE;
const COL_RED: u32 = 0xC23B22;
const COL_GREEN: u32 = 0x2BA84A;
const COL_CARD: u32 = 0x191C24;



const FLAGS_POS_X: usize = 50;
const FLAGS_POS_Y: usize = 50;

const STACK_POS_X: usize = 50;
const STACK_POS_Y: usize = 300;

const MEM_POS_X: usize = 1230;
const MEM_POS_Y: usize = 50;

const VRAM_POS_X: usize = 1230;
const VRAM_POS_Y: usize = 500;

const WIDTH: usize = 252;
const HEIGHT: usize = 252;

const WINDOW_WIDTH: usize = 1920;
const WINDOW_HEIGHT: usize = 1024;

const REGS_POS_X: usize = 50;
const REGS_POS_Y: usize = 550;

const OPCODE_POS_X: usize = 50;
const OPCODE_POS_Y: usize = 650;

const OPADDR_POS_X: usize = 50;
const OPADDR_POS_Y: usize = 750;

const DISPLAY_POS_X: usize = 450;
const DISPLAY_POS_Y: usize = 400;

static mut TERMINAL_POS_X: usize = 450;
static mut TERMINAL_POS_Y: usize = 50;

const KEYBOARD_POS_X: usize = 1000;
const KEYBOARD_POS_Y: usize = 400;

const TERMINAL_WIDTH: usize = 80;
const TERMINAL_HEIGHT: usize = 24;


static mut TERMINAL_BUFFER: [char; TERMINAL_WIDTH * TERMINAL_HEIGHT] = [' '; TERMINAL_WIDTH * TERMINAL_HEIGHT]; // Terminal Buffer

static mut TERMINAL_X : usize = 0;
static mut TERMINAL_Y : usize = 0;

// Instruction Opcodes
const LD : u8 = 0x1d;
const LDA : u8 = 0x1a;
const LDB : u8 = 0x1b;
const LDC : u8 = 0x1c;
const LDD : u8 = 0x1e;
const LDIA : u8 = 0xda;
const LDIB : u8 = 0xdb;
const LDIC : u8 = 0xdc;
const LDID : u8 = 0xdd;

// MOV Instructions
const MOV_AMem : u8 = 0xf0;
const MOV_MemA : u8 = 0xf1;
const MOV_AB : u8 = 0xf2;
const MOV_AC : u8 = 0xf3;
const MOV_AD : u8 = 0xf4;
const MOV_BMem : u8 = 0xf5;
const MOV_MemB : u8 = 0xf6;
const MOV_BA : u8 = 0xf7;
const MOV_BC : u8 = 0xf8;
const MOV_BD : u8 = 0xf9;
const MOV_CMem : u8 = 0xfa;
const MOV_MemC : u8 = 0xfb;
const MOV_CA : u8 = 0xfc;
const MOV_CB : u8 = 0xfd;
const MOV_CD : u8 = 0xfe;
const MOV_DMem : u8 = 0xff;
const MOV_SP_BP : u8 = 0x01;
const MOV_DI_I : u8 = 0x02;
const MOV_REG_BP : u8 = 0x03;

// ALU Instructions
const STA : u8 = 0x5a;
const STB : u8 = 0x5b;
const STC : u8 = 0x5c;
const STD : u8 = 0x5d;
const ADD : u8 = 0xaa;
const SUB : u8 = 0xa5;
const MUL : u8 = 0xa2;
const DIV : u8 = 0xad;
const DEC : u8 = 0xde;
const DECE : u8 = 0xdf;
const DECI : u8 = 0xd1;

const IADD : u8 = 0x6a;
const ISUB : u8 = 0x65;
const IMUL : u8 = 0x62;
const IDIV : u8 = 0x6d;

// I/O Instructions
const POKE : u8 = 0x95;
const POKX : u8 = 0x9a;
const POKY : u8 = 0x9b;
const PXYD : u8 = 0x9c;
const PIKX : u8 = 0x9d;
const PIKY : u8 = 0x9e;
const PIYD : u8 = 0x9f;
const OUTA : u8 = 0x05;
const OUTB : u8 = 0x06;
const OUTC : u8 = 0x07;
const OUTD : u8 = 0x08;

// Stack and Pointer Operations
const PSAX : u8 = 0xc1;
const PSAH : u8 = 0xc2;
const PSAL : u8 = 0xc3;
const POPX : u8 = 0xc4;
const POPH : u8 = 0xc5;
const POPL : u8 = 0xc6;

// Miscellaneous
const LDI : u8 = 0xde;
const HLT : u8 = 0x91;
const STO : u8 = 0x86;
const NOP : u8 = 0x11;

// Jump and Branch
const JMP : u8 = 0xe1;
const JZ : u8 = 0xe2;
const JC : u8 = 0xe3;
const JNZ : u8 = 0xe4;
const JNC : u8 = 0xe5;
const JNB : u8 = 0xe6;
const JB : u8 = 0xe7;
const JP : u8 = 0xe8;
const JNP : u8 = 0xe9;

const B : u8 = 0x80;
const BC : u8 = 0x81;
const BNC : u8 = 0x82;
const BZ : u8 = 0x83;
const BNZ : u8 = 0x84;
const BNB : u8 = 0x85;
const BP : u8 = 0x86;
const BNP : u8 = 0x87;
const BB : u8 = 0x88;
const BX : u8 = 0x89;

const RST : u8 = 0x77;
const PST : u8 = 0x78;

const PTRI : u8 = 0x79;
const PTRD : u8 = 0x7a;
const PTRL : u8 = 0x7b;
const PTRS : u8 = 0x7c;
const OUTT : u8 = 0x7d;
const OUTM : u8 = 0x7e;

const LDR : u8 = 0xaa;


static mut REG_A: u8 = 0;
static mut REG_B: u8 = 0;
static mut REG_C: u8 = 0;
static mut REG_D: u8 = 0;

static mut STACK: Vec<usize> = Vec::new();

static mut ZERO_FLAG: bool = false; // Zero Flag
static mut CARRY_FLAG: bool = false; // Carry Flag
static mut BORROW_FLAG: bool = false; // Borrow Flag
static mut PARITY_FLAG: bool = false; // Parity Flag
static mut GREATER_FLAG: bool = false; // Greater Flag
static mut LESSER_FLAG: bool = false; // Lesser Flag
static mut EQUAL_FLAG: bool = false; // Equal Flag

static mut WINDOW_BUFFER: [u32; WINDOW_HEIGHT * WINDOW_WIDTH] = [0x0F; WINDOW_HEIGHT * WINDOW_WIDTH]; // Framebuffer

static mut KEYBOARD_BUFFER: Vec<u8> = Vec::new(); // Keyboard Buffer

// VGA 256-color palette
const VGA_PALETTE: [(u8, u8, u8); 256] = [
	( 0x00, 0x00, 0x00), (0x00, 0x00, 0xaa), (0x00, 0xaa, 0x00), (0x00, 0xaa, 0xaa), (0xaa, 0x00, 0x00), (0xaa, 0x00, 0xaa), (0xaa, 0x55, 0x00), (0xaa, 0xaa, 0xaa),
	( 0x55, 0x55, 0x55), (0x55, 0x55, 0xff), (0x55, 0xff, 0x55), (0x55, 0xff, 0xff), (0xff, 0x55, 0x55), (0xff, 0x55, 0xff), (0xff, 0xff, 0x55), (0xff, 0xff, 0xff),
	( 0x00, 0x00, 0x00), (0x14, 0x14, 0x14), (0x20, 0x20, 0x20), (0x2c, 0x2c, 0x2c), (0x38, 0x38, 0x38), (0x45, 0x45, 0x45), (0x51, 0x51, 0x51), (0x61, 0x61, 0x61),
	( 0x71, 0x71, 0x71), (0x82, 0x82, 0x82), (0x92, 0x92, 0x92), (0xa2, 0xa2, 0xa2), (0xb6, 0xb6, 0xb6), (0xcb, 0xcb, 0xcb), (0xe3, 0xe3, 0xe3), (0xff, 0xff, 0xff),
	( 0x00, 0x00, 0xff), (0x41, 0x00, 0xff), (0x7d, 0x00, 0xff), (0xbe, 0x00, 0xff), (0xff, 0x00, 0xff), (0xff, 0x00, 0xbe), (0xff, 0x00, 0x7d), (0xff, 0x00, 0x41),
	( 0xff, 0x00, 0x00), (0xff, 0x41, 0x00), (0xff, 0x7d, 0x00), (0xff, 0xbe, 0x00), (0xff, 0xff, 0x00), (0xbe, 0xff, 0x00), (0x7d, 0xff, 0x00), (0x41, 0xff, 0x00),
	( 0x00, 0xff, 0x00), (0x00, 0xff, 0x41), (0x00, 0xff, 0x7d), (0x00, 0xff, 0xbe), (0x00, 0xff, 0xff), (0x00, 0xbe, 0xff), (0x00, 0x7d, 0xff), (0x00, 0x41, 0xff),
	( 0x7d, 0x7d, 0xff), (0x9e, 0x7d, 0xff), (0xbe, 0x7d, 0xff), (0xdf, 0x7d, 0xff), (0xff, 0x7d, 0xff), (0xff, 0x7d, 0xdf), (0xff, 0x7d, 0xbe), (0xff, 0x7d, 0x9e),
	( 0xff, 0x7d, 0x7d), (0xff, 0x9e, 0x7d), (0xff, 0xbe, 0x7d), (0xff, 0xdf, 0x7d), (0xff, 0xff, 0x7d), (0xdf, 0xff, 0x7d), (0xbe, 0xff, 0x7d), (0x9e, 0xff, 0x7d),
	( 0x7d, 0xff, 0x7d), (0x7d, 0xff, 0x9e), (0x7d, 0xff, 0xbe), (0x7d, 0xff, 0xdf), (0x7d, 0xff, 0xff), (0x7d, 0xdf, 0xff), (0x7d, 0xbe, 0xff), (0x7d, 0x9e, 0xff),
	( 0xb6, 0xb6, 0xff), (0xc7, 0xb6, 0xff), (0xdb, 0xb6, 0xff), (0xeb, 0xb6, 0xff), (0xff, 0xb6, 0xff), (0xff, 0xb6, 0xeb), (0xff, 0xb6, 0xdb), (0xff, 0xb6, 0xc7),
	( 0xff, 0xb6, 0xb6), (0xff, 0xc7, 0xb6), (0xff, 0xdb, 0xb6), (0xff, 0xeb, 0xb6), (0xff, 0xff, 0xb6), (0xeb, 0xff, 0xb6), (0xdb, 0xff, 0xb6), (0xc7, 0xff, 0xb6),
	( 0xb6, 0xff, 0xb6), (0xb6, 0xff, 0xc7), (0xb6, 0xff, 0xdb), (0xb6, 0xff, 0xeb), (0xb6, 0xff, 0xff), (0xb6, 0xeb, 0xff), (0xb6, 0xdb, 0xff), (0xb6, 0xc7, 0xff),
	( 0x00, 0x00, 0x71), (0x1c, 0x00, 0x71), (0x38, 0x00, 0x71), (0x55, 0x00, 0x71), (0x71, 0x00, 0x71), (0x71, 0x00, 0x55), (0x71, 0x00, 0x38), (0x71, 0x00, 0x1c),
	( 0x71, 0x00, 0x00), (0x71, 0x1c, 0x00), (0x71, 0x38, 0x00), (0x71, 0x55, 0x00), (0x71, 0x71, 0x00), (0x55, 0x71, 0x00), (0x38, 0x71, 0x00), (0x1c, 0x71, 0x00),
	( 0x00, 0x71, 0x00), (0x00, 0x71, 0x1c), (0x00, 0x71, 0x38), (0x00, 0x71, 0x55), (0x00, 0x71, 0x71), (0x00, 0x55, 0x71), (0x00, 0x38, 0x71), (0x00, 0x1c, 0x71),
	( 0x38, 0x38, 0x71), (0x45, 0x38, 0x71), (0x55, 0x38, 0x71), (0x61, 0x38, 0x71), (0x71, 0x38, 0x71), (0x71, 0x38, 0x61), (0x71, 0x38, 0x55), (0x71, 0x38, 0x45),
	( 0x71, 0x38, 0x38), (0x71, 0x45, 0x38), (0x71, 0x55, 0x38), (0x71, 0x61, 0x38), (0x71, 0x71, 0x38), (0x61, 0x71, 0x38), (0x55, 0x71, 0x38), (0x45, 0x71, 0x38),
	( 0x38, 0x71, 0x38), (0x38, 0x71, 0x45), (0x38, 0x71, 0x55), (0x38, 0x71, 0x61), (0x38, 0x71, 0x71), (0x38, 0x61, 0x71), (0x38, 0x55, 0x71), (0x38, 0x45, 0x71),
	( 0x51, 0x51, 0x71), (0x59, 0x51, 0x71), (0x61, 0x51, 0x71), (0x69, 0x51, 0x71), (0x71, 0x51, 0x71), (0x71, 0x51, 0x69), (0x71, 0x51, 0x61), (0x71, 0x51, 0x59),
	( 0x71, 0x51, 0x51), (0x71, 0x59, 0x51), (0x71, 0x61, 0x51), (0x71, 0x69, 0x51), (0x71, 0x71, 0x51), (0x69, 0x71, 0x51), (0x61, 0x71, 0x51), (0x59, 0x71, 0x51),
	( 0x51, 0x71, 0x51), (0x51, 0x71, 0x59), (0x51, 0x71, 0x61), (0x51, 0x71, 0x69), (0x51, 0x71, 0x71), (0x51, 0x69, 0x71), (0x51, 0x61, 0x71), (0x51, 0x59, 0x71),
	( 0x00, 0x00, 0x41), (0x10, 0x00, 0x41), (0x20, 0x00, 0x41), (0x30, 0x00, 0x41), (0x41, 0x00, 0x41), (0x41, 0x00, 0x30), (0x41, 0x00, 0x20), (0x41, 0x00, 0x10),
	( 0x41, 0x00, 0x00), (0x41, 0x10, 0x00), (0x41, 0x20, 0x00), (0x41, 0x30, 0x00), (0x41, 0x41, 0x00), (0x30, 0x41, 0x00), (0x20, 0x41, 0x00), (0x10, 0x41, 0x00),
	( 0x00, 0x41, 0x00), (0x00, 0x41, 0x10), (0x00, 0x41, 0x20), (0x00, 0x41, 0x30), (0x00, 0x41, 0x41), (0x00, 0x30, 0x41), (0x00, 0x20, 0x41), (0x00, 0x10, 0x41),
	( 0x20, 0x20, 0x41), (0x28, 0x20, 0x41), (0x30, 0x20, 0x41), (0x38, 0x20, 0x41), (0x41, 0x20, 0x41), (0x41, 0x20, 0x38), (0x41, 0x20, 0x30), (0x41, 0x20, 0x28),
	( 0x41, 0x20, 0x20), (0x41, 0x28, 0x20), (0x41, 0x30, 0x20), (0x41, 0x38, 0x20), (0x41, 0x41, 0x20), (0x38, 0x41, 0x20), (0x30, 0x41, 0x20), (0x28, 0x41, 0x20),
	( 0x20, 0x41, 0x20), (0x20, 0x41, 0x28), (0x20, 0x41, 0x30), (0x20, 0x41, 0x38), (0x20, 0x41, 0x41), (0x20, 0x38, 0x41), (0x20, 0x30, 0x41), (0x20, 0x28, 0x41),
	( 0x2c, 0x2c, 0x41), (0x30, 0x2c, 0x41), (0x34, 0x2c, 0x41), (0x3c, 0x2c, 0x41), (0x41, 0x2c, 0x41), (0x41, 0x2c, 0x3c), (0x41, 0x2c, 0x34), (0x41, 0x2c, 0x30),
	( 0x41, 0x2c, 0x2c), (0x41, 0x30, 0x2c), (0x41, 0x34, 0x2c), (0x41, 0x3c, 0x2c), (0x41, 0x41, 0x2c), (0x3c, 0x41, 0x2c), (0x34, 0x41, 0x2c), (0x30, 0x41, 0x2c),
	( 0x2c, 0x41, 0x2c), (0x2c, 0x41, 0x30), (0x2c, 0x41, 0x34), (0x2c, 0x41, 0x3c), (0x2c, 0x41, 0x41), (0x2c, 0x3c, 0x41), (0x2c, 0x34, 0x41), (0x2c, 0x30, 0x41),
	( 0x00, 0x00, 0x00), (0x00, 0x00, 0x00), (0x00, 0x00, 0x00), (0x00, 0x00, 0x00), (0x00, 0x00, 0x00), (0x00, 0x00, 0x00), (0x00, 0x00, 0x00), (0x00, 0x00, 0x00)
];

fn display_keyboard(window: &mut Window) {
    unsafe {
        // Draw background
        fill_rect(KEYBOARD_POS_X - 5, KEYBOARD_POS_Y - 5, 320, 240, COL_CARD);
        draw_text(KEYBOARD_POS_X, KEYBOARD_POS_Y, "KEYBOARD", 0xFFFFFF);
        let mut y = KEYBOARD_POS_Y + 20; // Start below header
        for (i, &key) in KEYBOARD_BUFFER.iter().enumerate() {
            let x = KEYBOARD_POS_X + (i % 16) * 20; // 16 keys per row
            if i > 0 && i % 16 == 0 {
                y += 20; // Move to next row after 16 keys
            }
            let key_str = format!("{}", key as char);
            draw_text(x, y, &key_str, 0x00FFFF); // Cyan color for keys
        }
    }
}

fn display_flags(window: &mut Window) {
    unsafe {
        // Draw background
        fill_rect(FLAGS_POS_X - 5, FLAGS_POS_Y - 5, 320, 240, COL_CARD);
        let flags: [(&str, bool); 7] = [
            ("ZERO", ZERO_FLAG),
            ("CARRY", CARRY_FLAG),
            ("BORROW", BORROW_FLAG),
            ("PARITY", PARITY_FLAG),
            ("GREATER", GREATER_FLAG),
            ("LESSER", LESSER_FLAG),
            ("EQUAL", EQUAL_FLAG),
        ];

        draw_text(FLAGS_POS_X, FLAGS_POS_Y, "FLAGS", 0xFFFFFF);
        
        for (i, (name, value)) in flags.iter().enumerate() {
            let mut x = FLAGS_POS_X; // Offset for alignment
            let y = FLAGS_POS_Y + (i * 20) + 20; // Offset by 20 for header

            // Draw flag name
            draw_text(x, y, name, 0xFFFFFF); // White color for text
            x += 128; // Offset for value
            // Draw flag value
            if *value {
                draw_text(x , y, "1", 0x00FF00); // Green for true
            } else {
                draw_text(x , y, "0", 0xFF0000); // Red for false
            }
        }
    }
}

fn display_registers(window: &mut Window) {
    unsafe {
        // Draw background
        fill_rect(REGS_POS_X - 5, REGS_POS_Y - 5, 320, 240, COL_CARD);
        draw_text(REGS_POS_X, REGS_POS_Y, "REGISTERS", 0xFFFFFF);
        let registers = [
            ("A", REG_A),
            ("B", REG_B),
            ("C", REG_C),
            ("D", REG_D),
        ];

        for (i, (name, value)) in registers.iter().enumerate() {
            let mut x = REGS_POS_X + i * 32;
            let mut y = REGS_POS_Y + 20;

            // Draw register name
            draw_text(x, y, name, 0xFFFFFF); // White color for text

            // Draw register value
            let value_str = format!("{:02X}", value);
            y += 20; // Offset for value
            draw_text(x, y, &value_str, 0x00FFFF); // Cyan color for values
        }
    }
}

fn display_opcode(window: &mut Window, opcode: u8) {
    unsafe {
        // Draw background
        fill_rect(OPCODE_POS_X - 5, OPCODE_POS_Y - 5, 320, 240, COL_CARD);
        draw_text(OPCODE_POS_X, OPCODE_POS_Y, "CURRENT OPCODE", 0xFFFFFF);

        let opcode_str = format!("{:02X}", opcode);
        draw_text(OPCODE_POS_X + (16*12), OPCODE_POS_Y , &opcode_str, 0xFFFF00); // Yellow color for opcode
    }
}

fn display_pc(window: &mut Window, pc: usize) {
    unsafe {
        // Draw background
        fill_rect(OPADDR_POS_X - 5, OPADDR_POS_Y - 5, 320, 240, COL_CARD);
        draw_text(OPADDR_POS_X, OPADDR_POS_Y, "PROGRAM COUNTER", 0xFFFFFF);

        let pc_str = format!("{:04X}", pc);
        draw_text(OPADDR_POS_X + (16*12), OPADDR_POS_Y, &pc_str, 0xFFFF00); // Yellow color for PC
    }
}

fn display_stack(window: &mut Window) {
    unsafe {
        // Draw background
        fill_rect(STACK_POS_X - 5, STACK_POS_Y - 5, 320, 240, COL_CARD);

        draw_text(STACK_POS_X, STACK_POS_Y, "STACK", 0xFFFFFF);
        let columns = 4;
        for (i, value) in STACK.iter().rev().enumerate() {
            let x = STACK_POS_X + (i % columns) * 80;
            let y = STACK_POS_Y + (i / columns) * 20 + 20; // Offset by 20 for header

            let value_str = format!("{:04X}", value);
            if i == 0 {
                draw_text(x, y, &value_str, 0xFFFF00); // Highlight top of stack in yellow
            } else {
                if i % 2 == 0 {
                    draw_text(x, y, &value_str, 0x0FFF00); // Green for even indices
                } else {
                    draw_text(x, y, &value_str, 0xF0FF00); // Red for odd indices
                }
            }
        }
    }
}

fn display_terminal(window: &mut Window) {
    unsafe {
        // Use smaller character dimensions to fit more text
        let char_width = 8;   // Reduced from 16
        let char_height = 12; // Keep height at 12
        let terminal_display_width = TERMINAL_WIDTH * char_width;
        let terminal_display_height = TERMINAL_HEIGHT * char_height;
        
        // Draw terminal background
        fill_rect(TERMINAL_POS_X - 5, TERMINAL_POS_Y - 5, 
                 terminal_display_width + 10, terminal_display_height + 30, 
                 COL_CARD);
        

        // Draw terminal title
        draw_text(TERMINAL_POS_X, TERMINAL_POS_Y, "TERMINAL", 0xFFFFFF);
        
        // Draw terminal content line by line using draw_text function
        for row in 0..TERMINAL_HEIGHT {
            let mut line = String::new();
            for col in 0..TERMINAL_WIDTH {
                let ch = TERMINAL_BUFFER[row * TERMINAL_WIDTH + col];
                line.push(ch);
            }
            
            let x = TERMINAL_POS_X;
            let y = TERMINAL_POS_Y + (row * char_height) + 20;
            
            if TERMINAL_X >= TERMINAL_WIDTH {
                TERMINAL_X = 0;
                TERMINAL_Y += 1;
            }
            if TERMINAL_Y >= TERMINAL_HEIGHT {
                TERMINAL_Y = 0;
                // Scroll terminal up by one line
                for row in 0..(TERMINAL_HEIGHT - 1) {
                    for col in 0..TERMINAL_WIDTH {
                        TERMINAL_BUFFER[row * TERMINAL_WIDTH + col] = TERMINAL_BUFFER[(row + 1) * TERMINAL_WIDTH + col];
                    }
                }
            } 

            // Use the existing draw_text function for the entire line
            draw_text(x, y, &line.trim_end(), COL_GREEN);
        }
        
        // Draw cursor at current position
        let cursor_x = TERMINAL_POS_X + (TERMINAL_X * 6); // 6 pixels per char in draw_text
        let cursor_y = TERMINAL_POS_Y + (TERMINAL_Y * char_height);
        fill_rect(cursor_x, cursor_y + 10, 6, 2, COL_GREEN); // Underline cursor
    }
}

fn display_memory(window: &mut Window, memory: &[u8], offset: usize) {
    let width = 16*32 + 128; // 16 values * 32 pixels each + address space
    let height = 16*20 + 20; // 16 rows * 20 pixels each + title space
    fill_rect(MEM_POS_X - 5, MEM_POS_Y - 5, width + 10, height + 10, COL_CARD);
    //Display memory content in a grid format, 16 values from offset, vertically
    let mut mem_display_start_x = MEM_POS_X;
    let mut mem_display_start_y = MEM_POS_Y;
    draw_text(mem_display_start_x, mem_display_start_y, "MEMORY", 0xFFFFFF);
    for i in 0..16 {
        for j in 0..16 {
            let addr = offset + i * 16 + j;
            if addr < memory.len() {
                let value = memory[addr];
                let x = mem_display_start_x + (j * 32);
                let y = mem_display_start_y + (i * 20) + 20;

                // Draw memory address
                let addr_str = format!("{:04X}:", addr);
                if addr % 16 == 0 {
                    draw_text(mem_display_start_x, y, &addr_str, 0xFF0000); // Red color for odd addresses
                }

                // Draw memory value
                let value_str = format!("{:02X}", value);
                draw_text(x + 128, y, &value_str, 0x00FF00); // Green color for values
            }
        }
    }
}

fn display_vram(window: &mut Window, vram: &[u8], offset: usize) {
    unsafe {
        let width = 16*32 + 128; // 16 values * 32 pixels each + address space
        let height = 16*20 + 20; // 16 rows * 20 pixels each + title space
        fill_rect(VRAM_POS_X - 5, VRAM_POS_Y - 5,
                  width + 10, height + 10, 
                  COL_CARD);
        //Display VRAM content in a grid format, 16 values from offset, vertically
        let vram_display_start_x = VRAM_POS_X;
        let vram_display_start_y = VRAM_POS_Y;
        draw_text(vram_display_start_x, vram_display_start_y, "VRAM", 0xFFFFFF);
        for i in 0..16 {
            for j in 0..16 {
                let addr = offset + i * 16 + j;
                if addr < vram.len() {
                    let value = vram[addr];
                    let x = vram_display_start_x + (j * 32);
                    let y = vram_display_start_y + (i * 20) + 20;

                    // Draw VRAM address
                    let addr_str = format!("{:04X}:", addr);
                    if addr % 16 == 0 {
                        draw_text(vram_display_start_x, y, &addr_str, 0xFF0000); // Red color for odd addresses
                    }

                    let value: String = format!("{:02X}", value);
                    draw_text(x + 128, y, &value, 0x00FF00); // Green color for values                   
                }
            }
        }
    }
}

fn set_reg(mut reg: u8, opcode: u8, value: u8) {    
    match opcode {
        ADD => unsafe { 
                    log(&format!("Adding {:#X} to REG_A", value), None); 
                    REG_A = REG_A.wrapping_add(value); 
                  
                },
        SUB => unsafe { 
                    log(&format!("Subtracting {:#X} from REG_A", value), None); 
                    REG_A = REG_A.wrapping_sub(value); 
                  
                },
        MUL => unsafe { 
                    log(&format!("Multiplying REG_A by {:#X}", value), None); 
                    REG_A = REG_A.wrapping_mul(value); 
                  
                },
        DIV => unsafe { 
                    log(&format!("Dividing REG_A by {:#X}", value), None); 
                    REG_A = REG_A.wrapping_div(value); 
                  
                },
        IADD => unsafe { 
                    log(&format!("Adding {:#X} to REG_A", value), None); 
                    REG_A = REG_A.wrapping_add(value); 
                  
                },
        ISUB => unsafe { 
                    log(&format!("Subtracting {:#X} from REG_A", value), None); 
                    REG_A = REG_A.wrapping_sub(value); 
                  
                },
        IMUL => unsafe { 
                    log(&format!("Multiplying REG_A by {:#X}", value), None); 
                    REG_A = REG_A.wrapping_mul(value); 
                  
                },
        IDIV => unsafe { 
                    log(&format!("Dividing REG_A by {:#X}", value), None); 
                    REG_A = REG_A.wrapping_div(value); 
                  
                },
        OUTA => unsafe { 
            log(&format!("Printing OUTA: {}", value), None);
            REG_A = value; 
        },
        OUTB => unsafe { 
            log(&format!("Printing OUTB: {}", value), None);
            REG_B = value; 
            
        },
        OUTC => unsafe { 
            log(&format!("Printing OUTC: {}", value), None);    
            REG_C = value; 
        },
        OUTD => unsafe { 
            log(&format!("Printing OUTD: {}", value), None);
            REG_D = value; 
        },
        LDA => unsafe { 
            log(&format!("Loading {:#X} into REG_A", value), None);
            REG_A = value; 
            
        },
        LDB => unsafe { 
            log(&format!("Loading {:#X} into REG_B", value), None);
            REG_B = value; 
            
        },
        LDC => unsafe { 
            log(&format!("Loading {:#X} into REG_C", value), None);
            REG_C = value; 
            
        },
        LDD => unsafe { 
            log(&format!("Loading {:#X} into REG_D", value), None);
            REG_D = value; 
            
        },
        LDIA => unsafe { 
            log(&format!("Loading {:#X} into REG_A", value), None);
            REG_A = value; 
            
        },
        LDIB => unsafe { 
            log(&format!("Loading {:#X} into REG_B", value), None);
            REG_B = value; 
            
        },
        LDIC => unsafe { 
            log(&format!("Loading {:#X} into REG_C", value), None);
            REG_C = value; 
            
        },
        LDID => unsafe { 
            log(&format!("Loading {:#X} into REG_D", value), None);
            REG_D = value; 
       
        },
        BX => unsafe {
            log(&format!("Branching with return value {:#X}", value), None);
            REG_A = value;
        },
        _ => log(&format!("Error: Unsupported opcode for set_reg: {:#X}", opcode), None),
    }
}

fn load_file_to_memory<P: AsRef<Path>>(path: P) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
    // Read the file as text
    let content = fs::read_to_string(path)?;
    
    // Split by whitespace and parse each hex value
    let mut bytes = Vec::new();    

    for hex_str in content.split_whitespace() {
        // Skip empty strings
        if hex_str.is_empty() {
            continue;
        }
        
        // Parse hex string to byte
        match u8::from_str_radix(hex_str, 16) {
            Ok(byte) => bytes.push(byte),
            Err(e) => {
                log(&format!("Error parsing hex value '{}': {}", hex_str, e), None);
                // Skip invalid hex values instead of failing
                continue;
            }
        }
    }

    log(&format!("Parsed {} hex values from file", bytes.len()), None);
    Ok(bytes)
}

fn vga_to_rgb(vga_index: u8) -> u32 {
    let (r, g, b) = VGA_PALETTE[vga_index as usize];
    ((r as u32) << 16) | ((g as u32) << 8) | (b as u32)
}

fn read_offset(mut addr: usize, mem: &[u8]) -> usize {
    addr += 1;
    let a = mem[addr];
    addr += 1;
    let b = mem[addr];
    addr += 1;
    let c = mem[addr];
    addr += 1;
    let d = mem[addr];

    let offset = (a as usize) << 24 | (b as usize) << 16 | (c as usize) << 8 | (d as usize);
    offset
}

fn push_stack(mut value: usize) {
    unsafe {
        STACK.push(value);
    }
}

fn pop_stack() -> usize {
    unsafe {
        STACK.pop().unwrap_or(0) // Return 0 if stack is empty
    }
}

fn set_flags(operand: u8) {
    unsafe {
        ZERO_FLAG = (REG_A == 0 && operand == 0);
        CARRY_FLAG = (REG_A + operand > 0);
        BORROW_FLAG = (REG_A - operand < 255);
        PARITY_FLAG = (operand.count_ones() % 2 == 0);
        GREATER_FLAG = (REG_A > operand);
        LESSER_FLAG = (REG_A < operand);
        EQUAL_FLAG = (REG_A == operand);
    }
}

fn append_to_textfile(path: &str, content: &str) -> std::io::Result<()> {
    let mut file = std::fs::OpenOptions::new()
        .create(true)
        .append(true)
        .open(path)?;
    writeln!(file, "{}", content)?;
    Ok(())
}

fn log(message: &str, args: Option<std::fmt::Arguments>) {
    if(args.is_some()) {
        let formatted = format!("{}{}", message, args.unwrap());
        let _ = append_to_textfile("emulator_log.txt", &formatted);
    } else {
        let _ = append_to_textfile("emulator_log.txt", message);
    }    
}

fn update_window(window: &mut Window) {
    unsafe {
        if !window.is_open() {
            return;
        }
        window
            .update_with_buffer(&WINDOW_BUFFER, WINDOW_WIDTH, WINDOW_HEIGHT)
            .unwrap();
    }
}

fn embed_int_in_terminal(value: u8) {
    let s = format!("{}", value);
    for c in s.chars() {
        embed_char_in_terminal(c);
    }
}

fn embed_char_in_terminal(c: char) {
    unsafe {
        // Handle special characters
        match c {
            '\n' => {
                TERMINAL_X = 0;
                TERMINAL_Y += 1;
            },
            '\r' => {
                TERMINAL_X -= 1;
            },
            _ => {
                if TERMINAL_X < TERMINAL_WIDTH && TERMINAL_Y < TERMINAL_HEIGHT {
                    TERMINAL_BUFFER[TERMINAL_Y * TERMINAL_WIDTH + TERMINAL_X] = c;
                    TERMINAL_X += 1;
                }
            }
        }
        
        // Handle terminal scrolling
        if TERMINAL_Y >= TERMINAL_HEIGHT {
            // Scroll up by one line
            for row in 0..(TERMINAL_HEIGHT - 1) {
                for col in 0..TERMINAL_WIDTH {
                    TERMINAL_BUFFER[row * TERMINAL_WIDTH + col] = 
                        TERMINAL_BUFFER[(row + 1) * TERMINAL_WIDTH + col];
                }
            }
            // Clear last line
            for col in 0..TERMINAL_WIDTH {
                TERMINAL_BUFFER[(TERMINAL_HEIGHT - 1) * TERMINAL_WIDTH + col] = ' ';
            }
            TERMINAL_Y = TERMINAL_HEIGHT - 1;
        }
    }
}

// === NEW: 5x7 font for the few labels we need ===
// Each glyph is 5 columns (LSB top), 7 rows used, stored as u8 columns.
#[allow(dead_code)]
fn font_glyph(c: char) -> [u8; 5] {
    match c {
        // ascii 32-255
        ' ' => [0x00, 0x00, 0x00, 0x00, 0x00],
        '!' => [0x00, 0x00, 0x5F, 0x00, 0x00],
        '"' => [0x00, 0x07, 0x00, 0x07, 0x00],
        '#' => [0x14, 0x7F, 0x14, 0x7F, 0x14],
        '$' => [0x24, 0x2A, 0x7F, 0x2A, 0x12],
        '%' => [0x23, 0x13, 0x08, 0x64, 0x62],
        '&' => [0x36, 0x49, 0x55, 0x22, 0x50],
        '\'' => [0x00, 0x05, 0x03, 0x00, 0x00],
        '(' => [0x00, 0x1C, 0x22, 0x41, 0x00],
        ')' => [0x00, 0x41, 0x22, 0x1C, 0x00],
        '*' => [0x14, 0x08, 0x3E, 0x08, 0x14],
        '+' => [0x08, 0x08, 0x3E, 0x08, 0x08],
        ',' => [0x00, 0x50, 0x30, 0x00, 0x00],
        '-' => [0x08, 0x08, 0x08, 0x08, 0x08],
        '.' => [0x00, 0x60, 0x60, 0x00, 0x00],
        '/' => [0x20, 0x10, 0x08, 0x04, 0x02],
        '0' => [0x3E, 0x51, 0x49, 0x45, 0x3E],
        '1' => [0x00, 0x42, 0x7F, 0x40, 0x00],
        '2' => [0x42, 0x61, 0x51, 0x49, 0x46],
        '3' => [0x21, 0x41, 0x45, 0x4B, 0x31],
        '4' => [0x18, 0x14, 0x12, 0x7F, 0x10],
        '5' => [0x27, 0x45, 0x45, 0x45, 0x39],
        '6' => [0x3C, 0x4A, 0x49, 0x49, 0x30],
        '7' => [0x01, 0x71, 0x09, 0x05, 0x03],
        '8' => [0x36, 0x49, 0x49, 0x49, 0x36],
        '9' => [0x06, 0x49, 0x49, 0x29, 0x1E],
        ':' => [0x00, 0x36, 0x36, 0x00, 0x00],
        ';' => [0x00, 0x56, 0x36, 0x00, 0x00],
        '<' => [0x08, 0x14, 0x22, 0x41, 0x00],
        '=' => [0x14, 0x14, 0x14, 0x14, 0x14],
        '>' => [0x00, 0x41, 0x22, 0x14, 0x08],
        '?' => [0x02, 0x01, 0x51, 0x09, 0x06],
        '@' => [0x32, 0x49, 0x79, 0x41, 0x3E],
        'A' => [0x7E, 0x11, 0x11, 0x11, 0x7E],
        'B' => [0x7F, 0x49, 0x49, 0x49, 0x36],
        'C' => [0x3E, 0x41, 0x41, 0x41, 0x22],
        'D' => [0x7F, 0x41, 0x41, 0x22, 0x1C],
        'E' => [0x7F, 0x49, 0x49, 0x49, 0x41],
        'F' => [0x7F, 0x09, 0x09, 0x09, 0x01],
        'G' => [0x3E, 0x41, 0x49, 0x49, 0x7A],
        'H' => [0x7F, 0x08, 0x08, 0x08, 0x7F],
        'I' => [0x00, 0x41, 0x7F, 0x41, 0x00],
        'J' => [0x20, 0x40, 0x41, 0x3F, 0x01],
        'K' => [0x7F, 0x08, 0x14, 0x22, 0x41],
        'L' => [0x7F, 0x40, 0x40, 0x40, 0x40],
        'M' => [0x7F, 0x02, 0x0C, 0x02, 0x7F],
        'N' => [0x7F, 0x04, 0x08, 0x10, 0x7F],
        'O' => [0x3E, 0x41, 0x41, 0x41, 0x3E],
        'P' => [0x7F, 0x09, 0x09, 0x09, 0x06],
        'Q' => [0x3E, 0x41, 0x51, 0x21, 0x5E],
        'R' => [0x7F, 0x09, 0x19, 0x29, 0x46],
        'S' => [0x46, 0x49, 0x49, 0x49, 0x31],
        'T' => [0x01, 0x01, 0x7F, 0x01, 0x01],
        'U' => [0x3F, 0x40, 0x40, 0x40, 0x3F],
        'V' => [0x1F, 0x20, 0x40, 0x20, 0x1F],
        'W' => [0x3F, 0x40, 0x38, 0x40, 0x3F],
        'X' => [0x63, 0x14, 0x08, 0x14, 0x63],
        'Y' => [0x07, 0x08, 0x70, 0x08, 0x07],
        'Z' => [0x61, 0x51, 0x49, 0x45, 0x43],
        'a' => [0x20, 0x54, 0x54, 0x54, 0x78],
        'b' => [0x7F, 0x48, 0x44, 0x44, 0x38],
        'c' => [0x38, 0x44, 0x44, 0x44, 0x20],
        'd' => [0x38, 0x44, 0x44, 0x48, 0x7F],
        'e' => [0x38, 0x54, 0x54, 0x54, 0x18],
        'f' => [0x08, 0x7E, 0x09, 0x01, 0x02],
        'g' => [0x0C, 0x52, 0x52, 0x52, 0x3E],
        'h' => [0x7F, 0x08, 0x04, 0x04, 0x78],
        'i' => [0x00, 0x44, 0x7D, 0x40, 0x00],
        'j' => [0x20, 0x40, 0x44, 0x3D, 0x00],
        'k' => [0x7F, 0x10, 0x28, 0x44, 0x00],
        'l' => [0x00, 0x41, 0x7F, 0x40, 0x00],
        'm' => [0x7C, 0x04, 0x18, 0x04, 0x78],
        'n' => [0x7C, 0x08, 0x04, 0x04, 0x78],
        'o' => [0x38, 0x44, 0x44, 0x44, 0x38],
        'p' => [0x7C, 0x14, 0x14, 0x14, 0x08],
        'q' => [0x08, 0x14, 0x14, 0x18, 0x7C],
        'r' => [0x7C, 0x08, 0x04, 0x04, 0x08],
        's' => [0x48, 0x54, 0x54, 0x54, 0x20],
        't' => [0x04, 0x3F, 0x44, 0x40, 0x20],
        'u' => [0x3C, 0x40, 0x40, 0x20, 0x7C],
        'v' => [0x1C, 0x20, 0x40, 0x20, 0x1C],
        'w' => [0x3C, 0x40, 0x30, 0x40, 0x3C],
        'x' => [0x44, 0x28, 0x10, 0x28, 0x44],
        'y' => [0x0C, 0x50, 0x50, 0x50, 0x3C],
        'z' => [0x44, 0x64, 0x54, 0x4C, 0x44],
        _ => [0x00, 0x00, 0x00, 0x00, 0x00], // Unknown chars as blank

    }
}

#[inline]
fn put_px(x: usize, y: usize, rgb: u32) {
    if x < WINDOW_WIDTH && y < WINDOW_HEIGHT {
        let idx = y * WINDOW_WIDTH + x;
        unsafe { WINDOW_BUFFER[idx] = rgb; }
    }
}

fn fill_rect(x: usize, y: usize, w: usize, h: usize, rgb: u32) {
    let x2 = (x + w).min(WINDOW_WIDTH);
    let y2 = (y + h).min(WINDOW_HEIGHT);
    for yy in y..y2 {
        let base = yy * WINDOW_WIDTH;
        for xx in x..x2 {
            unsafe { WINDOW_BUFFER[base + xx] = rgb; }
        }
    }
}

fn draw_char_wxh(x: usize, y: usize, c: char, rgb: u32, w: usize, h: usize) {
    // Draw a single character at (x, y) scaled to w x h pixels
    let glyph = font_glyph(c);
    for col in 0..5 {
        let bits = glyph[col];
        for row in 0..7 {
            if (bits & (1 << row)) != 0 {
                // Scale pixel to w x h
                for sx in 0..w {
                    for sy in 0..h {
                        put_px(x + col * w + sx, y + row * h + sy, rgb);
                    }
                }
            }
        }
    }
}

fn draw_char(x: usize, y: usize, c: char, rgb: u32) {
    // Draw a single character at (x, y) without scaling (for headers and labels)
    let glyph = font_glyph(c);
    for col in 0..5 {
        let bits = glyph[col];
        for row in 0..7 {
            if (bits & (1 << row)) != 0 {
                put_px(x + col, y + row, rgb);
            }
        }
    }
}

fn draw_text(mut x: usize, y: usize, s: &str, rgb: u32) {
    for ch in s.chars() {
        draw_char(x, y, ch, rgb);
        x += 6; // 5px character + 1px gap (no scaling for headers)
    }
}

fn draw_hex_u8(x: usize, y: usize, v: u8) {
    let s = format!("{:02X}", v);
    draw_text(x, y, &s, COL_TEXT);
}

fn draw_hex_u32(x: usize, y: usize, v: u32) {
    let s = format!("{:08X}", v);
    draw_text(x, y, &s, COL_TEXT);
}

fn init_terminal_buffer() {
    unsafe {
        // Clear terminal buffer
        for i in 0..(TERMINAL_WIDTH * TERMINAL_HEIGHT) {
            TERMINAL_BUFFER[i] = ' ';
        }        
    }
}

fn main() -> Result<(), Box<dyn std::error::Error>> {
    log(&format!("Starting Libre8 Emulator..."), None);
    
    // Initialize terminal buffer
    init_terminal_buffer();

    let mut bytes = load_file_to_memory("D:/l8rust/pixels_output.txt")?;
    //let mut bytes = load_file_to_memory("./output/bin.hex")?;
    let num_bytes = bytes.len();
    log(&format!("Loaded {} bytes into memory.", num_bytes), None);

    //spawn a new cmd and get the handle
    let mut child = Command::new("powershell.exe")
        .args(["-NoLogo", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", "-"])
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()?;

    // // Or CMD (interactive):
    // let mut child = Command::new("cmd.exe")
    //     .args(["/Q", "/K"]) // /Q: no echo, /K: keep session alive
    //     .stdin(Stdio::piped())
    //     .stdout(Stdio::piped())
    //     .stderr(Stdio::piped())
    //     .spawn()?;

    let mut stdin = child.stdin.take().expect("stdin piped");
    let stdout = child.stdout.take().expect("stdout piped");
    let stderr = child.stderr.take().expect("stderr piped");

    // Read stdout on a thread
    let out_handle = thread::spawn(move || {
        let reader = BufReader::new(stdout);
        for line in reader.lines() {
            log(&format!("[STDOUT] {}", line.unwrap_or_default()), None);
        }
    });

    // Read stderr on a thread
    let err_handle = thread::spawn(move || {
        let reader = BufReader::new(stderr);
        for line in reader.lines() {
            log(&format!("[STDERR] {}", line.unwrap_or_default()), None);
        }
    });

    let mut window = Window::new(
        "Libre8 Pixel Display",
        WINDOW_WIDTH,
        WINDOW_HEIGHT,
        WindowOptions {
            scale: minifb::Scale::X1,
            ..WindowOptions::default()
        },
    )?;

    let mut x: usize = 0;
    let mut y: usize = 0;
    let mut addr: usize = 0;

    println!("Press Enter to start processing...");
    let mut input = String::new();
    //std::io::stdin().read_line(&mut input)?;
    let mut VIDEO_BUFFER: [u8; WIDTH * HEIGHT] = [0; WIDTH * HEIGHT]; // 256x256 pixels
    let mut FRAME_BUFFER: [u32; WIDTH * HEIGHT] = [0; WIDTH * HEIGHT]; // 256x256 pixels RGB
    for i in 0..(WIDTH * HEIGHT) {
        VIDEO_BUFFER[i] = 0x0F; // Initialize to black
    }
    // Fill the window buffer with black before drawing
    while addr < num_bytes {        
        unsafe {
            // Fill background
            for i in 0..(WINDOW_WIDTH * WINDOW_HEIGHT) {
                WINDOW_BUFFER[i] = COL_BG;
            }
            fill_rect(DISPLAY_POS_X - 5, DISPLAY_POS_Y - 5, WIDTH*2 + 10, HEIGHT*2 + 10, COL_CARD);
            draw_text(DISPLAY_POS_X, DISPLAY_POS_Y, "VGA 256x256px Display", COL_TEXT);
        }
        let opcode = bytes[addr];
        //log(&format!("Executing opcode {:#X} at address {:#X}", opcode, addr), None);
        if opcode == POKE {
            for i in 1..64 {
                if addr + i >= num_bytes {
                    break;
                }
                
                // Convert VGA color to RGB and draw pixel
                
                if x < WIDTH && y < HEIGHT {
                    let vga_color = bytes[addr + i];
                    let pixel = vga_to_rgb(vga_color);
                    let window_row = DISPLAY_POS_Y + y;
                    let window_col = DISPLAY_POS_X + x;
                    let window_idx = window_row * WINDOW_WIDTH + window_col;
                    unsafe {
                        WINDOW_BUFFER[window_idx] = pixel;
                        VIDEO_BUFFER[y * WIDTH + x] = vga_color as u8; // Store only the LSB (not accurate, but for demo purposes)
                        FRAME_BUFFER[y * WIDTH + x] = pixel; // Store full RGB value
                    }
                    //log(&format!("Converting VGA color {:#X} to RGB @ ({}, {})", vga_color, window_col, window_row), None);
                } 

                // Advance coordinates
                x += 1;
                if x >= WIDTH {
                    x = 0;
                    y += 1;
                    if y >= HEIGHT {
                        y = 0;
                    }
                }
            }            
            
            addr += 64; // Skip the frame marker + 63 data bytes
        }

        if opcode == HLT {
            log(&format!("{}{}","HLT encountered at address {}. Stopping execution.", addr), None);
            break;
        }

        if opcode == OUTA || opcode == OUTB || opcode == OUTC || opcode == OUTD {        
            match opcode {
                OUTA => unsafe { 
                    print!(/*as char*/ "{}", REG_A as char);    
                    embed_char_in_terminal(unsafe { REG_A as char});
                 },
                OUTB => unsafe { 
                    let str = format!("0x{:02X}", REG_A as u8);
                    print!(/*as hex*/ "{}", str);
                    for c in str.chars() {
                        embed_char_in_terminal(c);
                    }
                    
                 },
                OUTC => unsafe { 
                    print!(/*as reserved*/ "{}", REG_A);
                    embed_char_in_terminal(unsafe { REG_A as char});
                },
                OUTD => unsafe { 
                    let str = format!("{}", REG_A as u8);
                    print!(/*as hex*/ "{}", str);
                    for c in str.chars() {
                        embed_char_in_terminal(c);
                    }
                },
                _ => unsafe { 
                    print!(/*as char*/ "{}", REG_A as char);
                    embed_char_in_terminal(unsafe { REG_A as char});
                },
            }
            addr += 1;
        }

        if opcode == LDA || opcode == LDB || opcode == LDC || opcode == LDD {
            let prev_addr = addr;
            addr = read_offset(addr, &bytes);
            // Print in hex
            log(&format!("Loading value from memory address {:#X}", addr), None);
            let value = bytes[addr];
            unsafe {
                set_reg(REG_A, opcode, value);
            }
            addr = prev_addr + 5; // Move to the next instruction            
        }

        if opcode == ADD || opcode == SUB || opcode == MUL || opcode == DIV {
            let prev_addr = addr;
            addr = read_offset(addr, &bytes);
            // Print in hex
            log(&format!("Performing ALU operation with value from memory address {:#X}", addr), None);
            let operand = bytes[addr];
            set_flags(operand);
            unsafe {
                set_reg(REG_A, opcode, operand);
            }            
            addr = prev_addr + 5; // Move to the next instruction
        }

        if opcode == B {
            let target_addr = read_offset(addr, &bytes);
            // Print in hex
            log(&format!("Unconditional branch to address {:#X}", target_addr), None);
            match opcode {
                B => {
                    addr += 5;
                    log(&format!("Pushing return address {:#X} onto stack", addr), None);
                    push_stack(addr);
                    addr = target_addr;
                }                
                _ => {
                    // For simplicity, treat all other branches as unconditional
                    addr = read_offset(addr, &bytes);
                }
            }            
        }

        if opcode == BC || opcode == BNC || opcode == BZ || opcode == BNZ || opcode == BNB || opcode == BP || opcode == BNP || opcode == BB {
            log(&format!("Evaluating conditional branch for opcode {:#X} at address {:#X}", opcode, addr), None);
            let target_addr = read_offset(addr, &bytes);
            // Print in hex
            log(&format!("Conditional branch to address {:#X} if condition met", target_addr), None);
            let branch = match opcode {
                BC => unsafe { CARRY_FLAG },
                BNC => unsafe { !CARRY_FLAG },
                BZ => unsafe { ZERO_FLAG },
                BNZ => unsafe { !ZERO_FLAG },
                BNB => unsafe { !BORROW_FLAG },
                BP => unsafe { PARITY_FLAG },
                BNP => unsafe { !PARITY_FLAG },
                BB => unsafe { BORROW_FLAG },
                BX => true, // Unconditional branch
                _ => false,
            };
            if branch {
                log(&format!("Branch condition met. Branching to address {:#X}", target_addr), None);
                addr += 5;
                log(&format!("Pushing return address {:#X} onto stack", addr), None);
                push_stack(addr); // Push return address onto stack
                addr = target_addr;
            } else {
                addr += 5; // Move past the address bytes
            }
        }

        if opcode == BX {
            // Print in hex
            log(&format!("Returning from branch with BX at address {:#X}", addr), None);
            let return_value_address = read_offset(addr, &bytes);
            unsafe {
                set_reg(REG_A, opcode, bytes[return_value_address]);
            }
            addr = pop_stack(); // Move to the next instruction after the call
            log(&format!("Returning from branch to address {:#X}", addr), None);
        }

        if opcode == JMP {            
            addr = read_offset(addr, &bytes);
            // Print in hex
            log(&format!("Unconditional jump to address {:#X}", addr), None);
        }

        if opcode == JZ || opcode == JNZ || opcode == JC || opcode == JNC || opcode == JNB || opcode == JB || opcode == JP || opcode == JNP {
            let target_addr = read_offset(addr, &bytes);
            // Print in hex
            log(&format!("Conditional jump to address {:#X} if condition met", target_addr), None);
            let jump = match opcode {
                JZ => unsafe { ZERO_FLAG },
                JNZ => unsafe { !ZERO_FLAG },
                JC => unsafe { CARRY_FLAG },
                JNC => unsafe { !CARRY_FLAG },
                JNB => unsafe { !BORROW_FLAG },
                JB => unsafe { BORROW_FLAG },
                JP => unsafe { PARITY_FLAG },
                JNP => unsafe { !PARITY_FLAG },
                _ => false,
            };
            if jump {
                addr = target_addr;
            } else {
                addr += 5; // Move past the address bytes
            }
        }

        if opcode == STA || opcode == STB || opcode == STC || opcode == STD {
            let prev_addr = addr;
            addr = read_offset(addr, &bytes);
            // Print in hex
            log(&format!("Storing register value to memory address {:#X}", addr), None);
            match opcode {
                STA => unsafe { bytes[addr] = REG_A; },
                STB => unsafe { bytes[addr] = REG_B; },
                STC => unsafe { bytes[addr] = REG_C; },
                STD => unsafe { bytes[addr] = REG_D; },
                _ => unsafe { bytes[addr] = REG_A; },
            }
            addr = prev_addr + 5; // Move to the next instruction
        }

        if opcode == IADD || opcode == ISUB || opcode == IMUL || opcode == IDIV {
            addr += 1;
            let operand = bytes[addr];
            log(&format!("Performing {:?} with operand {:#X}", opcode, operand), None);
            set_flags(operand);
            unsafe {
                set_reg(REG_A, opcode, operand);
            }
            addr += 1; // Move to the next instruction
        }

        if opcode == LDI || opcode == LDIA || opcode == LDIB || opcode == LDIC || opcode == LDID {            
            addr += 1;
            let value = bytes[addr];
            log(&format!("Loading immediate value {:#X} into register", value), None);
            unsafe {
                set_reg(REG_A, opcode, value);
            }
            addr += 1; // Move to the next instruction
        }

        if opcode == DEC || opcode == DECE {
            log(&format!("Waiting for keyboard input..."), None);
            // READ FROM KEYBOARD STORE IN A, with echo, without echo            
            match opcode {
                DEC => {
                    let mut input = String::new();
                    std::io::stdin().read_line(&mut input).unwrap();                    
                    if let Some(first_char) = input.chars().next() {
                        unsafe { 
                            REG_A = first_char as u8; 
                            KEYBOARD_BUFFER.push(first_char as u8);
                        }
                    }
                }
                DECE => {
                    let mut input = String::new();
                    std::io::stdin().read_line(&mut input).unwrap();                    
                    if let Some(first_char) = input.chars().next() {
                        unsafe { 
                            REG_A = first_char as u8; 
                            KEYBOARD_BUFFER.push(first_char as u8);
                            embed_char_in_terminal(first_char);
                        }
                    }
                }
                _ => {}
            }

            addr += 1; // Move to the next instruction
        }


        if opcode == NOP {
            addr += 1; // Just move to the next instruction
        }

        unsafe {        
            //Display things
            display_flags(&mut window);
            display_stack(&mut window);
            display_registers(&mut window);
            display_opcode(&mut window, opcode);
            display_pc(&mut window, addr);
            display_terminal(&mut window);        
            display_memory(&mut window, &bytes, addr);
            display_vram(&mut window, &VIDEO_BUFFER, (y * WIDTH + x) % (WIDTH * HEIGHT));
        }
        // Update the window display
        if opcode == POKE {
            // Sleep 1ms
            if(addr % (WIDTH * HEIGHT) == 0) {
                // Copy
                unsafe {
                    // Copy  FRAME_BUFFER to WINDOW_BUFFER at DISPLAY_POS_X, DISPLAY_POS_Y, 2x scale
                    for row in 0..HEIGHT {
                        for col in 0..WIDTH {
                            let pixel = FRAME_BUFFER[row * WIDTH + col];
                            let window_row = DISPLAY_POS_Y + row * 2;
                            let window_col = DISPLAY_POS_X + col * 2;
                            let window_idx1 = window_row * WINDOW_WIDTH + window_col;
                            let window_idx2 = (window_row + 1) * WINDOW_WIDTH + window_col;
                            unsafe {
                                WINDOW_BUFFER[window_idx1] = pixel;
                                WINDOW_BUFFER[window_idx1 + 1] = pixel;
                                WINDOW_BUFFER[window_idx2] = pixel;
                                WINDOW_BUFFER[window_idx2 + 1] = pixel;
                            }
                        }
                    }                    
                }
                update_window(&mut window);
                std::thread::sleep(std::time::Duration::from_millis(1));
            }
        } else {
            update_window(&mut window);            
            //std::thread::sleep(std::time::Duration::from_millis(1));
        }

                //std::io::stdin().read_line(&mut input)?;
        unsafe {
            if(KEYBOARD_BUFFER.len() > 0) {
                let k = KEYBOARD_BUFFER.remove(0);
            }
        }
    }
    stdin.flush()?;
    
    drop(stdin); // close stdin so the process can exit

    let status = child.wait()?;
    out_handle.join().ok();
    err_handle.join().ok();

    log(&format!("Libre8 Emulator finished with status: {}", status), None);
    // Wait for child to exit
    Ok(())
}
