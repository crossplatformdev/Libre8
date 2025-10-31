use anyhow::Result;
use minifb::{HasWindowHandle, Key, Window, WindowOptions};
use std::fs;
use std::io::{self, BufRead, BufReader, Write};
use std::path::Path;
use std::process::{Child, ChildStderr, ChildStdin, ChildStdout, Command, Stdio};
use std::thread;
use std::time::Duration;

const COL_BG: u32 = 0x101218;
const COL_GRID: u32 = 0x2a2f3a;
const COL_TEXT: u32 = 0xE6E6E6;
const COL_ACCENT: u32 = 0x4D7CFE;
const COL_RED: u32 = 0xC23B22;
const COL_GREEN: u32 = 0x2BA84A;
const COL_CARD: u32 = 0x191C24;

const FLAGS_POS_X: usize = 48;
const FLAGS_POS_Y: usize = 713;

const STACK_POS_X: usize = 48;
const STACK_POS_Y: usize = 53;

const MEM_POS_X: usize = 1236;
const MEM_POS_Y: usize = 48;

const VRAM_POS_X: usize = 1236;
const VRAM_POS_Y: usize = 408;

const WIDTH: usize = 252;
const HEIGHT: usize = 252;
// Add: heavy-card paging granularity for 16x16 grid
const CARD_PAGE_SIZE: usize = 256; // 16*16

const WINDOW_WIDTH: usize = 1920;
const WINDOW_HEIGHT: usize = 1020;

const REGS_POS_X: usize = 129;
const REGS_POS_Y: usize = 714;

const OPADDR_POS_X: usize = 129;
const OPADDR_POS_Y: usize = 834;

const OPCODE_POS_X: usize = 129;
const OPCODE_POS_Y: usize = 864;

const DISPLAY_POS_X: usize = 516; // snapped to 12px grid
const DISPLAY_POS_Y: usize = 384;

static mut TERMINAL_POS_X: usize = 456;
static mut TERMINAL_POS_Y: usize = 48;

const KEYBOARD_POS_X: usize = 1236;
const KEYBOARD_POS_Y: usize = 768;

const TERMINAL_WIDTH: usize = 80;
const TERMINAL_HEIGHT: usize = 24;

static mut TERMINAL_BUFFER: [char; TERMINAL_WIDTH * TERMINAL_HEIGHT] =
    [' '; TERMINAL_WIDTH * TERMINAL_HEIGHT]; // Terminal Buffer

static mut TERMINAL_X: usize = 0;
static mut TERMINAL_Y: usize = 0;

// Add: caches to skip unnecessary redraws
static mut PREV_TERMINAL_BUFFER: [char; TERMINAL_WIDTH * TERMINAL_HEIGHT] =
    [' '; TERMINAL_WIDTH * TERMINAL_HEIGHT];
static mut LAST_MEM_PAGE_BASE: usize = usize::MAX;
static mut LAST_VRAM_PAGE_BASE: usize = usize::MAX;
static mut LAST_KEY_TS: u128 = 0;

// Instruction Opcodes
const LD: u8 = 0x1d;
const LDA: u8 = 0x1a;
const LDB: u8 = 0x1b;
const LDC: u8 = 0x1c;
const LDD: u8 = 0x1e;
const LDIA: u8 = 0xda;
const LDIB: u8 = 0xdb;
const LDIC: u8 = 0xdc;
const LDID: u8 = 0xdd;

// MOV Instructions
const MOV_AMem: u8 = 0xf0;
const MOV_MemA: u8 = 0xf1;
const MOV_AB: u8 = 0xf2;
const MOV_AC: u8 = 0xf3;
const MOV_AD: u8 = 0xf4;
const MOV_BMem: u8 = 0xf5;
const MOV_MemB: u8 = 0xf6;
const MOV_BA: u8 = 0xf7;
const MOV_BC: u8 = 0xf8;
const MOV_BD: u8 = 0xf9;
const MOV_CMem: u8 = 0xfa;
const MOV_MemC: u8 = 0xfb;
const MOV_CA: u8 = 0xfc;
const MOV_CB: u8 = 0xfd;
const MOV_CD: u8 = 0xfe;
const MOV_DMem: u8 = 0xff;
const MOV_SP_BP: u8 = 0x01;
const MOV_DI_I: u8 = 0x02;
const MOV_REG_BP: u8 = 0x03;

// ALU Instructions
const STA: u8 = 0x5a;
const STB: u8 = 0x5b;
const STC: u8 = 0x5c;
const STD: u8 = 0x5d;
const ADD: u8 = 0xaa;
const SUB: u8 = 0xa5;
const MUL: u8 = 0xa2;
const DIV: u8 = 0xad;
const DEC: u8 = 0xde;
const DECE: u8 = 0xdf;
const DECI: u8 = 0xd1;

const IADD: u8 = 0x6a;
const ISUB: u8 = 0x65;
const IMUL: u8 = 0x62;
const IDIV: u8 = 0x6d;

// I/O Instructions
const POKE: u8 = 0x95;
const POKX: u8 = 0x9a;
const POKY: u8 = 0x9b;
const PXYD: u8 = 0x9c;
const PIKX: u8 = 0x9d;
const PIKY: u8 = 0x9e;
const PIYD: u8 = 0x9f;
const OUTA: u8 = 0x05;
const OUTB: u8 = 0x06;
const OUTC: u8 = 0x07;
const OUTD: u8 = 0x08;

// Stack and Pointer Operations
const PSAX: u8 = 0xc1;
const PSAH: u8 = 0xc2;
const PSAL: u8 = 0xc3;
const POPX: u8 = 0xc4;
const POPH: u8 = 0xc5;
const POPL: u8 = 0xc6;

// Miscellaneous
const LDI: u8 = 0xde;
const HLT: u8 = 0x91;
const STO: u8 = 0x86;
const NOP: u8 = 0x11;

// Jump and Branch
const JMP: u8 = 0xe1;
const JZ: u8 = 0xe2;
const JC: u8 = 0xe3;
const JNZ: u8 = 0xe4;
const JNC: u8 = 0xe5;
const JNB: u8 = 0xe6;
const JB: u8 = 0xe7;
const JP: u8 = 0xe8;
const JNP: u8 = 0xe9;

const B: u8 = 0x80;
const BC: u8 = 0x81;
const BNC: u8 = 0x82;
const BZ: u8 = 0x83;
const BNZ: u8 = 0x84;
const BNB: u8 = 0x85;
const BP: u8 = 0x86;
const BNP: u8 = 0x87;
const BB: u8 = 0x88;
const BX: u8 = 0x89;

const RST: u8 = 0x77;
const PST: u8 = 0x78;

const PTRI: u8 = 0x79;
const PTRD: u8 = 0x7a;
const PTRL: u8 = 0x7b;
const PTRS: u8 = 0x7c;
const OUTT: u8 = 0x7d;
const OUTM: u8 = 0x7e;

const LDR: u8 = 0xaa;

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

static mut WINDOW_BUFFER: [u32; WINDOW_HEIGHT * WINDOW_WIDTH] =
    [COL_GRID; WINDOW_HEIGHT * WINDOW_WIDTH]; // Framebuffer

