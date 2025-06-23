package org.elijaxapps.example;

import org.elijaxapps.libre8.AssemblerV8GPT;
import org.elijaxapps.libre8.MiniCCompiler;

public class TestC {

    public static void main(String[] args) throws Exception {
        MiniCCompiler.run(
            "C_example.c"
        );
        AssemblerV8GPT.run(
            "main.as"
        );
    }
}
