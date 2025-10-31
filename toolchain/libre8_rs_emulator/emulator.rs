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
        LDA | LDIA => {
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

pub fn load_file_to_memory<P: AsRef<Path>>(path: P) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
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

    log(
        &format!("Parsed {} hex values from file", ram_memory.len()),
        None,
    );
    Ok(ram_memory)
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

fn main() -> Result<(), Box<dyn std::error::Error>> {
    log("Starting Libre8 Emulator...", None);

    let mut child = Command::new("powershell.exe")
        .args([
            "-NoLogo",
            "-NoProfile",
            "-ExecutionPolicy",
            "Bypass",
            "-Command",
            "-",
        ])
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

    let mut state = EmulatorState::new();
    init_terminal_buffer(&mut state);

    //state.ram_memory = load_file_to_memory("./../output/bin.hex")?;
    state.ram_memory = load_file_to_memory("D:/l8rust/pixels_output.txt")?;
    state.keyboard_buffer.push(b' ');
    let num_bytes = state.ram_memory.len();
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
                let mem_addr = read_offset(state.addr, &state.ram_memory);
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
                if state.current_opcode == POKX {
                    log(&format!("Setting X coordinate to {:#X}", value), None);
                    state.x = value as usize % WIDTH;
                } else if state.current_opcode == POKY {
                    log(&format!("Setting Y coordinate to {:#X}", value), None);
                    state.y = value as usize % HEIGHT;
                } else if state.current_opcode == PXYD {
                    log(
                        &format!(
                            "Setting Data at (X,Y)=({},{}) to {:#X}",
                            state.x, state.y, value
                        ),
                        None,
                    );
                    if state.x < WIDTH && state.y < HEIGHT {
                        let vga_color = value;
                        let pixel = vga_to_rgb(vga_color);
                        // Compute destination (scaled)
                        let wx = state.x * SCALE;
                        let wy = state.y * SCALE;
                        let top_left = DISPLAY_ORIGIN + wy * WINDOW_WIDTH + wx;
                        if (DISPLAY_POS_X + wx) + (SCALE - 1) < WINDOW_WIDTH
                            && (DISPLAY_POS_Y + wy) + (SCALE - 1) < WINDOW_HEIGHT
                        {
                            // 2x2 write (SCALE)
                            state.window_buffer[top_left] = pixel;
                            state.window_buffer[top_left + 1] = pixel;
                            state.window_buffer[top_left + WINDOW_WIDTH] = pixel;
                            state.window_buffer[top_left + WINDOW_WIDTH + 1] = pixel;
                        }
                        state.video_buffer[state.y * WIDTH + state.x] = vga_color;
                    }
                }
                state.addr += 5;
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
                let operand = if mem_addr < state.ram_memory.len() {
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
                let return_value = if return_value_address < state.ram_memory.len() {
                    state.ram_memory[return_value_address]
                } else {
                    log(
                        &format!(
                            "Return value address {:#X} out of bounds (max: {})",
                            return_value_address,
                            state.ram_memory.len()
                        ),
                        None,
                    );
                    0
                };
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
                if mem_addr < state.ram_memory.len() {
                    match state.current_opcode {
                        STA => state.ram_memory[mem_addr] = state.reg_a,
                        STB => state.ram_memory[mem_addr] = state.reg_b,
                        STC => state.ram_memory[mem_addr] = state.reg_c,
                        STD => state.ram_memory[mem_addr] = state.reg_d,
                        _ => state.ram_memory[mem_addr] = state.reg_a,
                    }
                } else {
                    log(
                        &format!(
                            "Cannot store to memory address {:#X} - out of bounds (max: {})",
                            mem_addr,
                            state.ram_memory.len()
                        ),
                        None,
                    );
                }
                state.addr = prev_addr + 5;
                // Removed immediate refresh for STA/STB/STC/STD - will be handled by batch refresh
            }

            IADD | ISUB | IMUL | IDIV => {
                state.addr += 1;
                let operand = state.ram_memory[state.addr];
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

    stdin.flush()?;
    drop(stdin);

    let status = child.wait()?;
    out_handle.join().ok();
    err_handle.join().ok();

    log(
        &format!("Libre8 Emulator finished with status: {}", status),
        None,
    );
    Ok(())
}
