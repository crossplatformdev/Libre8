package org.elijaxapps.libre8.telnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SocketClient {

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
                        System.out.println("Respuesta del servidor: " + response);
                        System.out.print("Ingrese un comando: ");
                        String command = scanner.nextLine();

                        if (command.equalsIgnoreCase("exit")) {
                            System.out.println("Cerrando conexión...");
                            break;
                        }

                        out.println("@" + command);
                        out.flush();                        
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
