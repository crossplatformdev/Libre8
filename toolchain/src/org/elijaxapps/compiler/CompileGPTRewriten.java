package org.elijaxapps.compiler;

import org.elijaxapps.libre8.as.AssemblerV8GPT;
import org.elijaxapps.libre8.c.MiniCCompilerGPT;

public class CompileGPTRewriten {
    public static void main(String[] args) throws Exception {        
        MiniCCompilerGPT.run("C_example.c");
        AssemblerV8GPT.run("main.as");
    }
}