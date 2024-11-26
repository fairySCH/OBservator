package com.happy.observator.server;

import java.io.*;
import java.net.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.happy.observator.model.TradingOrderProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class HighSpeedTcpServer implements Runnable {

    @Autowired
    private TradingOrderProcessor tradingOrderProcessor;

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(9000)) {  // Port 9000
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

                    // Convert received data to JSON format
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonOrder = objectMapper.readTree(receivedData.toString().trim());

                    // Convert JsonNode to String and process the order
                    String jsonString = jsonOrder.toString();
                    tradingOrderProcessor.processReceivedOrder(jsonString);

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
