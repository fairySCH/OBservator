import socket

def simple_server():
    server_ip = "0.0.0.0"  # 모든 IP에서 요청 허용
    server_port = 9000     # 서버 포트

    try:
        # 소켓 생성 및 바인딩
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind((server_ip, server_port))
        server_socket.listen(1)  # 하나의 클라이언트 연결 대기
        print(f"서버가 포트 {server_port}에서 대기 중입니다...")

        # 클라이언트 연결 수락
        client_socket, client_address = server_socket.accept()
        print(f"클라이언트 연결됨: {client_address}")

        # 클라이언트 메시지 읽기
        client_message = client_socket.recv(1024).decode()
        print(f"받은 메시지: {client_message}")

        # 응답 메시지 전송
        response_message = "Hello Client! Your message was received."
        client_socket.sendall(response_message.encode())

        # 소켓 종료
        client_socket.close()
        server_socket.close()
    except Exception as e:
        print(f"서버 오류: {e}")

if __name__ == "__main__":
    simple_server()
