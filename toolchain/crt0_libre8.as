.data
; simple C stack for Libre8 C ABI prototype
stack_bottom:
    .byte 0xFF, 0xFF  ; top of RAM (placeholder, adjust to real map)

.code
.Main:
    ; initialize software SP variable if you use one, then call main
    ; For now this is just documentation; real implementation will
    ; depend on the ABI described in README_LIBRE8_BACKEND.md.

    ; call main()
    ;   TCC backend should eventually emit something like:
    ;   CALL main

    ; on return, halt CPU (pseudo-op; replace with a busy loop or
    ; OUT opcode depending on Libre8 conventions)
    ;   HLT

    BX FFFFFFFF     ; temporary: return from reset vector
