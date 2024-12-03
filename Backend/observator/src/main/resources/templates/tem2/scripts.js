            // const socket = new WebSocket("wss://14.32.188.229:8765");

            // socket.onerror = function (error) {
            //     console.error("WebSocket error:", error);
            // };
            
            // socket.onclose = function (event) {
            //     console.log("WebSocket closed:", event);
            // };
            //     // WebSocket 이벤트 처리
            // socket.onopen = function () {
            //     console.log("Connected to WebSocket server!");

            //     // 서버에 메시지 전송
            //     const message = {
            //         type: "init",
            //         content: "Hello, Server! Client connected.",
            //     };
            //     socket.send(JSON.stringify(message));
            //     console.log("Message sent to server:", message);
            // };


            // WebSocket을 통해 서버 연결
        const serverWebSocket = new WebSocket("wss://14.32.188.229:8765");

        serverWebSocket.onopen = () => {
            console.log("Connected to the server WebSocket");
        };

            serverWebSocket.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);

                    // 데이터가 예상하는 형식인지 확인
                    if (data.timestamp && data.predicted_price) {
                        const time = new Date(data.timestamp).toLocaleTimeString();
                        const predictedPrice = data.predicted_price;

                        // 데이터 업데이트
                        cumulativeData.push({ time });
                        predictedData.push({
                            time,
                            price: predictedPrice
                        });

                        // 차트 업데이트
                        updateChart();
                    } else {
                        console.warn("Received unexpected data format:", data);
                    }
                } catch (error) {
                    console.error("Error processing WebSocket message:", error);
                }
            };

            serverWebSocket.onerror = (error) => {
                console.error("WebSocket error:", error);
            };

            serverWebSocket.onclose = () => {
                console.log("WebSocket connection closed");
            };


            // 실시간 시장 데이터에 대한 WebSocket 연결
            function initializeWebSocketConnections() {
                // Upbit WebSocket 설정
                upbitWebSocket = new WebSocket("wss://api.upbit.com/websocket/v1");
            
                // KRW-BTC 티커, 호가창, 거래에 대한 구독
                upbitWebSocket.onopen = () => {
                    const payload = [
                        { "ticket": "test" },
                        { "type": "ticker", "codes": ["KRW-BTC"] },
                        { "type": "orderbook", "codes": ["KRW-BTC"] },
                        { "type": "trade", "codes": ["KRW-BTC"] }
                    ];
                    upbitWebSocket.send(JSON.stringify(payload));
                };
            
                // 들어오는 메시지 처리
                upbitWebSocket.onmessage = async (event) => {
                    const reader = new FileReader();
                    reader.onload = function () {
                        const data = JSON.parse(reader.result);
                        if (data.type === "ticker") {
                            currentBitcoinPrice = data.trade_price; // 실시간 비트코인 시세를 업데이트
                            updateTickerData(data);
                            updateChartData(data);
                        } else if (data.type === "orderbook") {
                            updateOrderBookData(data);
                        } else if (data.type === "trade") {
                            updateTradeData(data);
                        }
                    };
                    reader.readAsText(event.data);
                };
            
                upbitWebSocket.onerror = (error) => {
                    console.error("WebSocket error:", error);
                };
            
                upbitWebSocket.onclose = () => {
                    console.warn("WebSocket closed. Reconnecting...");
                    initializeWebSocketConnections(); // 연결이 끊어지면 다시 연결
                };
            }

let btcChart;
function updateChartData(data) {
    if (!btcChart) {
        const ctx = document.getElementById("btcChart").getContext("2d");
        btcChart = new Chart(ctx, {
            type: "line",
            data: {
                labels: [],
                datasets: [
                    {
                        label: "비트코인 가격 (KRW)",
                        data: [],
                        borderColor: "rgba(54, 162, 235, 1)",
                        borderWidth: 2,
                        tension: 0.4, // 부드러운 곡선
                        pointRadius: 0
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        title: {
                            display: true,
                            text: "시간"
                        }
                    },
                    y: {
                        title: {
                            display: true,
                            text: "가격 (KRW)"
                        }
                    }
                }
            }
        });
    }
    btcChart.data.labels.push(new Date(data.timestamp).toLocaleTimeString());
    btcChart.data.datasets[0].data.push(data.trade_price);
    btcChart.update();
}

