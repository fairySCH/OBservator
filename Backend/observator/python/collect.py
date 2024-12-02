import asyncio
import pandas as pd
from multiprocessing import Manager
from datetime import datetime, timedelta
import jinupbit

# Upbit API 객체 초기화
jupbit = jinupbit.JinUpbit("", "")

# 공유 메모리 설정
manager = Manager()

# 공유 리스트 설정
merged_data = manager.list()
orderbook_data_list = manager.list()
ticks_data_list = manager.list()

# 데이터 전처리 함수
def preprocess_orderbook(orderbook_data):
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

# 병합 및 저장 함수
def merge_and_save():
    global orderbook_data_list, ticks_data_list, merged_data
    try:
        orderbook_df = pd.DataFrame(list(orderbook_data_list))
        ticks_df = pd.DataFrame(list(ticks_data_list))
        processed_ticks_df = preprocess_ticks(ticks_df)

        if not orderbook_df.empty and not processed_ticks_df.empty:
            merged_df = pd.merge_asof(orderbook_df.sort_values('timestamp'),
                                      processed_ticks_df.sort_values('timestamp'),
                                      on='timestamp',
                                      direction='nearest')
            merged_data[:] = merged_df.to_dict('records')  # 공유 리스트에 업데이트
    except Exception as e:
        print(f"Merge Error: {e}")

# Upbit 호가 데이터를 수집하는 비동기 함수
async def upbit_get_orderbook():
    global orderbook_data_list
    try:
        bf_timestamp = 0
        while True:
            upbit_res = await jupbit.async_get_orderbook("KRW-BTC")

            if isinstance(upbit_res, dict):
                continue

            upbit_res = upbit_res[0]
            if bf_timestamp >= int(upbit_res['timestamp']):
                await asyncio.sleep(0.2)
                continue

            bf_timestamp = int(upbit_res['timestamp'])
            processed_data = preprocess_orderbook(upbit_res)
            orderbook_data_list.append(processed_data)

            await asyncio.sleep(0.2)
    except Exception as e:
        print(f"Orderbook Error: {e}")

# Upbit 체결 데이터를 수집하는 비동기 함수
async def upbit_get_ticks():
    global ticks_data_list
    try:
        bf_sequential_id = 0
        while True:
            upbit_res = await jupbit.async_get_ticks("KRW-BTC", "", "", "", "")

            for r in upbit_res:
                if bf_sequential_id < r['sequential_id']:
                    bf_sequential_id = r['sequential_id']

                    ticks_data_list.append({
                        "timestamp": r['timestamp'],
                        "trade_price": r['trade_price'],
                        "ask_bid": 1 if r['ask_bid'] == 'BID' else 0
                    })

            await asyncio.sleep(0.5)
    except Exception as e:
        print(f"Ticks Error: {e}")

# 하루가 지나면 데이터를 초기화하는 함수
async def reset_daily_data():
    global orderbook_data_list, ticks_data_list, merged_data
    while True:
        now = datetime.now()
        next_reset = (now + timedelta(days=1)).replace(hour=0, minute=0, second=0, microsecond=0)
        sleep_duration = (next_reset - now).total_seconds()
        await asyncio.sleep(sleep_duration)

        orderbook_data_list[:] = []
        ticks_data_list[:] = []
        merged_data[:] = []
        print(f"[{datetime.now()}] Data has been reset for the new day.")

# 병합 작업을 주기적으로 수행하는 함수
async def periodic_merge_and_save():
    while True:
        merge_and_save()
        await asyncio.sleep(0.2)  # 0.2초마다 병합 수행

# 메인 실행 함수
async def main():
    try:
        tasks = [
            asyncio.create_task(upbit_get_orderbook()),
            asyncio.create_task(upbit_get_ticks()),
            asyncio.create_task(periodic_merge_and_save()),
            asyncio.create_task(reset_daily_data())
        ]
        await asyncio.gather(*tasks)
    except Exception as e:
        print(f"Main Procedure Error: {e}")

if __name__ == "__main__":
    asyncio.run(main())
