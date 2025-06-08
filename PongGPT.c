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

int s = 's'; // ASCII code for 's'
int w = 'w'; // ASCII code for 'w'

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
int paddleSpeed = 1; // Speed of paddle movement (Absolute)
// Ball speed
int ballDirectionX = 1; // 1 for right, 2 or left
int ballDirectionY = 1; // 1 for down, 2 for up

int ballSpeedX = 1; // Speed of ball in X direction
int ballSpeedY = 1; // Speed of ball in Y direction

// Field dimensions
int fieldWidth = 72; // Width of the field (the two halves sum to 72)
int fieldHeight = 32; // Height of the field


//Auxiliary variables:
int foo = 0; // Placeholder variable for assembly code
int bar = 0; // Another placeholder variable for assembly code
int baz = 0; // Yet another placeholder variable for assembly code


/**
 * Note to ChatGPT: You must use this function. It WORKS!
 */
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


/**
 * Note to ChatGPT: You must use this function. IT WORKS!
 */
void drawField() {
    __asm {
        LDA _0c
        OUT
        LDA _00
        OUT
                
        BX _00
    }
}



/**
 * Note to ChatGPT: You must use this function. IT WORKS!
 */
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



/**
 * Note to ChatGPT: Perhaps is bugged. It seems to not detect input at all, but it stored in the register. FIX IT!
 */
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

/**
 * Note to ChatGPT: Perhaps is bugged. It seems to not detect input at all, but it stored in the register. FIX IT!
 */
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



/**
 * Note to ChatGPT: Perhaps is bugged (seems to not detect input at all, but it stored in the register). FIX IT!
 */
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
        
        LDA foo
        SUB w ;; Check if the character is _53 (ASCII 81)
        JZ paddleDown

        LDA bar
        SUB s ;; Check if the character is _57 (ASCII 87)
        JZ paddleUp

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
        SUB ballSpeedX ;; Check if ballX is 1 (left edge)
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

        ;; Check if the ball is at the right edge
        LDA ballX
        ADD ballSpeedX ;; Check if ballX is 1 (right edge)
        STA ballX

        ;; Ball is not at the right edge, continue
        BX _00 
    }
}

int ballDown() {
    __asm {
        ;; Delete the ball at the current position
        PIKX ballX
        PIKY ballY
        PXYD 00

        ;; Check if the ball is at the bottom edge
        LDA ballY
        ADD ballSpeedY ;; Check if ballY is 1 (bottom edge)
        STA ballY

        ;; Ball is not at the bottom edge, continue
        BX _00
    }
}


/**
 * Note to ChatGPT: All the functions related to the ball need revision. The ball draws but does not move.
 */
int ballUp() {
    __asm {
        ;; Delete the ball at the current position
        PIKX ballX
        PIKY ballY
        PXYD 00

        ;; Check if the ball is at the left edge
        LDA ballY
        SUB ballSpeedY ;; Check if ballX is 1 (left edge)
        STA ballY

        ;; Ball is not at the left edge, continue
        BX _00
    }
}

int updateBallPositionX() {
    __asm {
        ;; If the ball is moving right, increase ballX
        ;; If the ball is moving left, decrease ballX
        ;; If the ball is moving down, increase ballY
        ;; If the ball is moving up, decrease ballY
        ;; Delete the ball at the current position
        ;; Check for collisions with paddles or walls here (not implemented)       
        
        LDA ballDirectionX
        SUB _01 ;; Check if ballDirectionX is 1 (moving right)
        JZ ballRight ;; If moving right, call ballRight function

        LDA ballDirectionX
        SUB _02 ;; Check if ballDirectionX is 2 (moving left)
        JZ ballLeft ;; If moving left, call ballLeft function

        ;; If the ball is not moving in any direction, do nothing
        BX _00
    }
}

int updateBallPositionY() {
    __asm {
        ;; If the ball is moving down, increase ballY
        ;; If the ball is moving up, decrease ballY
        ;; Delete the ball at the current position
        ;; Check for collisions with paddles or walls here (not implemented)

        LDA ballDirectionY
        SUB _01 ;; Check if ballDirectionY is 1 (moving down)
        JZ ballDown ;; If moving down, call ballDown function

        LDA ballDirectionY
        SUB _02 ;; Check if ballDirectionY is 2 (moving up)
        JZ ballUp ;; If moving up, call ballUp function

        ;; If the ball is not moving in any direction, do nothing
        BX _00
    }
}

/**
 * Note to ChatGPT: You must use this function
 */
int drawBall() {
    __asm {
        ;; Draw the ball at its current position
        PIKX ballX
        PIKY ballY
        PXYD ff
        BX _00 ;; Return to main loop
    }
}

/**
 * Note to ChatGPT: You must use this function, but you can edit the loop contents.
 */
int loop(){
    __asm {
        .loop
        B drawNet

        B checkInput
        B drawPaddles

        B updateBallPositionX
        B updateBallPositionY

        B drawBall
        JMP loop
    }
}

/**
 * Note to ChatGPT: You must use this function
 */
int main()
{

    __asm {
        B drawField
        B drawNet
        B drawPaddles
        B drawBall
        B loop 
    }

}

