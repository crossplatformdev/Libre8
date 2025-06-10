;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Push, Pop, and also, LDIx
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; This program shows how to use direct load 
;; So declaring values is no longer needed to
;; inject them into the registers

;; LDIA Loads 10 into REG A
LDIA 42h;

;; Testing MOVs
MOV A, B;
MOV A, C;
MOV A, D;

;;
PUSH;

B testParameterPassing;

.testParameterPassing
POP;
DEC;

BX