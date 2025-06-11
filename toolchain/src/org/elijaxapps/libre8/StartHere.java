package org.elijaxapps.libre8;
import org.elijaxapps.libre8.assembler.AssemblerV8;
import org.elijaxapps.libre8.compiler.MiniCCompiler;
public class StartHere {

    public static void main(String[] args) throws Exception {
        MiniCCompiler.run("StartHere.c");
        AssemblerV8.run("main.as");
    }
}
