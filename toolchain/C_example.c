int a = 40;
int b = 2;

char *str = "Hello, World!";

int hello_world() {
    __asm {
        ; Print "Hello, World!" to the console
        LDA str0
        OUTA
        LDA str1
        OUTA
        LDA str2
        OUTA
        LDA str3
        OUTA
        LDA str4
        OUTA
        LDA str5
        OUTA
        LDA str6
        OUTA
        LDA str7
        OUTA
        LDA str8
        OUTA
        LDA str9
        OUTA
        LDA str10
        OUTA
        LDA str11
        OUTA
        LDA str12
        OUTA
        LDA _0a
        OUTA

        BX _0a
    }
}

int one_argument_function(int arg1) {
    __asm {
        POPX arg1 ;; Pop the argument from the stack
        STA arg1 ;; Store the argument in arg1
        LDA arg1 ;; Load the argument
        OUTA ;; Print the argument

        BX arg1 ;; Return from the function
    }
}

int main() {
    // Call the hello_world function
    hello_world();

    // Perform a simple addition
    int result1 = a + b;

    __asm {
        LDA result1 ;; Load the result of the addition
        OUTA ;; Print 42
    }

        // Perform a simple addition
    int result2 = a + 2;

    __asm {
        LDA result2 ;; Load the result of the addition
        OUTA ;; Print 42
    }

    int result3 = 40 + 2;

    __asm {
        LDA result3 ;; Load the result of the addition
        OUTA ;; Print 42
    }

    // this is equivalent
    one_argument_function(42);
    // to this
    /*
    __asm {
        LDIA 42 ;; Load the immediate value 42
        PSAX ;; Push the argument onto the stack
        B one_argument_function ;; Call the function
    }
    */
    
    return 0;
}