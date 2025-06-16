char a = '0'; // Global variable
char b = '5'; // Another global variable

char gt = '>'; // Global variable for greater than
char lt = '<'; // Global variable for less than
char eq = '='; // Global variable for equal to

int lesser(){
    __asm {
        LDA a // Load the value of 'a' into the accumulator
        OUTA
        LDA lt // Load the greater than symbol into the accumulator
        OUTA
        LDA b // Load the value of 'b' into the accumulator
        OUTA
        LDA _0a // Load the new line character into the accumulator
        OUTA
        BX _00
    }
}


int equal(){
    __asm {
        LDA a // Load the value of 'a' into the accumulator
        OUTA
        LDA eq // Load the equal to symbol into the accumulator
        OUTA
        LDA b // Load the value of 'b' into the accumulator
        OUTA
        LDA _0a // Load the new line character into the accumulator
        OUTA
        BX _00
    }
}

int greater(){
    __asm {
        LDA a // Load the value of 'a' into the accumulator
        OUTA
        LDA gt // Load the less than symbol into the accumulator
        OUTA
        LDA b // Load the value of 'b' into the accumulator
        OUTA
        LDA _0a // Load the new line character into the accumulator
        OUTA
        BX _00
    }
}

int main() {

    __asm {
        DECE // Read input character into 'a'
        STA a // Store the value in global variable 'a'
    }

    if (a < b) {
        lesser();
    } else if (a == b) {
        equal();
    } else {
        greater();
    }

    __asm {
        JMP main // Jump back to the start of the main function
    }
}