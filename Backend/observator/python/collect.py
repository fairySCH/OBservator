import asyncio
import pandas as pd
from collections import deque
import jinupbit
import traceback

# Upbit API 객체 초기화
jupbit = jinupbit.JinUpbit("", "")

# 큐 크기 제한 설정
ORDERBOOK_QUEUE_LIMIT = 240
TICK_QUEUE_LIMIT = 240
MERGED_QUEUE_SIZE = 180

# 주문서 및 채결 데이터 큐 초기화
orderbook_queue = deque(maxlen=ORDERBOOK_QUEUE_LIMIT)
ticks_queue = deque(maxlen=TICK_QUEUE_LIMIT)
merged_data_queue = deque(maxlen=MERGED_QUEUE_SIZE)

# 전처리 함수 정의
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

# 비동기 데이터 수집 및 병합
async def upbit_data_collector():
    global orderbook_queue, ticks_queue, merged_data_queue
    bf_timestamp = 0
    bf_sequential_id = 0

    while True:
        try:
            # 주문서 데이터 수집
            upbit_orderbook = await jupbit.async_get_orderbook("KRW-BTC")
            if upbit_orderbook and isinstance(upbit_orderbook, list):
                orderbook_data = preprocess_orderbook(upbit_orderbook[0])
                if orderbook_data:
                    orderbook_queue.append(orderbook_data)

            # 채결 데이터 수집
            upbit_ticks = await jupbit.async_get_ticks("KRW-BTC", "", "", "", "")
            for r in upbit_ticks:
                if bf_sequential_id < r['sequential_id']:
                    bf_sequential_id = r['sequential_id']
                    ticks_queue.append({
                        "timestamp": r['timestamp'],
                        "trade_price": r['trade_price'],
                        "ask_bid": 1 if r['ask_bid'] == 'BID' else 0
                    })

            # 병합
            orderbook_df = pd.DataFrame(list(orderbook_queue))
            ticks_df = pd.DataFrame(list(ticks_queue))
            if not orderbook_df.empty and not ticks_df.empty:
                processed_ticks_df = preprocess_ticks(ticks_df)
                merged_data = pd.merge_asof(orderbook_df.sort_values('timestamp'),
                                            processed_ticks_df.sort_values('timestamp'),
                                            on='timestamp',
                                            direction='nearest')
                for _, row in merged_data.iterrows():
                    merged_data_queue.append(row.to_dict())
        except Exception as e:
            print(f"Data Collection Error: {e}")
        await asyncio.sleep(1)

if __name__ == "__main__":
    asyncio.run(upbit_data_collector())
