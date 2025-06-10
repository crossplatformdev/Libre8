// Type your code here, or load an example.
#include "Libre8.h";

#ifndef PI
#define PI 3.141598
#endif

/* Function declaration */
int sum(int num);

int sum(int num) {
    return num + num;
}

int main(){
	/* Local variable definition */
    int z = 0x31;
    for(int i = 0 ; i < 3; i++){
        //Calling a function
        int ii = sum(i);
        z = ii + PI;

        printf("Value is: %d\n",z)
    }
    return z;
}