static mut KEYBOARD_BUFFER: Vec<u8> = Vec::new(); // Keyboard Buffer

static mut draw_rect: bool = true;
static mut video_speed_factor: usize = 1;
static mut full_screen: bool = false;
static mut paused: bool = true;
static mut reset : bool = false;
static mut step : bool = false;
// Scaling and font metrics
const SCALE: usize = 2;
const DISPLAY_W_SCALED: usize = WIDTH * SCALE;
const DISPLAY_H_SCALED: usize = HEIGHT * SCALE;
const DISPLAY_ORIGIN: usize = DISPLAY_POS_Y * WINDOW_WIDTH + DISPLAY_POS_X;

const FONT_W: usize = 5;
const FONT_H: usize = 7;
const CHAR_ADV: usize = 6;
const TEXT_BG_H: usize = 8;

// Dirty caches to skip drawing unchanged "cards"
static mut PREV_FLAGS: (bool, bool, bool, bool, bool, bool, bool) = (true, true, true, true, true, true, true);
static mut PREV_REGS: [u8; 4] = [0xFF, 0xFF, 0xFF, 0xFF];
static mut PREV_PC: usize = usize::MAX;
static mut PREV_OPCODE: u8 = 0xFF;
static mut PREV_STACK_LEN: usize = 0;
static mut PREV_STACK_HASH: u64 = 0;
static mut PREV_KB_LEN: usize = 0;

// VGA 256-color palette
const VGA_PALETTE: [(u8, u8, u8); 256] = [
    (0x00, 0x00, 0x00),
    (0x00, 0x00, 0xaa),
    (0x00, 0xaa, 0x00),
    (0x00, 0xaa, 0xaa),
    (0xaa, 0x00, 0x00),
    (0xaa, 0x00, 0xaa),
    (0xaa, 0x55, 0x00),
    (0xaa, 0xaa, 0xaa),
    (0x55, 0x55, 0x55),
    (0x55, 0x55, 0xff),
    (0x55, 0xff, 0x55),
    (0x55, 0xff, 0xff),
    (0xff, 0x55, 0x55),
    (0xff, 0x55, 0xff),
    (0xff, 0xff, 0x55),
    (0xff, 0xff, 0xff),
    (0x00, 0x00, 0x00),
    (0x14, 0x14, 0x14),
    (0x20, 0x20, 0x20),
    (0x2c, 0x2c, 0x2c),
    (0x38, 0x38, 0x38),
    (0x45, 0x45, 0x45),
    (0x51, 0x51, 0x51),
    (0x61, 0x61, 0x61),
    (0x71, 0x71, 0x71),
    (0x82, 0x82, 0x82),
    (0x92, 0x92, 0x92),
    (0xa2, 0xa2, 0xa2),
    (0xb6, 0xb6, 0xb6),
    (0xcb, 0xcb, 0xcb),
    (0xe3, 0xe3, 0xe3),
    (0xff, 0xff, 0xff),
    (0x00, 0x00, 0xff),
    (0x41, 0x00, 0xff),
    (0x7d, 0x00, 0xff),
    (0xbe, 0x00, 0xff),
    (0xff, 0x00, 0xff),
    (0xff, 0x00, 0xbe),
    (0xff, 0x00, 0x7d),
    (0xff, 0x00, 0x41),
    (0xff, 0x00, 0x00),
    (0xff, 0x41, 0x00),
    (0xff, 0x7d, 0x00),
    (0xff, 0xbe, 0x00),
    (0xff, 0xff, 0x00),
    (0xbe, 0xff, 0x00),
    (0x7d, 0xff, 0x00),
    (0x41, 0xff, 0x00),
    (0x00, 0xff, 0x00),
    (0x00, 0xff, 0x41),
    (0x00, 0xff, 0x7d),
    (0x00, 0xff, 0xbe),
    (0x00, 0xff, 0xff),
    (0x00, 0xbe, 0xff),
    (0x00, 0x7d, 0xff),
    (0x00, 0x41, 0xff),
    (0x7d, 0x7d, 0xff),
    (0x9e, 0x7d, 0xff),
    (0xbe, 0x7d, 0xff),
    (0xdf, 0x7d, 0xff),
    (0xff, 0x7d, 0xff),
    (0xff, 0x7d, 0xdf),
    (0xff, 0x7d, 0xbe),
    (0xff, 0x7d, 0x9e),
    (0xff, 0x7d, 0x7d),
    (0xff, 0x9e, 0x7d),
    (0xff, 0xbe, 0x7d),
    (0xff, 0xdf, 0x7d),
    (0xff, 0xff, 0x7d),
    (0xdf, 0xff, 0x7d),
    (0xbe, 0xff, 0x7d),
    (0x9e, 0xff, 0x7d),
    (0x7d, 0xff, 0x7d),
    (0x7d, 0xff, 0x9e),
    (0x7d, 0xff, 0xbe),
    (0x7d, 0xff, 0xdf),
    (0x7d, 0xff, 0xff),
    (0x7d, 0xdf, 0xff),
    (0x7d, 0xbe, 0xff),
    (0x7d, 0x9e, 0xff),
    (0xb6, 0xb6, 0xff),
    (0xc7, 0xb6, 0xff),
    (0xdb, 0xb6, 0xff),
    (0xeb, 0xb6, 0xff),
    (0xff, 0xb6, 0xff),
    (0xff, 0xb6, 0xeb),
    (0xff, 0xb6, 0xdb),
    (0xff, 0xb6, 0xc7),
    (0xff, 0xb6, 0xb6),
    (0xff, 0xc7, 0xb6),
    (0xff, 0xdb, 0xb6),
    (0xff, 0xeb, 0xb6),
    (0xff, 0xff, 0xb6),
    (0xeb, 0xff, 0xb6),
    (0xdb, 0xff, 0xb6),
    (0xc7, 0xff, 0xb6),
    (0xb6, 0xff, 0xb6),
    (0xb6, 0xff, 0xc7),
    (0xb6, 0xff, 0xdb),
    (0xb6, 0xff, 0xeb),
    (0xb6, 0xff, 0xff),
    (0xb6, 0xeb, 0xff),
    (0xb6, 0xdb, 0xff),
    (0xb6, 0xc7, 0xff),
    (0x00, 0x00, 0x71),
    (0x1c, 0x00, 0x71),
    (0x38, 0x00, 0x71),
    (0x55, 0x00, 0x71),
    (0x71, 0x00, 0x71),
    (0x71, 0x00, 0x55),
    (0x71, 0x00, 0x38),
    (0x71, 0x00, 0x1c),
    (0x71, 0x00, 0x00),
    (0x71, 0x1c, 0x00),
    (0x71, 0x38, 0x00),
    (0x71, 0x55, 0x00),
    (0x71, 0x71, 0x00),
    (0x55, 0x71, 0x00),
    (0x38, 0x71, 0x00),
    (0x1c, 0x71, 0x00),
    (0x00, 0x71, 0x00),
    (0x00, 0x71, 0x1c),
    (0x00, 0x71, 0x38),
    (0x00, 0x71, 0x55),
    (0x00, 0x71, 0x71),
    (0x00, 0x55, 0x71),
    (0x00, 0x38, 0x71),
    (0x00, 0x1c, 0x71),
    (0x38, 0x38, 0x71),
    (0x45, 0x38, 0x71),
    (0x55, 0x38, 0x71),
    (0x61, 0x38, 0x71),
    (0x71, 0x38, 0x71),
    (0x71, 0x38, 0x61),
    (0x71, 0x38, 0x55),
    (0x71, 0x38, 0x45),
    (0x71, 0x38, 0x38),
    (0x71, 0x45, 0x38),
    (0x71, 0x55, 0x38),
    (0x71, 0x61, 0x38),
    (0x71, 0x71, 0x38),
    (0x61, 0x71, 0x38),
    (0x55, 0x71, 0x38),
    (0x45, 0x71, 0x38),
    (0x38, 0x71, 0x38),
    (0x38, 0x71, 0x45),
    (0x38, 0x71, 0x55),
    (0x38, 0x71, 0x61),
    (0x38, 0x71, 0x71),
    (0x38, 0x61, 0x71),
    (0x38, 0x55, 0x71),
    (0x38, 0x45, 0x71),
    (0x51, 0x51, 0x71),
    (0x59, 0x51, 0x71),
    (0x61, 0x51, 0x71),
    (0x69, 0x51, 0x71),
    (0x71, 0x51, 0x71),
    (0x71, 0x51, 0x69),
    (0x71, 0x51, 0x61),
    (0x71, 0x51, 0x59),
    (0x71, 0x51, 0x51),
    (0x71, 0x59, 0x51),
    (0x71, 0x61, 0x51),
    (0x71, 0x69, 0x51),
    (0x71, 0x71, 0x51),
    (0x69, 0x71, 0x51),
    (0x61, 0x71, 0x51),
    (0x59, 0x71, 0x51),
    (0x51, 0x71, 0x51),
    (0x51, 0x71, 0x59),
    (0x51, 0x71, 0x61),
    (0x51, 0x71, 0x69),
    (0x51, 0x71, 0x71),
    (0x51, 0x69, 0x71),
    (0x51, 0x61, 0x71),
    (0x51, 0x59, 0x71),
    (0x00, 0x00, 0x41),
    (0x10, 0x00, 0x41),
    (0x20, 0x00, 0x41),
    (0x30, 0x00, 0x41),
    (0x41, 0x00, 0x41),
    (0x41, 0x00, 0x30),
    (0x41, 0x00, 0x20),
    (0x41, 0x00, 0x10),
    (0x41, 0x00, 0x00),
    (0x41, 0x10, 0x00),
    (0x41, 0x20, 0x00),
    (0x41, 0x30, 0x00),
    (0x41, 0x41, 0x00),
    (0x30, 0x41, 0x00),
    (0x20, 0x41, 0x00),
    (0x10, 0x41, 0x00),
    (0x00, 0x41, 0x00),
    (0x00, 0x41, 0x10),
    (0x00, 0x41, 0x20),
    (0x00, 0x41, 0x30),
    (0x00, 0x41, 0x41),
    (0x00, 0x30, 0x41),
    (0x00, 0x20, 0x41),
    (0x00, 0x10, 0x41),
    (0x20, 0x20, 0x41),
    (0x28, 0x20, 0x41),
    (0x30, 0x20, 0x41),
    (0x38, 0x20, 0x41),
    (0x41, 0x20, 0x41),
    (0x41, 0x20, 0x38),
    (0x41, 0x20, 0x30),
    (0x41, 0x20, 0x28),
    (0x41, 0x20, 0x20),
    (0x41, 0x28, 0x20),
    (0x41, 0x30, 0x20),
    (0x41, 0x38, 0x20),
    (0x41, 0x41, 0x20),
    (0x38, 0x41, 0x20),
    (0x30, 0x41, 0x20),
    (0x28, 0x41, 0x20),
    (0x20, 0x41, 0x20),
    (0x20, 0x41, 0x28),
    (0x20, 0x41, 0x30),
    (0x20, 0x41, 0x38),
    (0x20, 0x41, 0x41),
    (0x20, 0x38, 0x41),
    (0x20, 0x30, 0x41),
    (0x20, 0x28, 0x41),
    (0x2c, 0x2c, 0x41),
    (0x30, 0x2c, 0x41),
    (0x34, 0x2c, 0x41),
    (0x3c, 0x2c, 0x41),
    (0x41, 0x2c, 0x41),
    (0x41, 0x2c, 0x3c),
    (0x41, 0x2c, 0x34),
    (0x41, 0x2c, 0x30),
    (0x41, 0x2c, 0x2c),
    (0x41, 0x30, 0x2c),
    (0x41, 0x34, 0x2c),
    (0x41, 0x3c, 0x2c),
    (0x41, 0x41, 0x2c),
    (0x3c, 0x41, 0x2c),
    (0x34, 0x41, 0x2c),
    (0x30, 0x41, 0x2c),
    (0x2c, 0x41, 0x2c),
    (0x2c, 0x41, 0x30),
    (0x2c, 0x41, 0x34),
    (0x2c, 0x41, 0x3c),
    (0x2c, 0x41, 0x41),
    (0x2c, 0x3c, 0x41),
    (0x2c, 0x34, 0x41),
    (0x2c, 0x30, 0x41),
    (0x00, 0x00, 0x00),
    (0x00, 0x00, 0x00),
    (0x00, 0x00, 0x00),
    (0x00, 0x00, 0x00),
    (0x00, 0x00, 0x00),
    (0x00, 0x00, 0x00),
    (0x00, 0x00, 0x00),
    (0x00, 0x00, 0x00),
];

