/**
 * Comprehensive C example for Libre-8 toolchain
 * This example demonstrates variable declarations, arithmetic operations, and inline assembly.
 * It is designed to be compiled with the Libre-8 toolchain.
 * C language standard: C17 
 * Author: Elías A. Angulo Klein
 */


/*
* All variables are global, always. 
* Each one of the function's arguments and inner variables are treated by the
* compiler as global variables too.
* There is a stack, used to track the depth in the function calls. Increments with a branch call and decrements with a return.
* Supported types are always 8 bytes (void, bool, int, char). Any variable must fit any of this types.
* There are no pointers, arrays, structs, nor floating point types.
* Control structures supported: if, if-else, while, do-while, for, switch case.
*/

//Global variables. Use const just to avoid warnings, but they are not really constant.
int a = 0;
int b = 1;
int c = 1;

int n = 1;
int fibonacci(int n){
    if (n == 0){
        return n;
    } else if(n == 1){
        return n;
    } else {
        int n_minus_1 = fibonacci(n - 1);
        int n_minus_2 = fibonacci(n - 2);
        int result = n_minus_1 + n_minus_2;
        return result;
    }
}

int is_prime(int n){
    // Doing substractions and comparisons only
    if (n <= 1){
        return 0; // Not prime
    }
    for (int i = 2; i < n; i = i + 1){
        int temp = n;
        while (temp >= i){
            temp = temp - i;
        }
        if (temp == 0){
            return 0; // Not prime
        }
    }
    return 1; // Is prime
}

int prime_nth(int n){
    int count = 0;
    int candidate = 1;
    while (count < n){
        candidate = candidate + 1;
        if (is_prime(candidate) == 1){
            count = count + 1;
        }
    }
    return candidate;
}

int main(){
    for (int i = 0; i < 10; i = i + 1){
        a = prime_nth(i);
        b = fibonacci(i);
        // Inline assembly to output the result (assuming OUTT outputs the value in register A)
        printf("Prime %d: %d, Fibonacci %d: %d\n", i, a, i, b);
    }
    return 0;
}
