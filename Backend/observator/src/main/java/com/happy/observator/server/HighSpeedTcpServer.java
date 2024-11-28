package com.happy.observator.server;

import java.io.*;
import java.net.*;

import org.springframework.stereotype.Component;
import com.happy.observator.model.TradingOrderProcessor;

@Component
public class HighSpeedTcpServer implements Runnable {

    private final TradingOrderProcessor orderProcessor;

    public HighSpeedTcpServer(TradingOrderProcessor orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(9000)) {  // 9000번 포트에서 대기
            System.out.println("Server is ready to receive data on port 9000...");

            while (true) {
                try (Socket socket = serverSocket.accept()) { // 클라이언트 연결 수락
                    InputStream inputStream = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder receivedData = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        receivedData.append(line);
                    }

                    // 수신된 JSON 명령 출력
                    String jsonOrder = receivedData.toString().trim();
                    System.out.println("Received data: " + jsonOrder);

                    // TradingOrderProcessor로 전달
                    orderProcessor.processReceivedOrder(jsonOrder);

                    // 클라이언트에 응답
                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(outputStream, true);
                    writer.println("Order processed successfully!");
                } catch (Exception e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }
}
