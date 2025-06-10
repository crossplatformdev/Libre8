;;
;; This is the first program accomplishing passing arguments.
;; 


.data
;; Define something
one ffffffh 'a'
;; Define function1, with one argument called local_one
funcion1 000200h local_one

;;MAIN
.code
;; Load argument
LDA one
;; Push argument in the Stack
PSAX one
;; Call branch
B funcion1
;; Roll Over again
JMP 000000h


.funcion1
;; Argument retrieve
POPX 
;; Store
STA local_one
;; Load
LDA local_one
;; Print
OUT
;;Return
BX 000000h