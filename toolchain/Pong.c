int ballX = 0 + 40;
int ballY = 0 + 12;
int ballDX = 0 + 1;
int ballDY = 0 + 1;

int paddle1Y = 0 + 10;
int paddle2Y = 0 + 10;

int score1 = 0;
int score2 = 0;

int _0 = 0;
int _1 = 0 + 1;
int _3 = 0 + 3;
int _12 = 0 + 12;
int _24 = 0 + 24;
int _40 = 0 + 40;
int _78 = 0 + 78;
int _NEG1 = 0 - 1;

int _temp1 = 0;
int _temp2 = 0;

int draw() {
    int ret = 0;
    __asm {
        ;; Dibujar posiciones
        LDA _ballX
        OUT ballX
        LDA _ballY
        OUT ballY
        LDA _paddle1Y
        OUT paddle1Y
        LDA _paddle2Y
        OUT paddle2Y
        LDA _score1
        OUT score1
        LDA _score2
        OUT score2

        ;; Finalizar función
        LDA _0
        STA ret
        BX ret
    }
}

int input() {
    int ret = 0;
    __asm {
        ;; Leer entrada simulada
        KBI
        STA paddle1Y
        KBI
        STA paddle2Y

        ;; Finalizar función
        LDA _0
        STA ret
        BX ret
    }
}

int logic() {
    int ret = 0;
    __asm {
        ;; ballX = ballX + ballDX
        LDA ballX
        LDB ballDX
        ADD B
        STA ballX

        ;; ballY = ballY + ballDY
        LDA ballY
        LDB ballDY
        ADD B
        STA ballY

        ;; Rebote si ballY <= 0 o >= 24
        LDA ballY
        LDB _0
        SUB
        JZ reflectY
        JB reflectY
        ADD B

        LDA ballY
        LDB _24
        SUB
        JZ reflectY
        JNB reflectY
        ADD B
        JMP skip_reflectY

    reflectY:
        LDA _0
        LDB ballDY
        SUB
        STA ballDY

    skip_reflectY:

        ;; Colisión izquierda: ballX <= 1
        LDA ballX
        LDB _1
        SUB
        JZ left_check
        JB left_check
        ADD B
        JMP right_check

    left_check:
        ;; ballY >= paddle1Y
        LDA ballY
        LDB paddle1Y
        SUB
        JZ left_hit
        JNB left_hit
        ADD B

        ;; ballY <= paddle1Y + 3
        LDA paddle1Y
        LDB _3
        ADD B
        STA _temp1
        LDA ballY
        LDB _temp1
        SUB
        JZ left_hit
        JB left_hit
        ADD B
        JMP left_miss

    left_hit:
        LDA _0
        LDB ballDX
        SUB
        STA ballDX
        JMP logic_end

    left_miss:
        LDA score2
        LDB _1
        ADD B
        STA score2

        LDA _40
        STA ballX
        LDA _12
        STA ballY
        LDA _1
        STA ballDX
        JMP logic_end

    right_check:
        ;; ballX >= 78
        LDA ballX
        LDB _78
        SUB
        JZ right_check2
        JNB right_check2
        ADD B
        JMP logic_end

    right_check2:
        ;; ballY >= paddle2Y
        LDA ballY
        LDB paddle2Y
        SUB
        JZ right_hit
        JNB right_hit
        ADD B

        ;; ballY <= paddle2Y + 3
        LDA paddle2Y
        LDB _3
        ADD B
        STA _temp2
        LDA ballY
        LDB _temp2
        SUB
        JZ right_hit
        JB right_hit
        ADD B
        JMP right_miss

    right_hit:
        LDA _0
        LDB ballDX
        SUB
        STA ballDX
        JMP logic_end

    right_miss:
        LDA score1
        LDB _1
        ADD B
        STA score1

        LDA _40
        STA ballX
        LDA _12
        STA ballY
        LDA _NEG1
        STA ballDX

    logic_end:
        LDA _0
        STA ret
        BX ret
    }
}

int loop() {
    int ret = 0;
    __asm {
    loop_start:
        LDA _input
        B input
        LDA _logic
        B logic
        LDA _draw
        B draw
        LDA _loop_start
        JMP loop_start

        ;; (Nunca se ejecuta, pero por convención)
        LDA _0
        STA ret
        BX ret
    }
}

int main() {
    int ret = 0;
    __asm {
        LDA _loop
        B loop
        LDA _0
        STA ret
        BX ret
    }
}
