import asyncio
import pandas as pd
from collections import deque
import jinupbit
import socket
import json
import traceback

# Upbit API 객체 초기화
jupbit = jinupbit.JinUpbit("", "")

# 큐 크기 제한 설정
ORDERBOOK_QUEUE_LIMIT = 210
TICK_QUEUE_LIMIT = 210

# 주문서 및 체결 데이터 큐 초기화
orderbook_queue = deque(maxlen=ORDERBOOK_QUEUE_LIMIT)
ticks_queue = deque(maxlen=TICK_QUEUE_LIMIT)

# TCP 서버 정보
SERVER_IP = "0.0.0.0"  # 서버 IP
SERVER_PORT = 9000     # 서버 포트

# 전처리 함수 - 주문서 데이터
def preprocess_orderbook(orderbook_data):
    try:
        orderbook_data['spread'] = orderbook_data['orderbook_units'][0]['ask_price'] - orderbook_data['orderbook_units'][0]['bid_price']
        orderbook_data['mid_price'] = (orderbook_data['orderbook_units'][0]['ask_price'] + orderbook_data['orderbook_units'][0]['bid_price']) / 2
        orderbook_data['obi'] = (orderbook_data['total_bid_size'] - orderbook_data['total_ask_size']) / \
                                (orderbook_data['total_bid_size'] + orderbook_data['total_ask_size'])
        return {
            "timestamp": orderbook_data['timestamp'],
            "spread": orderbook_data['spread'],
            "mid_price": orderbook_data['mid_price'],
            "obi": orderbook_data['obi']
        }
    except Exception as e:
        print(f"Orderbook Preprocessing Error: {e}")
        return None

# 전처리 함수 - 체결 데이터
def preprocess_ticks(ticks_df):
    if not isinstance(ticks_df, pd.DataFrame):
        ticks_df = pd.DataFrame(ticks_df)

    if len(ticks_df) >= 30:
        ticks_df['price_volatility'] = ticks_df['trade_price'].rolling(window=30).std()
        ticks_df['buy_sell_ratio'] = ticks_df['ask_bid'].rolling(window=30).mean()
        ticks_df['price_change_rate'] = (
            ticks_df['trade_price']
            .rolling(window=30)
            .apply(lambda x: (x.max() - x.min()) / x.max() if x.max() > 0 else 0, raw=True)
        )
    else:
        ticks_df['price_volatility'] = None
        ticks_df['buy_sell_ratio'] = None
        ticks_df['price_change_rate'] = None

    required_columns = ['timestamp', 'trade_price', 'price_volatility', 'buy_sell_ratio', 'price_change_rate']
    for col in required_columns:
        if col not in ticks_df.columns:
            ticks_df[col] = None

    return ticks_df[required_columns]

# 비동기 주문서 데이터 수집
async def upbit_get_orderbook():
    global orderbook_queue
    bf_timestamp = 0
    while True:
        try:
            upbit_res = await jupbit.async_get_orderbook("KRW-BTC")
            if isinstance(upbit_res, list) and len(upbit_res) > 0:
                upbit_res = upbit_res[0]
                if bf_timestamp < int(upbit_res['timestamp']):
                    bf_timestamp = int(upbit_res['timestamp'])
                    processed_data = preprocess_orderbook(upbit_res)
                    if processed_data:
                        orderbook_queue.append(processed_data)
            await asyncio.sleep(0.2)
        except Exception as e:
            print(f"Orderbook Collection Error: {e}")

# 비동기 체결 데이터 수집
async def upbit_get_ticks():
    global ticks_queue
    bf_sequential_id = 0
    while True:
        try:
            upbit_res = await jupbit.async_get_ticks("KRW-BTC", "", "", "", "")
            for r in upbit_res:
                if bf_sequential_id < r['sequential_id']:
                    bf_sequential_id = r['sequential_id']
                    ticks_queue.append({
                        "timestamp": r['timestamp'],
                        "trade_price": r['trade_price'],
                        "ask_bid": 1 if r['ask_bid'] == 'BID' else 0
                    })
            await asyncio.sleep(0.5)
        except Exception as e:
            print(f"Ticks Collection Error: {e}")

# 병합 및 클라이언트로 전송
async def merge_and_send_to_client():
    global orderbook_queue, ticks_queue
    try:
        # 서버 소켓 생성
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind((SERVER_IP, SERVER_PORT))
        server_socket.listen(1)
        print(f"Server waiting at port {SERVER_PORT}...")

        # 클라이언트 연결 수락
        client_socket, client_address = server_socket.accept()
        print(f"Connected: {client_address}")

        while True:
            orderbook_df = pd.DataFrame(list(orderbook_queue))
            ticks_df = pd.DataFrame(list(ticks_queue))

            if not orderbook_df.empty and not ticks_df.empty:
                processed_ticks_df = preprocess_ticks(ticks_df)
                merged_data = pd.merge_asof(orderbook_df.sort_values('timestamp'),
                                            processed_ticks_df.sort_values('timestamp'),
                                            on='timestamp',
                                            direction='nearest')
                json_data = merged_data.to_json(orient="records")
                print("Data sent:", json_data)  # 로그 추가
                client_socket.sendall(json_data.encode('utf-8'))
            else:
                print("Insufficient data to send.")

            await asyncio.sleep(5)
    except Exception as e:
        print(f"Merge and Send Error: {e}")
    finally:
        server_socket.close()

# 메인 실행 함수
async def main():
    tasks = [
        asyncio.create_task(upbit_get_orderbook()),
        asyncio.create_task(upbit_get_ticks()),
        asyncio.create_task(merge_and_send_to_client())
    ]
    await asyncio.gather(*tasks)

if __name__ == "__main__":
    asyncio.run(main())
