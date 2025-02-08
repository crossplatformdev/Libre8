;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Interactive menu with functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; This program displays a numerated 
;; menu. You can type then a number selecting 
;; the corresponding option. You will be asked 
;; to introduce numbers or a string

;; Data declaration
.data
msgCR	ff07ffh  0a
operand ff08ffh  00
caseCtr ff09ffh  00
charPtr ff10ffh  ff
caseCtr	ff11ffh  00
one		ff12ffh  01
zero	ff12feh  00
foo     ff13ffh  00
bar     ff13feh  00
baz		ff13fdh  00
var     ff13fch  00
toInt	ff14ffh  30
int     ff15ffh  99
slash	ff16ffh  47
percent ff17ffh  37
space   ff18ffh  20


menuStr0 0bffffh 0a
menuStr1 0bfffeh 'M'
menuStr2 0bfffdh 'E'
menuStr3 0bfffch 'N'
menuStr4 0bfffbh 'U'
menuStr5 0bfffah ':'
menuStr6 0bfff9h 0a
menuStr7 0bfff8h 0a
menuStr8 0bfff7h 0a
menuStr9 0bfff6h 09
menuStr10 0bfff5h '1'
menuStr11 0bfff4h ' '
menuStr12 0bfff3h '.'
menuStr13 0bfff2h '-'
menuStr14 0bfff1h ' '
menuStr15 0bfff0h 'A'
menuStr16 0bffefh 'D'
menuStr17 0bffeeh 'D'
menuStr18 0bffedh 20
menuStr19 0bffech 't'
menuStr20 0bffebh 'w'
menuStr21 0bffeah 'o'
menuStr22 0bffe9h 20
menuStr23 0bffe8h 'n'
menuStr24 0bffe7h 'u'
menuStr25 0bffe6h 'm'
menuStr26 0bffe5h 'b'
menuStr27 0bffe4h 'e'
menuStr28 0bffe3h 'r'
menuStr29 0bffe2h 's'
menuStr30 0bffe1h 0a
menuStr31 0bffe0h 09
menuStr32 0bffdfh '2'
menuStr33 0bffdeh 20
menuStr34 0bffddh '.'
menuStr35 0bffdch '-'
menuStr36 0bffdbh 20
menuStr37 0bffdah 'S'
menuStr38 0bffd9h 'U'
menuStr39 0bffd8h 'B'
menuStr40 0bffd7h 'S'
menuStr41 0bffd6h 'T'
menuStr42 0bffd5h 'R'
menuStr43 0bffd4h 'A'
menuStr44 0bffd3h 'C'
menuStr45 0bffd2h 'T'
menuStr46 0bffd1h 20
menuStr47 0bffd0h 't'
menuStr48 0bffcfh 'w'
menuStr49 0bffceh 'o'
menuStr50 0bffcdh 20
menuStr51 0bffcch 'n'
menuStr52 0bffcbh 'u'
menuStr53 0bffcah 'm'
menuStr54 0bffc9h 'b'
menuStr55 0bffc8h 'e'
menuStr56 0bffc7h 'r'
menuStr57 0bffc6h 's'
menuStr58 0bffc5h 0a
menuStr59 0bffc4h 09
menuStr60 0bffc3h '3'
menuStr61 0bffc2h 20
menuStr62 0bffc1h '.'
menuStr63 0bffc0h '-'
menuStr64 0bffbfh 20
menuStr65 0bffbeh 'D'
menuStr66 0bffbdh 'I'
menuStr67 0bffbch 'V'
menuStr68 0bffbbh 'I'
menuStr69 0bffbah 'D'
menuStr70 0bffb9h 'E'
menuStr71 0bffb8h 20
menuStr72 0bffb7h 't'
menuStr73 0bffb6h 'w'
menuStr74 0bffb5h 'o'
menuStr75 0bffb4h 20
menuStr76 0bffb3h 'n'
menuStr77 0bffb2h 'u'
menuStr78 0bffb1h 'm'
menuStr79 0bffb0h 'b'
menuStr80 0bffafh 'e'
menuStr81 0bffaeh 'r'
menuStr82 0bffadh 's'
menuStr83 0bffach 0a
menuStr84 0bffabh 09
menuStr85 0bffaah '4'
menuStr86 0bffa9h 20
menuStr87 0bffa8h '.'
menuStr88 0bffa7h '-'
menuStr89 0bffa6h 20
menuStr90 0bffa5h 'M'
menuStr91 0bffa4h 'U'
menuStr92 0bffa3h 'L'
menuStr93 0bffa2h 'T'
menuStr94 0bffa1h 'I'
menuStr95 0bffa0h 'P'
menuStr96 0bff9fh 'L'
menuStr97 0bff9eh 'Y'
menuStr98 0bff9dh 20
menuStr99 0bff9ch 't'
menuStr100 0bff9bh 'w'
menuStr101 0bff9ah 'o'
menuStr102 0bff99h 20
menuStr103 0bff98h 'n'
menuStr104 0bff97h 'u'
menuStr105 0bff96h 'm'
menuStr106 0bff95h 'b'
menuStr107 0bff94h 'e'
menuStr108 0bff93h 'r'
menuStr109 0bff92h 's'
menuStr110 0bff91h 0a
menuStr111 0bff90h 09
menuStr112 0bff8fh '5'
menuStr113 0bff8eh 20
menuStr114 0bff8dh '.'
menuStr115 0bff8ch '-'
menuStr116 0bff8bh 20
menuStr117 0bff8ah 'E'
menuStr118 0bff89h 'C'
menuStr119 0bff88h 'H'
menuStr120 0bff87h 'O'
menuStr121 0bff86h 20
menuStr122 0bff85h 'a'
menuStr123 0bff84h 20
menuStr124 0bff83h 's'
menuStr125 0bff82h 't'
menuStr126 0bff81h 'r'
menuStr127 0bff80h 'i'
menuStr128 0bff7fh 'n'
menuStr129 0bff7eh 'g'
menuStr130 0bff7dh 0a
menuStr131 0bff7ch 09
menuStr132 0bff7bh '6'
menuStr133 0bff7ah 20
menuStr134 0bff79h '.'
menuStr135 0bff78h '-'
menuStr136 0bff77h 20
menuStr137 0bff76h '?'
menuStr138 0bff75h '?'
menuStr139 0bff74h '?'
menuStr140 0bff73h '?'
menuStr141 0bff72h '?'
menuStr142 0bff71h 20
menuStr143 0bff70h 'B'
menuStr144 0bff6fh 'O'
menuStr145 0bff6eh 'N'
menuStr146 0bff6dh 'U'
menuStr147 0bff6ch 'S'
menuStr148 0bff6bh 20
menuStr149 0bff6ah '?'
menuStr150 0bff69h '?'
menuStr151 0bff68h '?'
menuStr152 0bff67h '?'
menuStr153 0bff66h '?'
menuStr154 0bff65h 0a
menuStr155 0bff64h 0a
menuStr156 0bff63h 0a
menuStr157 0bff62h 'P'
menuStr158 0bff61h 'l'
menuStr159 0bff60h 'e'
menuStr160 0bff5fh 'a'
menuStr161 0bff5eh 's'
menuStr162 0bff5dh 'e'
menuStr163 0bff5ch 20
menuStr164 0bff5bh 's'
menuStr165 0bff5ah 'e'
menuStr166 0bff59h 'l'
menuStr167 0bff58h 'e'
menuStr168 0bff57h 'c'
menuStr169 0bff56h 't'
menuStr170 0bff55h 20
menuStr171 0bff54h 'a'
menuStr172 0bff53h 'n'
menuStr173 0bff52h 'y'
menuStr174 0bff51h 20
menuStr175 0bff50h 'o'
menuStr176 0bff4fh 'p'
menuStr177 0bff4eh 't'
menuStr178 0bff4dh 'i'
menuStr179 0bff4ch 'o'
menuStr180 0bff4bh 'n'
menuStr181 0bff4ah ':'
menuStr182 0bff49h 20

