__asm {
	//Declare label .main
	.main
	
}


void helloWorld(char arg1, char arg2, char arg3);

char arg1 = 'H';
char arg2 = 'i';
char arg3 = '!';


void helloWorld(char arg1, char arg2, char arg3) {
	//output with OUT instruction when its parsed by CompilerV3
	__asm {
		LDA arg1
		OUT
		LDA arg2
		OUT
		LDA arg3
		OUT
	}	

	return;
}

int main() {
	//output with OUT instruction when its parsed by CompilerV3
	//Declare label .main
	helloWorld(arg1, arg2, arg3);
	
	__asm {
		JMP .main
	}
	
}