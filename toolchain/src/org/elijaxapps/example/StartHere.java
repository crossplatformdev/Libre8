package org.elijaxapps.libre8;

public class StartHere {

    public static void main(String[] args) throws Exception {
        MiniCCompiler.run("StartHere.c");
        AssemblerV8.run("main.as");
    }
}
