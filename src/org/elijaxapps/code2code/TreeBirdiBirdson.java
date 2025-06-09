package org.elijaxapps.code2code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TreeBirdiBirdson {

    private static final int FRAMES_AMMOUNT = 82; // Total number of frames
    //Funtion to open a textfile and return it as string

    public static String readTextFile(String filename) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static void main(String[] args) {
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter("main.as");

            writer.print(".data\n");
            long add = 0x00000004;

            writer.print("\n");
            writer.print(".code\n");
            writer.print(".main\n");
            add = 0;
            writer.print(".run\n");

            writer.print("\n");
            String[] frames = new String[FRAMES_AMMOUNT - 0];

            char[][][] bytes = new char[(FRAMES_AMMOUNT - 0)][32][128];
            for (int i = 1; i < FRAMES_AMMOUNT - 0; i += 1) {
                String ii = "" + (i + 0);
                while (ii.length() < 8) {
                    ii = "0" + ii; // Pad with leading zeros to ensure 4 digits
                }
                int counter = 0;

                frames[i] = readTextFile("frames/frame" + ii + ".png.txt");
                for (int y = 0; y < 32; y++) {
                    writer.print("POKE ");
                    for (int x = 0; x < 72; x++) {
                        String ch1 = "" + '@';
                        String ch2 = "" + frames[i].charAt((y * 73) + x);
                        String str = ch2;// ? "FF" : "00";
                        writer.print(ch2);
                        counter += 1;
                    }

                    writer.print("\n");
                }

            }

            writer.print("JMP run\n");
            writer.close();

        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
