/*
 *  Libre8 code generator for TCC.
 *
 *  This initial implementation targets the minimal subset required to start
 *  emitting Libre8 assembly. It currently supports constants and global
 *  loads/stores, enabling simple functions (e.g. returning constants) to be
 *  produced while the richer code generator is developed.
 */

#ifdef TARGET_DEFS_ONLY

#define NB_REGS         2
#define NB_ASM_REGS     0

#define RC_INT     0x0001
#define RC_FLOAT   RC_INT

#define RC_IRET    RC_INT
#define RC_LRET    RC_INT
#define RC_FRET    RC_INT

#define R_DATA_PTR   0
#define R_DATA_32    0
#define R_JMP_SLOT   0
#define R_RELATIVE   0
#define R_GLOB_DAT   0
#define R_COPY       0

#define PCRELATIVE_DLLPLT 0
#define RELOCATE_DLLPLT   0

#define ELF_PAGE_SIZE   0x100
#define ELF_START_ADDR  0

#define EM_TCC_TARGET   EM_NONE

#define REG_IRET   0
#define REG_LRET   0
#define REG_FRET   0

#define PTR_SIZE       4
#define LDOUBLE_SIZE   8
#define LDOUBLE_ALIGN  8
#define MAX_ALIGN      8

#else /* !TARGET_DEFS_ONLY */

#include "tcc.h"
#include <string.h>

ST_FUNC int code_reloc(int reloc_type)
{
    (void)reloc_type;
    return 0;
}

ST_FUNC int gotplt_entry_type(int reloc_type)
{
    (void)reloc_type;
    return NO_GOTPLT_ENTRY;
}

ST_FUNC unsigned create_plt_entry(TCCState *s1, unsigned got_offset, struct sym_attr *attr)
{
    (void)s1;
    (void)got_offset;
    (void)attr;
    return 0;
}

ST_FUNC void relocate_init(Section *sr)
{
    (void)sr;
}

ST_FUNC void relocate(TCCState *s1, ElfW_Rel *rel, int type, unsigned char *ptr, addr_t addr, addr_t val)
{
    (void)s1;
    (void)rel;
    (void)type;
    (void)ptr;
    (void)addr;
    (void)val;
    tcc_error("Libre8 backend: relocation emission not supported");
}

ST_FUNC void relocate_plt(TCCState *s1)
{
    (void)s1;
}

enum {
    TREG_A = 0,
    TREG_B = 1,
};

static const char *const reg_names[]       = { "A", "B" };
static const char *const reg_load_imm[]    = { "LDIA", "LDIB" };
static const char *const reg_load_mem[]    = { "LDA",  "LDB"  };
static const char *const reg_store_mem[]   = { "STA",  "STB"  };
static const char *const reg_move[NB_REGS][NB_REGS] = {
    { NULL,     "MOV_AB" },
    { "MOV_BA", NULL      },
};

static int text_header_emitted;
static int function_depth;

static void emit_line(const char *fmt, ...)
{
    char buf[256];
    va_list ap;
    int len;

    va_start(ap, fmt);
    len = vsnprintf(buf, sizeof(buf), fmt, ap);
    va_end(ap);
    if (len < 0)
        return;
    if (len >= (int)sizeof(buf)) {
        len = sizeof(buf) - 1;
        buf[len] = '\0';
    }
    if (len == 0)
        return;
    if (buf[len - 1] != '\n') {
        if (len + 1 >= (int)sizeof(buf))
            len--;
        buf[len++] = '\n';
        buf[len] = '\0';
    }

    if (!cur_text_section)
        cur_text_section = text_section;
    if (ind + len > cur_text_section->data_allocated)
        section_realloc(cur_text_section, ind + len);
    memcpy(cur_text_section->data + ind, buf, len);
    ind += len;
}

static void ensure_text_header(void)
{
    if (!text_header_emitted) {
        emit_line(".code");
        text_header_emitted = 1;
    }
}

ST_DATA const int reg_classes[NB_REGS] = {
    RC_INT,
    RC_INT,
};

static const char *sym_label(SValue *sv)
{
    if (!sv->sym)
        return NULL;
    if (sv->sym->asm_label)
        return get_tok_str(sv->sym->asm_label, NULL);
    return get_tok_str(sv->sym->v, NULL);
}

ST_FUNC void gsym_addr(int t, int a)
{
    (void)t;
    (void)a;
    /* textual backend keeps labels symbolic for now */
}

ST_FUNC void gsym(int t)
{
    (void)t;
}

