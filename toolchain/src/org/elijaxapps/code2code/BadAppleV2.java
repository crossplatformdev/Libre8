package org.elijaxapps.code2code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BadAppleV2 {

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
            String[] frames = new String[1754];
            boolean lastIsWhite = true;
            boolean cr = false;
            String[] video = new String[1754*32];
            int counter = 0;
            char[][] bytes = new char[1754*32][144];
            int y = 0;
            for(int i = 0; i < 1754; i+=1){
                if(i > 0){
                String ii = "" + i;
                while (ii.length() < 8){
                    ii = "0" + ii;
                }
    
                frames[i] = readTextFile("frames/frame" + ii + ".png.txt");
                //boolean lastIsWhite = lastIsWhite1;
                boolean black = true;
                boolean white = true;
                int x = 0;
                for (int j = 0; j < frames[i].length(); j++) {    
                    /*
                    writer.print("LDA _x\n");
                    writer.print("PSAX\n");
                    writer.print("LDA _y\n");
                    writer.print("PSAX\n");
                      */            
                    String addString = ""+i+""+x+""+y+"";
                    while (addString.length() < 8){
                        addString = "0" + addString;
                    }
                    switch (frames[i].charAt(j)) {
                        case ' ':
                            addString += "00"; 
                            //writer.print("frame_"+i+"_"+x+"_"+y+" "+addString+"h 00\n");                               
                            bytes[y][x] = "00";
                            x++;                                                                
                            break;
                        case '\n':                            
                            y++;
                            x = 0;
                            break;
                        case '#':
                            addString += "FF";
                            //writer.print("frame_"+i+"_"+x+"_"+y+"   "+addString+"h ff\n");   
                            bytes[y][x] = "FF"; 
                            //
                            x++;
                            break;
                    }
                    add +=1;
                }
            }
        }
        
        writer.print("\n");
        writer.print(".code\n");
        writer.print(".main\n");
        add = 0;
        counter = 0;
        writer.print(".run\n");
        
        for(int i = 0; i < 1753*32; i++){
            String[] v = bytes[i];
            writer.print("POKE ");
            for(int j = 0; j < 72; j++){
                writer.print(v[j]);
            }
            writer.print("\n");
        }
        

        writer.print("JMP run\n");
        writer.close();


        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}
