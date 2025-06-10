package org.elijaxapps.code2code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BadApple {

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
            long add = 0x00fffff0;
            writer.print(" _W  00" + Long.toHexString(add) + "h '#'\n");
            writer.print(" _B  00" + Long.toHexString(add+1) + "h 20\n");
            writer.print(" _13 00" + Long.toHexString(add+2) + "h 0C\n");
            writer.print(" _cr 00" + Long.toHexString(add+3) + "h 0A\n");
            writer.print(" _one 00" + Long.toHexString(add+4) + "h 01\n");
            writer.print(" _ten 00" + Long.toHexString(add+5) + "h 0A\n");
            writer.print(".code\n");
           
            writer.print(".main\n");
            String[] frames = new String[1754];
            boolean lastIsWhite = true;
            boolean cr = false;
            for(int i = 0; i < 1754; i+=1){
                if(i > 0){
                String ii = "" + i;
                while (ii.length() < 8){
                    ii = "0" + ii;
                }
    
                frames[i] = readTextFile("frames/frame" + ii + ".png.txt");
                int counter = 0;
                //boolean lastIsWhite = lastIsWhite1;
                boolean black = true;
                boolean white = true;
                for (int j = 0; j < frames[i].length(); j++) {                    
                    switch (frames[i].charAt(j)) {
                        case ' ':
                            if(black){
                                writer.print("LDA _B\n");
                                black = false;
                                white = true;
                            }
                            break;
                        case '\n':
                            if(j<frames[i].length()-1){
                                writer.print("LDA _cr\n");
                            } else {
                                writer.print("LDA 00ffffffh\n");
                            }
                            counter = 0;
                            black = true;
                            white = true;
                        break;
                        case '#':
                            if(white){
                                writer.print("LDA _W\n");
                                white = false;
                                black = true;
                            }
                    }
                    writer.print("OUTA\n");
                    
                    counter++;                    
                }

                /*
                writer.print("LDA _ten\n");
                writer.print(".framewait"+i+"\n");
                writer.print("SUB _one\n");
                writer.print("JNZ framewait"+i+"\n");
*/
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _B\n");
                writer.print("LDA _13\n");
                writer.print("OUTA\n");
            }
        }

            writer.print("HLT\n");
            writer.close();
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
