/**
 * This program produces one Integer Overflow and one Integer Underflow.
 */
int main() {
    __asm {
        LDA _ff     ;; Load the value 255 into the accumulator
        ADD _01     ;; Add 1 to the accumulator

        LDA _00     ;; Load the value 0 into the accumulator
        SUB _01     ;; Subtract 1 from the accumulator
    }
}