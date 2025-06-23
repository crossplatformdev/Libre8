char a = 0;     // Global variable for input character

char e = 'e';   // Global variable for error
char r = 'r';   // Global variable for error
char o = 'o';   // Global variable for error
char k = 'k';   // Global variable for ok

/**
 * Prints "ok"
 */
int ok() {
    __asm {
        LDA o   // Load the 'o' character into the accumulator
        OUTA    // Output the character
        LDA k   // Load the 'k' character into the accumulator
        OUTA    // Output the character
        LDA _0a // Load the new line character into the accumulator
        OUTA    // Output the new line character
        BX _00  // Return to the calling function
    }
}

/**
 * Prints "error"
 */
int error() {
    __asm {
        LDA e   // Load the 'e' character into the accumulator
        OUTA    // Output the character
        LDA r   // Load the 'r' character into the accumulator
        OUTA    // Output the character
        LDA r   // Load the 'r' character into the accumulator
        OUTA    // Output the character
        LDA o   // Load the 'o' character into the accumulator
        OUTA    // Output the character
        LDA r   // Load the 'r' character into the accumulator
        OUTA    // Output the character
        LDA _0a // Load the new line character into the accumulator
        OUTA    // Output the new line character
        BX _00  // Return to the calling function
    }
}

/**
 * Main function
 */
int main() {
    __asm {
        DEC         // Read input character into 'a'
        STA a       // Store the value in global variable 'a'
    }

    // Check if 'a' is not zero
    if (a != 0) {
        ok();       // Call the ok function if 'a' is not zero
    } else {
        error();    // Call the error function if 'a' is zero
    }

    __asm {
        JMP main    // Jump back to the start of the main function
    }
    return 0;
}