ST_FUNC void load(int r, SValue *sv)
{
    ensure_text_header();

    if ((sv->r & (VT_VALMASK | VT_LVAL | VT_SYM)) == VT_CONST) {
        unsigned v = (unsigned)(sv->c.i) & 0xff;
        emit_line("    %s %02Xh", reg_load_imm[r], v);
    } else if (sv->r & VT_LVAL) {
        const char *label = sym_label(sv);
        if (!label) {
            tcc_error("Libre8 backend: unsupported lvalue load");
            return;
        }
        emit_line("    %s %s", reg_load_mem[r], label);
    } else if ((sv->r & VT_VALMASK) < VT_CONST) {
        int cur = sv->r & VT_VALMASK;
        if (cur != r) {
            const char *mov = reg_move[cur][r];
            if (mov)
                emit_line("    %s", mov);
            else
                emit_line("    ;; move %s -> %s (no-op)", reg_names[cur], reg_names[r]);
        }
    } else {
        tcc_error("Libre8 backend: unsupported load source");
    }
    sv->r = r;
}

ST_FUNC void store(int r, SValue *sv)
{
    const char *label;

    ensure_text_header();

    if (!(sv->r & VT_LVAL)) {
        tcc_error("Libre8 backend: store requires lvalue");
        return;
    }
    label = sym_label(sv);
    if (!label) {
        tcc_error("Libre8 backend: unsupported store destination");
        return;
    }
    emit_line("    %s %s", reg_store_mem[r], label);
}

ST_FUNC int gfunc_sret(CType *vt, int variadic, CType *ret, int *align, int *regsize)
{
    (void)vt;
    (void)variadic;
    (void)ret;
    (void)align;
    (void)regsize;
    return 0;
}

static int extract_constant_string(SValue *sv,
                                   const unsigned char **out_bytes,
                                   int *out_size)
{
    if (!sv || !out_bytes || !out_size)
        return 0;

    if (sv->c.str.data && sv->c.str.size > 0) {
        *out_bytes = sv->c.str.data;
        *out_size = sv->c.str.size;
        return 1;
    }

    if ((sv->r & (VT_SYM | VT_CONST)) == (VT_SYM | VT_CONST) && sv->sym) {
        ElfSym *esym = elfsym(sv->sym);
        if (!esym)
            return 0;
        if (esym->st_shndx == SHN_UNDEF)
            return 0;
        if (esym->st_shndx >= tcc_state->nb_sections)
            return 0;
        Section *sec = tcc_state->sections[esym->st_shndx];
        if (!sec || !sec->data)
            return 0;

        unsigned long offset = esym->st_value;
        unsigned long size = esym->st_size;
#if PTR_SIZE == 4
        unsigned long addend = (unsigned long)(uint32_t)sv->c.i;
#else
        unsigned long addend = (unsigned long)sv->c.i;
#endif
        offset += addend;
        if (size) {
            if (size <= addend)
                return 0;
            size -= addend;
        }

        if (offset >= sec->data_offset)
            return 0;

        const unsigned char *base = sec->data + offset;
        unsigned long max = sec->data_offset - offset;
        if (!size) {
            size = 0;
            while (size < max && base[size] != '\0')
                size++;
            if (size < max)
                size++;
        } else if (size > max) {
            size = max;
        }

        *out_bytes = base;
        *out_size = (int)size;
        return 1;
    }

    return 0;
}

static void emit_ldia_out(unsigned char ch)
{
    emit_line("    LDIA %02Xh", ch);
    emit_line("    OUT");
}

static void emit_printf_int_arg(SValue *sv)
{
    SValue value = *sv;
    load(TREG_A, &value);
    emit_line("    OUTD");
}

static void emit_printf_char_arg(SValue *sv)
{
    SValue value = *sv;
    load(TREG_A, &value);
    emit_line("    OUT");
}

static void emit_printf_string_arg(SValue *sv)
{
    const unsigned char *bytes = NULL;
    int size = 0;
    if (!extract_constant_string(sv, &bytes, &size)) {
        const char *label = sym_label(sv);
        tcc_error("Libre8 backend: printf %%s requires constant string (got %s)",
                  label ? label : "<unknown>");
        return;
    }

    int length = size;
    if (length > 0 && bytes[length - 1] == '\0')
        length--;

    for (int i = 0; i < length; ++i)
        emit_ldia_out(bytes[i]);
}

static void emit_inline_printf(const unsigned char *bytes, int size,
                               SValue *arg_base, int nb_args)
{
    if (!bytes || size <= 0)
        return;

    int length = size;
    if (length > 0 && bytes[length - 1] == '\0')
        length--;

    emit_line("    ;; inline printf expansion");

    int next_arg = 1; /* skip format string */
    for (int i = 0; i < length; ++i) {
        unsigned char ch = bytes[i];
        if (ch != '%') {
            emit_ldia_out(ch);
            continue;
        }

        if (i + 1 >= length) {
            tcc_error("Libre8 backend: dangling %% in format string");
            return;
        }

        unsigned char spec = bytes[++i];
        if (spec == '%') {
            emit_ldia_out('%');
            continue;
        }

        if (next_arg >= nb_args) {
            tcc_error("Libre8 backend: not enough printf arguments for format");
            return;
        }

        SValue *sv = arg_base + next_arg;
        switch (spec) {
            case 'd':
            case 'i':
                emit_printf_int_arg(sv);
                break;
            case 'c':
                emit_printf_char_arg(sv);
                break;
            case 's':
                emit_printf_string_arg(sv);
                break;
            default:
                tcc_error("Libre8 backend: unsupported printf specifier %%%c", spec);
                return;
        }

        next_arg++;
    }

}

