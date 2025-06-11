package org.elijaxapps.example;

import org.elijaxapps.libre8.AssemblerV8GPT;
import org.elijaxapps.old.CompilerV8;

public class TestC {

    public static void main(String[] args) throws Exception {
        CompilerV8.run(new String[]{
            "StartHere.c"
        });
        AssemblerV8GPT.run(
            "main.as"
        );
    }
}
