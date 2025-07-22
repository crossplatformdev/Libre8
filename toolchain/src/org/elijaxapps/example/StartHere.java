package org.elijaxapps.example;

import org.elijaxapps.libre8.as.AssemblerV8;
import org.elijaxapps.libre8.c.MiniCCompiler;

public class StartHere {

    public static void main(String[] args) throws Exception {
        MiniCCompiler.run("StartHere.c");
        AssemblerV8.run("main.as");
    }
}
