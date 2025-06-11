package org.elijaxapps.libre8;

public class TestC {

    public static void main(String[] args) throws Exception {
        CompilerV8.run(new String[]{
            "StartHere.c"
        });
        AssemblerV8GPT.run(new String[]{
            "main.as"
        });
    }
}
