/**
 * Pong Game in C
 * 0,0
 * |-------------------------------------------------------|
 * |                                                       |
 * |                                                       |
 * |                                                       |
 * |                                                       |
 * |                                                       |
 * |                                                       |
 * |                                                       |
 * |                                                       |
 * |-------------------------------------------------------|0x47, 0x1f
 * 
 * Note: NUMBERS MUST BE IN HEXADECIMAL FORMAT 
 * 
 */

int paddleLeftX = 1;
int paddleRightX = 70;

int paddleLeftY = 4;
int paddleRightY = 23;
int inputChar = 0; // Placeholder for input character

// Coordinates
int ballX = 23;
int ballY = 12;
// Ball movement direction
int ballDirectionX = 1; // 1 for right, 2 for left
int ballDirectionY = 1; // 1 for down, 2 for up
// Paddle movement speed
int paddleSpeed = 1; // Speed of paddle movement
// Ball speed
int ballSpeedX = 1; // Speed of ball in X direction
int ballSpeedY = 1; // Speed of ball in Y direction
// Field dimensions
int fieldWidth = 72; // Width of the field (the two halves sum to 72)
int fieldHeight = 32; // Height of the field


int foo = 0; // Placeholder variable for assembly code
int bar = 0; // Another placeholder variable for assembly code
int baz = 0; // Yet another placeholder variable for assembly code

void drawPaddles() {
    // Draw left paddle
    __asm {
        ;; Draw the left paddle (5 pixels height)       
        LDA paddleLeftY        
        STA foo ;; Store paddle position in a temporary variable

        ;; 1 pixel 
        PIKY foo
        PIKX paddleLeftX
        PXYD ff

        LDA foo
        ADD _01 ;; Move down one pixel
        STA foo

        ;; 2 pixels
        PIKY foo
        PIKX paddleLeftX ;; Set X position for left paddle
        PXYD ff ;; Draw the paddle at the current position

        LDA foo
        ADD _01 ;; Move down one pixel
        STA foo

        ;; 3 pixels
        PIKY foo
        PIKX paddleLeftX ;; Set X position for left paddle
        PXYD ff ;; Draw the paddle at the current position

        LDA foo
        ADD _01 ;; Move down one pixel
        STA foo

        ;; 4 pixels
        PIKY foo
        PIKX paddleLeftX ;; Set X position for left paddle
        PXYD ff ;; Draw the paddle at the current position
        
        LDA foo
        ADD _01 ;; Move down one pixel
        STA foo

        ;; 5 pixels
        PIKY foo
        PIKX paddleLeftX ;; Set X position for left paddle
        PXYD ff ;; Draw the paddle at the current position

        ;; Same for right paddle
        LDA paddleRightY
        STA bar ;; Store right paddle position in a temporary variable
        
        ;; 1 pixel
        PIKY bar ;; Set Y position for right paddle
        PIKX paddleRightX ;; Set X position for right paddle
        PXYD ff ;; Draw the paddle at the current position
        LDA bar
        ADD _01 ;; Move down one pixel
        STA bar

        ;; 2 pixels
        PIKY bar
        PIKX paddleRightX ;; Set X position for right paddle
        PXYD ff ;; Draw the paddle at the current position
        LDA bar
        ADD _01 ;; Move down one pixel
        STA bar

        ;; 3 pixels
        PIKY bar
        PIKX paddleRightX ;; Set X position for right paddle
        PXYD ff ;; Draw the paddle at the current position
        LDA bar
        ADD _01 ;; Move down one pixel
        STA bar

        ;; 4 pixels
        PIKY bar
        PIKX paddleRightX ;; Set X position for right paddle
        PXYD ff ;; Draw the paddle at the current position
        LDA bar
        ADD _01 ;; Move down one pixel
        STA bar

        ;; 5 pixels
        PIKY bar
        PIKX paddleRightX ;; Set X position for right paddle
        PXYD ff ;; Draw the paddle at the current position
        
        ;; Return to main loop
        BX _00 ;; Return to main loop
    }
}

void drawField() {
    __asm {
        LDA _0c
        OUT
        LDA _00
        OUT
                
        BX _00
    }
}

void drawNet() {
    // Draw the net
    __asm {
        POKX 23         
        POKY 01         
        PXYD ff
        POKX 23
        POKY 02
        PXYD ff
        POKX 23
        POKY 03
        PXYD ff

        POKX 23
        POKY 05
        PXYD ff
        POKX 23
        POKY 06
        PXYD ff
        POKX 23
        POKY 07
        PXYD ff

        POKX 23
        POKY 09
        PXYD ff
        POKX 23
        POKY 0a
        PXYD ff
        POKX 23
        POKY 0b
        PXYD ff

        POKX 23
        POKY 0d
        PXYD ff
        POKX 23
        POKY 0e
        PXYD ff
        POKX 23
        POKY 0f
        PXYD ff

        POKX 23
        POKY 11
        PXYD ff
        POKX 23
        POKY 12
        PXYD ff
        POKX 23
        POKY 13
        PXYD ff

        POKX 23
        POKY 15
        PXYD ff
        POKX 23
        POKY 16
        PXYD ff
        POKX 23
        POKY 17
        PXYD ff

        POKX 23
        POKY 19
        PXYD ff
        POKX 23
        POKY 1a
        PXYD ff
        POKX 23
        POKY 1b
        PXYD ff

        POKX 23
        POKY 1d
        PXYD ff
        POKX 23
        POKY 1e
        PXYD ff
        POKX 23
        POKY 1f
        PXYD ff

        BX _00
    }
}

