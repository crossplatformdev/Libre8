/**
 * Recursive function to demonstrate what a stack overflow looks like.
 */
int main() {
    __asm {
        B main
    }
    return 0;
}