msgStr0 c0ffffh 0a
msgStr1 c0fffeh 'P'
msgStr2 c0fffdh 'l'
msgStr3 c0fffch 'e'
msgStr4 c0fffbh 'a'
msgStr5 c0fffah 's'
msgStr6 c0fff9h 'e'
msgStr7 c0fff8h 20
msgStr8 c0fff7h 'e'
msgStr9 c0fff6h 'n'
msgStr10 c0fff5h 't'
msgStr11 c0fff4h 'e'
msgStr12 c0fff3h 'r'
msgStr13 c0fff2h 20
msgStr14 c0fff1h 'a'
msgStr15 c0fff0h 20
msgStr16 c0aaefh 'n'
msgStr17 c0aaeeh 'u'
msgStr18 c0ffedh 'm'
msgStr19 c0ffech 'b'
msgStr20 c0ffebh 'e'
msgStr21 c0ffeah 'r'
msgStr22 c0ffe9h ':'
msgStr23 c0ffe8h 20

resultStr0 d0ffffh 0a
resultStr1 d0fffeh 'R'
resultStr2 d0fffdh 'E'
resultStr3 d0fffch 'S'
resultStr4 d0fffbh 'U'
resultStr5 d0fffah 'L'
resultStr6 d0fff9h 'T'
resultStr7 d0fff8h ':'
resultStr8 d0fff7h 20

