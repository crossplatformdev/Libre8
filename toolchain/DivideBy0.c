                        
int dividend = 42;      // Declare and initialize variable 'dividend' with value 42
int divisor = 0;        // Declare and initialize variable 'divisor' with value 0
int quotient = 0;       // Declare and initialize variable 'quotient' with value 0

char e = 'e';           // Declare a character variable 'e' with value 'e'
char r = 'r';           // Declare a character variable 'r' with value 'r'
char o = 'o';           // Declare a character variable 'o' with value 'o'

int error(){
    __asm {
        LDA e   // Load the address of the e letter into the accumulator
        OUT     // Output the letter 'e' to the console      
        LDA r   // Load the address of the r letter into the accumulator
        OUT     // Output the letter 'r' to the console
        LDA r   // Load the address of the r letter into the accumulator
        OUT     // Output the letter 'r' to the console
        LDA o   // Load the address of the o letter into the accumulator
        OUT     // Output the letter 'o' to the console
        LDA r   // Load the address of the r letter into the accumulator
        OUT     // Output the letter 'r' to the console
        HLT // Halt the program
        BX _OO
    }
}

int main() {
    __asm {
        LDA divisor     // Load the value of 'divisor' into the accumulator
        SUB _01         // Subtract 1 from the accumulator
        JC error        // Jump to error if divisor is zero (if there's an underflow and Carry Flag is set)

        LDA dividend    // Load the value of 'dividend' into the accumulator
        DIV divisor     // Divide 'dividend' by 'divisor'
        OUT quotient    // Output the result to the console
    }
    return 0;
}