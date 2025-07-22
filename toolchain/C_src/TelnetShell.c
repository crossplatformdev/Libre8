char space = ' ';
char input = 0;

int writeUser(){
    __asm {
        BX _00
    }
}

int waitForInput() {
    __asm {
        OUTM    ;; Output the prompt
        SUB _02
        JB waitForInput ;; Wait for input character
        BX _00
    }
}

int writeChat(){
    __asm {
        B waitForInput
        BX _00
    }
}

int readInput() {
    __asm {
        DEC ;; Read input character into 'a'
        STA input   ;; Store the value in global variable 'a'       
        LDA input 
        OUT
        SUB _0a
        JNZ readInput ;; If input is not '0', repeat reading input
        BX _00
    }
}


int printResponse() {
    __asm {
        OUTT   ;; Output the response
        ADD _00
        JNZ printResponse
        BX _00
    }
}

int main() {
    writeUser();
    readInput();
    writeChat();
    printResponse();
    __asm {
        JMP main
    }
    return 0;
}