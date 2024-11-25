package com.happy.observator.server;

import java.io.*;
import java.net.*;

import org.springframework.stereotype.Component;

@Component
public class HighSpeedTcpServer implements Runnable {

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(9000)) {  // 포트 9000
            System.out.println("Server is ready to receive data on port 9000...");

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    InputStream inputStream = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder receivedData = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        receivedData.append(line).append("\n");
                    }

                    System.out.println("Received data: " + receivedData.toString().trim());

                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(outputStream, true);
                    writer.println("Acknowledged: " + receivedData.toString().trim());
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }
}
