#![allow(static_mut_refs)]
#![allow(deprecated)]
#![allow(unused_unsafe)]

mod opcodes;
mod renderer;
mod state;
mod ui;

use crate::opcodes::*;
use crate::renderer::*;
use crate::state::EmulatorState;
use crate::ui::*;

use anyhow::Result;
use minifb::{Window, WindowOptions};
use std::fs;
use std::io::{BufRead, BufReader, Write};
use std::path::Path;
use std::process::{Command, Stdio};
use std::thread;

pub struct Emulator;

const DEFAULT_RAM_SIZE: usize = 4 * 1024 * 1024; // 4 MiB addressable RAM image
const DATA_STACK_GUARD: u32 = 0x100; // reserve low RAM for MMIO/zero page
#[inline]
pub fn set_reg(state: &mut EmulatorState,value: u8) {
    match state.current_opcode {
        ADD | IADD | SUB | ISUB | MUL | IMUL | DIV | IDIV => {
            set_flags(state, value);            
        }
        _ => {}
    }
    match state.current_opcode {
        ADD => {
            log(&format!("Adding {:#X} to REG_A", value), None);
            let new_val = state.reg_a.wrapping_add(value);
            state.reg_a = new_val;
        }
        SUB => {
            log(&format!("Subtracting {:#X} from REG_A", value), None);
            let new_val = state.reg_a.wrapping_sub(value);
            state.reg_a = new_val;
        }
        MUL => {
            log(&format!("Multiplying REG_A by {:#X}", value), None);
            let new_val = state.reg_a.wrapping_mul(value);
            state.reg_a = new_val;
        }
        DIV => {
            log(&format!("Dividing REG_A by {:#X}", value), None);
            let new_val = state.reg_a.wrapping_div(value);
            state.reg_a = new_val;
        }
        IADD => {
            log(&format!("Adding {:#X} to REG_A", value), None);
            let new_val = state.reg_a.wrapping_add(value);
            state.reg_a = new_val;
        }
        ISUB => {
            log(&format!("Subtracting {:#X} from REG_A", value), None);
            let new_val = state.reg_a.wrapping_sub(value);
            state.reg_a = new_val;
        }
        IMUL => {
            log(&format!("Multiplying REG_A by {:#X}", value), None);
            let new_val = state.reg_a.wrapping_mul(value);
            state.reg_a = new_val;
        }
        IDIV => {
            log(&format!("Dividing REG_A by {:#X}", value), None);
            let new_val = state.reg_a.wrapping_div(value);
            state.reg_a = new_val;
        }
        OUTA => {
            log(&format!("Printing OUTA: {}", value), None);
            state.reg_a = value;
        }
        OUTB => {
            log(&format!("Printing OUTB: {}", value), None);
            state.reg_b = value;
        }
        OUTC => {
            log(&format!("Printing OUTC: {}", value), None);
            state.reg_c = value;
        }
        OUTD => {
            log(&format!("Printing OUTD: {}", value), None);
            state.reg_d = value;
        }
        LDI | LDA | LDIA => {
            log(&format!("Loading {:#X} into REG_A", value), None);
            state.reg_a = value;
        }
        LDB | LDIB => {
            log(&format!("Loading {:#X} into REG_B", value), None);
            state.reg_b = value;
        }
        LDC | LDIC => {
            log(&format!("Loading {:#X} into REG_C", value), None);
            state.reg_c = value;
        }
        LDD | LDID => {
            log(&format!("Loading {:#X} into REG_D", value), None);
            state.reg_d = value;
        }
        BX => {
            log(&format!("Branching with return value {:#X}", value), None);
            state.reg_a = value;
        }
        _ => log(
            &format!("Error: Unsupported opcode for set_reg: {:#X}", state.current_opcode),
            None,
        ),
    }
    // Sync changes to global state for compatibility with existing renderer
}

