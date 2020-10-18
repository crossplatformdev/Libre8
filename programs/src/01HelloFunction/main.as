;;;;;;;;;;;;;;;;;
;; Hello Function
;;;;;;;;;;;;;;;;;


;; The following program just prints "Hello, World!"
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

printHelloWorld 010000h

;; Start of coding
.code

.Main
;; Call to the function printHelloWorld.
;; It will jump to printHelloWorld offset.
;; Will return to this run point when finds BX

B printHelloWorld
;; Start over Main
JMP Main



;; Function definition at above memory offset
.printHelloWorld
LDA H
OUTA
LDA e
OUTA
LDA l
OUTA
OUTA
LDA o
OUTA
LDA comma
OUTA
LDA space
OUTA
LDA W
OUTA
LDA o
OUTA 
LDA r
OUTA
LDA l
OUTA
LDA d
OUTA
LDA excl
OUTA
LDA cReturn
OUTA
BX