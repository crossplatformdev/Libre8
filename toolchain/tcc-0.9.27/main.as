.code
.main
    ;; prolog (stack setup pending)
    ;; inline printf expansion
    LDIA 48h
    OUT
    LDIA 65h
    OUT
    LDIA 6Ch
    OUT
    LDIA 6Ch
    OUT
    LDIA 6Fh
    OUT
    LDIA 2Ch
    OUT
    LDIA 20h
    OUT
    LDIA 57h
    OUT
    LDIA 6Fh
    OUT
    LDIA 72h
    OUT
    LDIA 6Ch
    OUT
    LDIA 64h
    OUT
    LDIA 21h
    OUT
    LDIA 20h
    OUT
    LDIA 2Ah
    OUTD
    LDIA 21h
    OUT
    LDIA 0Ah
    OUT
    LDIA 00h
    BX _00
