package org.elijaxapps.example.twominutes;

import org.elijaxapps.libre8.as.AssemblerV8GPT;
import org.elijaxapps.libre8.c.MiniCCompilerGPT;

public class BASICV8 {
    public static void main(String[] args) throws Exception {
        MiniCCompilerGPT.run("C_src/BASICV8.c");
        AssemblerV8GPT.run("main.as");
    }
}