#[inline]
fn vga_to_rgb(vga_index: u8) -> u32 {
    let (r, g, b) = VGA_PALETTE[vga_index as usize];
    ((r as u32) << 16) | ((g as u32) << 8) | (b as u32)
}

fn display_keyboard(_window: &mut Window) {
    unsafe {
        // Draw background
        if draw_rect {
            fill_rect(KEYBOARD_POS_X - 5, KEYBOARD_POS_Y - 5, 650, 120, COL_CARD);
        }
        draw_text(KEYBOARD_POS_X, KEYBOARD_POS_Y, "KEYBOARD", 0xFFFFFF, COL_CARD);
        let mut y = KEYBOARD_POS_Y + 20; // Start below header
        for (i, &key) in KEYBOARD_BUFFER.iter().enumerate() {
            let x = KEYBOARD_POS_X + (i % 16) * 20; // 16 keys per row
            if i > 0 && i % 16 == 0 {
                y += 20; // Move to next row after 16 keys
            }
            let key_str = (key as char).to_string();
            draw_text(x, y, &key_str, 0x00FFFF, COL_CARD); // Cyan color for keys
        }
    }
}

fn display_flags(_window: &mut Window) {
    unsafe {
        if draw_rect {
            fill_rect(FLAGS_POS_X - 5, FLAGS_POS_Y - 5, 70, 170, COL_CARD);
        }
        let flags: [(&str, bool); 7] = [
            ("ZERO", ZERO_FLAG),
            ("CARRY", CARRY_FLAG),
            ("BORROW", BORROW_FLAG),
            ("PARITY", PARITY_FLAG),
            ("GREATER", GREATER_FLAG),
            ("LESSER", LESSER_FLAG),
            ("EQUAL", EQUAL_FLAG),
        ];

        draw_text(FLAGS_POS_X, FLAGS_POS_Y, "FLAGS", 0xFFFFFF, COL_CARD);

        for (i, (name, value)) in flags.iter().enumerate() {
            let mut x = FLAGS_POS_X;
            let y = FLAGS_POS_Y + (i * 20) + 20;

            draw_text(x, y, name, 0xFFFFFF, COL_CARD);
            x += 48;
            if *value {
                draw_text(x, y, "1", 0x00FF00, COL_CARD);
            } else {
                draw_text(x, y, "0", 0xFF0000, COL_CARD);
            }
        }
    }
}

