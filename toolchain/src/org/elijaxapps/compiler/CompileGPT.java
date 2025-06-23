package org.elijaxapps.example;

import org.elijaxapps.libre8.AssemblerV8GPT;
import org.elijaxapps.libre8.MiniCCompilerGPT;

public class CompileGPT {
    public static void main(String[] args) throws Exception {        
        MiniCCompilerGPT.run("C_example.c");
        AssemblerV8GPT.run("main.as");
    }
}