echoMsgStr0 eeffffh 0a
echoMsgStr1 eefffeh 0a
echoMsgStr2 eefffdh 'P'
echoMsgStr3 eefffch 'l'
echoMsgStr4 eefffbh 'e'
echoMsgStr5 eefffah 'a'
echoMsgStr6 eefff9h 's'
echoMsgStr7 eefff8h 'e'
echoMsgStr8 eefff7h 20
echoMsgStr9 eefff6h 'e'
echoMsgStr10 eefff5h 'n'
echoMsgStr11 eefff4h 't'
echoMsgStr12 eefff3h 'e'
echoMsgStr13 eefff2h 'r'
echoMsgStr14 eefff1h 20
echoMsgStr15 eefff0h 'a'
echoMsgStr16 eeffefh 20
echoMsgStr17 eeffeeh 's'
echoMsgStr18 eeffedh 't'
echoMsgStr19 eeffech 'r'
echoMsgStr20 eeffebh 'i'
echoMsgStr21 eeffeah 'n'
echoMsgStr22 eeffe9h 'g'
echoMsgStr23 eeffe8h 20
echoMsgStr24 eeffe7h 't'
echoMsgStr25 eeffe6h 'o'
echoMsgStr26 eeffe5h 20
echoMsgStr27 eeffe4h 'e'
echoMsgStr28 eeffe3h 'c'
echoMsgStr29 eeffe2h 'h'
echoMsgStr30 eeffe1h 'o'
echoMsgStr31 eeffe0h 3a
echoMsgStr32 eeffdfh 20

storeString0 e000ffh 00
storeString1 e000feh 00
storeString2 e000fdh 00
storeString3 e000fch 00
storeString4 e000fbh 00
storeString5 e000fah 00
storeString6 e000f9h 00
storeString7 e000f8h 00
storeString8 e000f7h 00
storeString9 e000f6h 00
storeStringA e000f5h 00
storeStringB e000f4h 00
storeStringC e000f3h 00
storeStringD e000f2h 00
storeStringE e000f1h 00
storeStringF e000f0h 00

readNum		e12342h 00
res		    ffffffh ff

;; Function headers (could point anywhere)
readIntWithEcho     100000h
readOperand			200000h
printMenu			300000h
printResultMsg		400000h
printMsg			500000h
printEchoMsg		600000h
decrementAndStoreInt				700000h
;;subFoo				800000h
countOne			900000h
sub 				121000h
add 				131000h
mul 				141000h
div 				151000h
echoString			114a11h

bonus				bbcafeh

;; Start of code
.code
;; Declaration of Main
.Main
B printMenu
B readIntWithEcho

B decrementAndStoreInt
BZ add
B decrementAndStoreInt
JB Main

B decrementAndStoreInt
BZ sub
B decrementAndStoreInt
JB Main

B decrementAndStoreInt
BZ div
B decrementAndStoreInt
JB Main

B decrementAndStoreInt
BZ mul
B decrementAndStoreInt
JB Main

