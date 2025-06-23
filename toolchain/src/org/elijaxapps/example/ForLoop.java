package org.elijaxapps.example;

import org.elijaxapps.libre8.AssemblerV8;
import org.elijaxapps.libre8.MiniCCompiler;

public class ForLoop {

    public static void main(String[] args) throws Exception {
        MiniCCompiler.run("ForLoop.c");
        AssemblerV8.run("main.as");
    }
}