pub fn load_file_to_memory<P: AsRef<Path>>(path: P) -> Result<(Vec<u8>, usize), Box<dyn std::error::Error>> {
    let file = fs::File::open(path)?;
    let meta = file.metadata().ok();
    let mut reader = BufReader::new(file);
    let mut ram_memory: Vec<u8> = Vec::new();

    // Reserve approximate capacity to avoid repeated reallocations
    if let Some(m) = meta {
        // each hex byte is typically 2 chars + separator -> estimate /2
        ram_memory.reserve((m.len() / 2) as usize);
    }

    let mut line = String::new();
    while reader.read_line(&mut line)? != 0 {
        for hex_str in line.split_whitespace() {
            if hex_str.is_empty() {
                continue;
            }
            match u8::from_str_radix(hex_str, 16) {
                Ok(byte) => ram_memory.push(byte),
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

    let program_len = ram_memory.len();
    log(
        &format!("Parsed {} hex values from file", program_len),
        None,
    );
    Ok((ram_memory, program_len))
}

#[inline]
pub fn read_offset(addr: usize, mem: &[u8]) -> usize {
    if addr + 4 >= mem.len() {
        return 0; // Return 0 if we can't read 4 bytes
    }
    let a = mem[addr + 1] as usize;
    let b = mem[addr + 2] as usize;
    let c = mem[addr + 3] as usize;
    let d = mem[addr + 4] as usize;
    (a << 24) | (b << 16) | (c << 8) | d
}

#[inline]
pub fn push_stack(state: &mut EmulatorState) {
    state.push_stack(state.addr);
}

#[inline]
pub fn pop_stack(state: &mut EmulatorState) -> usize {
    state.pop_stack()
}

pub fn set_flags(state: &mut EmulatorState, operand: u8) {
    state.set_flags(operand);
}

pub fn append_to_textfile(path: &str, content: &str) -> std::io::Result<()> {
    let mut file = std::fs::OpenOptions::new()
        .create(true)
        .append(true)
        .open(path)?;
    writeln!(file, "{}", content)?;
    Ok(())
}

pub fn log(message: &str, args: Option<std::fmt::Arguments>) {
    if let Some(args) = args {
        let formatted = format!("{}{}", message, args);
        let _ = append_to_textfile("emulator_log.txt", &formatted);
    } else {
        let _ = append_to_textfile("emulator_log.txt", message);
    }
}

pub fn embed_int_in_terminal(state: &mut EmulatorState, value: u8) {
    state.embed_int_in_terminal(value);
}

pub fn embed_char_in_terminal(state: &mut EmulatorState, c: char) {
    state.embed_char_in_terminal(c);
}

pub fn init_terminal_buffer(state: &mut EmulatorState) {
    state.init_terminal_buffer();
}

fn read_byte(state: &EmulatorState, address: usize) -> u8 {
    if address < state.ram_memory.len() {
        state.ram_memory[address]
    } else {
        log(
            &format!("Read out of bounds at {:#X} (len={})", address, state.ram_memory.len()),
            None,
        );
        0
    }
}

fn write_byte(state: &mut EmulatorState, address: usize, value: u8) {
    if address < state.ram_memory.len() {
        state.ram_memory[address] = value;
    } else {
        log(
            &format!(
                "Write out of bounds ignored at {:#X} (len={})",
                address,
                state.ram_memory.len()
            ),
            None,
        );
    }
}

fn read_u32_le(state: &EmulatorState, address: usize) -> u32 {
    let mut value: u32 = 0;
    for i in 0..4 {
        let byte = read_byte(state, address + i) as u32;
        value |= byte << (i * 8);
    }
    value
}

fn write_u32_le(state: &mut EmulatorState, address: usize, value: u32) {
    for (i, byte) in value.to_le_bytes().iter().enumerate() {
        write_byte(state, address + i, *byte);
    }
}

fn push_data_byte(state: &mut EmulatorState, value: u8) {
    if state.sp <= state.data_stack_min {
        log("Data stack overflow", None);
        return;
    }
    state.sp = state.sp.wrapping_sub(1);
    write_byte(state, state.sp as usize, value);
}

fn pop_data_byte(state: &mut EmulatorState) -> u8 {
    if state.sp >= state.stack_base {
        log("Data stack underflow", None);
        return 0;
    }
    let value = read_byte(state, state.sp as usize);
    state.sp = state.sp.wrapping_add(1);
    value
}

fn main() -> Result<(), Box<dyn std::error::Error>> {
    log("Starting Libre8 Emulator...", None);

    // Removed PowerShell process spawning and related threads to prevent hanging.

    let mut state = EmulatorState::new();
    init_terminal_buffer(&mut state);

    let (mut ram_image, program_len) = load_file_to_memory("bin.hex")?;

    state.ram_memory = ram_image;
    state.program_len = program_len;
    state.stack_base = state.ram_memory.len() as u32;
    state.data_stack_min = DATA_STACK_GUARD;
    state.sp = state.stack_base;
    state.bp = state.stack_base;
    state.di = 0;
    //state.ram_memory = load_file_to_memory("D:/l8rust/pixels_output.txt")?;
    state.keyboard_buffer.push(b' ');
    let num_bytes = state.program_len;
    log(&format!("Loaded {} bytes into memory.", num_bytes), None);

    let mut window = Window::new(
        "Libre8 Pixel Display",
        WINDOW_WIDTH,
        WINDOW_HEIGHT,
        WindowOptions {
            scale: minifb::Scale::X1,
            ..WindowOptions::default()
        },
    )?;

    fill_rect(
        DISPLAY_POS_X - 5,
        DISPLAY_POS_Y - 5,
        WIDTH * 2 + 10,
        HEIGHT * 2 + 10,
        COL_BG,
        &mut state,
    );

    refresh_update(&mut window, &mut state);
    
    // Optimization: Track instruction count for batched refreshes
    let mut instruction_count = 0;

    while state.addr < num_bytes {
        state.current_opcode = state.ram_memory[state.addr];
        let prev_addr = state.addr;
        {
            if state.reset {
                state.video_speed_factor = 1;
                state.full_screen = false;
                state.draw_rect = false;

                state.addr = 0;
                state.reg_a = 0;
                state.reg_b = 0;
                state.reg_c = 0;
                state.reg_d = 0;
                state.x = 0;
                state.y = 0;
                state.stack.clear();
                state.keyboard_buffer.clear();
                state.keyboard_buffer.push(b' ');
                state.terminal_buffer.fill(' ');
                state.prev_terminal_buffer.fill(' ');
                init_terminal_buffer(&mut state);
                fill_rect(
                    DISPLAY_POS_X - 5,
                    DISPLAY_POS_Y - 5,
                    WIDTH * 2 + 10,
                    HEIGHT * 2 + 10,
                    COL_CARD,
                    &mut state,
                );
                refresh_update(&mut window, &mut state);
                log("System RESET performed.", None);
                state.reset = false;
            }

            if state.paused {
                log("Execution PAUSED. Press F9 to resume.", None);
            }
            while state.paused {
                refresh_update(&mut window, &mut state);
                if state.step {
                    state.paused = false;
                }
                if !state.paused {
                    log("Execution resumed.", None);
                }
            }

            if state.step {
                state.paused = true;
                state.step = false;
            }

            if state.full_screen {
                //window.set_fullscreen(true);
            } else {
                //window.set_fullscreen(false);
            }
        }

        match state.current_opcode {
            POKE => {
                // Blit up to 63 pixels (scaled) with bounds checks
                for i in 1..64 {
                    let p = state.addr + i;
                    if p >= num_bytes {
                        break;
                    }
                    if state.x < WIDTH && state.y < HEIGHT {
                        let vga_color = state.ram_memory[p];
                        let pixel = vga_to_rgb(vga_color);

                        // Compute destination (scaled)
                        let wx = state.x * SCALE;
                        let wy = state.y * SCALE;
                        let top_left = DISPLAY_ORIGIN + wy * WINDOW_WIDTH + wx;

                        unsafe {
                            if (DISPLAY_POS_X + wx) + (SCALE - 1) < WINDOW_WIDTH
                                && (DISPLAY_POS_Y + wy) + (SCALE - 1) < WINDOW_HEIGHT
                            {
                                // 2x2 write (SCALE)
                                state.window_buffer[top_left] = pixel;
                                state.window_buffer[top_left + 1] = pixel;
                                state.window_buffer[top_left + WINDOW_WIDTH] = pixel;
                                state.window_buffer[top_left + WINDOW_WIDTH + 1] = pixel;
                                state.video_buffer[state.y * WIDTH + state.x] = vga_color;
                            }

                            // Optimize: Only refresh when paused and every 16 pixels for better performance
                            /*
                            let old_addr = state.addr;
                            state.addr = p;
                            refresh_update(&mut window, &mut state);
                            state.addr = old_addr;
                            */
                            
                        }
                    }
                    // advance display cursor
                    state.x += 1;
                    if state.x >= WIDTH {
                        state.x = 0;
                        state.y += 1;
                        if state.y >= HEIGHT {
                            state.y = 0;
                        }
                    }
                }
                if !state.paused {
                    state.addr += 64;
                }
                // NOTE: no per-increment refresh/update here (POKE excluded)
            }

            POKX | POKY | PXYD => {
                let operand_addr = state.addr + 1;
                if operand_addr >= num_bytes {
                    log(
                        &format!(
                            "GPU opcode at {:#X} missing operand; advancing to {:#X}",
                            state.addr, operand_addr
                        ),
                        None,
                    );
                    state.addr = operand_addr;
                    continue;
                }
                let value = state.ram_memory[operand_addr];
                match state.current_opcode {
                    POKX => {
                        state.x = (value as usize) % WIDTH;
                        log(&format!("Setting X coordinate to {}", state.x), None);
                    }
                    POKY => {
                        state.y = (value as usize) % HEIGHT;
                        log(&format!("Setting Y coordinate to {}", state.y), None);
                    }
                    PXYD => {
                        if state.x < WIDTH && state.y < HEIGHT {
                            let vga_color = value;
                            let pixel = vga_to_rgb(vga_color);
                            let wx = state.x * SCALE;
                            let wy = state.y * SCALE;
                            let top_left = DISPLAY_ORIGIN + wy * WINDOW_WIDTH + wx;
                            if (DISPLAY_POS_X + wx) + (SCALE - 1) < WINDOW_WIDTH
                                && (DISPLAY_POS_Y + wy) + (SCALE - 1) < WINDOW_HEIGHT
                            {
                                state.window_buffer[top_left] = pixel;
                                state.window_buffer[top_left + 1] = pixel;
                                state.window_buffer[top_left + WINDOW_WIDTH] = pixel;
                                state.window_buffer[top_left + WINDOW_WIDTH + 1] = pixel;
                            }
                            state.video_buffer[state.y * WIDTH + state.x] = vga_color;
                            log(
                                &format!(
                                    "Setting pixel ({}, {}) to {:#X}",
                                    state.x, state.y, vga_color
                                ),
                                None,
                            );
                        } else {
                            log(
                                &format!(
                                    "Ignoring PXYD outside viewport at ({}, {})",
                                    state.x, state.y
                                ),
                                None,
                            );
                        }
                    }
                    _ => {}
                }
                state.addr = operand_addr + 1;
                // Removed immediate refresh for POKX/POKY/PXYD - will be handled by batch refresh
            }

            HLT => {
                log(
                    &format!(
                        "{}{}",
                        "HLT encountered at address {}. Stopping execution.", state.addr
                    ),
                    None,
                );
                refresh_update(&mut window, &mut state); // Final refresh before exit
                break;
            }

            OUTA | OUTB | OUTC | OUTD => {
                match state.current_opcode {
                    OUTA => {
                        let reg_a_val = state.reg_a;
                        print!("{}", reg_a_val as char);
                        embed_char_in_terminal(&mut state, reg_a_val as char);
                    }
                    OUTB => {
                        let reg_a_val = state.reg_a;
                        let s = format!("0x{:02X}", { reg_a_val });
                        print!("{}", s);
                        for c in s.chars() {
                            embed_char_in_terminal(&mut state, c);
                        }
                    }
                    OUTC => {
                        let reg_a_val = state.reg_a;
                        print!("{}", reg_a_val);
                        embed_char_in_terminal(&mut state, reg_a_val as char);
                    }
                    OUTD => {
                        let reg_a_val = state.reg_a;
                        let s = format!("{}", reg_a_val);
                        print!("{}", s);
                        for c in s.chars() {
                            embed_char_in_terminal(&mut state, c);
                        }
                    }
                    _ => {
                        let reg_a_val = state.reg_a;
                        print!("{}", reg_a_val as char);
                        embed_char_in_terminal(&mut state, reg_a_val as char);
                    }
                }
                state.addr += 1;
                // Removed immediate refresh for OUTA/OUTB/OUTC/OUTD - will be handled by batch refresh
            }

            LDA | LDB | LDC | LDD => {
                let prev_addr = state.addr;
                let mem_addr = read_offset(state.addr, &state.ram_memory);
                log(
                    &format!("Loading value from memory address {:#X}", mem_addr),
                    None,
                );
                let value = if mem_addr < state.ram_memory.len() {
                    state.ram_memory[mem_addr]
                } else {
                    log(
                        &format!(
                            "Memory address {:#X} out of bounds (max: {})",
                            mem_addr,
                            state.ram_memory.len()
                        ),
                        None,
                    );
                    0
                };
                set_reg(&mut state, value);
                state.addr = prev_addr + 5;
                // Removed immediate refresh for LDA/LDB/LDC/LDD - will be handled by batch refresh
            }

            ADD | SUB | MUL | DIV => {
                let prev_addr = state.addr;
                let mem_addr = read_offset(state.addr, &state.ram_memory);
                log(
                    &format!(
                        "Performing ALU operation with value from memory address {:#X}",
                        mem_addr
                    ),
                    None,
                );
                let operand = read_byte(&state, mem_addr);
                set_flags(&mut state, operand);
                set_reg(&mut state, operand);
                state.addr = prev_addr + 5;
                // Removed immediate refresh for ADD/SUB/MUL/DIV - will be handled by batch refresh
            }

            B => {
                let target_addr = read_offset(state.addr, &state.ram_memory);
                log(
                    &format!("Unconditional branch to address {:#X}", target_addr),
                    None,
                );
                state.addr += 5;
                log(
                    &format!("Pushing return address {:#X} onto stack", state.addr),
                    None,
                );
                push_stack(&mut state);
                state.addr = target_addr;
                // Removed immediate refresh for B - will be handled by batch refresh
            }

            BC | BNC | BZ | BNZ | BNB | BP | BNP | BB => {
                log(
                    &format!(
                        "Evaluating conditional branch for state.current_opcode {:#X} at address {:#X}",
                        state.current_opcode, state.addr
                    ),
                    None,
                );
                let target_addr = read_offset(state.addr, &state.ram_memory);
                log(
                    &format!(
                        "Conditional branch to address {:#X} if condition met",
                        target_addr
                    ),
                    None,
                );
                let branch = match state.current_opcode {
                    BC => state.carry_flag,
                    BNC => !state.carry_flag,
                    BZ => state.zero_flag,
                    BNZ => !state.zero_flag,
                    BNB => !state.borrow_flag,
                    BP => state.parity_flag,
                    BNP => !state.parity_flag,
                    BB => state.borrow_flag,
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
                    state.addr = target_addr;
                    // Removed immediate refresh for conditional branches - will be handled by batch refresh
                } else {
                    state.addr += 5;
                    // Removed immediate refresh for conditional branches - will be handled by batch refresh
                }
            }

            BX => {
                log(
                    &format!("Returning from branch with BX at address {:#X}", state.addr),
                    None,
                );
                let return_value_address = read_offset(state.addr, &state.ram_memory);
                let return_value = read_byte(&state, return_value_address);
                set_reg(&mut state, return_value);
                state.addr = pop_stack(&mut state);
                log(
                    &format!("Returning from branch to address {:#X}", state.addr),
                    None,
                );
                // Removed immediate refresh for BX - will be handled by batch refresh
            }

            JMP => {
                state.addr = read_offset(prev_addr, &state.ram_memory);
                log(
                    &format!("Unconditional jump to address {:#X}", state.addr),
                    None,
                );
                // Removed immediate refresh for JMP - will be handled by batch refresh
            }

            JZ | JNZ | JC | JNC | JNB | JB | JP | JNP => {
                let target_addr = read_offset(state.addr, &state.ram_memory);
                log(
                    &format!(
                        "Conditional jump to address {:#X} if condition met",
                        target_addr
                    ),
                    None,
                );
                let jump = match state.current_opcode {
                    JZ => state.zero_flag,
                    JNZ => !state.zero_flag,
                    JC => state.carry_flag,
                    JNC => !state.carry_flag,
                    JNB => !state.borrow_flag,
                    JB => state.borrow_flag,
                    JP => state.parity_flag,
                    JNP => !state.parity_flag,
                    _ => false,
                };
                if jump {
                    state.addr = target_addr;
                    // Removed immediate refresh for conditional jumps - will be handled by batch refresh
                } else {
                    state.addr += 5;
                    // Removed immediate refresh for conditional jumps - will be handled by batch refresh
                }
            }

            STA | STB | STC | STD => {
                let prev_addr = state.addr;
                let mem_addr = read_offset(state.addr, &state.ram_memory);
                log(
                    &format!("Storing register value to memory address {:#X}", mem_addr),
                    None,
                );
                let value = match state.current_opcode {
                    STA => state.reg_a,
                    STB => state.reg_b,
                    STC => state.reg_c,
                    STD => state.reg_d,
                    _ => state.reg_a,
                };
                write_byte(&mut state, mem_addr, value);
                state.addr = prev_addr + 5;
                // Removed immediate refresh for STA/STB/STC/STD - will be handled by batch refresh
            }

            MOV_AB | MOV_AC | MOV_AD | MOV_BA | MOV_BC | MOV_BD | MOV_CA | MOV_CB | MOV_CD => {
                match state.current_opcode {
                    MOV_AB => state.reg_a = state.reg_b,
                    MOV_AC => state.reg_a = state.reg_c,
                    MOV_AD => state.reg_a = state.reg_d,
                    MOV_BA => state.reg_b = state.reg_a,
                    MOV_BC => state.reg_b = state.reg_c,
                    MOV_BD => state.reg_b = state.reg_d,
                    MOV_CA => state.reg_c = state.reg_a,
                    MOV_CB => state.reg_c = state.reg_b,
                    MOV_CD => state.reg_c = state.reg_d,
                    _ => {}
                }
                state.addr += 1;
            }

            MOV_AMEM | MOV_BMEM | MOV_CMEM | MOV_DMEM => {
                let addr = state.di as usize;
                let value = read_byte(&state, addr);
                match state.current_opcode {
                    MOV_AMEM => state.reg_a = value,
                    MOV_BMEM => state.reg_b = value,
                    MOV_CMEM => state.reg_c = value,
                    MOV_DMEM => state.reg_d = value,
                    _ => {}
                }
                state.addr += 1;
            }

            MOV_MEM_A | MOV_MEM_B | MOV_MEM_C => {
                let addr = state.di as usize;
                let value = match state.current_opcode {
                    MOV_MEM_A => state.reg_a,
                    MOV_MEM_B => state.reg_b,
                    MOV_MEM_C => state.reg_c,
                    _ => 0,
                };
                write_byte(&mut state, addr, value);
                state.addr += 1;
            }

            MOV_SP_BP => {
                log("Copying SP into BP", None);
                state.bp = state.sp;
                state.addr += 1;
            }

            MOV_REG_BP => {
                log("Copying BP into SP", None);
                state.sp = state.bp;
                state.addr += 1;
            }

            MOV_DI_I => {
                let new_di = read_offset(state.addr, &state.ram_memory) as u32;
                log(&format!("Loading immediate pointer {:#X} into DI", new_di), None);
                state.di = new_di;
                state.addr += 5;
            }

            PTRI => {
                state.di = state.di.wrapping_add(1);
                state.addr += 1;
            }

            PTRD => {
                state.di = state.di.wrapping_sub(1);
                state.addr += 1;
            }

            PTRL => {
                let ptr_addr = read_offset(state.addr, &state.ram_memory);
                let loaded = read_u32_le(&state, ptr_addr);
                log(
                    &format!(
                        "Loading DI from memory[{:#X}] => {:#X}",
                        ptr_addr, loaded
                    ),
                    None,
                );
                state.di = loaded;
                state.addr += 5;
            }

            PTRS => {
                let ptr_addr = read_offset(state.addr, &state.ram_memory);
                let di_value = state.di;
                log(
                    &format!(
                        "Storing DI {:#X} into memory address {:#X}",
                        di_value, ptr_addr
                    ),
                    None,
                );
                write_u32_le(&mut state, ptr_addr, di_value);
                state.addr += 5;
            }

            IADD | ISUB | IMUL | IDIV => {
                state.addr += 1;
                let operand = read_byte(&state, state.addr);
                log(
                    &format!("Performing {:?} with operand {:#X}", state.current_opcode, operand),
                    None,
                );
                set_flags(&mut state, operand);
                set_reg(&mut state, operand);
                state.addr += 1;
                // Removed immediate refresh for IADD/ISUB/IMUL/IDIV - will be handled by batch refresh
            }

            LDI | LDIA | LDIB | LDIC | LDID => {
                state.addr += 1;
                let value = state.ram_memory[state.addr];
                log(
                    &format!("Loading immediate value {:#X} into register", value),
                    None,
                );

                set_reg(&mut state, value);

                state.addr += 1;
                // Removed immediate refresh for LDI/LDIA/LDIB/LDIC/LDID - will be handled by batch refresh
            }

            DEC | DECE => {
                log("Waiting for keyboard input...", None);
                match state.current_opcode {
                    DEC => {
                        if !state.keyboard_buffer.is_empty() && state.keyboard_buffer.len() > 1 {
                            let _k = state.keyboard_buffer.remove(0);
                            state.reg_a = _k;
                        }
                    }
                    DECE => {
                        if !state.keyboard_buffer.is_empty() && state.keyboard_buffer.len() > 1 {
                            let _k = state.keyboard_buffer.remove(0);
                            state.reg_a = _k;
                            embed_char_in_terminal(&mut state, _k as char);
                        }
                    }
                    _ => {}
                }
                state.addr += 1;
                // Removed immediate refresh for DEC/DECE - will be handled by batch refresh
            }

            PSAX => {
                let reg_a = state.reg_a;
                log(
                    &format!("Pushing REG_A value {:#X} onto stack", reg_a),
                    None,
                );
                push_data_byte(&mut state, reg_a);
                state.addr += 1;
                // Removed immediate refresh for PSAX - will be handled by batch refresh
            }

            POPX => {
                let value = pop_data_byte(&mut state);
                log(&format!("Popped value {:#X} from stack into REG_A", value), None);
                state.reg_a = value;
                state.addr += 1;
                // Removed immediate refresh for POP - will be handled by batch refresh
            }

            NOP => {
                state.addr += 1;
                // No refresh for NOP to improve performance
            }

            _ => {
                // Unrecognized state.current_opcode: keep behavior (no-op)
                state.addr += 1;
                // No refresh for unknown opcodes
            }
        }

        update_hz_counter(&mut state);
        instruction_count += 1;

        // Optimized refresh strategy: 
        // - Always refresh for visual opcodes (POKE variants)
        // - Refresh every 50 instructions for terminal output opcodes for responsiveness
        // - Batch refresh every 100 instructions for other opcodes
        // - Always refresh when paused/stepping for debugging
        // - Always refresh for interactive opcodes (DEC/DECE)
        let needs_immediate_refresh = match state.current_opcode {
            POKE => state.addr % (WIDTH * HEIGHT * state.video_speed_factor) == 0, // Full screen POKE needs immediate refresh
            POKX | POKY | PXYD => true, // Visual opcodes need immediate refresh
            DEC | DECE => true, // Interactive opcodes need immediate refresh
            HLT => true, // Final state should be visible
            _ => false,
        };

        let is_terminal_output = matches!(state.current_opcode, OUTA | OUTB | OUTC | OUTD);
        let should_terminal_refresh = is_terminal_output && (instruction_count % 50 == 0);
        
        if needs_immediate_refresh || should_terminal_refresh || state.paused || state.step {
            refresh_update(&mut window, &mut state);
        }

        // Update the window if necessary
        if window.is_open() == false {
            log("Window closed by user. Exiting emulator.", None);
            break;
        }
    }
    log("Emulation finished.", None);
    Ok(())
}
