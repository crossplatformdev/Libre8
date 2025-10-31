# Copilot Instructions for Libre-8 Rust Emulator

## Project Overview
- This is a Rust-based emulator for the Libre-8 platform, organized as a monolithic codebase with modular subdirectories for rendering, opcodes, and UI.
- The main entry point and core logic reside in `emulator.rs`. Rendering and UI logic are in `renderer/` and `ui/`.
- The project uses `minifb` for windowing and keyboard input, and custom VGA palette logic for display.

## Architecture & Patterns
- **Renderer**: All graphics and UI overlays are handled in `renderer/mod.rs`. It manages window buffers, drawing routines, and UI cards (registers, flags, stack, terminal, memory, VRAM, keyboard).
- **State**: Emulator state (registers, flags, stack, terminal buffer, etc.) is managed via `static mut` globals in `renderer/mod.rs`.
- **Performance**: Dirty caches (e.g., `PREV_FLAGS`, `PREV_REGS`, `PREV_TERMINAL_BUFFER`) are used to avoid unnecessary redraws.
- **Input**: Keyboard input is mapped to ASCII via `key_to_ascii`. Debouncing and buffer logic are in `refresh()`.
- **Display**: The emulator uses a fixed-size window and custom font rendering. All drawing is done via software routines.
- **Memory/VRAM**: Displayed in paged views, with highlighting for the current address.

## Developer Workflows
- **Build**: Use `cargo build` to compile. No custom build scripts are required.
- **Run**: Use `cargo run` to launch the emulator. The main binary is defined in `emulator.rs`.
- **Debug**: Logging is available via `emulator_log.txt` and `log.txt`.
- **Test**: No standard Rust tests are present; testing is manual via emulator execution.
- **Hotkeys**: F9 (pause), F10 (speed), F11 (reset), F12 (fullscreen), Esc (exit), F8 (step). See `refresh()` in `renderer/mod.rs` for details.

## Conventions & Tips
- **Unsafe**: Extensive use of `unsafe` and `static mut` for global state. Be cautious with concurrency and reentrancy.
- **Rendering**: All UI elements are drawn manually; see `draw_text`, `fill_rect`, and related helpers.
- **Performance**: Use the dirty cache pattern for any new UI elements to avoid unnecessary redraws.
- **Extending**: Add new UI cards or overlays by following the pattern in `renderer/mod.rs` (see `display_*` functions).
- **Keyboard**: Extend key handling in `key_to_ascii` and `refresh()`.

## Key Files & Directories
- `emulator.rs`: Main logic and entry point
- `renderer/mod.rs`: Rendering, UI, and state management
- `opcodes/mod.rs`: Opcode definitions and logic
- `ui/mod.rs`: UI helpers (if present)
- `old/`: Legacy emulator versions for reference

## External Dependencies
- `minifb`: Windowing and input
- No external graphics or GUI frameworks

---

For questions or unclear patterns, review `renderer/mod.rs` for the most up-to-date conventions. When in doubt, follow the structure and style of the existing display and input handling routines.
