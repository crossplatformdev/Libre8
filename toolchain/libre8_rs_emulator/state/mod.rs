// State module for the Libre-8 emulator
// Contains all previously global mutable state in a single struct

use crate::renderer::{TERMINAL_HEIGHT, TERMINAL_WIDTH, WINDOW_HEIGHT, WINDOW_WIDTH};
use crate::ui::COL_GRID;

#[derive(Debug)]
pub struct EmulatorState {
    //TODO: Rename to ram_memory
    pub ram_memory: Vec<u8>,

    // CPU Registers
    pub reg_a: u8,
    pub reg_b: u8,
    pub reg_c: u8,
    pub reg_d: u8,

    // Extended CPU registers for Libre8 C ABI
    pub sp: u32,
    pub bp: u32,
    pub di: u32,

    // Terminal state
    pub terminal_x: usize,
    pub terminal_y: usize,
    pub terminal_pos_x: usize,
    pub terminal_pos_y: usize,
    pub terminal_buffer: [char; TERMINAL_WIDTH * TERMINAL_HEIGHT],
    pub prev_terminal_buffer: [char; TERMINAL_WIDTH * TERMINAL_HEIGHT],

    // Stack
    pub stack: Vec<usize>,
    pub stack_base: u32,
    pub data_stack_min: u32,

    // CPU Flags
    pub zero_flag: bool,
    pub carry_flag: bool,
    pub borrow_flag: bool,
    pub parity_flag: bool,
    pub greater_flag: bool,
    pub lesser_flag: bool,
    pub equal_flag: bool,

    // Dirty caches for UI optimization
    pub prev_flags: (bool, bool, bool, bool, bool, bool, bool),
    pub prev_regs: [u8; 4],
    pub prev_pc: usize,
    pub prev_opcode: u8,
    pub prev_stack_len: usize,
    pub prev_stack_hash: u64,
    pub prev_kb_len: usize,

    // Counters and timing
    pub counter: f64,

    // Opcode execution state
    pub current_opcode: u8,

    // Graphics state
    pub window_buffer: Vec<u32>,
    pub keyboard_buffer: Vec<u8>,

    // Memory and VRAM page tracking
    pub last_mem_page_base: usize,
    pub last_vram_page_base: usize,
    pub last_key_ts: u128,
    pub last_key_ascii: u8,

    // UI/Display state
    pub draw_rect: bool,
    pub video_speed_factor: usize,
    pub full_screen: bool,
    pub paused: bool,
    pub reset: bool,
    pub step: bool,

    pub x: usize,
    pub y: usize,
    pub addr: usize,

    pub _input: String,
    // std::io::stdin().read_line(&mut input)?;
    pub video_buffer: Vec<u8>,
    //let mut FRAME_BUFFER: [u32; WIDTH * HEIGHT] = [COL_GRID; WIDTH * HEIGHT];
    pub timer: std::time::Instant,
    pub hz_counter: f64,
    pub last_refresh_time: std::time::Instant,
    
    // CPU throttling for maintaining target frequency
    pub last_instruction_time: std::time::Instant,
    pub target_instruction_duration: std::time::Duration, // Duration per instruction for target frequency
    pub instruction_batch_count: u64, // Count instructions in current batch

    pub program_len: usize,
}