fn display_registers(_window: &mut Window) {
    unsafe {
        if draw_rect {
            fill_rect(REGS_POS_X - 5, REGS_POS_Y - 5, 168, 110, COL_CARD);
        }   

        draw_text(REGS_POS_X+5, REGS_POS_Y+5, "REGISTERS", 0xFFFFFF, COL_CARD);
        let registers = [("A", REG_A), ("B", REG_B), ("C", REG_C), ("D", REG_D)];

        draw_text(REGS_POS_X+5, REGS_POS_Y+45, "HEX ", 0xFFFFFF, COL_CARD);
        draw_text(REGS_POS_X+5, REGS_POS_Y+65, "DEC ", 0xFFFFFF, COL_CARD);
        draw_text(REGS_POS_X+5, REGS_POS_Y+85, "CHR ", 0xFFFFFF, COL_CARD);

        for (i, (name, value)) in registers.iter().enumerate() {
            let mut x = REGS_POS_X + i * 32 + 40;
            let mut y = REGS_POS_Y + 20 + 5;

            // letters
            draw_text(x, y, name, 0xFFFFFF, COL_CARD);
            y += 20;
            let mut value_str = format!("0x{:02X}", value);
            draw_text(x, y, &value_str, 0x00FFFF, COL_CARD);
            // Decimal
            value_str = format!("{}", value);
            y += 20;
            draw_text(x, y, &value_str, 0x00FFFF, COL_CARD);
            // Char
            value_str = format!("'{:.1}'", if *value >= 32 && *value <= 126 {
                *value as char
            } else {
                '.'
            });
            y += 20;
            draw_text(x, y, &value_str, 0x00FFFF, COL_CARD);
        }
    }
}

fn display_opcode(_window: &mut Window, opcode: u8) {
    unsafe {
        if draw_rect {
            fill_rect(OPCODE_POS_X - 5, OPCODE_POS_Y - 5, 168, 20, COL_CARD);
        }

        draw_text(OPCODE_POS_X, OPCODE_POS_Y, "CURRENT OPCODE", 0xFFFFFF, COL_CARD);

        let opcode_str = format!("0x{:02X}", opcode);
        draw_text(OPCODE_POS_X + (6 * 22), OPCODE_POS_Y, &opcode_str, 0xFFFF00, COL_CARD);
    }
}

fn display_pc(_window: &mut Window, pc: usize) {
    unsafe {
        if draw_rect {
            fill_rect(OPADDR_POS_X - 5, OPADDR_POS_Y - 5, 168, 20, COL_CARD);
        }
        draw_text(OPADDR_POS_X, OPADDR_POS_Y, "PROGRAM COUNTER", 0xFFFFFF, COL_CARD);

        let pc_str = format!("{:08X}", pc);
        draw_text(OPADDR_POS_X + (6 * 19), OPADDR_POS_Y, &pc_str, 0xFFFF00, COL_CARD);
    }
}

fn display_stack(_window: &mut Window) {
    unsafe {
        if draw_rect {
            fill_rect(STACK_POS_X - 5, STACK_POS_Y - 5, 320, 640, COL_CARD);
        }
        
        draw_text(STACK_POS_X, STACK_POS_Y, "STACK", 0xFFFFFF, COL_CARD);
        let columns = 8;
        for (i, value) in STACK.iter().rev().enumerate() {
            let x = STACK_POS_X + (i % columns) * 80;
            let y = STACK_POS_Y + (i / columns) * 20 + 20;

            let value_str = format!("{:08X}", value);
            if Some(value) == STACK.last() {
                draw_text(x, y, &value_str, 0xFF0000, COL_CARD);
            } else {
                if i == 0 {
                    draw_text(x, y, &value_str, 0xFFFF00, COL_CARD);
                } else if i % 2 == 0 {
                    draw_text(x, y, &value_str, 0x0FFF00, COL_CARD);
                } else {
                    draw_text(x, y, &value_str, 0xF0FF00, COL_CARD);
                }
            }
        }
    }
}

fn display_terminal(_window: &mut Window) {
    let char_width = 8;
    let char_height = 12;
    let terminal_display_width = TERMINAL_WIDTH * char_width;
    let terminal_display_height = TERMINAL_HEIGHT * char_height;
    unsafe {
        if draw_rect {
            fill_rect(TERMINAL_POS_X - 5, TERMINAL_POS_Y - 5, terminal_display_width + 10, terminal_display_height + 30, COL_CARD);
        }
    
        draw_text(TERMINAL_POS_X, TERMINAL_POS_Y, "TERMINAL", 0xFFFFFF, COL_CARD);

        for row in 0..TERMINAL_HEIGHT {
            let base = row * TERMINAL_WIDTH;
            // build current line
            let line: &[char] = &TERMINAL_BUFFER[base..base + TERMINAL_WIDTH];
            // Only re-draw row if changed
            if line != &PREV_TERMINAL_BUFFER[base..base + TERMINAL_WIDTH] {
                let s: String = line.iter().collect();
                let x = TERMINAL_POS_X;
                let y = TERMINAL_POS_Y + (row * char_height) + 20;
                draw_text(x, y, s.trim_end(), COL_GREEN, COL_CARD);
                // shadow copy
                PREV_TERMINAL_BUFFER[base..base + TERMINAL_WIDTH]
                    .copy_from_slice(&TERMINAL_BUFFER[base..base + TERMINAL_WIDTH]);
            }

            // handle cursor advance/scroll logic stays unchanged
            if TERMINAL_X >= TERMINAL_WIDTH {
                TERMINAL_X = 0;
                TERMINAL_Y += 1;
            }
            if TERMINAL_Y >= TERMINAL_HEIGHT {
                TERMINAL_Y = 0;
                TERMINAL_BUFFER.copy_within(TERMINAL_WIDTH.., 0);
                let start = TERMINAL_WIDTH * (TERMINAL_HEIGHT - 1);
                TERMINAL_BUFFER[start..start + TERMINAL_WIDTH].fill(' ');
            }
        }

        let cursor_x = TERMINAL_POS_X + (TERMINAL_X * 6);
        let cursor_y = TERMINAL_POS_Y + (TERMINAL_Y * char_height);
        fill_rect(cursor_x, cursor_y + 10, 6, 2, COL_GREEN);
        // draw_text(cursor_x, cursor_y, "_", COL_GREEN);
    }
}

