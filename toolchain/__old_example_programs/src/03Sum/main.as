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
DEC 
SUB one
STA result
BX result
HLT