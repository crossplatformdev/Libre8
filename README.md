# Libre-8
An 8 bit OpenSource full computer

This is <b>Libre 8</b>, an <b>8 bit OpenSource computer.</b> 

Inspired by the SAP-1 computer (Simplest As Possible). Contains 4 8-bit registers 
and it is able to address 24-bit memory addresses, having a total ammout
of 16MB of RAM.
The designs were made with <b>Logisim</b>, the microcode and the compiler
are writen in <b>Java</b>. The simulation is able to reach up to 4.1 Khz.

![Circuit Image](https://github.com/ElijaxApps/Libre-8/raw/main/CircuitImage.png)

# How to write a program for it: <br />

Currently must be programmed using Assembly like operations.

---------------------------------
| Code token | Meaning | Examples|
|------------|---------|---------|
| ;;  | This is a comment. line will be ignored                                                |;;This is a comment|
| LDA | Basic load A register. Always followed by a variable name or an memory address (up to 24 bit)|LDA one <br />LDA ff0010h|
| LDB | Same as LDA with B register |
| LDC | Same as LDA with C register |
| LDD | Same as LDA with D register |
| STA |Store contents of register A into the following address memory (also up to 24 bit)     |STA one <br />STA ff0010h|
| STB | Same as STA with B register |
| STC | Same as STA with C register |
| STD | Same as STA with D register |
| OUTA|Echoes the value of register A to LCD Screen                                           |LDA one <br />OUTA (prints value of variable "one")|
| OUTB | Same as OUTA with B register |
| OUTC | Same as OUTA with C register |
| OUTD | Same as OUTA with D register |
| DEC |Reads a byte from keyboard and stores in A register                                                            | DEC <br/> STA readChar |
| ADD |Adds the value of the following address or variable to the contents of register A                              | DEC <br/> ADD one     |	
| SUB |Substracts the value of the following address or variable to the contents of register A                        | DEC <br/> ADD one     |	
| JZ  |Jumps to the specified address or to specified address contained within a variable if Zero Flag == 0           | JZ addressToJumpTo, JZ 0000ffh    |
| JNZ |Jumps to the specified address or to specified address contained within a variable if Zero Flag != 0           | JNZ addressToJumpTo, JNZ 0000ffh  |
| JC  |Jumps to the specified address or to specified address contained within a variable if Carry Flag == 0          | JC addressToJumpTo,  JC 0000ffh   |
| JNC |Jumps to the specified address or to specified address contained within a variable if Carry Flag != 0          | JNC addressToJumpTo, JNC 0000ffh  |
| J   |Jumps always                                                                           | J addressToJumpTo                  |
| BZ  |Creates a branch (calls a function) if Zero Flag == 0                                  | BZ offsetOfFunction                |
| BNZ |Creates a branch (calls a function)  if Zero Flag != 0                                 | BNZ offsetOfFunction               |
| BC  |Creates a branch (calls a function)  Carry Flag == 0                                   | BC offsetOfFunction                |
| BNC |Creates a branch (calls a function)  if Carry Flag != 0                                | BNC offsetOfFunction               |
| B   |Creates a branch (calls a function) always it is found                                 | B offsetOfFunction                 |
| BX  |Returns a value contained in the following address                                     | BX returnValueAddress              |
| NOP | No operation. Just reads the next operation                                           | NOP                                |
| HLT | Halts the computer                                                                    | HLT                                |
| MOV | Limited, not documented support for some MOV operations. SEE THE CODE!!!              | MOV A, C                           |

#Example program:
```
;; This is a comment
;; First thing is to write .data, and variables and function offset declaration below.
.data

;; Here we can define variables or function offsets.
;; A variable declaration is as following:
;; <name> <address> <value>
one 0000ffh 01

;; A function declaration is the same without value
addOne 0001ffh

;; To start coding you have to write .code (beginnings of the program)
.code
;; Similar to .code, you can define a tag using "."
;; We are now defining .Main tag. (saving the current offset of the execution in the name of the tag)
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
# How to compile a program

To compile any code, you have to save it in a file and pass it
as an argument to <b>Compiler.java</b>. You can also simply name it "main.as" 
and place it on root folder of the project. If you run <br/>
Compiler.java on eclipse it will find out.


# How to run the program inside the schematic with Logisim

To run the simulation, open oe86.circ with Logisim. The file resides
inside logisim folder. Once the file is loaded, click on labeled RAM component,
and load the program you previously compiled on the component<br/>

Then you can Enable simulation and run clock. 

# How to write custom instructions
There are a total of 256 possible operation codes. Even you can create an operation code
able to fetch complex instructions, formed by several opcodes concatenated.

So there is the other java file, Microcode.java. Within it are defined the instruction opcode
in this format (0xab00). The opcode is just ab, but you need to write the trailing zeroes to
make the program work.

Once you have your opcode, write the following inside the main method (anywhere just below any previpus 
create<Operation>() invocations would do) the following sentences:
```
setOffset(OPCODE_CONST, icuadrant);
write(Signals.CO + Signals.MII);
write(Signals.RO + Signals.II + Signals.CE);
... your custom signaling goes here ...
write(Signals.clpcr);  
```

# Signals description
| Signal Name     |     Abbreviation  | Function         |
|-----------------|-------------------|------------------|
|  r0,r1,r2,r3    |         -         | Controls register operations. See <b>Registers</b>                                                                              |
|  Flags In       |        FI         | Enables input to flags register                                                                                                 |
|  Memory In      |     MI0, MI2      | Controls Memory Address Register, in other words, is the pointer of the RAM                                                     |
|  Halt Signal    |       HALT        | Halts the computer                                                                                                              |
| ALU Substraction|     ALU_SUB       | Enables SUBSTRACT operation in the ALU. Use in conjuntion with Signals.SUM (to connect registers A + B), and FI (to set flags)  |
| ALU Addition    |     ALU_EOUT      | Enables Addition operation in the ALU. The second point is exactly as above                                                     |
| Console Out     |       COUT        | Enables LCD console register. It is just a cursor, with no knowledge of its own position if you do not track it by yourself     |
| Instruction In  |        II         | Loads from the bus to the Instruction Register                                                                                  |
| Jump Registers  |      J0, J2       | Enables the load from the bus to the Jump registers (COUNTER)                                                                   |
| RAM In          |        RI         | Enable RAM load from the bus                                                                                                    |
| RAM Out         |        RO         | Enable RAM Output to the bus                                                                                                    |
| Clear Program Counter |  clpcr      | Resets the instruction counter, used for microcode operations.                                                                  |
| Count Enable          |      CE     | Increments the program counter by one in the next cicle                                                                         |
| Counter Out           |      CO     | Outputs the current program counter value to the bus                                                                            |
| Stack operations      |LR0, LR2, LRO| Controls the stack selection (LR0, LR2) and stack output utput enable (LRO). You can only echo one stack at a time.             |
| One Cycle Memory IN   |    MII      | Special operation for transfer program counter register values to memory address register                                       |
|Stack pointer increment|    CPP      | Increments the stack pointer. Useful after a push operation on the stack                                                        |
|Stack pointer decrement|    CMM      | Decrements the stack pointer. Useful before a pop operation on the stack                                                        |
| Keyboard Out          |    KBO      | Copy Keyboard component register to cursor                                                                                      |
| Keyboard In           |    KBI      | Enables Keyboard read                                                                                                           |

# Flags
· ```CARRY``` is up when an overflow has occured while substracting or adding. <br/>
· ```ZERO```  is up when the ALU value is zero while substracting or adding.<br/>