B decrementAndStoreInt
BZ echoString
B decrementAndStoreInt
JB Main

B decrementAndStoreInt
BZ bonus
B decrementAndStoreInt
JB Main

JMP Main



.bonus
;; TO DO
BX 000000h

.echoString
B printEchoMsg
B readIntWithEcho
STA storeString0
B readIntWithEcho
STA storeString1
B readIntWithEcho
STA storeString2
B readIntWithEcho
STA storeString3

B readIntWithEcho
STA storeString4
B readIntWithEcho
STA storeString5
B readIntWithEcho
STA storeString6
B readIntWithEcho
STA storeString7

B readIntWithEcho
STA storeString8
B readIntWithEcho
STA storeString9
B readIntWithEcho
STA storeStringA
B readIntWithEcho
STA storeStringB

B readIntWithEcho
STA storeStringC
B readIntWithEcho
STA storeStringD
B readIntWithEcho
STA storeStringE
B readIntWithEcho
STA storeStringF

LDA msgCR
OUTA
OUTA
LDA storeString0
OUTA
LDA storeString1
OUTA
LDA storeString2
OUTA
LDA storeString3
OUTA

LDA storeString4
OUTA
LDA storeString5
OUTA
LDA storeString6
OUTA
LDA storeString7
OUTA

LDA storeString8
OUTA
LDA storeString9
OUTA
LDA storeStringA
OUTA
LDA storeStringB
OUTA

LDA storeStringC
OUTA
LDA storeStringD
OUTA
LDA storeStringE
OUTA
LDA storeStringF
OUTA
BX 000000h

;; foo: dividend, bar: divider, baz: remainder, var: division
.div
B printMsg
B readOperand
STA foo
B printMsg
B readOperand
STA bar
LDA zero
STA res

.divide
LDA res
ADD one
STA res
LDA foo
SUB bar
STA foo
JNZ divide
B printResultMsg
OUTA
BX res


;; foo: firstOp, bar: secondOp, baz: counter, var: multipication
.mul
.aMayor
B printMsg
B readOperand
STA foo
B printMsg
B readOperand
STA bar
LDA zero
STA res
.multiply
LDA res
ADD bar
STA res
LDA foo
SUB one
STA foo
JNZ multiply
B printResultMsg
OUTA
BX res




.countOne
LDA var
ADD one
STA var
BX var

.add
B printMsg
B readOperand
STA foo
B printMsg
B readOperand
STA bar
ADD foo
STA res
B printResultMsg
BX res

.sub
B printMsg
B readOperand
STA bar
B printMsg
B readOperand
STA foo
LDA bar
SUB foo
STA res
B printResultMsg
LDA res
OUTA
BX res


.decrementAndStoreInt
LDA caseCtr
SUB one
STA caseCtr
BX caseCtr

;;.subFoo
;;LDA foo
;;SUB bar
;;STA foo
;;BX foo

.readIntWithEcho
DECE
STA readNum
LDA readNum
SUB toInt
STA readNum
BX readNum

.readOperand
DECE
STA operand
LDA operand
SUB toInt
STA operand
BX operand