void paddleUp() {
    __asm {
        ;; If _57 (W) is pressed, move left paddle up
        LDA inputChar
        SUB inputChar
        STA inputChar ;; Clear the input character
        
        LDA paddleLeftY
        ADD _06
        STA foo
        
        ;; DELETE LOWER PART OF THE PADDLE
        PIKY foo
        PIKX paddleLeftX
        PXYD 00

        LDA paddleLeftY
        SUB _01 ;; Move paddle up
        STA paddleLeftY ;; Update paddle position

        BX paddleLeftY
    }
}

void paddleDown() {
    __asm {
        LDA inputChar
        SUB inputChar
        STA inputChar ;; Clear the input character
        
        ;; If _53 (S) is pressed, move left paddle up
        LDA paddleLeftY
        SUB _01
        STA foo
        
        ;; DELETE UPPER PART OF THE PADDLE
        PIKY foo
        PIKX paddleLeftX
        PXYD 00

        LDA paddleLeftY
        ADD _01 ;; Move paddle down
        STA paddleLeftY ;; Update paddle position

        BX paddleLeftY
    }
}


// Function to draw the field and paddles
void checkInput() {
    // Check for input to move paddles
    __asm {
        ;; Check for input to move paddles
        ;; If up arrow pressed, move left paddle up
        ;; If down arrow pressed, move left paddle down
        ;; If _57 pressed, move right paddle up
        ;; If _55 pressed, move right paddle down

        ;; Placeholder for input checking logic
        ;; This would typically involve reading from a keyboard buffer or input port
        ;; and updating paddle positions accordingly.
        
        ;; Example: if (upArrowPressed) { paddleLeftY -= paddleSpeed; }
        ;; Example: if (downArrowPressed) { paddleLeftY += paddleSpeed; }

        DECE ;; Reads one character from the keyboard buffer, stores in A reg.
        STA inputChar ;; Store the character in paddleLeftY for processing
        STA foo
        STA bar        
        .checkS
        LDA foo
        SUB _53 ;; Check if the character is _53 (ASCII 81)
        JZ paddleDown

        .checkW
        LDA bar
        SUB _57 ;; Check if the character is _57 (ASCII 87)
        JZ paddleUp

        .clearInput
        LDA inputChar
        SUB inputChar ;; Clear the input character
        STA inputChar ;; Store the cleared value back
        STA foo
        STA bar
        BX paddleLeftY
    }
}

int ballLeft() {
    __asm {
        ;; Delete the ball at the current position
        PIKX ballX
        PIKY ballY
        PXYD 00

        ;; Check if the ball is at the left edge
        LDA ballX
        SUB _01 ;; Check if ballX is 1 (left edge)
        STA ballX


        ;; Ball is not at the left edge, continue
        BX _00
    }
}

int ballRight() {
    __asm {
        ;; Delete the ball at the current position
        PIKX ballX
        PIKY ballY
        PXYD 00

        ;; Check if the ball is at the left edge
        LDA ballX
        ADD _01 ;; Check if ballX is 1 (left edge)
        STA ballX


        ;; Ball is not at the left edge, continue
        BX _00
    }
}

int ballDown() {
    __asm {
        ;; Delete the ball at the current position
        PIKX ballX
        PIKY ballY
        PXYD 00

        ;; Check if the ball is at the left edge
        LDA ballY
        ADD _01 ;; Check if ballX is 1 (left edge)
        STA ballY

        ;; Ball is not at the left edge, continue
        BX _00
    }
}

int ballUp() {
    __asm {
        ;; Delete the ball at the current position
        PIKX ballX
        PIKY ballY
        PXYD 00

        ;; Check if the ball is at the left edge
        LDA ballY
        SUB _01 ;; Check if ballX is 1 (left edge)
        STA ballY

        ;; Ball is not at the left edge, continue
        BX _00
    }
}

int updateBallPosition() {
    __asm {
        LDA ballDirectionX
        SUB _02
        JZ ballLeft

        LDA ballDirectionX
        SUB _01
        JZ ballRight

        LDA ballDirectionY
        SUB _02
        JZ ballUp

        LDA ballDirectionY
        SUB _01
        JZ ballDown

    }
}

int drawBall() {
    __asm {
        ;; Draw the ball at its current position
        PIKX ballX
        PIKY ballY
        PXYD ff
        BX _00 ;; Return to main loop
    }
}

int loop(){
    __asm {
        ;;B drawNet
        B checkInput
        B drawPaddles
        ;;B updateBallPosition
        ;;B drawBall
        JMP loop
    }
}

int main()
{

    __asm {
        B drawField
        B loop 
    }

}