impl EmulatorState {
    pub fn new() -> Self {
        Self {
            // Memory
            ram_memory: vec![0; 65536],

            // CPU Registers
            reg_a: 0,
            reg_b: 0,
            reg_c: 0,
            reg_d: 0,

            sp: 0,
            bp: 0,
            di: 0,

            // Terminal state
            terminal_x: 0,
            terminal_y: 0,
            terminal_pos_x: 456,
            terminal_pos_y: 48,
            terminal_buffer: [' '; TERMINAL_WIDTH * TERMINAL_HEIGHT],
            prev_terminal_buffer: [' '; TERMINAL_WIDTH * TERMINAL_HEIGHT],

            // Stack
            stack: Vec::new(),
            stack_base: 0,
            data_stack_min: 0,

            // CPU Flags
            zero_flag: false,
            carry_flag: false,
            borrow_flag: false,
            parity_flag: false,
            greater_flag: false,
            lesser_flag: false,
            equal_flag: false,

            // Dirty caches for UI optimization
            prev_flags: (true, true, true, true, true, true, true),
            prev_regs: [0xFF, 0xFF, 0xFF, 0xFF],
            prev_pc: usize::MAX,
            prev_opcode: 0xFF,
            prev_stack_len: 0,
            prev_stack_hash: 0,
            prev_kb_len: 0,

            // Counters and timing
            counter: 0.0,

            // Opcode execution state
            current_opcode: 0,

            // Graphics state
            window_buffer: vec![COL_GRID; WINDOW_HEIGHT * WINDOW_WIDTH],
            keyboard_buffer: Vec::new(),

            // Memory and VRAM page tracking
            last_mem_page_base: usize::MAX,
            last_vram_page_base: usize::MAX,
            last_key_ts: 0,
            last_key_ascii: 0,

            // UI/Display state
            draw_rect: true,
            video_speed_factor: 1,
            full_screen: false,
            paused: true,
            reset: false,
            step: false,

            x: 0,
            y: 0,
            addr: 0,
            _input: String::new(),
            video_buffer: vec![0x00; WINDOW_WIDTH * WINDOW_HEIGHT],
            timer: std::time::Instant::now(),
            hz_counter: 0.0,
            last_refresh_time: std::time::Instant::now(),
            
            // Initialize CPU throttling for 980KHz base frequency
            last_instruction_time: std::time::Instant::now(),
            target_instruction_duration: std::time::Duration::from_nanos(1020), // ~980KHz (1/980000 * 1e9 nanoseconds)
            instruction_batch_count: 0,

            program_len: 0,
        }
    }

    pub fn reset_state(&mut self) {
        self.reg_a = 0;
        self.reg_b = 0;
        self.reg_c = 0;
        self.reg_d = 0;
        self.video_speed_factor = 1;
        self.full_screen = false;
        self.draw_rect = false;
        self.stack.clear();
        self.keyboard_buffer.clear();
        self.keyboard_buffer.push(b' ');
        self.terminal_buffer.fill(' ');
        self.prev_terminal_buffer.fill(' ');
        self.terminal_x = 0;
        self.terminal_y = 0;
        self.sp = self.stack_base;
        self.bp = self.stack_base;
        self.di = 0;
        self.reset = false;
    }

    pub fn init_terminal_buffer(&mut self) {
        self.terminal_buffer.fill(' ');
        self.prev_terminal_buffer.fill(' ');
        self.terminal_x = 0;
        self.terminal_y = 0;
    }

    pub fn set_flags(&mut self, operand: u8) {
        self.zero_flag = self.reg_a == 0 && operand == 0;
        self.carry_flag = ((self.reg_a + operand) as i8) > 0;
        self.borrow_flag = ((self.reg_a - operand) as i8) > 0;
        self.parity_flag = self.reg_a.is_multiple_of(2);
        self.greater_flag = self.reg_a > operand;
        self.lesser_flag = self.reg_a < operand;
        self.equal_flag = self.reg_a == operand;
    }

    pub fn push_stack(&mut self, value: usize) {
        self.stack.push(value);
    }

    pub fn pop_stack(&mut self) -> usize {
        self.stack.pop().unwrap_or(0)
    }

    pub fn embed_char_in_terminal(&mut self, c: char) {
        if c == '\n' {
            self.terminal_x = 0;
            self.terminal_y += 1;
            if self.terminal_y >= TERMINAL_HEIGHT {
                self.terminal_y = 0;
            }
        } else {
            let index = self.terminal_y * TERMINAL_WIDTH + self.terminal_x;
            if index < self.terminal_buffer.len() {
                self.terminal_buffer[index] = c;
            }
            self.terminal_x += 1;
            if self.terminal_x >= TERMINAL_WIDTH {
                self.terminal_x = 0;
                self.terminal_y += 1;                                                                                                                                   
                if self.terminal_y >= TERMINAL_HEIGHT {
                    self.terminal_y = 0;
                }
            }
        }                                                                                                               
    }

    pub fn embed_int_in_terminal(&mut self, value: u8) {
        for c in value.to_string().chars() {
            self.embed_char_in_terminal(c);
        }
    }
}

impl Default for EmulatorState {
    fn default() -> Self {
        Self::new()
    }
}
