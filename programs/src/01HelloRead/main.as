;;;;;;;;;;;;;;;;;
;; Hello Read
;;;;;;;;;;;;;;;;;


;; The following program asks for your name length (up to 9)
;; then asks your name, then prints "Hello, <name>!"
.data

H		0000ffh	'H'
e		0000feh 'e'
l		0000fdh 'l'
o		0000fch	'o'
comma 	0000fbh ','
space 	0000fah ' '
W   	0000f9h 'W'
r   	0000f8h 'r'
d   	0000f7h 'd'
excl 	0000f6h '!'
cReturn 0000f5h 0a

n		0000f3h	'n'
g		0000f2h	'g'
t		0000f1h	't'
h		0000f0h	'h'

colon		0000efh	':'
readValue	0000eeh	00
toInt		0000edh 30

length		00ffech 00
one 	00ffebh 01

m		0000eah 'm'
a		0000e9h 'a'


printHello	    010000h
printAskLength  020000h
printAskName    030000h
printName       040000h
printEnd		050000h
read			0f0000h
readName		0e0000h
endRead			0d0000h
subOne			0c0000h

name1		0400ffh 00
name2		0400feh 00
name3		0400fdh 00
name4		0400fch 00
name5		0400fbh 00
name6		0400fah 00
name7		0400f9h 00
name8		0400f8h 00
name9		0400f7h 00


;; Start of coding
.code

.Main
;; Call to the function printHelloWorld.
;; It will jump to printHelloWorld offset.
;; Will return to this run point when finds BX

B printAskLength
B read
;;We demote read value (char) to (int)
LDA readValue
SUB toInt
STA length
B printAskName
B readName
B printHello
B printName
B printEnd
;; Start over Main
JMP Main

.read
DEC
STA readValue
BX readValue

.endRead
BX 000000h

.readName
B read
STA name1
BNZ subOne
BNZ read
STA name2
BNZ subOne
BNZ read
STA name3
BNZ subOne
BNZ read
STA name4
BNZ subOne
BNZ read
STA name5
BNZ subOne
BNZ read
STA name6
BNZ subOne
BNZ read
STA name7
BNZ subOne
BNZ read
STA name8
BNZ subOne
BNZ read
STA name9
BX 000000h

.subOne
LDA length
SUB one
STA length
BX length

.printAskLength
LDA l
OUTA
LDA e
OUTA
LDA n
OUTA
LDA g
OUTA
LDA t
OUTA
LDA h
OUTA
LDA colon
OUTA
LDA cReturn
OUTA
BX 000000h

.printAskName
LDA n
OUTA
LDA a
OUTA
LDA m
OUTA
LDA e
OUTA
LDA colon
OUTA
LDA cReturn
OUTA
BX 000000h

;; Function definition at above memory offset
.printHello
LDA H
OUTA
LDA e
OUTA
LDA l
OUTA
LDA l
OUTA
LDA o
OUTA
LDA comma
OUTA
LDA space
OUTA
BX 000000h

.printName
LDA name1
OUTA
LDA name2
OUTA
LDA name3
OUTA
LDA name4
OUTA
LDA name5
OUTA
LDA name6
OUTA
LDA name7
OUTA
LDA name8
OUTA
LDA name9
OUTA
BX 000000h

.printEnd
LDA excl
OUTA
LDA cReturn
OUTA
BX 000000h