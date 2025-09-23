// Libre8 Baremetal C Standard Library
// Hardware-specific functions and constants

#ifndef LIBRE8_H
#define LIBRE8_H

// Memory layout
#define SCREEN_WIDTH  64
#define SCREEN_HEIGHT 32
#define VRAM_BASE     0x00010000
#define IO_BASE       0x00020000
#define STACK_BASE    0x00030000

// Hardware registers
#define REG_KEYPAD    (IO_BASE + 0x00)
#define REG_TIMER     (IO_BASE + 0x04)
#define REG_SOUND     (IO_BASE + 0x08)

// Colors (VGA palette indexes)
#define BLACK   0
#define BLUE    1
#define GREEN   2
#define CYAN    3
#define RED     4
#define MAGENTA 5
#define BROWN   6
#define WHITE   15

// Keypad bits
#define KEY_UP    0x01
#define KEY_DOWN  0x02
#define KEY_LEFT  0x04
#define KEY_RIGHT 0x08
#define KEY_A     0x10
#define KEY_B     0x20

// Basic types
typedef unsigned char  uint8;
typedef unsigned short uint16;
typedef unsigned int   uint32;

// Hardware abstraction functions
void poke(uint32 addr, uint8 value);
uint8 peek(uint32 addr);
void set_pixel(int x, int y, int color);
void clear_screen(void);
uint8 read_keypad(void);
void delay(int cycles);
void halt(void);

#endif
