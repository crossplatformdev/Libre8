package org.elijaxapps.example;

import org.elijaxapps.libre8.AssemblerV8;
import org.elijaxapps.libre8.MiniCCompiler;

public class IfElseifElse {

    public static void main(String[] args) throws Exception {
        MiniCCompiler.run("IfElseifElse.c");
        AssemblerV8.run("main.as");
    }
}
