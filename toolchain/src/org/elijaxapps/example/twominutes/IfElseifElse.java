package org.elijaxapps.example.twominutes;

import org.elijaxapps.libre8.as.AssemblerV8GPT;
import org.elijaxapps.libre8.c.MiniCCompilerGPT;

public class IfElseifElse {

    public static void main(String[] args) throws Exception {
        MiniCCompilerGPT.run("C_src/IfElseifElse.c");
        AssemblerV8GPT.run("main.as");
    }
}
