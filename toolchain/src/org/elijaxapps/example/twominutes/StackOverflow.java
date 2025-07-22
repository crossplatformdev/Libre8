package org.elijaxapps.example.twominutes;

import org.elijaxapps.libre8.as.AssemblerV8GPT;
import org.elijaxapps.libre8.c.MiniCCompilerGPT;

public class StackOverflow {

    public static void main(String[] args) throws Exception {
        MiniCCompilerGPT.run("C_src/StackOverflow.c");
        AssemblerV8GPT.run("main.as");
    }
}
