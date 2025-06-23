package org.elijaxapps.example;

import org.elijaxapps.libre8.AssemblerV8GPT;
import org.elijaxapps.libre8.MiniCCompilerGPT;

public class PromptGPT {
    public static void main(String[] args) throws Exception {        
        MiniCCompilerGPT.run("PromptGPT.c");
        AssemblerV8GPT.run("main.as");
    }
}