.printMenu
LDA msgCR
OUTA
LDA menuStr0
OUTA
LDA menuStr1
OUTA
LDA menuStr2
OUTA
LDA menuStr3
OUTA
LDA menuStr4
OUTA
LDA menuStr5
OUTA
LDA menuStr6
OUTA
LDA menuStr7
OUTA
LDA menuStr8
OUTA
LDA menuStr9
OUTA
LDA menuStr10
OUTA
LDA menuStr11
OUTA
LDA menuStr12
OUTA
LDA menuStr13
OUTA
LDA menuStr14
OUTA
LDA menuStr15
OUTA
LDA menuStr16
OUTA
LDA menuStr17
OUTA
LDA menuStr18
OUTA
LDA menuStr19
OUTA
LDA menuStr20
OUTA
LDA menuStr21
OUTA
LDA menuStr22
OUTA
LDA menuStr23
OUTA
LDA menuStr24
OUTA
LDA menuStr25
OUTA
LDA menuStr26
OUTA
LDA menuStr27
OUTA
LDA menuStr28
OUTA
LDA menuStr29
OUTA
LDA menuStr30
OUTA
LDA menuStr31
OUTA
LDA menuStr32
OUTA
LDA menuStr33
OUTA
LDA menuStr34
OUTA
LDA menuStr35
OUTA
LDA menuStr36
OUTA
LDA menuStr37
OUTA
LDA menuStr38
OUTA
LDA menuStr39
OUTA
LDA menuStr40
OUTA
LDA menuStr41
OUTA
LDA menuStr42
OUTA
LDA menuStr43
OUTA
LDA menuStr44
OUTA
LDA menuStr45
OUTA
LDA menuStr46
OUTA
LDA menuStr47
OUTA
LDA menuStr48
OUTA
LDA menuStr49
OUTA
LDA menuStr50
OUTA
LDA menuStr51
OUTA
LDA menuStr52
OUTA
LDA menuStr53
OUTA
LDA menuStr54
OUTA
LDA menuStr55
OUTA
LDA menuStr56
OUTA
LDA menuStr57
OUTA
LDA menuStr58
OUTA
LDA menuStr59
OUTA
LDA menuStr60
OUTA
LDA menuStr61
OUTA
LDA menuStr62
OUTA
LDA menuStr63
OUTA
LDA menuStr64
OUTA
LDA menuStr65
OUTA
LDA menuStr66
OUTA
LDA menuStr67
OUTA
LDA menuStr68
OUTA
LDA menuStr69
OUTA
LDA menuStr70
OUTA
LDA menuStr71
OUTA
LDA menuStr72
OUTA
LDA menuStr73
OUTA
LDA menuStr74
OUTA
LDA menuStr75
OUTA
LDA menuStr76
OUTA
LDA menuStr77
OUTA
LDA menuStr78
OUTA
LDA menuStr79
OUTA
LDA menuStr80
OUTA
LDA menuStr81
OUTA
LDA menuStr82
OUTA
LDA menuStr83
OUTA
LDA menuStr84
OUTA
LDA menuStr85
OUTA
LDA menuStr86
OUTA
LDA menuStr87
OUTA
LDA menuStr88
OUTA
LDA menuStr89
OUTA
LDA menuStr90
OUTA
LDA menuStr91
OUTA
LDA menuStr92
OUTA
LDA menuStr93
OUTA
LDA menuStr94
OUTA
LDA menuStr95
OUTA
LDA menuStr96
OUTA
LDA menuStr97
OUTA
LDA menuStr98
OUTA
LDA menuStr99
OUTA
LDA menuStr100
OUTA
LDA menuStr101
OUTA
LDA menuStr102
OUTA
LDA menuStr103
OUTA
LDA menuStr104
OUTA
LDA menuStr105
OUTA
LDA menuStr106
OUTA
LDA menuStr107
OUTA
LDA menuStr108
OUTA
LDA menuStr109
OUTA
LDA menuStr110
OUTA
LDA menuStr111
OUTA
LDA menuStr112
OUTA
LDA menuStr113
OUTA
LDA menuStr114
OUTA
LDA menuStr115
OUTA
LDA menuStr116
OUTA
LDA menuStr117
OUTA
LDA menuStr118
OUTA
LDA menuStr119
OUTA
LDA menuStr120
OUTA
LDA menuStr121
OUTA
LDA menuStr122
OUTA
LDA menuStr123
OUTA
LDA menuStr124
OUTA
LDA menuStr125
OUTA
LDA menuStr126
OUTA
LDA menuStr127
OUTA
LDA menuStr128
OUTA
LDA menuStr129
OUTA
LDA menuStr130
OUTA
LDA menuStr131
OUTA
LDA menuStr132
OUTA
LDA menuStr133
OUTA
LDA menuStr134
OUTA
LDA menuStr135
OUTA
LDA menuStr136
OUTA
LDA menuStr137
OUTA
LDA menuStr138
OUTA
LDA menuStr139
OUTA
LDA menuStr140
OUTA
LDA menuStr141
OUTA
LDA menuStr142
OUTA
LDA menuStr143
OUTA
LDA menuStr144
OUTA
LDA menuStr145
OUTA
LDA menuStr146
OUTA
LDA menuStr147
OUTA
LDA menuStr148
OUTA
LDA menuStr149
OUTA
LDA menuStr150
OUTA
LDA menuStr151
OUTA
LDA menuStr152
OUTA
LDA menuStr153
OUTA
LDA menuStr154
OUTA
LDA menuStr155
OUTA
LDA menuStr156
OUTA
LDA menuStr157
OUTA
LDA menuStr158
OUTA
LDA menuStr159
OUTA
LDA menuStr160
OUTA
LDA menuStr161
OUTA
LDA menuStr162
OUTA
LDA menuStr163
OUTA
LDA menuStr164
OUTA
LDA menuStr165
OUTA
LDA menuStr166
OUTA
LDA menuStr167
OUTA
LDA menuStr168
OUTA
LDA menuStr169
OUTA
LDA menuStr170
OUTA
LDA menuStr171
OUTA
LDA menuStr172
OUTA
LDA menuStr173
OUTA
LDA menuStr174
OUTA
LDA menuStr175
OUTA
LDA menuStr176
OUTA
LDA menuStr177
OUTA
LDA menuStr178
OUTA
LDA menuStr179
OUTA
LDA menuStr180
OUTA
LDA menuStr181
OUTA
LDA menuStr182
OUTA
LDA msgCR
OUTA
BX 000000h

