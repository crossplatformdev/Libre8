// Libre8 Runtime Library Implementation
#include "libre8.h"

// Hardware abstraction layer
void poke(uint32 addr, uint8 value) {
    // Assembly: POKX addr; POKY value
    __asm {
        POKX addr
        POKY value  
    }
}

uint8 peek(uint32 addr) {
    // Assembly: PIKX addr; return A
    __asm {
        PIKX addr
        OUTA
    }
    return 0; // Placeholder
}

void set_pixel(int x, int y, int color) {
    if (x >= 0 && x < SCREEN_WIDTH && y >= 0 && y < SCREEN_HEIGHT) {
        uint32 addr = VRAM_BASE + (y * SCREEN_WIDTH) + x;
        poke(addr, color);
    }
}

void clear_screen(void) {
    int i;
    for (i = 0; i < SCREEN_WIDTH * SCREEN_HEIGHT; i++) {
        poke(VRAM_BASE + i, BLACK);
    }
}

uint8 read_keypad(void) {
    return peek(REG_KEYPAD);
}

void delay(int cycles) {
    int i;
    for (i = 0; i < cycles; i++) {
        __asm { NOP }
    }
}

void halt(void) {
    __asm { HLT }
}
