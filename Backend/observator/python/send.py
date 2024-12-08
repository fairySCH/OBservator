import sys
import asyncio
import socket
import json

# 데이터 수신 및 전송
async def fetch_and_send(threshold, user_id):
    SERVER_IP = "127.0.0.1"  # 데이터 수집 서버 IP
    SERVER_PORT = 10000      # 데이터 수집 서버 포트
    CLIENT_PORT = 9000       # 클라이언트 전송용 서버 포트

    # 클라이언트와의 통신을 위한 서버 소켓 생성
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind(("0.0.0.0", CLIENT_PORT))
    server_socket.listen(1)
    print(f"Sender Server running on port {CLIENT_PORT}...")

    while True:
        client_socket, client_address = server_socket.accept()
        print(f"[INFO] Client connected: {client_address}")
        try:
            while True:
                # 데이터 수집 서버와 연결
                data_collector_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                data_collector_socket.connect((SERVER_IP, SERVER_PORT))
                print("[INFO] Connected to Data Collector Server.")

                # 데이터 수신
                buffer = b""
                while True:
                    chunk = data_collector_socket.recv(4096)
                    if not chunk:
                        break
                    buffer += chunk

                data_collector_socket.close()
                print("[INFO] Data received from Data Collector Server.")

                # JSON 데이터 파싱
                merged_data = json.loads(buffer.decode('utf-8'))

                # threshold 및 user_id 추가
                for record in merged_data:
                    record["threshold"] = threshold
                    record["user_id"] = user_id

                # 디버깅 메시지: 수신 데이터 내용 확인
                print(f"[DEBUG] First record received: {merged_data[0] if merged_data else 'No data received'}")

                # 최신 180개의 데이터만 클라이언트로 전송
                data_to_send = merged_data[-180:] if len(merged_data) > 180 else merged_data
                client_socket.sendall(json.dumps(data_to_send).encode('utf-8'))
                print("[INFO] Data sent successfully to client.")

                # 1초 대기
                await asyncio.sleep(1)

        except json.JSONDecodeError as jde:
            print(f"[ERROR] JSON Decode Error: {jde}")
        except socket.error as se:
            print(f"[ERROR] Socket Error: {se}")
        except Exception as e:
            print(f"[ERROR] Unexpected Error: {e}")
        finally:
            client_socket.close()
            print("[INFO] Client connection closed.")

if __name__ == "__main__":
    # 명령줄 인자 받기
    if len(sys.argv) != 3:
        print("Usage: python send.py <threshold> <user_id>")
        sys.exit(1)

    try:
        threshold = float(sys.argv[1])
        user_id = int(sys.argv[2])
    except ValueError:
        print("Invalid arguments: <threshold> must be a float, <user_id> must be an integer.")
        sys.exit(1)

    print(f"[INFO] Starting server with threshold={threshold}, user_id={user_id}")
    asyncio.run(fetch_and_send(threshold, user_id))
