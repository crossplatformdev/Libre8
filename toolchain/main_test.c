
//Global variables
char arg1 = 'H';
char arg2 = 'i';
char arg3 = '!';

//Function prototype
void helloWorld();

//Function definition
void helloWorld() {
	//output with OUT instruction when its parsed by CompilerV3
	__asm {

		//IF A == _B
		LDA _A
		SUB _B
		JZ _IF1
		JMP _ELSE1
		._IF1:
		OUT
		JMP _END1
		._ELSE1:
		._END1:

		LDA arg1
		OUT
		LDA arg2
		OUT
		LDA arg3
		OUT
		BX arg1
	}	
}

//Main function
int main() {
	arg1 = arg1 + 1;
	//Function call
	helloWorld();
	
	//Libre 8 assembly can be embedded in C code
	__asm {
		JMP __MAIN__
	}
	
}

