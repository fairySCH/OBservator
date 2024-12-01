package com.happy.observator.server;

import java.io.*;
import java.net.*;

import org.springframework.stereotype.Component;
import com.happy.observator.model.TradingOrderProcessor;
import org.json.JSONObject;
import org.json.JSONException;
import java.nio.charset.StandardCharsets;

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
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        try (socket) {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder messageBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("END")) {
                    // 메시지 끝을 알리는 "END" 구분자를 받음
                    String jsonMessage = messageBuilder.toString().trim();
                    messageBuilder.setLength(0);  // 버퍼 초기화

                    // 수신된 JSON 명령 출력 및 처리
                    processJsonMessage(jsonMessage, writer);
                } else {
                    // 메시지 누적
                    messageBuilder.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
        }
    }

    private void processJsonMessage(String jsonMessage, PrintWriter writer) {
        try {
            // 수신된 JSON 명령 출력
            System.out.println("Received data: " + jsonMessage);

            // JSON 파싱
            JSONObject jsonObject = new JSONObject(jsonMessage);

            // 명령 처리
            orderProcessor.processReceivedOrder(jsonObject.toString());

            // 응답 전송
            writer.println("Order processed successfully!");

        } catch (JSONException e) {
            System.err.println("Invalid JSON data received: " + e.getMessage());
            // 에러 응답 전송
            writer.println("Error: Invalid JSON data");
        }
    }
}
