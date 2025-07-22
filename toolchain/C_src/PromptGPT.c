char C = 'C';
char h = 'h';
char a = 'a';
char t = 't';
char G = 'G';
char P = 'P';
char T = 'T';

char space = ' ';

char prompt = '>';

char U = 'U';
char s = 's';
char e = 'e';
char r = 'r';

char input = 0;

int writeUser(){
    __asm {
        LDA _0a
        OUT     ;; Output '\n'
        LDA U
        OUT     ;; Output 'U'
        LDA s
        OUT     ;; Output 's'
        LDA e
        OUT     ;; Output 'e'   
        LDA r
        OUT     ;; Output 'r'
        
        LDA space
        OUT     ;; Output space

        LDA prompt
        OUT     ;; Output '>'

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
        LDA _0a
        OUT     ;; Output '\n'
        LDA C
        OUT     ;; Output 'C'
        LDA h
        OUT     ;; Output 'h'
        LDA a
        OUT     ;; Output 'a'
        LDA t
        OUT     ;; Output 't'
        
        LDA space
        OUT     ;; Output space

        LDA G
        OUT     ;; Output 'G'
        LDA P
        OUT     ;; Output 'P'
        LDA T
        OUT     ;; Output 'T'

        LDA space
        OUT     ;; Output space
        LDA prompt
        OUT     ;; Output '>'

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
        OUTT
        ADD _00
        JNZ printResponse
        LDA _0a
        OUT     ;; Output '\n'
        OUT
        OUT
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