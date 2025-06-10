// Loads 40 + 2 into variable 'result' and prints it to the console.
int a = 40; // Declare and initialize variable 'a' with value 40
int b = 2;  // Declare and initialize variable 'b' with value 2
int result = 0;

int main() {
    // Print the result to the console

    result = a + b;
    
    __asm {
        LDA result  // Load the value of 'result' into the accumulator
        OUT result
    }
    return 0;
}