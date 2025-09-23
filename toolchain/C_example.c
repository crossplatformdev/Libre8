

// Baremetal Pong for Libre8 - Hardware Direct Programming
#define SCREEN_WIDTH  64
#define SCREEN_HEIGHT 32
#define BLACK   0
#define WHITE   15

// Game state variables
int ball_x = 32;
int ball_y = 16;
int ball_dx = 1;
int ball_dy = 1;
int paddle_left = 12;
int paddle_right = 12;
int score_left = 0;
int score_right = 0;

// Hardware abstraction functions
void set_pixel(int x, int y, int color) {
    if (x >= 0 && x < SCREEN_WIDTH && y >= 0 && y < SCREEN_HEIGHT) {
        __asm {
            POKX x
            POKY y
            POKE color
        }
    }
}

void clear_screen(void) {
    int i;
    for (i = 0; i < 2048; i++) {
        __asm {
            POKX i
            POKE BLACK
        }
    }
}

uint8 read_keypad(void) {
    __asm {
        PIKX REG_KEYPAD
        OUTA
    }
    return 0;
}

void draw_paddle(int x, int y) {
    set_pixel(x, y, WHITE);
    set_pixel(x, y + 1, WHITE);
    set_pixel(x, y + 2, WHITE);
    set_pixel(x, y + 3, WHITE);
    set_pixel(x, y + 4, WHITE);
}

void draw_ball(int x, int y) {
    set_pixel(x, y, WHITE);
}

void pong_game(void) {
    int frame = 0;
    while (frame < 10000) {
        clear_screen();

        // Read input (simplified)
        uint8 keys = read_keypad();
        int left_move = 0;
        int right_move = 0;
        
        // Move paddles based on input
        paddle_left = paddle_left + left_move;
        paddle_right = paddle_right + right_move;
        
        // Clamp paddle positions
        if (paddle_left < 0) paddle_left = 0;
        if (paddle_left > 27) paddle_left = 27;
        if (paddle_right < 0) paddle_right = 0;
        if (paddle_right > 27) paddle_right = 27;

        // Move ball
        ball_x = ball_x + ball_dx;
        ball_y = ball_y + ball_dy;

        // Bounce off top/bottom
        if (ball_y <= 0) ball_dy = 1;
        if (ball_y >= 31) ball_dy = -1;

        // Left paddle collision
        if (ball_x == 1) {
            if (ball_y >= paddle_left && ball_y <= paddle_left + 4) {
                ball_dx = 1;
            } else {
                score_right = score_right + 1;
                ball_x = 32;
                ball_y = 16;
            }
        }
        
        // Right paddle collision
        if (ball_x == 62) {
            if (ball_y >= paddle_right && ball_y <= paddle_right + 4) {
                ball_dx = -1;
            } else {
                score_left = score_left + 1;
                ball_x = 32;
                ball_y = 16;
            }
        }

        // Draw game objects
        draw_paddle(0, paddle_left);
        draw_paddle(63, paddle_right);
        draw_ball(ball_x, ball_y);

        frame = frame + 1;
    }
}

int main(void) {
    pong_game();
    return 0;
}

 