fn display_memory(_window: &mut Window, memory: &[u8], offset: usize) {
    let width = 16 * 32 + 128;
    let height = 16 * 20 + 20;
    unsafe {
        if draw_rect {
            fill_rect(MEM_POS_X - 5, MEM_POS_Y - 5, width + 10, height + 10, COL_CARD);
        }
    }

    let mem_display_start_x = MEM_POS_X;
    let mem_display_start_y = MEM_POS_Y;
    draw_text(mem_display_start_x, mem_display_start_y, "MEMORY", 0xFFFFFF, COL_CARD);

    for i in 0..16 {
        let y = mem_display_start_y + (i * 20) + 20;
        for j in 0..16 {
            let addr = offset + i * 16 + j;
            if addr >= memory.len() {
                continue;
            }
            let x = mem_display_start_x + (j * 32);

            if j == 0 {
                let addr_str = format!("{:04X}:", addr);
                draw_text(mem_display_start_x, y, &addr_str, 0xFF0000, COL_CARD);
            }

            let value_str = format!("{:02X}", memory[addr]);
            if offset == addr {
                draw_text(x + 128, y, &value_str, 0xFF0000, 0xFFFF00);
            } else {
                draw_text(x + 128, y, &value_str, 0x00FF00, COL_CARD);
            }
        }
    }

}

fn display_vram(_window: &mut Window, vram: &[u8], offset: usize) {
    let width = 16 * 32 + 128;
    let height = 16 * 20 + 20;
    unsafe {
        if draw_rect {
            fill_rect(VRAM_POS_X - 5, VRAM_POS_Y - 5, width + 10, height + 10, COL_CARD);
        }
    }

    let vram_display_start_x = VRAM_POS_X;
    let vram_display_start_y = VRAM_POS_Y;
    draw_text(vram_display_start_x, vram_display_start_y, "VRAM", 0xFFFFFF, COL_CARD);

    for i in 0..16 {
        let y = vram_display_start_y + (i * 20) + 20;
        for j in 0..16 {
            let addr = offset + i * 16 + j;
            if addr >= vram.len() {
                continue;
            }
            let x = vram_display_start_x + (j * 32);

            if j == 0 {
                let addr_str = format!("{:04X}:", addr);
                draw_text(vram_display_start_x, y, &addr_str, 0xFF0000, COL_CARD);
            }

            let value = format!("{:02X}", vram[addr]);
            if offset == addr {
                draw_text(x + 128, y, &value, 0xFF0000, 0xFFFF00);
            } else {
                draw_text(x + 128, y, &value, 0x00FF00, COL_CARD);
            }
        }
    }
}

#[inline]
fn set_reg(_reg: u8, opcode: u8, value: u8) {
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
        LDA | LDIA => unsafe {
            log(&format!("Loading {:#X} into REG_A", value), None);
            REG_A = value;
        },
        LDB | LDIB => unsafe {
            log(&format!("Loading {:#X} into REG_B", value), None);
            REG_B = value;
        },
        LDC | LDIC => unsafe {
            log(&format!("Loading {:#X} into REG_C", value), None);
            REG_C = value;
        },
        LDD | LDID => unsafe {
            log(&format!("Loading {:#X} into REG_D", value), None);
            REG_D = value;
        },
        BX => unsafe {
            log(&format!("Branching with return value {:#X}", value), None);
            REG_A = value;
        },
        _ => log(
            &format!("Error: Unsupported opcode for set_reg: {:#X}", opcode),
            None,
        ),
    }
}

fn load_file_to_memory<P: AsRef<Path>>(path: P) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
    let file = fs::File::open(path)?;
    let meta = file.metadata().ok();
    let mut reader = BufReader::new(file);
    let mut bytes: Vec<u8> = Vec::new();

    // Reserve approximate capacity to avoid repeated reallocations
    if let Some(m) = meta {
        // each hex byte is typically 2 chars + separator -> estimate /2
        bytes.reserve((m.len() / 2) as usize);
    }

    let mut line = String::new();
    while reader.read_line(&mut line)? != 0 {
        for hex_str in line.split_whitespace() {
            if hex_str.is_empty() {
                continue;
            }
            match u8::from_str_radix(hex_str, 16) {
                Ok(byte) => bytes.push(byte),
                Err(e) => {
                    log(
                        &format!("Error parsing hex value '{}': {}", hex_str, e),
                        None,
                    );
                    continue;
                }
            }
        }
        line.clear(); // reuse buffer for next line to keep memory low
    }

    log(&format!("Parsed {} hex values from file", bytes.len()), None);
    Ok(bytes)
}

#[inline]
fn read_offset(addr: usize, mem: &[u8]) -> usize {
    let a = mem[addr + 1] as usize;
    let b = mem[addr + 2] as usize;
    let c = mem[addr + 3] as usize;
    let d = mem[addr + 4] as usize;
    (a << 24) | (b << 16) | (c << 8) | d
}

#[inline]
fn push_stack(value: usize) {
    unsafe {
        STACK.push(value);
    }
}

#[inline]
fn pop_stack() -> usize {
    unsafe { STACK.pop().unwrap_or(0) }
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
    if let Some(args) = args {
        let formatted = format!("{}{}", message, args);
        let _ = append_to_textfile("emulator_log.txt", &formatted);
    } else {
        let _ = append_to_textfile("emulator_log.txt", message);
    }
}

#[inline]
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
    for c in value.to_string().chars() {
        embed_char_in_terminal(c);
    }
}

fn embed_char_in_terminal(c: char) {
    unsafe {
        match c {
            '\n' => {
                TERMINAL_X = 0;
                TERMINAL_Y += 1;
            }
            '\r' => {
                if TERMINAL_X > 0 {
                    TERMINAL_X -= 1;
                }
            }
            _ => {
                if TERMINAL_X < TERMINAL_WIDTH && TERMINAL_Y < TERMINAL_HEIGHT {
                    TERMINAL_BUFFER[TERMINAL_Y * TERMINAL_WIDTH + TERMINAL_X] = c;
                    TERMINAL_X += 1;
                }
            }
        }

        if TERMINAL_Y >= TERMINAL_HEIGHT {
            TERMINAL_BUFFER.copy_within(TERMINAL_WIDTH.., 0);
            let start = TERMINAL_WIDTH * (TERMINAL_HEIGHT - 1);
            TERMINAL_BUFFER[start..start + TERMINAL_WIDTH].fill(' ');
            TERMINAL_Y = TERMINAL_HEIGHT - 1;
        }
    }
}

// === 5x7 font ===
#[allow(dead_code)]
fn font_glyph(c: char) -> [u8; 5] {
    match c {
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
        _ => [0x00, 0x00, 0x00, 0x00, 0x00],
    }
}

#[inline]
fn put_px(x: usize, y: usize, rgb: u32) {
    let idx = y * WINDOW_WIDTH + x;
    unsafe {
        if rgb != WINDOW_BUFFER[idx] {
            WINDOW_BUFFER[idx..idx + 1].copy_from_slice(&[rgb]);// = rgb;
        }
    }
}

