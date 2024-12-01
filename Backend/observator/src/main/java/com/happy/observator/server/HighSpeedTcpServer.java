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
        try (ServerSocket serverSocket = new ServerSocket(9001)) {  // 9001번 포트에서 대기
            System.out.println("Server is ready to receive data on port 9001...");

            while (true) {
                Socket socket = serverSocket.accept(); // 클라이언트 연결 수락
                System.out.println("Client connected from " + socket.getRemoteSocketAddress());

                // 클라이언트와 연결 유지
                handleClient(socket);
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        try (socket) {
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);

            String line;
            while ((line = reader.readLine()) != null) {
                // 수신된 JSON 명령 출력
                System.out.println("Received data: " + line.trim());

                // 명령 처리
                orderProcessor.processReceivedOrder(line.trim());

                // 응답 전송
                writer.println("Order processed successfully!");
            }
        } catch (IOException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
        }
    }
}
