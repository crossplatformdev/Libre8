package org.elijaxapps.example;

import org.elijaxapps.libre8.AssemblerV8;
import org.elijaxapps.libre8.MiniCCompiler;

public class Pong {

    public static void main(String[] args) throws Exception {
        String[] commands = {
            "PongGPT.c"
        };
        MiniCCompiler.run("PongGPT.c");
        AssemblerV8.run("PongGPT.as");
    }
}