.printMsg
LDA msgCR
OUTA
LDA msgStr0
OUTA
LDA msgStr1
OUTA
LDA msgStr2
OUTA
LDA msgStr3
OUTA
LDA msgStr4
OUTA
LDA msgStr5
OUTA
LDA msgStr6
OUTA
LDA msgStr7
OUTA
LDA msgStr8
OUTA
LDA msgStr9
OUTA
LDA msgStr10
OUTA
LDA msgStr11
OUTA
LDA msgStr12
OUTA
LDA msgStr13
OUTA
LDA msgStr14
OUTA
LDA msgStr15
OUTA
LDA msgStr16
OUTA
LDA msgStr17
OUTA
LDA msgStr18
OUTA
LDA msgStr19
OUTA
LDA msgStr20
OUTA
LDA msgStr21
OUTA
LDA msgStr22
OUTA
LDA msgStr23
OUTA
LDA msgCR
OUTA
BX 000000h


.printReultMsg
LDA msgCR
OUTA
LDA resultStr0
OUTA
LDA resultStr1
OUTA
LDA resultStr2
OUTA
LDA resultStr3
OUTA
LDA resultStr4
OUTA
LDA resultStr5
OUTA
LDA resultStr6
OUTA
LDA resultStr7
OUTA
LDA resultStr8
OUTA
LDA msgCR
OUTA
LDA res
ADD toInt
OUTA
BX res

.printEchoMsg
LDA echoMsgStr0
OUTA
LDA echoMsgStr1
OUTA
LDA echoMsgStr2
OUTA
LDA echoMsgStr3
OUTA
LDA echoMsgStr4
OUTA
LDA echoMsgStr5
OUTA
LDA echoMsgStr6
OUTA
LDA echoMsgStr7
OUTA
LDA echoMsgStr8
OUTA
LDA echoMsgStr9
OUTA
LDA echoMsgStr10
OUTA
LDA echoMsgStr11
OUTA
LDA echoMsgStr12
OUTA
LDA echoMsgStr13
OUTA
LDA echoMsgStr14
OUTA
LDA echoMsgStr15
OUTA
LDA echoMsgStr16
OUTA
LDA echoMsgStr17
OUTA
LDA echoMsgStr18
OUTA
LDA echoMsgStr19
OUTA
LDA echoMsgStr20
OUTA
LDA echoMsgStr21
OUTA
LDA echoMsgStr22
OUTA
LDA echoMsgStr23
OUTA
LDA echoMsgStr24
OUTA
LDA echoMsgStr25
OUTA
LDA echoMsgStr26
OUTA
LDA echoMsgStr27
OUTA
LDA echoMsgStr28
OUTA
LDA echoMsgStr29
OUTA
LDA echoMsgStr30
OUTA
LDA echoMsgStr31
OUTA
LDA echoMsgStr32
OUTA
BX 000000h