fn fill_rect(x: usize, y: usize, w: usize, h: usize, rgb: u32) {
    let max_size: usize = w * h;
    let fill_rect: Vec<u32> = vec![rgb; max_size];
    // copy from fill_rect to WINDOW_BUFFER
    let idx = y * WINDOW_WIDTH + x;
    unsafe {
        for i in 0..h {
            let start = idx + i * WINDOW_WIDTH;
            let end = start + w;
            if end <= WINDOW_BUFFER.len() {
                WINDOW_BUFFER[start..end].copy_from_slice(&fill_rect[i * w..(i + 1) * w]);
            }
        }
    }
}

fn draw_char(x: usize, y: usize, c: char, rgb: u32) {
    // Faster glyph rendering: iterate only set bits using bit-twiddling
    let glyph = font_glyph(c);
    for (col, &bits) in glyph.iter().enumerate() {
        let mut b = bits;
        while b != 0 {
            let row = b.trailing_zeros() as usize;
            // glyph height is 7; trailing_zeros on non-zero u8 will be < 8
            if row < 7 {
                put_px(x + col, y + row, rgb);
            }
            // clear lowest set bit
            b &= b - 1;
        }
    }
}


fn draw_text(mut x: usize, y: usize, s: &str, rgb: u32, blanking: u32) {
    // Clear background
    let text_width = s.len() * CHAR_ADV;
    fill_rect(x, y, text_width, TEXT_BG_H, blanking);
    for ch in s.chars() {
        draw_char(x, y, ch, rgb);
        x += CHAR_ADV;
    }
}

fn init_terminal_buffer() {
    unsafe {
        TERMINAL_BUFFER.fill(' ');
        PREV_TERMINAL_BUFFER.fill(' ');
        TERMINAL_X = 0;
        TERMINAL_Y = 0;
    }
}

// Simple stack hasher to detect content changes cheaply
fn stack_hash() -> u64 {
    unsafe {
        let mut h: u64 = 1469598103934665603; // FNV offset basis
        for v in STACK.iter() {
            h ^= *v as u64;
            h = h.wrapping_mul(1099511628211); // FNV prime
        }
        h ^ (STACK.len() as u64)
    }
}

// Map a subset of minifb::Key to ASCII (best-effort)
fn key_to_ascii(k: Key) -> Option<u8> {
    use Key::*;
    Some(match k {
        Space => b' ',
        Enter => b'\n',
        Backspace => 8,
        // numbers
        Key0 => b'0', Key1 => b'1', Key2 => b'2', Key3 => b'3', Key4 => b'4',
        Key5 => b'5', Key6 => b'6', Key7 => b'7', Key8 => b'8', Key9 => b'9',
        // letters (uppercase)
        A => b'A', B => b'B', C => b'C', D => b'D', E => b'E', F => b'F',
        G => b'G', H => b'H', I => b'I', J => b'J', K => b'K', L => b'L',
        M => b'M', N => b'N', O => b'O', P => b'P', Q => b'Q', R => b'R',
        S => b'S', T => b'T', U => b'U', V => b'V', W => b'W', X => b'X',
        Y => b'Y', Z => b'Z',
        // punctuation (minimal)
        Comma => b',', Period => b'.', Minus => b'-', Equal => b'=',
        Semicolon => b';', Quote => b'\'', Slash => b'/', Backslash => b'\\',
        LBracket => b'[', RBracket => b']',
        _ => return None,
    })
}

fn page_base_256(addr: usize) -> usize {
    addr & 0xFFFFFF00
}

fn refresh(window: &mut Window, bytes: &[u8], opcode: u8, addr: usize, x: usize, y: usize, video_buffer: &[u8]) {
    // simple debounce parameters (ms)
    const DEBOUNCE_MS: u128 = 150;
    // persistent debounce state (kept in function scope as statics)
    static mut LAST_KEY_ASCII: u8 = 0;

    // Non-blocking key polling into keyboard buffer with debounce
    let keys = window.get_keys_pressed(minifb::KeyRepeat::No);
    for k in keys {
        // current time in ms
        let now_ms = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_millis();

        // simple debounce check
        if let Some(ascii) = key_to_ascii(k) {
            unsafe {
                        // map to ascii where possible
                let ch = key_to_ascii(k);

                
                match k {
                    Key::Escape => {
                        // immediate exit (no debounce)
                        std::process::exit(0);
                    }
                    Key::F12 => {
                        // toggle full screen (debounced)
                        if now_ms.wrapping_sub(LAST_KEY_TS) > DEBOUNCE_MS {
                            full_screen = !full_screen;
                            LAST_KEY_TS = now_ms;
                            LAST_KEY_ASCII = 0;
                            let str = &format!("{:>32}", if full_screen { "Full Screen" } else { "   Windowed   " });
                            draw_text(10, 10, str, 0xFFFFFF, COL_CARD);
                            update_window(window);
                        }
                    }
                    Key::F11 => {
                        if now_ms.wrapping_sub(LAST_KEY_TS) > DEBOUNCE_MS {
                            reset = true;
                            LAST_KEY_TS = now_ms;
                            LAST_KEY_ASCII = 0;
                            let str = &format!("{:>32}", "   Resetting   ");
                            draw_text(10, 10, str, 0xFFFFFF, COL_CARD);
                            update_window(window);
                        }
                    }
                    Key::F10 => {
                        if now_ms.wrapping_sub(LAST_KEY_TS) > DEBOUNCE_MS {
                            video_speed_factor = (video_speed_factor % 5) + 1;
                            LAST_KEY_TS = now_ms;
                            LAST_KEY_ASCII = 0;
                            let speed_msg = format!("Speed x{}", video_speed_factor);
                            draw_text(10, 10, &speed_msg, 0xFFFFFF, COL_CARD);
                            update_window(window);
                        }
                    }
                    Key::F9 => {
                        if now_ms.wrapping_sub(LAST_KEY_TS) > DEBOUNCE_MS {
                            paused = !paused;
                            LAST_KEY_TS = now_ms;
                            LAST_KEY_ASCII = 0;
                            let str = &format!("{:>32}", if paused { "   Paused   " } else { "   Running   " });
                            draw_text(10, 10, str, 0xFFFFFF, COL_CARD);
                            update_window(window);
                        }
                    }
                    Key::F8 => {
                        if now_ms.wrapping_sub(LAST_KEY_TS) > DEBOUNCE_MS {
                            step = true;
                            LAST_KEY_TS = now_ms;
                            LAST_KEY_ASCII = 0;
                            let str = &format!("{:>32}", "   Step   ");
                            draw_text(10, 10, str, 0xFFFFFF, COL_CARD);
                            update_window(window);
                        }
                    }
                    _ => {
                        if let Some(c) = ch {
                            // Accept if different key than last or enough time has passed
                            if c != LAST_KEY_ASCII || now_ms.wrapping_sub(LAST_KEY_TS) > DEBOUNCE_MS {
                                KEYBOARD_BUFFER.push(c);
                                LAST_KEY_ASCII = c;
                                LAST_KEY_TS = now_ms;
                            }
                        }
                    }
                }
            }
        }
    }

    unsafe {
        // Flags card (only if changed)
        let flags_now = (ZERO_FLAG, CARRY_FLAG, BORROW_FLAG, PARITY_FLAG, GREATER_FLAG, LESSER_FLAG, EQUAL_FLAG);
        if flags_now != PREV_FLAGS || draw_rect {
            display_flags(window);
            PREV_FLAGS = flags_now;
        }

        // Stack card (check length + hash)
        let st_len = STACK.len();
        let st_hash = stack_hash();
        if st_len != PREV_STACK_LEN || st_hash != PREV_STACK_HASH || draw_rect {
            display_stack(window);
            PREV_STACK_LEN = st_len;
            PREV_STACK_HASH = st_hash;
        }

        // Registers card
        let regs_now = [REG_A, REG_B, REG_C, REG_D];
        if regs_now != PREV_REGS || draw_rect {
            display_registers(window);
            PREV_REGS = regs_now;
        }

        // Opcode/PC cards
        if opcode != PREV_OPCODE || draw_rect {
            display_opcode(window, opcode);
            PREV_OPCODE = opcode;
        }
        if addr != PREV_PC || draw_rect {
            display_pc(window, addr);
            PREV_PC = addr;
        }

        // Terminal card does row-diff itself
        display_terminal(window);

        // Memory page optimization: redraw only when page changes
        let mem_page = page_base_256(addr.min(bytes.len().saturating_sub(1)));
        if mem_page != LAST_MEM_PAGE_BASE || draw_rect {
            display_memory(window, bytes, mem_page);
            LAST_MEM_PAGE_BASE = mem_page;
        }

        // VRAM page optimization: track current pixel position
        let cur_px = (y * WIDTH + x) % (WIDTH * HEIGHT);
        let vram_page = page_base_256(cur_px);
        if vram_page != LAST_VRAM_PAGE_BASE || draw_rect {
            display_vram(window, video_buffer, vram_page);
            LAST_VRAM_PAGE_BASE = vram_page;
        }

        // Keyboard card (only if new input)
        if KEYBOARD_BUFFER.len() != PREV_KB_LEN || draw_rect {
            display_keyboard(window);
            PREV_KB_LEN = KEYBOARD_BUFFER.len();
        }
    }
}