const upbitWebSocket = new WebSocket("wss://api.upbit.com/websocket/v1");

// KRW-BTC 티커, 호가창, 거래에 대한 구독
upbitWebSocket.onopen = () => {
    const payload = [
        { "ticket": "test" },
        { "type": "ticker", "codes": ["KRW-BTC"] },
        { "type": "orderbook", "codes": ["KRW-BTC"] },
        { "type": "trade", "codes": ["KRW-BTC"] }
    ];
    upbitWebSocket.send(JSON.stringify(payload));
};

// 들어오는 메시지 처리
upbitWebSocket.onmessage = async (event) => {
    const reader = new FileReader();
    reader.onload = function () {
        const data = JSON.parse(reader.result);
        if (data.type === "ticker") {
            updateTickerData(data);
             updateChartData(data);
        } else if (data.type === "orderbook") {
            updateOrderBookData(data);
        } else if (data.type === "trade") {
            updateTradeData(data);
        }
    };
    reader.readAsText(event.data);
};

// 페이지의 티커 데이터를 업데이트하는 함수
function updateTickerData(data) {
    document.querySelector(".current-price").textContent = `${data.trade_price.toLocaleString()} KRW`;
    document.querySelector(".change-percent").textContent = `${(data.signed_change_rate * 100).toFixed(2)}%`;
    document.querySelector(".change-amount").textContent = `${data.signed_change_price > 0 ? "▲ " : "▼ "}${data.signed_change_price.toLocaleString()}`;
    document.querySelector(".volume").textContent = `${data.acc_trade_volume_24h.toFixed(3)} BTC`;
    document.querySelector(".trade-amount").textContent = `${data.acc_trade_price_24h.toLocaleString()} KRW`;

    // 가격 요소의 색상을 가격 변동에 따라 업데이트
    const priceElement = document.querySelector(".current-price");
    priceElement.style.color = data.signed_change_price > 0 ? "#28a745" : "#dc3545";
}

// 페이지의 호가 데이터를 업데이트하는 함수
function updateOrderBookData(data) {
    const orderbookBody = document.getElementById("special-orderbook-body");
    orderbookBody.innerHTML = "";

    const maxVolume = Math.max(...data.orderbook_units.map((item) => Math.max(item.ask_size, item.bid_size)));

    data.orderbook_units.forEach((item, index) => {
        const type = index < data.orderbook_units.length / 2 ? "매도" : "매수";
        const price = type === "매도" ? item.ask_price : item.bid_price;
        const volume = type === "매도" ? item.ask_size : item.bid_size;

        const percentage = (volume / maxVolume) * 100;

        const row = `
            <tr style="background-color: ${type === "매수" ? "rgba(40, 167, 69, 0.1)" : "rgba(220, 53, 69, 0.1)"};">
                <td style="font-weight: bold; color: ${type === "매수" ? "#28a745" : "#dc3545"};">${price.toLocaleString()}</td>
                <td>${volume.toFixed(3)}</td>
                <td style=" text-align: right;">${(price * volume).toLocaleString()}</td>
                <td style="width: 100px; text-align: left;">
                    <div style="height: 20px; background-color: ${type === "매수" ? "#28a745" : "#dc3545"}; width: ${percentage}%;"></div>
                </td>
                <td>${percentage.toFixed(2)}%</td>
            </tr>
        `;

        orderbookBody.innerHTML += row;
    });
}