ST_FUNC void gfunc_call(int nb_args)
{
    ensure_text_header();

    SValue *target = vtop - nb_args;
    const char *fname = sym_label(target);

    if (fname && strcmp(fname, "printf") == 0) {
        if (nb_args < 1) {
            tcc_error("Libre8 backend: printf expects at least one argument");
            return;
        }

        SValue *format_arg = vtop - (nb_args - 1);
        const unsigned char *bytes = NULL;
        int size = 0;
        if (!extract_constant_string(format_arg, &bytes, &size)) {
            const char *arg_label = sym_label(format_arg);
            fprintf(stderr,
                "[libre8] printf arg diagnostic: r=0x%x sym=%s\n",
                format_arg->r,
                arg_label ? arg_label : "<none>");
            tcc_error("Libre8 backend: printf requires constant format string");
            return;
        }

        emit_inline_printf(bytes, size, format_arg, nb_args);

        for (int i = 0; i < nb_args; ++i)
            vtop--;

        vtop--;
        return;
    }

    tcc_error("Libre8 backend: function call '%s' not supported",
              fname ? fname : "<anon>");
}

ST_FUNC void gfunc_prolog(CType *func_type)
{
    Sym *sym = func_type ? func_type->ref : NULL;

    loc = 0;
    func_vc = 0;
    func_vt = *func_type;
    func_var = sym && (sym->f.func_type == FUNC_ELLIPSIS);

    ensure_text_header();
    emit_line("");
    emit_line(".%s", (funcname && *funcname) ? funcname : "_anon");
    emit_line("    ;; prolog (stack setup pending)");
    function_depth++;
}

ST_FUNC void gfunc_epilog(void)
{
    if (function_depth > 0) {
        emit_line("    BX _00");
        function_depth--;
    }
}

ST_FUNC int gjmp(int t)
{
    (void)t;
    tcc_error("Libre8 backend: jumps not implemented yet");
    return 0;
}

ST_FUNC void gjmp_addr(int a)
{
    (void)a;
    tcc_error("Libre8 backend: gjmp_addr not implemented yet");
}

ST_FUNC int gtst(int inv, int t)
{
    (void)inv;
    (void)t;
    tcc_error("Libre8 backend: gtst not implemented yet");
    return 0;
}

ST_FUNC void gen_opi(int op)
{
    (void)op;
    tcc_error("Libre8 backend: integer operations not implemented yet");
}

ST_FUNC void gen_opf(int op)
{
    (void)op;
    tcc_error("Libre8 backend: floating-point operations not implemented yet");
}

ST_FUNC void gen_cvt_ftoi(int t)
{
    (void)t;
    tcc_error("Libre8 backend: float->int conversion not implemented yet");
}

ST_FUNC void gen_cvt_ftof(int t)
{
    (void)t;
    tcc_error("Libre8 backend: float->float conversion not implemented yet");
}

ST_FUNC void ggoto(void)
{
    tcc_error("Libre8 backend: computed goto not implemented yet");
}

#if defined(__GNUC__)
ST_FUNC void o(unsigned int c) __attribute__((unused));
#endif
ST_FUNC void o(unsigned int c)
{
    (void)c;
    tcc_error("Libre8 backend: raw opcode emission not supported");
}

ST_FUNC void gen_cvt_itof(int t)
{
    (void)t;
    tcc_error("Libre8 backend: int->float conversion not implemented yet");
}

ST_FUNC void gen_vla_sp_save(int addr)
{
    (void)addr;
}

ST_FUNC void gen_vla_sp_restore(int addr)
{
    (void)addr;
}

ST_FUNC void gen_vla_alloc(CType *type, int align)
{
    (void)type;
    (void)align;
    tcc_error("Libre8 backend: VLA allocation not implemented yet");
}

ST_FUNC int libre8_output_file(TCCState *s1, const char *filename)
{
    const char *outname = filename && *filename ? filename : s1->outfile;
    FILE *fp;
    int result = -1;

    if (cur_text_section)
        cur_text_section->data_offset = ind;

    if (!outname || !*outname)
        outname = "a.out";

    fp = fopen(outname, "wb");
    if (!fp) {
        tcc_error_noabort("could not create '%s': %s", outname, strerror(errno));
        return -1;
    }

    if (text_section && text_section->data_offset) {
        size_t total = (size_t)text_section->data_offset;
        size_t written = fwrite(text_section->data, 1, total, fp);
        if (written != total) {
            tcc_error_noabort("could not write '%s': %s", outname, strerror(errno));
            goto close_and_fail;
        }
    }

    result = 0;

close_and_fail:
    if (fclose(fp) != 0) {
        tcc_error_noabort("could not close '%s': %s", outname, strerror(errno));
        result = -1;
    }

    return result;
}

#endif /* !TARGET_DEFS_ONLY */
