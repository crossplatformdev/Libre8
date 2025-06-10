;;;;;;;;;;;;;;;;
;; DATA BEGIN;;;
;;;;;;;;;;;;;;;;
.data
;; CASTS
__ERRORCODE__ 00fffeffh ff
fromCharToInt 00fffe01h 30
fromIntToChar 00fffe00h 30

{ gliphs }

;; RT Functions
__ERROR__ 00110000h
__MAIN__  00133700h

;;;;;;;;;;;;;;;;
;; DATA BEGIN ;;
;;;;;;;;;;;;;;;;
{ data }
;;;;;;;;;;;;;;;
;; DATA END ;;
;;;;;;;;;;;;;;;

;; CODE BEGIN
.code
B __MAIN__

{ code }

B __ERROR__
HLT
;;;;;;;;;;;;;;;
;; FUNCTIONS ;;
;;;;;;;;;;;;;;;
.__ERROR__
LDA __ERRORCODE__
OUTA
BX

.printf
POPX
.__print_f_jmp
LDA arg1
SUB _00
JNZ __print_f_jmp
