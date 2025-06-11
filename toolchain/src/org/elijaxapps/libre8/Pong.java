package org.elijaxapps.libre8;

public class Pong {

    public static void main(String[] args) throws Exception {
        String[] commands = {
            "PongGPT.c"
        };
        MiniCCompiler.run("PongGPT.c");
        AssemblerV8.run("PongGPT.as");
    }
}
