package org.elijaxapps.code2code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Dino {

    private static final int FRAMES_AMMOUNT = 7022; // Total number of frames
    //Funtion to open a textfile and return it as string
    public static String readTextFile(String filename) throws FileNotFoundException {
        //Read txt Java 8
        File file = new File(filename);
        String content = "";
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                // Remove leading and trailing whitespace
                line = line.trim(); 
                // Leave only '1', '2', '3', '4' characters
                line = line.replaceAll("[^1234]", ""); // Keep only '1', '2', '3', '4'
                content += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

            writer.print("\n");
            String[] frames = new String[FRAMES_AMMOUNT - 0];

            char[][][] bytes = new char[(FRAMES_AMMOUNT-0)][32][72];
            for(int i = 1; i < FRAMES_AMMOUNT - 0; i+=1){
                String ii = "" + (i + 0);
                while (ii.length() < 8){
                    ii = "0" + ii; // Pad with leading zeros to ensure 4 digits
                }
                int counter = 0;
                    
                frames[i] = readTextFile("frames/frame" + ii + ".png.txt");
                for(int y = 0; y < 32; y++){
                    writer.print("POKE ");
                    for (int x = 0; x < 72; x++) { 
                        String ch = ""+ frames[i].charAt((y * 73) + x);
                        String hex = "";
                        if (ch.equals("1")) {
                            hex = "F0";
                        } else if (ch.equals("2")) {
                            hex = "A0";
                        } else if (ch.equals("3")) {
                            hex = "40";
                        } else if (ch.equals("4")) {
                            hex = "00";
                        } 
                        writer.print(hex);
                        counter += 1;  
                    }
                    
                    writer.print("\n");
                }
            
            }
            
        
 
       
            writer.print("JMP main\n");
            writer.close();


            } catch (java.io.FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        
}
