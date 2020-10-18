# Libre-8
An 8 bit OpenSource full computer

This is <b>Libre 8</b>, an <b>8 bit OpenSource computer.</b><br /> 
The designs were made with <b>Logisim</b>, the microcode and the compiler<br />
are writen in <b>Java</b>.<br />


# How to write a program for it: <br />

Currently must be programmed using Assembly like operations.

---------------------------------
| Code token | Meaning | Examples|
|------------|---------|---------|
| ;;  |This is a comment. line will be ignored                                                |;;This is a comment|
| LDA |Basic Load A register. Is followed by a variable name or an up to 24 bit memory address|LDA one <br />LDA ff0010h|
| LDB | Simmilar with B register |
| LDC | Simmilar with C register |
| LDD | Simmilar with D register |
| STA |Store contents of register A into the following address memory (also up to 24 bit)     |STA one <br />STA ff0010h|
| STB | Simmilar with B register |
| STC | Simmilar with C register |
| STD | Simmilar with D register |
| OUTA|Echoes the value of register A to LCD Screen                                           |LDA one <br />OUTA (prints value of variable "one")|
| OUTB | Simmilar with B register |
| OUTC | Simmilar with C register |
| OUTD | Simmilar with D register |
| DEC |Reads a byte from keyboard and stores in A register                                    | DEC <br/> STA readChar |
| ADD |Adds the value of the following address or variable to the contents of register A      | DEC <br/> ADD one     |	
| SUB |Substracts the value of the following address or variable to the contents of register A| DEC <br/> ADD one     |	
| JZ  |Jumps to the folowing address if Zero Flag == 0                                        | JZ addressToJumpTo    |
| JNZ |Jumps to the folowing address if Zero Flag != 0                                        | JNZ addressToJumpTo   |
| JC  |Jumps to the folowing address if Carry Flag == 0                                       | JC addressToJumpTo    |
| JNC |Jumps to the folowing address if Carry Flag != 0                                       | JNC addressToJumpTo   |
| J   |Jumps always                                                                           | J addressToJumpTo     |
| BZ  |Creates a branch (calls a function) if Zero Flag == 0                                  | BZ offsetOfFunction   |
| BNZ |Creates a branch (calls a function)  if Zero Flag != 0                                 | BNZ offsetOfFunction  |
| BC  |Creates a branch (calls a function)  Carry Flag == 0                                   | BC offsetOfFunction   |
| BNC |Creates a branch (calls a function)  if Carry Flag != 0                                | BNC offsetOfFunction  |
| B   |Creates a branch (calls a function) always it is found                                 | B offsetOfFunction    |
| BX  |Returns a value contained in the following address                                     | BX returnValueAddress |


Example program:

```
;; This is a comment
;; First thing is data and function offset declaration.
.data

;; Here we can define variables or function offsets.
;; A variable declaration is as following:
;; <name> <address> <value>
one 0000ffh 01

;; A function declaration is the same without value
addOne 0001ffh

;; The following is the start of coding
.code
.Main
;; We will load value of 'one' var (01) to A and ADD one to its value.
LDA one
;; We call addOne function
B addOne
;; Ant print it on screen
OUTA

;; We define addOne function
.addOne
ADD one
STA result
BX result
```

You can also define running offset tags to help you to jump (only backwars) to them.
```
;; Same program of above with JNC to tag
.data
one 0000ffh 01
addOne 0001ffh
.code
.Main
LDA one
;; We define a running offset before the function call
.tag
B addOne
OUTA
;; We jump to tag to add on over and over again until carry flag is set.
JNC tag

.addOne
ADD one
STA result
BX result
```

To compile it, you have to save it in a file and pass it as an argument to Compiler.java <br/>
You can also simply name it "main.as" and place it on root folder of the project. If you run
Compiler.java on eclipse it will find out.


To run the simulation, open oe86.circ with Logisim. The file resides inside logisim folder.<br>
Once the file is loaded, click on labeled RAM component, and load the program you previously compiled on the component<br/>

Then you can Enable simulation and run clock. 