fn main() -> Result<(), Box<dyn std::error::Error>> {
    log(&format!("Starting Libre8 Emulator..."), None);

    init_terminal_buffer();

    let mut bytes = load_file_to_memory("D:/l8rust/pixels_output.txt")?;
    //let mut bytes = load_file_to_memory("./output/bin.hex")?;
    let num_bytes = bytes.len();
    log(&format!("Loaded {} bytes into memory.", num_bytes), None);

    let mut child = Command::new("powershell.exe")
        .args(["-NoLogo", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", "-"])
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()?;

    let mut stdin = child.stdin.take().expect("stdin piped");
    let stdout = child.stdout.take().expect("stdout piped");
    let stderr = child.stderr.take().expect("stderr piped");

    let out_handle = thread::spawn(move || {
        let reader = BufReader::new(stdout);
        for line in reader.lines() {
            log(&format!("[STDOUT] {}", line.unwrap_or_default()), None);
        }
    });

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
    // std::io::stdin().read_line(&mut input)?;
    let mut video_buffer: [u8; WIDTH * HEIGHT] = [0x00; WIDTH * HEIGHT];
    //let mut FRAME_BUFFER: [u32; WIDTH * HEIGHT] = [COL_GRID; WIDTH * HEIGHT];

    fill_rect(
        DISPLAY_POS_X - 5,
        DISPLAY_POS_Y - 5,
        WIDTH * 2 + 10,
        HEIGHT * 2 + 10,
        COL_CARD,
    );
    refresh(&mut window, &bytes, 0x00, addr, x, y, &video_buffer);
    unsafe { draw_rect = false; }
    let mut timer = std::time::Instant::now();
    let mut HZ_COUNTER: f64 = 0.0;
    while addr < num_bytes {
        let opcode = bytes[addr];
        let prev_addr = addr;
        unsafe {
            if reset {
                addr = 0;
                REG_A = 0;
                REG_B = 0;
                REG_C = 0;
                REG_D = 0;
                video_speed_factor = 1;
                full_screen = false;
                draw_rect = false;
                x = 0;
                y = 0;
                STACK.clear();
                KEYBOARD_BUFFER.clear();
                init_terminal_buffer();
                fill_rect(
                    DISPLAY_POS_X - 5,
                    DISPLAY_POS_Y - 5,
                    WIDTH * 2 + 10,
                    HEIGHT * 2 + 10,
                    COL_CARD,
                );
                refresh(&mut window, &bytes, 0x00, addr, x, y, &video_buffer);
                update_window(&mut window);
                log(&format!("System reset performed."), None);
                reset = false;
            }

            if paused {
                log(&format!("Execution paused. Press F9 to resume."), None);
            } 
            while paused {                
                refresh(&mut window, &bytes, opcode, addr, x, y, &video_buffer);
                update_window(&mut window);
                std::thread::sleep(std::time::Duration::from_millis(50));
                if step {
                    paused = false;
                }
                if !paused {
                    log(&format!("Execution resumed."), None);
                }

            }

            if step {
                paused = true;
                step = false;
            }

            if full_screen {
                //window.set_fullscreen(true);
            } else {
                //window.set_fullscreen(false);
            }
        }
        
        match opcode {
            POKE => {
                // Blit up to 63 pixels (scaled) with bounds checks
                for i in 1..64 {
                    let p = addr + i;
                    if p >= num_bytes { break; }
                    if x < WIDTH && y < HEIGHT {
                        let vga_color = bytes[p];
                        let pixel = vga_to_rgb(vga_color);

                        // Compute destination (scaled)
                        let wx = x * SCALE;
                        let wy = y * SCALE;
                        let top_left = DISPLAY_ORIGIN + wy * WINDOW_WIDTH + wx;

                        unsafe {
                            if (DISPLAY_POS_X + wx) + (SCALE - 1) < WINDOW_WIDTH &&
                               (DISPLAY_POS_Y + wy) + (SCALE - 1) < WINDOW_HEIGHT {
                                // 2x2 write (SCALE)
                                WINDOW_BUFFER[top_left] = pixel;
                                WINDOW_BUFFER[top_left + 1] = pixel;
                                WINDOW_BUFFER[top_left + WINDOW_WIDTH] = pixel;
                                WINDOW_BUFFER[top_left + WINDOW_WIDTH + 1] = pixel;
                            }
                        }
                        video_buffer[y * WIDTH + x] = vga_color;
                    }
                    // advance display cursor
                    x += 1;
                    if x >= WIDTH {
                        x = 0;
                        y += 1;
                        if y >= HEIGHT { y = 0; }
                    }
                }
                addr += 64;
            }

            HLT => {
                log(
                    &format!("{}{}", "HLT encountered at address {}. Stopping execution.", addr),
                    None,
                );
                break;
            }

            OUTA | OUTB | OUTC | OUTD => {
                match opcode {
                    OUTA => unsafe {
                        print!("{}", REG_A as char);
                        embed_char_in_terminal(REG_A as char);
                    },
                    OUTB => unsafe {
                        let s = format!("0x{:02X}", REG_A as u8);
                        print!("{}", s);
                        for c in s.chars() {
                            embed_char_in_terminal(c);
                        }
                    },
                    OUTC => unsafe {
                        print!("{}", REG_A);
                        embed_char_in_terminal(REG_A as char);
                    },
                    OUTD => unsafe {
                        let s = format!("{}", REG_A as u8);
                        print!("{}", s);
                        for c in s.chars() {
                            embed_char_in_terminal(c);
                        }
                    },
                    _ => unsafe {
                        print!("{}", REG_A as char);
                        embed_char_in_terminal(REG_A as char);
                    },
                }
                addr += 1;
            }

            LDA | LDB | LDC | LDD => {
                let prev_addr = addr;
                let mem_addr = read_offset(addr, &bytes);
                log(
                    &format!("Loading value from memory address {:#X}", mem_addr),
                    None,
                );
                let value = bytes[mem_addr];
                unsafe {
                    set_reg(REG_A, opcode, value);
                }
                addr = prev_addr + 5;
            }

            ADD | SUB | MUL | DIV => {
                let prev_addr = addr;
                let mem_addr = read_offset(addr, &bytes);
                log(
                    &format!(
                        "Performing ALU operation with value from memory address {:#X}",
                        mem_addr
                    ),
                    None,
                );
                let operand = bytes[mem_addr];
                set_flags(operand);
                unsafe {
                    set_reg(REG_A, opcode, operand);
                }
                addr = prev_addr + 5;
            }

            B => {
                let target_addr = read_offset(addr, &bytes);
                log(
                    &format!("Unconditional branch to address {:#X}", target_addr),
                    None,
                );
                addr += 5;
                log(&format!("Pushing return address {:#X} onto stack", addr), None);
                push_stack(addr);
                addr = target_addr;
            }

            BC | BNC | BZ | BNZ | BNB | BP | BNP | BB => {
                log(
                    &format!(
                        "Evaluating conditional branch for opcode {:#X} at address {:#X}",
                        opcode, addr
                    ),
                    None,
                );
                let target_addr = read_offset(addr, &bytes);
                log(
                    &format!("Conditional branch to address {:#X} if condition met", target_addr),
                    None,
                );
                let branch = match opcode {
                    BC => unsafe { CARRY_FLAG },
                    BNC => unsafe { !CARRY_FLAG },
                    BZ => unsafe { ZERO_FLAG },
                    BNZ => unsafe { !ZERO_FLAG },
                    BNB => unsafe { !BORROW_FLAG },
                    BP => unsafe { PARITY_FLAG },
                    BNP => unsafe { !PARITY_FLAG },
                    BB => unsafe { BORROW_FLAG },
                    _ => false,
                };
                if branch {
                    log(
                        &format!(
                            "Branch condition met. Branching to address {:#X}",
                            target_addr
                        ),
                        None,
                    );
                    addr += 5;
                    log(&format!("Pushing return address {:#X} onto stack", addr), None);
                    push_stack(addr);
                    addr = target_addr;
                } else {
                    addr += 5;
                }
            }

            BX => {
                log(
                    &format!("Returning from branch with BX at address {:#X}", addr),
                    None,
                               );
                let return_value_address = read_offset(addr, &bytes);
                unsafe {
                    set_reg(REG_A, opcode, bytes[return_value_address]);
                }
                addr = pop_stack();
                log(
                    &format!("Returning from branch to address {:#X}", addr),
                    None,
                );
            }

            JMP => {
                addr = read_offset(addr, &bytes);
                log(
                    &format!("Unconditional jump to address {:#X}", addr),
                    None,
                );
            }

            JZ | JNZ | JC | JNC | JNB | JB | JP | JNP => {
                let target_addr = read_offset(addr, &bytes);
                log(
                    &format!("Conditional jump to address {:#X} if condition met", target_addr),
                    None,
                );
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
                    addr += 5;
                }
            }

            STA | STB | STC | STD => {
                let prev_addr = addr;
                let mem_addr = read_offset(addr, &bytes);
                log(
                    &format!("Storing register value to memory address {:#X}", mem_addr),
                    None,
                );
                match opcode {
                    STA => unsafe { bytes[mem_addr] = REG_A },
                    STB => unsafe { bytes[mem_addr] = REG_B },
                    STC => unsafe { bytes[mem_addr] = REG_C },
                    STD => unsafe { bytes[mem_addr] = REG_D },
                    _ => unsafe { bytes[mem_addr] = REG_A },
                }
                addr = prev_addr + 5;
            }

            IADD | ISUB | IMUL | IDIV => {
                addr += 1;
                let operand = bytes[addr];
                log(&format!("Performing {:?} with operand {:#X}", opcode, operand), None);
                set_flags(operand);
                unsafe {
                    set_reg(REG_A, opcode, operand);
                }
                addr += 1;
            }

            LDI | LDIA | LDIB | LDIC | LDID => {
                addr += 1;
                let value = bytes[addr];
                log(
                    &format!("Loading immediate value {:#X} into register", value),
                    None,
                );
                unsafe {
                    set_reg(REG_A, opcode, value);
                }
                addr += 1;
            }

            DEC | DECE => {
                log(&format!("Waiting for keyboard input..."), None);
                match opcode {
                    DEC => {
                        unsafe {
                            if !KEYBOARD_BUFFER.is_empty() {
                                let _k = KEYBOARD_BUFFER.remove(0);
                                REG_A = _k as u8;
                            }
                        }
                    }
                    DECE => {
                        unsafe {
                            if !KEYBOARD_BUFFER.is_empty() {
                                let _k = KEYBOARD_BUFFER.remove(0);
                                REG_A = _k as u8;
                                embed_char_in_terminal(_k as char);
                            }
                        }
                    }
                    _ => {}
                }
                addr += 1;
            }

            NOP => {
                addr += 1;
            }

            _ => {
                // Unrecognized opcode: keep behavior (no-op)
                addr += 1;
            }
        }

        unsafe {
        
            HZ_COUNTER = HZ_COUNTER + 1.0;
            if timer.elapsed() >= Duration::from_secs(1) {
                let freq = HZ_COUNTER * video_speed_factor as f64 / timer.elapsed().as_secs_f64();
                HZ_COUNTER = 0.0;
                timer = std::time::Instant::now();
                draw_text(WINDOW_WIDTH - 200, 10, &format!("Hz: {:.3}", freq), 0xFFFFFF, COL_CARD);
            } 

            // Update display
            refresh(&mut window, &bytes, opcode, addr, x, y, &video_buffer);
        
            if opcode == POKE {
                    if addr % (WIDTH * HEIGHT * video_speed_factor) == 0 {   
                        update_window(&mut window);
                    }
            } else {
                update_window(&mut window);
                // avoid busy-wait; keep responsiveness
                while window.is_open() && !window.is_key_down(Key::F9) {
                    std::thread::sleep(Duration::from_millis(1));
                }
            }
        }
    }

    stdin.flush()?;
    drop(stdin);

    let status = child.wait()?;
    out_handle.join().ok();
    err_handle.join().ok();

    log(&format!("Libre8 Emulator finished with status: {}", status), None);
    Ok(())
}
