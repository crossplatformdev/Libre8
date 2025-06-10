package org.elijaxapps.old;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
public class Test {

    static FileWriter writer;
    static FileWriter preprocessed;
    static BufferedReader reader;

    private static Long argCounter = 0L;


    //Method to read a whole file
    public static String readFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public static void main(String[] args) throws IOException {
            //Open main.as (text file) in the same directory
            writer = new FileWriter("main.as");        
            preprocessed = new FileWriter("preprocessed.as");


            //Open main.c file. Then read it, and for every include, read the file and write it to the preprocessed file.
            //Every include file can countain more includes, so we need to read the file and write it to the preprocessed file.
            //Then we add the .c part of each header file. The entry point is the main.c file.
            
            reader = new BufferedReader(new FileReader("rogue-3.1/rogue-3.1/main.c"));

            //Read line by line
            String line = "";
            ArrayList<String> names = new ArrayList<String>();
            while((line = reader.readLine()) != null) {
               if (line.startsWith("#include ")){
                    //Clean the include name
                    String include = line.replace("#include ", "").replace("\"", "").replace("<", "").replace(">", "");
                    //Read the file
                    String content = "";
                    String name = "rogue-3.1/rogue-3.1/"+include;
                    names.add(name);
                    try {
                        while((line = reader.readLine()) != null) {
                            if (line.startsWith("#include ")){
                                 //Clean the include name
                                 String include2 = line.replace("#include ", "").replace("\"", "").replace("<", "").replace(">", "");
                                 //Read the file
                                 String content2 = "";
                                 String name2 = "rogue-3.1/rogue-3.1/"+include2;
                                 try {
                         
                                    content2 = readFile(name2);
                                   } catch (IOException e) {
                                    System.out.println("Error reading file: " + include);
                                }

                            //Write the content to the preprocessed file
                                preprocessed.write(content2);                    
                        }
                        content += line + ";;;;;;;;;;; ";
                        preprocessed.write(content);
                    }

                    } catch (IOException e) {
                        System.out.println("Error reading file: " + include);
                    }

                    //Write the content to the preprocessed file
                    preprocessed.write(content);                    
               }
            }

            //Save the preprocessed file:
            preprocessed.close();
            //Close the reader
            reader.close();


            write(".data");
            write(".code");
    }


    public static void write(String text) throws IOException {
        writer.write(text);
    }

    public static void close() throws IOException {
        writer.close();
    }

    public static void functionDeclaration(String name, String[] args, String address) throws IOException {
        while(address.length() < 8){
            address = "0" + address;
        }
        writer.write("."+ name + " "+ address + "h ");
        
        for(String arg : args) {
            writer.write(" arg"+argCounter);
            argCounter+=1L;
        }
    }

    public static void call(String address, String[] args) throws IOException {
        while(address.length() < 8) {
                address = "0" + address;            
        }
        for(String arg : args) {
            write("PSAX " + arg);
        }
        write("B" + address);
    }

    public static void functionEntered(String[] args) throws IOException {
        for(String arg : args) {
            write("POPX");
            write("LDA " + arg);
        }
    }
}
