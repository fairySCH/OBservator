import pandas as pd
import socket
import asyncio
from multiprocessing import Manager
from collect import merged_data  # 공유 메모리에서 데이터 가져오기

# TCP 서버 정보
SERVER_IP = "0.0.0.0"
SERVER_PORT = 9000
MERGED_QUEUE_SIZE = 180

# 가장 최신의 180개 데이터를 주기적으로 클라이언트로 전송하는 함수
async def send_merged_data():
    try:
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_socket.bind((SERVER_IP, SERVER_PORT))
        server_socket.listen(1)
        print(f"Server waiting at port {SERVER_PORT}...")

        while True:
            client_socket, client_address = server_socket.accept()
            print(f"Client connected: {client_address}")

            try:
                while True:
                    if len(merged_data) > 0:
                        # 가장 최신의 180개 데이터를 가져오기
                        data_to_send = pd.DataFrame(list(merged_data))[-MERGED_QUEUE_SIZE:]
                        json_data = data_to_send.to_json(orient="records") + "\n"
                        client_socket.sendall(json_data.encode('utf-8'))
                        print("Data successfully sent to client.")
                    else:
                        print("No data to send.")

                    await asyncio.sleep(5)  # 5초마다 데이터 전송
            except (socket.error, BrokenPipeError):
                print(f"Client disconnected: {client_address}")
            finally:
                client_socket.close()
    except Exception as e:
        print(f"Server Error: {e}")
    finally:
        server_socket.close()

# 메인 실행 함수
async def main():
    await send_merged_data()

if __name__ == "__main__":
    asyncio.run(main())
