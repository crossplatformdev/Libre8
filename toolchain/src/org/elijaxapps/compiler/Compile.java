package org.elijaxapps.compiler;

import org.elijaxapps.libre8.as.AssemblerV8;
import org.elijaxapps.libre8.c.MiniCCompiler;

public class Compile {
    public static void main(String[] args) throws Exception {        
        MiniCCompiler.run("C_example.c");
        AssemblerV8.run("main.as");
    }
}