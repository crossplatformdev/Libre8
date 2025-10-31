package org.elijaxapps.compiler;

import org.elijaxapps.libre8.as.AssemblerV8GPT;
import org.elijaxapps.libre8.c.MiniCCompilerGPT;

public class CompileDoom {
    public static void main(String[] args) throws Exception {        
        MiniCCompilerGPT.run("doom-nano-master/doom-nano.c");
        AssemblerV8GPT.run("main.as");
    }
}