package org.elijaxapps.example;

import org.elijaxapps.libre8.AssemblerV8;
import org.elijaxapps.libre8.MiniCCompiler;

public class StackOverflow {

    public static void main(String[] args) throws Exception {
        MiniCCompiler.run("StackOverflow.c");
        AssemblerV8.run("main.as");
    }
}
