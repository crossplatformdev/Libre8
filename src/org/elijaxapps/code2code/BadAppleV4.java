package org.elijaxapps.code2code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BadAppleV4 {

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
            String[] frames = new String[1834 - 0];

            char[][][] bytes = new char[(1834-0)][32][145];
            for(int i = 0; i < 1834 - 0; i+=1){
                String ii = "" + (i + 0);
                int counter = 0;
                    
                frames[i] = readTextFile("frames/resized-__" + ii + ".txt");
                for(int y = 0; y < 32; y++){
                    writer.print("POKE ");
                    for (int x = 0; x < 145; x++) { 
                        writer.print(frames[i].charAt(counter));
                        counter += 1;  
                    }
                    
 //                   writer.print("\n");
                }
            
            }
            
        
 
       
            writer.print("JMP run\n");
            writer.close();


            } catch (java.io.FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        
}