// 페이지의 거래 데이터를 업데이트하는 함수
function updateTradeData(data) {
    const tradeBody = document.getElementById("trade-history-body");
    if (tradeBody) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${new Date(data.timestamp).toLocaleTimeString()}</td>
            <td style="color: ${data.ask_bid === "ASK" ? "#dc3545" : "#28a745"}; font-weight: bold;">${data.trade_price.toLocaleString()}</td>
            <td>${data.trade_volume.toFixed(3)}</td>
        `;
        tradeBody.insertBefore(row, tradeBody.firstChild);
    }
}


// 각 거래소의 가격을 가져오기 위한 WebSocket 설정

const bitflyerWebSocket = new WebSocket("wss://ws.lightstream.bitflyer.com/json-rpc");
const binanceWebSocket = new WebSocket("wss://stream.binance.com:9443/ws/btcusdt@ticker");

// bitFlyer WebSocket 설정
bitflyerWebSocket.onopen = () => {
    bitflyerWebSocket.send(JSON.stringify({
        method: "subscribe",
        params: { channel: "lightning_ticker_BTC_JPY" },
        id: 1
    }));
};

bitflyerWebSocket.onmessage = (event) => {
    const data = JSON.parse(event.data);
    if (data.params && data.params.message) {
        const price = data.params.message.ltp;
        document.getElementById("bitflyer-price").textContent = `${(price * 10).toLocaleString()} KRW`; // JPY-KRW 환율 적용 (10 가정)
    }
};

// Binance WebSocket 설정
binanceWebSocket.onmessage = (event) => {
    const data = JSON.parse(event.data);
    const price = parseFloat(data.c);
    document.getElementById("binance-price").textContent = `${(price * 1400).toLocaleString()} KRW`; // USD-KRW 환율 적용 (1400 가정)
    
};


// 작업중

const okxWebSocket = new WebSocket("wss://ws.okx.com:8443/ws/v5/public");

// WebSocket 연결이 열리면 구독 요청 전송
okxWebSocket.onopen = () => {
    // console.log("WebSocket 연결 성공");
    const subscribeMessage = JSON.stringify({
        op: "subscribe",
        args: [
            {
                channel: "tickers",
                instId: "BTC-USDT", // 비트코인/USDT 거래쌍
            },
        ],
    });
    okxWebSocket.send(subscribeMessage);
    // console.log("구독 메시지 전송");
};

// WebSocket에서 메시지 수신
okxWebSocket.onmessage = (event) => {
    const response = JSON.parse(event.data);
    if (response.event === "subscribe") {
        // console.log("구독 성공:", response);
    } else if (response.arg && response.arg.channel === "tickers") {
        const tickerData = response.data[0];
        const price = parseFloat(tickerData.last);
        const krwPrice = price * 1400; // 환율 적용
        document.getElementById("okx-price").textContent = `${krwPrice.toLocaleString()} KRW`;
    }
};

// WebSocket 오류 처리
okxWebSocket.onerror = (error) => {
    console.error("WebSocket 오류:", error);
};

// WebSocket 연결 종료 처리
okxWebSocket.onclose = () => {
    console.log("WebSocket 연결 종료");
};


const otherPricesWebSocket = new WebSocket("wss://api.upbit.com/websocket/v1");

const krwMarkets = [
    { code: "KRW-BTC", name: "비트코인" },
    { code: "KRW-ETH", name: "이더리움" },
    { code: "KRW-XRP", name: "리플" },
    { code: "KRW-ADA", name: "에이다" },
    { code: "KRW-SOL", name: "솔라나" },
    { code: "KRW-DOT", name: "폴카닷" },
    { code: "KRW-DOGE", name: "도지코인" },
    { code: "KRW-AVAX", name: "아발란체" },
    { code: "KRW-TRX", name: "트론" },
    { code: "KRW-MATIC", name: "폴리곤" },
    { code: "KRW-LTC", name: "라이트코인" },
    { code: "KRW-BCH", name: "비트코인캐시" },
    { code: "KRW-ATOM", name: "코스모스" },
    { code: "KRW-ALGO", name: "알고랜드" },
    { code: "KRW-VET", name: "비체인" },
    { code: "KRW-ICP", name: "인터넷컴퓨터" },
    { code: "KRW-FTT", name: "FTX 토큰" },
    { code: "KRW-THETA", name: "쎄타토큰" },
    { code: "KRW-AXS", name: "엑시인피니티" },
    { code: "KRW-FIL", name: "파일코인" },
    { code: "KRW-EOS", name: "이오스" },
    { code: "KRW-AAVE", name: "에이브" },
    { code: "KRW-NEO", name: "네오" },
    { code: "KRW-KSM", name: "쿠사마" },
    { code: "KRW-CHZ", name: "칠리즈" },
    { code: "KRW-SAND", name: "샌드박스" },
    { code: "KRW-ENJ", name: "엔진코인" },
    { code: "KRW-MANA", name: "디센트럴랜드" },
    { code: "KRW-ZIL", name: "질리카" },
    { code: "KRW-HBAR", name: "헤데라" }
];

function initializeWebSocket() {
    const marketCodes = krwMarkets.map((market) => market.code);

    otherPricesWebSocket.onopen = () => {
        const payload = [
            { ticket: "test" },
            { type: "ticker", codes: marketCodes }
        ];
        otherPricesWebSocket.send(JSON.stringify(payload));
    };

    otherPricesWebSocket.onmessage = (event) => {
        const reader = new FileReader();
        reader.onload = function () {
            const data = JSON.parse(reader.result);
            updateCryptoPrices(data);
        };
        reader.readAsText(event.data);
    };

    otherPricesWebSocket.onerror = (error) => {
        console.error("WebSocket error:", error);
    };

    otherPricesWebSocket.onclose = () => {
        console.log("WebSocket connection closed");
    };
}

function updateCryptoPrices(data) {
    const tableBody = document.getElementById("crypto-table");
    const marketInfo = krwMarkets.find((market) => market.code === data.code);

    if (!marketInfo) return;

    const existingRow = document.querySelector(`.crypto-table-row[data-market='${data.code}']`);

    if (existingRow) {
        existingRow.querySelector(".price-cell").textContent = `${data.trade_price.toLocaleString()} KRW`;
        const changeRate = (data.signed_change_rate * 100).toFixed(2);
        const changeCell = existingRow.querySelector(".change-cell");
        changeCell.textContent = `${changeRate}%`;
        changeCell.className = `change-cell ${changeRate > 0 ? "crypto-positive" : "crypto-negative"}`;
        existingRow.querySelector(".volume-cell").textContent = `${data.acc_trade_volume_24h.toFixed(2)}`;
    } else {
        const row = document.createElement("tr");
        row.className = "crypto-table-row";
        row.setAttribute("data-market", data.code);

        const nameCell = document.createElement("td");
        nameCell.textContent = marketInfo.name;
        nameCell.className = "crypto-table-row-cell";
        row.appendChild(nameCell);

        const priceCell = document.createElement("td");
        priceCell.textContent = `${data.trade_price.toLocaleString()} KRW`;
        priceCell.className = "crypto-table-row-cell price-cell";
        row.appendChild(priceCell);

        const changeCell = document.createElement("td");
        const changeRate = (data.signed_change_rate * 100).toFixed(2);
        changeCell.textContent = `${changeRate}%`;
        changeCell.className = `crypto-table-row-cell change-cell ${changeRate > 0 ? "crypto-positive" : "crypto-negative"}`;
        row.appendChild(changeCell);

        const volumeCell = document.createElement("td");
        volumeCell.textContent = `${data.acc_trade_volume_24h.toFixed(2)}`;
        volumeCell.className = "crypto-table-row-cell volume-cell";
        row.appendChild(volumeCell);

        tableBody.appendChild(row);
    }
}

initializeWebSocket();
const NEWS_API_URL = "https://min-api.cryptocompare.com/data/v2/news/?categories=BTC";

async function fetchBitcoinNews() {
    try {
        const response = await fetch(NEWS_API_URL);
        const data = await response.json();

        if (data.Data) {
            return data.Data.slice(0, 3); // 최근 3개 뉴스
        } else {
            console.error("Failed to fetch news:", data);
            return [];
        }
    } catch (error) {
        console.error("Error fetching news:", error);
        return [];
    }
}

function updateNewsTable(articles) {
    const tableBody = document.getElementById("news-table-body");

    // 기존 뉴스 목록 초기화
    tableBody.innerHTML = "";

    articles.forEach((article) => {
        const row = document.createElement("tr");
        row.className = "table-row";

        // Date 필드 (항상 "방금" 표시)
        const dateCell = document.createElement("td");
        dateCell.className = "table-cell";
        dateCell.textContent = "방금";
        row.appendChild(dateCell);

        // Title 필드
        const titleCell = document.createElement("td");
        titleCell.className = "table-cell";
        const titleLink = document.createElement("a");
        titleLink.href = article.url; // 뉴스 URL로 링크
        titleLink.textContent = article.title; // 뉴스 제목
        titleLink.target = "_blank"; // 새 탭에서 열기
        titleCell.appendChild(titleLink);
        row.appendChild(titleCell);

        tableBody.appendChild(row);
    });
}

// 초기 호출 및 주기적 업데이트
(async () => {
    const initialArticles = await fetchBitcoinNews();
    updateNewsTable(initialArticles);

    setInterval(async () => {
        const newsArticles = await fetchBitcoinNews();
        updateNewsTable(newsArticles);
    }, 10000); // 10초마다 업데이트
})();
