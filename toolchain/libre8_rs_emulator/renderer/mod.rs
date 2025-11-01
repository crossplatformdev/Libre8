use crate::*;

use minifb::{Key, Window};
use std::time::Duration;

pub const TERMINAL_WIDTH: usize = 80;
pub const TERMINAL_HEIGHT: usize = 24;

// Constants for window dimensi                                                                                             ons and layout

pub const WINDOW_WIDTH: usize = 1920;
pub const WINDOW_HEIGHT: usize = 1020;
pub const WIDTH: usize = 252;
pub const HEIGHT: usize = 252;

// Scaling and font metrics
pub const SCALE: usize = 2;
pub const DISPLAY_ORIGIN: usize = DISPLAY_POS_Y * WINDOW_WIDTH + DISPLAY_POS_X;

pub const FONT_W: usize = 5;
pub const FONT_H: usize = 7;
pub const CHAR_ADV: usize = FONT_W + 1;
pub const TEXT_BG_H: usize = FONT_H + 1;

// Simple VGA palette placeholder (compile-friendly)

// VGA 256-color palette
pub const VGA_PALETTE: [(u8, u8, u8); 256] = [
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

// Simple stack hasher to detect content changes cheaply
pub fn stack_hash(state: &EmulatorState) -> u64 {
    let mut h: u64 = 1469598103934665603; // pub fnV offset basis
    for v in state.stack.iter() {
        h ^= *v as u64;
        h = h.wrapping_mul(1099511628211); // pub fnV prime
    }
    h ^ (state.stack.len() as u64)
}
// Map a subset of minifb::Key to ASCII (best-effort)
pub fn key_to_ascii_with_modifiers(window: &Window, k: Key) -> Option<u8> {
    use Key::*;
    let shift = window.is_key_down(LeftShift) || window.is_key_down(RightShift);
    let ctrl = window.is_key_down(LeftCtrl) || window.is_key_down(RightCtrl);

    let out: Option<u8> = match k {
        // whitespace / control-like
        Space => Some(b' '),
        Enter => Some(b'\n'),
        Tab => Some(b'\t'),
        Backspace => Some(8),

        // numbers row with shift symbols
        Key1 => Some(if shift { b'!' } else { b'1' }),
        Key2 => Some(if shift { b'@' } else { b'2' }),
        Key3 => Some(if shift { b'#' } else { b'3' }),
        Key4 => Some(if shift { b'$' } else { b'4' }),
        Key5 => Some(if shift { b'%' } else { b'5' }),
        Key6 => Some(if shift { b'^' } else { b'6' }),
        Key7 => Some(if shift { b'&' } else { b'7' }),
        Key8 => Some(if shift { b'*' } else { b'8' }),
        Key9 => Some(if shift { b'(' } else { b'9' }),
        Key0 => Some(if shift { b')' } else { b'0' }),
        // punctuation pairs with shift
        Minus => Some(if shift { b'_' } else { b'-' }),
        Equal => Some(if shift { b'+' } else { b'=' }),
        // letters a-z with shift => uppercase
        A => Some(if shift { b'A' } else { b'a' }),
        B => Some(if shift { b'B' } else { b'b' }),
        C => Some(if shift { b'C' } else { b'c' }),
        D => Some(if shift { b'D' } else { b'd' }),
        E => Some(if shift { b'E' } else { b'e' }),
        F => Some(if shift { b'F' } else { b'f' }),
        G => Some(if shift { b'G' } else { b'g' }),
        H => Some(if shift { b'H' } else { b'h' }),
        I => Some(if shift { b'I' } else { b'i' }),
        J => Some(if shift { b'J' } else { b'j' }),
        K => Some(if shift { b'K' } else { b'k' }),
        L => Some(if shift { b'L' } else { b'l' }),
        M => Some(if shift { b'M' } else { b'm' }),
        N => Some(if shift { b'N' } else { b'n' }),
        O => Some(if shift { b'O' } else { b'o' }),
        P => Some(if shift { b'P' } else { b'p' }),
        Q => Some(if shift { b'Q' } else { b'q' }),
        R => Some(if shift { b'R' } else { b'r' }),
        S => Some(if shift { b'S' } else { b's' }),
        T => Some(if shift { b'T' } else { b't' }),
        U => Some(if shift { b'U' } else { b'u' }),
        V => Some(if shift { b'V' } else { b'v' }),
        W => Some(if shift { b'W' } else { b'w' }),
        X => Some(if shift { b'X' } else { b'x' }),
        Y => Some(if shift { b'Y' } else { b'y' }),
        Z => Some(if shift { b'Z' } else { b'z' }),
        LeftBracket => Some(if shift { b'{' } else { b'[' }),
        RightBracket => Some(if shift { b'}' } else { b']' }),
        Backslash => Some(if shift { b'|' } else { b'\\' }),
        Semicolon => Some(if shift { b':' } else { b';' }),
        Apostrophe => Some(if shift { b'"' } else { b'\'' }),
        Comma => Some(if shift { b'<' } else { b',' }),
        Period => Some(if shift { b'>' } else { b'.' }),
        Slash => Some(if shift { b'?' } else { b'/' }),
        Escape => Some(27),

        _ => None,
    };

    // Ctrl-letter -> ASCII control code (1..=26)
    if ctrl {
        if let Some(ch) = out {
            let upper = (ch as char).to_ascii_uppercase() as u8;
            if upper.is_ascii_uppercase() {
                return Some(upper & 0x1F);
            }
        }
    }

    out
}

pub fn page_base_256(addr: usize) -> usize {
    addr & 0xFFFFFF00
}

// Specific function for VRAM that limits to 16-bit address space
pub fn vram_page_base_256(addr: usize) -> usize {
    (addr & 0xFFFF) & 0xFF00  // Limit to 16-bit, then align to 256-byte boundary
}

#[inline]
pub fn vga_to_rgb(vga_index: u8) -> u32 {
    let (r, g, b) = VGA_PALETTE[vga_index as usize];
    ((r as u32) << 16) | ((g as u32) << 8) | (b as u32)
}

#[inline]
pub fn update_window(window: &mut Window, state: &EmulatorState) {
    if !window.is_open() {
        return;
    }
    window
        .update_with_buffer(&state.window_buffer, WINDOW_WIDTH, WINDOW_HEIGHT)
        .unwrap();
}

// === 5x7 font ===
pub fn font_glyph(c: char) -> [u8; FONT_W] {
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
pub fn put_px(x: usize, y: usize, rgb: u32, state: &mut EmulatorState) {
    let idx = y * WINDOW_WIDTH + x;
    if rgb != state.window_buffer[idx] {
        state.window_buffer[idx..idx + 1].copy_from_slice(&[rgb]);
    }
}

pub fn fill_rect(x: usize, y: usize, w: usize, h: usize, rgb: u32, state: &mut EmulatorState) {
    let max_size: usize = w * h;
    let fill_rect: Vec<u32> = vec![rgb; max_size];
    // copy from fill_rect to window_buffer
    let idx = y * WINDOW_WIDTH + x;
    for i in 0..h {
        let start = idx + i * WINDOW_WIDTH;
        let end = start + w;
        if end <= state.window_buffer.len() {
            state.window_buffer[start..end].copy_from_slice(&fill_rect[i * w..(i + 1) * w]);
        }
    }
}

pub fn draw_char(x: usize, y: usize, c: char, rgb: u32, state: &mut EmulatorState) {
    // Faster glyph rendering: iterate only set bits using bit-twiddling
    let glyph = font_glyph(c);
    for (col, &bits) in glyph.iter().enumerate() {
        let mut b = bits;
        while b != 0 {
            let row = b.trailing_zeros() as usize;
            // glyph height is FONT_H; trailing_zeros on non-zero u8 will be < 8
            if row < FONT_H {
                put_px(x + col, y + row, rgb, state);
            }
            // clear lowest set bit
            b &= b - 1;
        }
    }
}

pub fn draw_text(
    mut x: usize,
    y: usize,
    s: &str,
    rgb: u32,
    blanking: u32,
    state: &mut EmulatorState,
) {
    // Clear background
    let text_width = s.len() * CHAR_ADV;
    fill_rect(x, y, text_width, TEXT_BG_H, blanking, state);
    for ch in s.chars() {
        draw_char(x, y, ch, rgb, state);
        x += CHAR_ADV;
    }
}

pub fn display_keyboard(_window: &mut Window, state: &mut EmulatorState) {
    // Draw background
    if state.draw_rect {
        fill_rect(
            KEYBOARD_POS_X - 5,
            KEYBOARD_POS_Y - 5,
            650,
            120,
            COL_CARD,
            state,
        );
    }
    draw_text(
        KEYBOARD_POS_X,
        KEYBOARD_POS_Y,
        "KEYBOARD",
        COL_TEXT,
        COL_CARD,
        state,
    );
    let carry_return_at = 106; // Carry/Return key position
    let mut y = KEYBOARD_POS_Y + 20; // Start below header

    // Clone the keyboard buffer to avoid borrow checker issues
    let keyboard_chars: Vec<u8> = state.keyboard_buffer.clone();
    for (i, &char) in keyboard_chars.iter().enumerate() {
        let x = KEYBOARD_POS_X + (i % carry_return_at) * CHAR_ADV;
        let color = COL_ACCENT;
        let display_char = char as char;
        if i % carry_return_at == 0 && i != 0 {
            y += 20;
        }
        draw_text(x, y, &format!("{}", display_char), color, COL_CARD, state);
    }
}

pub fn display_flags(_window: &mut Window, state: &mut EmulatorState) {
    if state.draw_rect {
        fill_rect(FLAGS_POS_X - 5, FLAGS_POS_Y - 5, 70, 170, COL_CARD, state);
    }
    let flags: [(&str, bool); 7] = [
        ("ZERO", state.zero_flag),
        ("CARRY", state.carry_flag),
        ("BORROW", state.borrow_flag),
        ("PARITY", state.parity_flag),
        ("GREATER", state.greater_flag),
        ("LESSER", state.lesser_flag),
        ("EQUAL", state.equal_flag),
    ];

    draw_text(FLAGS_POS_X, FLAGS_POS_Y, "FLAGS", COL_TEXT, COL_CARD, state);

    for (i, (name, value)) in flags.iter().enumerate() {
        let mut x = FLAGS_POS_X;
        let y = FLAGS_POS_Y + (i * 20) + 20;

        draw_text(x, y, name, COL_TEXT, COL_CARD, state);
        x += 48;
        if *value {
            draw_text(x, y, "1", COL_GREEN, COL_CARD, state);
        } else {
            draw_text(x, y, "0", COL_RED, COL_CARD, state);
        }
    }
}

pub fn display_registers(_window: &mut Window, state: &mut EmulatorState) {
    if state.draw_rect {
        fill_rect(REGS_POS_X - 5, REGS_POS_Y - 5, 168, 110, COL_CARD, state);
    }

    draw_text(
        REGS_POS_X + 5,
        REGS_POS_Y + 5,
        "REGISTERS",
        COL_TEXT,
        COL_CARD,
        state,
    );
    let registers = [
        ("A", state.reg_a),
        ("B", state.reg_b),
        ("C", state.reg_c),
        ("D", state.reg_d),
    ];

    draw_text(
        REGS_POS_X + 5,
        REGS_POS_Y + 45,
        "HEX ",
        COL_TEXT,
        COL_CARD,
        state,
    );
    draw_text(
        REGS_POS_X + 5,
        REGS_POS_Y + 65,
        "DEC ",
        COL_TEXT,
        COL_CARD,
        state,
    );
    draw_text(
        REGS_POS_X + 5,
        REGS_POS_Y + 85,
        "CHR ",
        COL_TEXT,
        COL_CARD,
        state,
    );

    for (i, (name, value)) in registers.iter().enumerate() {
        let x = REGS_POS_X + i * 32 + 40;
        let mut y = REGS_POS_Y + 20 + 5;

        // letters
        draw_text(x, y, name, COL_TEXT, COL_CARD, state);
        y += 20;
        let mut value_str = format!("0x{:02X}", value);
        draw_text(x, y, &value_str, COL_ACCENT, COL_CARD, state);
        // Decimal
        value_str = format!("{}", value);
        y += 20;
        draw_text(x, y, &value_str, COL_ACCENT, COL_CARD, state);
        // Char
        value_str = format!(
            "'{:.1}'",
            if *value >= 32 && *value <= 126 {
                *value as char
            } else {
                '.'
            }
        );
        y += 20;
        draw_text(x, y, &value_str, COL_ACCENT, COL_CARD, state);
    }
}

pub fn display_opcode(_window: &mut Window, opcode: u8, state: &mut EmulatorState) {
    if state.draw_rect {
        fill_rect(OPCODE_POS_X - 5, OPCODE_POS_Y - 5, 168, 20, COL_CARD, state);
    }

    draw_text(
        OPCODE_POS_X,
        OPCODE_POS_Y,
        "CURRENT OPCODE",
        COL_TEXT,
        COL_CARD,
        state,
    );

    let opcode_str = format!("0x{:02X}", opcode);
    draw_text(
        OPCODE_POS_X + (6 * 22),
        OPCODE_POS_Y,
        &opcode_str,
        COL_YELLOW,
        COL_CARD,
        state,
    );
}

pub fn display_pc(_window: &mut Window, pc: usize, state: &mut EmulatorState) {
    if state.draw_rect {
        fill_rect(OPADDR_POS_X - 5, OPADDR_POS_Y - 5, 168, 20, COL_CARD, state);
    }
    draw_text(
        OPADDR_POS_X,
        OPADDR_POS_Y,
        "PROGRAM COUNTER",
        COL_TEXT,
        COL_CARD,
        state,
    );

    let pc_str = format!("{:08X}", pc);
    draw_text(
        OPADDR_POS_X + (6 * 18),
        OPADDR_POS_Y,
        &pc_str,
        COL_YELLOW,
        COL_CARD,
        state,
    );
}

pub fn display_stack(_window: &mut Window, state: &mut EmulatorState) {
    if state.draw_rect {
        fill_rect(STACK_POS_X - 5, STACK_POS_Y - 5, 320, 640, COL_CARD, state);
    }

    draw_text(STACK_POS_X, STACK_POS_Y, "STACK", COL_TEXT, COL_CARD, state);
    let columns = 8;
    let stack_values: Vec<usize> = state.stack.clone(); // Clone to avoid borrow issues
    for (i, value) in stack_values.iter().rev().enumerate() {
        let x = STACK_POS_X + (i % columns) * 80;
        let y = STACK_POS_Y + (i / columns) * 20 + 20;

        let value_str = format!("{:08X}", value);
        if Some(value) == stack_values.last() {
            draw_text(x, y, &value_str, COL_RED, COL_CARD, state);
        } else if i == 0 {
            draw_text(x, y, &value_str, COL_YELLOW, COL_CARD, state);
        } else if i % 2 == 0 {
            draw_text(x, y, &value_str, COL_LIGHT_GREEN, COL_CARD, state);
        } else {
            draw_text(x, y, &value_str, COL_LIME, COL_CARD, state);
        }
    }
}

pub fn display_terminal(_window: &mut Window, state: &mut EmulatorState) {
    let char_width = 8;
    let char_height = 12;
    let terminal_display_width = TERMINAL_WIDTH * char_width;
    let terminal_display_height = TERMINAL_HEIGHT * char_height;
    if state.draw_rect {
        fill_rect(
            state.terminal_pos_x - 5,
            state.terminal_pos_y - 5,
            terminal_display_width + 10,
            terminal_display_height + 10 + 20,
            COL_CARD,
            state,
        );
    }
    // terminal area
    let terminal_start_x = state.terminal_pos_x;
    let terminal_start_y = state.terminal_pos_y + 20;
    fill_rect(
        terminal_start_x,
        terminal_start_y,
        terminal_display_width,
        terminal_display_height,
        COL_CARD,
        state,
    );

    // draw terminal characters
    for row in 0..TERMINAL_HEIGHT {
        for col in 0..TERMINAL_WIDTH {
            let ch = state.terminal_buffer[row * TERMINAL_WIDTH + col] as char;
            let x = terminal_start_x + col * char_width;
            let y = terminal_start_y + row * char_height;
            draw_char(x, y, ch, COL_LIGHT_GREEN, state);
        }
    }

}

// Update signature: add current_addr
pub fn display_memory(
    _window: &mut Window,
    page_base: usize,
    current_addr: usize,
    state: &mut EmulatorState,
) {
    let width = 16 * 32 + 128;
    let height = 16 * 20 + 20;
    if state.draw_rect {
        fill_rect(
            MEM_POS_X - 5,
            MEM_POS_Y - 5,
            width + 10,
            height + 10,
            COL_CARD,
            state,
        );
    }

    let mem_display_start_x = MEM_POS_X;
    let mem_display_start_y = MEM_POS_Y;

    // Header + current address
    draw_text(
        mem_display_start_x,
        mem_display_start_y,
        "MEMORY",
        COL_TEXT,
        COL_CARD,
        state,
    );
    draw_text(
        mem_display_start_x + 90,
        mem_display_start_y,
        &format!(
            "CUR {:08X}    ",
            current_addr.min(state.ram_memory.len().saturating_sub(1))
        ),
        COL_YELLOW,
        COL_CARD,
        state,
    );

    for i in 0..16 {
        let y = mem_display_start_y + (i * 20) + 20;
        for j in 0..16 {
            let cell_addr = page_base + i * 16 + j;
            if cell_addr >= state.ram_memory.len() {
                continue;
            }
            let x = mem_display_start_x + (j * 32);

            if j == 0 {
                let addr_str = format!("{:08X}:    ", cell_addr);
                draw_text(mem_display_start_x, y, &addr_str, COL_RED, COL_CARD, state);
            }

            let value_str = format!("{:02X}", state.ram_memory[cell_addr]);
            if cell_addr == current_addr {
                draw_text(x + 128, y, &value_str, COL_RED, COL_YELLOW, state);
            } else {
                draw_text(x + 128, y, &value_str, COL_GREEN, COL_CARD, state);
            }
        }
    }
}

// Update signature: add current_addr
pub fn display_vram(
    _window: &mut Window,
    page_base: usize,
    current_addr: usize,
    state: &mut EmulatorState,
) {
    let width = 16 * 32 + 128;
    let height = 16 * 20 + 20;
    if state.draw_rect {
        fill_rect(
            VRAM_POS_X - 5,
            VRAM_POS_Y - 5,
            width + 10,
            height + 10,
            COL_CARD,
            state,
        );
    }

    let vram_display_start_x = VRAM_POS_X;
    let vram_display_start_y = VRAM_POS_Y;

    // Header + current address
    draw_text(
        vram_display_start_x,
        vram_display_start_y,
        "VRAM",
        COL_TEXT,
        COL_CARD,
        state,
    );
    draw_text(
        vram_display_start_x + 70,
        vram_display_start_y,
        &format!(
            "CUR {:04X}    ",
            (current_addr.min(state.video_buffer.len().saturating_sub(1))) & 0xFFFF
        ),
        COL_YELLOW,
        COL_CARD,
        state,
    );

    for i in 0..16 {
        let y = vram_display_start_y + (i * 20) + 20;
        for j in 0..16 {
            let cell_addr = (page_base + i * 16 + j) & 0xFFFF; // Limit to 16-bit address space for VRAM
            if cell_addr >= state.video_buffer.len() {
                continue;
            }
            let x = vram_display_start_x + (j * 32);

            if j == 0 {
                let addr_str = format!("{:04X}:    ", cell_addr);
                draw_text(vram_display_start_x, y, &addr_str, COL_RED, COL_CARD, state);
            }

            let value = format!("{:02X}", state.video_buffer[cell_addr]);
            if cell_addr == current_addr {
                draw_text(x + 128, y, &value, COL_RED, COL_YELLOW, state);
            } else {
                draw_text(x + 128, y, &value, COL_GREEN, COL_CARD, state);
            }
        }
    }
}

pub fn update_hz_counter(state: &mut EmulatorState) {
    state.hz_counter += 1.0;
    if state.timer.elapsed() >= Duration::from_secs(1) {
        let freq = state.hz_counter * state.video_speed_factor as f64
            / state.timer.elapsed().as_secs_f64();
        // Display in appropriate units ###.### (three units with limit to three decimals)
        let hz_str = if freq > 1_000_000.0 {
            format!("MHz: {:3.3}", freq / 1_000_000.0)
        } else if freq > 1_000.0 {
            format!("KHz: {:3.3}", freq / 1_000.0)
        } else {
            format!("Hz: {:3.3}", freq)
        };
        state.hz_counter = 0.0;
        state.timer = std::time::Instant::now();
        draw_text(WINDOW_WIDTH - 200, 10, &hz_str, COL_TEXT, COL_CARD, state);
    }
}

pub fn refresh(window: &mut Window, state: &mut EmulatorState) {
    // simple debounce parameters (ms)
    pub const DEBOUNCE_MS: u128 = 25;

    // Non-blocking key polling into keyboard buffer with debounce
    let keys = window.get_keys_pressed(minifb::KeyRepeat::No);
    for k in keys {
        // current time in ms
        let now_ms = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_millis();

        // simple debounce check
        // map key to ASCII with modifiers
        let ch = key_to_ascii_with_modifiers(window, k);
        match k {
            Key::Escape => {
                // immediate exit (no debounce)
                std::process::exit(0);
            }
            Key::F12 => {
                // toggle full screen (debounced)
                if now_ms.wrapping_sub(state.last_key_ts) > DEBOUNCE_MS {
                    state.full_screen = !state.full_screen;
                    state.last_key_ts = now_ms;
                    state.last_key_ascii = 0;
                    let str = &format!(
                        "{:>32}",
                        if state.full_screen {
                            "Full Screen"
                        } else {
                            "   Windowed   "
                        }
                    );
                    draw_text(10, 10, str, COL_TEXT, COL_CARD, state);
                }
            }
            Key::F11 => {
                if now_ms.wrapping_sub(state.last_key_ts) > DEBOUNCE_MS {
                    state.reset = true;
                    state.last_key_ts = now_ms;
                    state.last_key_ascii = 0;
                    let str = &format!("{:>32}", "   RESETting   ");
                    draw_text(10, 10, str, COL_TEXT, COL_CARD, state);
                }
            }
            Key::F10 => {
                if now_ms.wrapping_sub(state.last_key_ts) > DEBOUNCE_MS {
                    state.video_speed_factor = (state.video_speed_factor % 5) + 1;
                    state.last_key_ts = now_ms;
                    state.last_key_ascii = 0;
                    let speed_msg = format!("Speed x{}", state.video_speed_factor);
                    draw_text(10, 10, &speed_msg, COL_TEXT, COL_CARD, state);
                }
            }
            Key::F9 => {
                if now_ms.wrapping_sub(state.last_key_ts) > DEBOUNCE_MS {
                    state.paused = !state.paused;
                    state.last_key_ts = now_ms;
                    state.last_key_ascii = 0;
                    let str = &format!(
                        "{:>32}",
                        if state.paused {
                            "   PAUSED   "
                        } else {
                            "   Running   "
                        }
                    );
                    draw_text(10, 10, str, COL_TEXT, COL_CARD, state);
                }
            }
            Key::F8 => {
                if now_ms.wrapping_sub(state.last_key_ts) > DEBOUNCE_MS {
                    state.step = true;
                    state.last_key_ts = now_ms;
                    state.last_key_ascii = 0;
                    let str = &format!("{:>32}", "   STEP   ");
                    draw_text(10, 10, str, COL_TEXT, COL_CARD, state);
                }
            }
            _ => {
                if let Some(c) = ch {
                    if c != 0
                        && (c != state.last_key_ascii
                            || now_ms.wrapping_sub(state.last_key_ts) > DEBOUNCE_MS)
                    {
                        if state.keyboard_buffer.len() < 640 {
                            state.keyboard_buffer.push(c);
                        }
                        state.last_key_ts = now_ms;
                        state.last_key_ascii = c;
                    }
                } else {
                    state.last_key_ascii = 0; // RESET on non-ASCII key
                }
            }
        }
    }

    // Throttle refresh to prevent excessive updates (max 60 FPS)
    let now = std::time::Instant::now();
    let min_refresh_interval = std::time::Duration::from_millis(16); // ~60 FPS

    if now.duration_since(state.last_refresh_time) < min_refresh_interval {
        return; // Skip this refresh to prevent overloading
    }

    state.last_refresh_time = now;


    // Flags card (only if changed)
    let flags_now = (
        state.zero_flag,
        state.carry_flag,
        state.borrow_flag,
        state.parity_flag,
        state.greater_flag,
        state.lesser_flag,
        state.equal_flag,
    );
    if flags_now != state.prev_flags || state.draw_rect {
        display_flags(window, state);
        state.prev_flags = flags_now;
    }

    // Stack card (check length + hash)
    let st_len = state.stack.len();
    let st_hash = stack_hash(state);
    if st_len != state.prev_stack_len || st_hash != state.prev_stack_hash || state.draw_rect {
        display_stack(window, state);
        state.prev_stack_len = st_len;
        state.prev_stack_hash = st_hash;
    }

    // Registers card
    let regs_now = [state.reg_a, state.reg_b, state.reg_c, state.reg_d];
    if regs_now != state.prev_regs || state.draw_rect {
        display_registers(window, state);
        state.prev_regs = regs_now;
    }

    // Opcode/PC cards
    if state.current_opcode != state.prev_opcode || state.draw_rect {
        display_opcode(window, state.current_opcode, state);
        state.prev_opcode = state.current_opcode;
    }
    if state.addr != state.prev_pc || state.draw_rect {
        display_pc(window, state.addr, state);
        state.prev_pc = state.addr;
    }

    if state.prev_terminal_buffer != state.terminal_buffer || state.draw_rect {
        display_terminal(window, state);
        state
            .prev_terminal_buffer
            .copy_from_slice(&state.terminal_buffer);
    }

    // Memory: compute page and pass current absolute address
    let mem_page = page_base_256(state.addr.min(state.ram_memory.len().saturating_sub(1)));
    if mem_page != state.last_mem_page_base || state.draw_rect {
        display_memory(
            window,
            mem_page,
            state.addr.min(state.ram_memory.len().saturating_sub(1)),
            state,
        );
        state.last_mem_page_base = mem_page;
    } else {
        // Still redraw header highlight when addr changed within same page
        display_memory(
            window,
            mem_page,
            state.addr.min(state.ram_memory.len().saturating_sub(1)),
            state,
        );
    }

    // VRAM: compute current pixel index and page; pass current absolute VRAM address
    let cur_px = (state.y * WIDTH + state.x) % (WIDTH * HEIGHT);
    let vram_page = vram_page_base_256(cur_px); // Use VRAM-specific page function
    if vram_page != state.last_vram_page_base || state.draw_rect {
        display_vram(window, vram_page, cur_px, state);
        state.last_vram_page_base = vram_page;
    } else {
        // Still redraw header highlight when cur_px changed within same page
        display_vram(window, vram_page, cur_px, state);
    }

    // Keyboard card
    if state.keyboard_buffer.len() != state.prev_kb_len || state.draw_rect {
        display_keyboard(window, state);
        state.prev_kb_len = state.keyboard_buffer.len();
    }
}

// NEW: helper to refresh and update in one shot
#[inline]
pub fn refresh_update(window: &mut Window, state: &mut EmulatorState) {
    refresh(window, state);
    update_window(window, state);
}
