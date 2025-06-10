char O = 'O';
char K = 'K';
char newLine = '\n';

int *IN_STR_PTR = 0xfffffd00;
int IN_STR_CTR = 0;
int IN_CHAR = 0;
int *STR_PTR = 0xfffffe00;
int _A = 0xff;
int _B = 0xff;
int _IF_BYTE = 0xff;
int *IF_PTR = 0xfffffd00;
int _IF_LEFT = 0xff;
int _IF_RIGHT = 0xff;
int _AUX_1 = 0x00;

void prompt();
void insert_char();
void input();
void _if_eq();
void _if_neq();
void _if_gt();
void _if_lt();
void _if_gte();
void _if_lte();
void print_char();
void print_str();
void eval_input();
void add();
void sub();
void mul();
void div();

//Prompt: Prints "OK" and a new line
void prompt(){
    _asm{
        LDA O
        OUT
        LDA K
        OUT
        LDA newLine
        OUT
        BX _00
    }
}

//Inserts a character into the string pointer and increments the pointer
void insert_char(){
    __asm{
        PTRL IN_STR_PTR
        STA IN_CHAR
        PTRI IN_STR_PTR
        BX _00
    }
}


void input(){
    __asm{
        DECE
        STA IN_CHAR
        B insert_char
        SUB newLine       ;; Check if enter pressed
        JZ input_done_nl  ;; If enter pressed, exit input
        LDA IN_STR_CTR    ;; else check if max lenght reached
        SUB _80           ;; Check if max lenght reached
        JZ input_done_80  ;; If max lenght reached, exit input
        ADD _81           ;; else, increment counter    
        STA IN_STR_CTR
        .input_done_80    ;; If max lenght reached, clean and exit input
        ADD _80
        BX
        .input_done_nl    ;; If enter pressed, clean and exit input
        ADD newLine
        BX _00 
    }
}

void _if_eq(){
    __asm{
        LDA _IF_LEFT
        SUB _IF_RIGHT

        JZ _if_eq_true

        JMP _if_eq_false

        .if_eq_true
        ADD _IF_RIGHT
        PTRB IF_PTR
        BX _01

        .if_eq_false
        ADD _IF_RIGHT

        .if_eq_end
        BX _00
    }
}

void print_char(){
    __asm{
        PTRL IN_STR_PTR
        OUT
        BX _00
    }
}

void print_str(){
    __asm{
        PTRL IN_STR_PTR
        OUT
        PTRI
        PTRS IN_STR_PTR
        PTRL IN_STR_PTR
        SUB newLine
        JZ print_str_end
        JMP print_str
        .print_str_end
        BX _00
    }
}


void add(){
    __asm{
        .add
        PTRL IN_STR_PTR
        ADD _AUX_1
        STA _AUX_1
        OUT 
        BX _AUX_1
    }
}

void sub(){
    __asm{
        .sub
        PTRL IN_STR_PTR
        SUB _AUX_1
        STA _AUX_1
        OUT 
        BX _AUX_1
    }
}

void mul(){
    __asm{
        .mul
        PTRL IN_STR_PTR
        MUL _AUX_1
        STA _AUX_1
        OUT 
        BX _AUX_1
    }
}

void div(){
    __asm{
        .div
        PTRL IN_STR_PTR
        DIV _AUX_1
        STA _AUX_1
        OUT 
        BX _AUX_1
    }
}

void eval_input(){
    __asm{
        .read_char
        PTRL IN_STR_PTR
        // IF not " "
        STA _AUX_1
        SUB _20
        JZ continue_eval
        // IF not "0"
        PTRL IN_STR_PTR
        PTRI IN_STR_PTR
        SUB _80
        JZ end
        ADD _80
        LDA _00
        STA IN_STR_D
        JMP eval_char

        .eval_char
        
        .eval_print
        // IF 'P'
        SUB _80
        JNZ clean_flags
        JZ eval_p
        .eval_p
        ADD _80
        // IF 'R'
        SUB _82
        JNZ clean_flags
        JZ eval_r

        .eval_r
        ADD _82
        // IF 'I'
        SUB _73
        JNZ clean_flags
        JZ eval_i
        
        .eval_i
        ADD _73
        // IF 'N'
        SUB _78
        JNZ clean_flags
        JZ eval_n
        
        .eval_n
        ADD _78
        // IF 'T'
        SUB _84
        JNZ clean_flags
        JZ eval_t
        
        .eval_t
        ADD _84
        // IF ' '
        SUB _20
        JNZ clean_flags
        JZ eval_space
        
        .eval_space
        ADD _32
        JNZ clean_flags
        JZ _print_str
        
        .clean_flags
        LDA _00
        ADD _01

        .sum_eval_print ;; EVALS a + b
        .eval_a_op_b
        PTRL IN_STR_PTR
        STA _A
        LDA _IN_STR_D
        ADD _1
        // IF +, -, *, /
        .eval_add
        SUB _43
        JZ add
        ADD _43
        .eval_sub
        SUB _45
        JZ sub
        ADD _45
        .eval_mul
        SUB _42
        JZ mul
        ADD _42
        .eval_div
        SUB _47
        JZ div
        ADD _47
        LDA _00
        ADD _01
                
        .continue_eval
        ADD _20
        LDA IN_STR_D
        ADD _1
        STA IN_STR_D
        JMP read_char
        
        .end
        BX _00
    }
}
    


int main(){
    __asm{
        .test
        PTRL IN_STR_PTR
        .basic_flow
        B prompt
        B input
        B eval_input
        JMP basic_flow
    }
}