package org.elijaxapps.example.twominutes;

import org.elijaxapps.libre8.as.AssemblerV8GPT;
import org.elijaxapps.libre8.c.MiniCCompilerGPT;

public class DivideBy0 {

    public static void main(String[] args) throws Exception {
        MiniCCompilerGPT.run("C_src/DivideBy0.c");
        AssemblerV8GPT.run("main.as");
    }
}
