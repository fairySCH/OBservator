// 민재 작업 ---------------------------------------------------------------

const socket = new WebSocket("ws://14.32.188.229:8765");

socket.onopen = () => {
    console.log("WebSocket connection established.");
};

socket.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log("Prediction data received:", data);
    // Update the chart or UI here
};

socket.onclose = () => {
    console.log("WebSocket connection closed.");
};

socket.onerror = (error) => {
    console.error("WebSocket error:", error);
};
// 민재 작업 ---------------------------------------------------------------

// WebSocket 연결 초기화
function initializeWebSocketConnections() {
    const upbitWebSocket = new WebSocket("wss://api.upbit.com/websocket/v1");

    upbitWebSocket.onopen = () => {
       
        const payload = [
            { ticket: "test" },
            { type: "ticker", codes: ["KRW-BTC"] } // 비트코인 티커 데이터
        ];
        upbitWebSocket.send(JSON.stringify(payload));
    };

    upbitWebSocket.onmessage = (event) => {
        const reader = new FileReader();
        reader.onload = () => {
            const data = JSON.parse(reader.result);
            if (data.type === "ticker") {
                updateChartData(data); // 차트 업데이트
                updateTickerData(data); // HTML에 실시간 가격 표시
            }
        };
        reader.readAsText(event.data);
    };

    upbitWebSocket.onerror = (error) => {
        console.error("WebSocket 에러:", error);
    };

    upbitWebSocket.onclose = () => {
        console.warn("WebSocket 연결 종료. 다시 연결 시도 중...");
        setTimeout(initializeWebSocketConnections, 3000); // 3초 후 재연결
    };
}


let btcChart;
function updateChartData(data) {
    if (!btcChart) {
        const ctx = document.getElementById("btcChart").getContext("2d");
        btcChart = new Chart(ctx, {
            type: "line",
            data: {
                labels: [], // X축 레이블 (시간)
                datasets: [
                    {
                        label: "비트코인 가격 (KRW)",
                        data: [], // Y축 데이터 (실시간 가격)
                        borderColor: "rgba(54, 162, 235, 1)",
                        borderWidth: 2,
                        tension: 0.5, // 부드러운 곡선
                        pointRadius: 0
                    },
                    {
                        label: "예측 가격 (KRW)",
                        data: [], // Y축 데이터 (예측 가격)
                        borderColor: "rgba(255, 99, 132, 1)",
                        borderWidth: 4,
                        tension: 0.5,
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

    // 현재 시간과 5초 뒤 시간 계산
    const currentTime = new Date();
    const futureTime = new Date(currentTime.getTime() + 5000);
    const currentTimeString = currentTime.toLocaleTimeString();
    const futureTimeString = futureTime.toLocaleTimeString();

    // 현재 가격 데이터 추가
    btcChart.data.labels.push(currentTimeString);
    btcChart.data.datasets[0].data.push(data.trade_price);

    // 예측 가격 계산 (더미 데이터)
    const predictedPrice = generatePrediction(data.trade_price);

    // 예측 데이터 추가 (5초 뒤 시간)
    btcChart.data.labels.push(futureTimeString);
    btcChart.data.datasets[1].data.push(predictedPrice);

    // 데이터 개수 제한 (최대 50개 유지)
    if (btcChart.data.labels.length > 100) {
        btcChart.data.labels.splice(0, 2); // 현재와 예측 데이터를 한 쌍으로 삭제
        btcChart.data.datasets[0].data.shift();
        btcChart.data.datasets[1].data.shift();
    }

    // 차트 업데이트
    btcChart.update();
}
// 더미 예측 데이터 생성 함수
function generatePrediction(currentPrice) {
    const fluctuation = (Math.random() - 0.5) * 0.010002 * currentPrice; // ±2% 변동
    return currentPrice + fluctuation;
}


// 실시간 티커 데이터 업데이트
function updateTickerData(data) {
    document.getElementById("current-price").textContent = `${data.trade_price.toLocaleString()} KRW`;
}

// WebSocket 연결 초기화
initializeWebSocketConnections();

// 실시간 티커 데이터 업데이트
function updateTickerData(data) {
    document.getElementById("current-price").textContent = `${data.trade_price.toLocaleString()} KRW`;
}
// 실시간 호가 데이터 업데이트 (예제)
function updateOrderBookData(data) {
    console.log("호가 데이터:", data);
}
// 실시간 거래 내역 업데이트 (예제)
function updateTradeData(data) {
    console.log("거래 데이터:", data);
}
// WebSocket 연결 초기화
initializeWebSocketConnections();

// Chart.js 초기 설정
const upbitWebSocket = new WebSocket("wss://api.upbit.com/websocket/v1");

//// 최근 일주일 비트코인 가격 차트 만들기  
//// 최근 일주일 비트코인 가격 차트 만들기  
//// 최근 일주일 비트코인 가격 차트 만들기  

// 최근 일주일 데이터 저장
// 최근 7일 데이터 저장
const weekData = {
    timestamps: [], // 날짜 데이터
    prices: []      // 가격 데이터
};

// 차트 초기화
let btcWeekChart;
function initializeWeekChart() {
    const ctx = document.getElementById("btcWeekChart").getContext("2d");
    btcWeekChart = new Chart(ctx, {
        type: "line",
        data: {
            labels: weekData.timestamps, // X축 날짜
            datasets: [{
                label: "최근 7일 비트코인 가격",
                data: weekData.prices,   // Y축 가격
                borderColor: "rgba(255, 99, 132, 1)", // 선 색상 (분홍색)
                backgroundColor: "rgba(255, 99, 132, 0.2)", // 배경색 (투명 분홍색)
                tension: 0.4, // 부드러운 곡선
                pointRadius: 4, // 데이터 포인트 크기
                pointHoverRadius: 8, // 커서를 올릴 때 데이터 포인트 크기
                fill: true // 선 아래를 채움
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                tooltip: {
                    enabled: true, // 툴팁 활성화
                    callbacks: {
                        label: function (context) {
                            const value = context.raw; // 해당 데이터 값
                            return `가격: ${value.toLocaleString()} KRW`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    title: {
                        display: true,
                        text: "날짜"
                    },
                    ticks: {
                        maxTicksLimit: 7 // X축에 표시되는 최대 날짜 수
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: "가격 (KRW)"
                    },
                    ticks: {
                        callback: function (value) {
                            return value.toLocaleString() + " KRW"; // Y축 값 포맷
                        }
                    }
                }
            },
            interaction: {
                mode: "nearest", // 가장 가까운 데이터 포인트에 반응
                intersect: false // 선 위에 있을 때도 반응
            }
        }
    });
}

// Upbit API를 통해 최근 7일 데이터 가져오기
async function fetchWeekData() {
    const apiUrl = "https://api.upbit.com/v1/candles/days?market=KRW-BTC&count=7";

    try {
        const response = await fetch(apiUrl);
        const data = await response.json();

        // 데이터 처리
        weekData.timestamps = data.map((item) => item.candle_date_time_kst.split("T")[0]); // 날짜 (YYYY-MM-DD)
        weekData.prices = data.map((item) => item.trade_price); // 종가

        console.log("Fetched Week Data:", weekData);

        // 차트 업데이트
        btcWeekChart.data.labels = weekData.timestamps.reverse(); // 날짜 역순으로 변경
        btcWeekChart.data.datasets[0].data = weekData.prices.reverse(); // 가격 역순으로 변경
        btcWeekChart.update();
    } catch (error) {
        console.error("Error fetching week data:", error);
    }
}

// 차트 초기화 및 데이터 가져오기
initializeWeekChart();
fetchWeekData();


// ---------------------------------------------------------------

// 최근 한달 차트 만들기 

// 최근 한 달 데이터 저장
const monthData = {
    timestamps: [], // 날짜 데이터
    prices: []      // 가격 데이터
};

// 차트 초기화
let btcMonthChart;
function initializeMonthChart() {
    const ctx = document.getElementById("btcMonthChart").getContext("2d");
    btcMonthChart = new Chart(ctx, {
        type: "line",
        data: {
            labels: monthData.timestamps, // X축 날짜
            datasets: [{
                label: "최근 한 달 비트코인 가격",
                data: monthData.prices,   // Y축 가격
                borderColor: "rgba(75, 192, 192, 1)", // 선 색상
                backgroundColor: "rgba(75, 192, 192, 0.2)", // 배경 색상
                tension: 0.4, // 부드러운 곡선
                pointRadius: 3, // 데이터 포인트 크기
                pointHoverRadius: 6, // 커서를 올릴 때 데이터 포인트 크기
                fill: true // 선 아래를 채움
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                tooltip: {
                    enabled: true, // 툴팁 활성화
                    callbacks: {
                        label: function (context) {
                            const value = context.raw; // 해당 데이터 값
                            return `가격: ${value.toLocaleString()} KRW`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    title: {
                        display: true,
                        text: "날짜"
                    },
                    ticks: {
                        maxTicksLimit: 10 // X축에 표시되는 최대 날짜 수
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: "가격 (KRW)"
                    },
                    ticks: {
                        callback: function (value) {
                            return value.toLocaleString() + " KRW"; // Y축 값 포맷
                        }
                    }
                }
            },
            interaction: {
                mode: "nearest", // 가장 가까운 데이터 포인트에 반응
                intersect: false // 선 위에 있을 때도 반응
            }
        }
    });
}

// Upbit API를 통해 최근 한 달 데이터 가져오기
async function fetchMonthData() {
    const apiUrl = "https://api.upbit.com/v1/candles/days?market=KRW-BTC&count=30";

    try {
        const response = await fetch(apiUrl);
        const data = await response.json();

        // 데이터 처리
        monthData.timestamps = data.map((item) => item.candle_date_time_kst.split("T")[0]); // 날짜 (YYYY-MM-DD)
        monthData.prices = data.map((item) => item.trade_price); // 종가

        console.log("Fetched Month Data:", monthData);

        // 차트 업데이트
        btcMonthChart.data.labels = monthData.timestamps.reverse(); // 날짜 역순으로 변경
        btcMonthChart.data.datasets[0].data = monthData.prices.reverse(); // 가격 역순으로 변경
        btcMonthChart.update();
    } catch (error) {
        console.error("Error fetching month data:", error);
    }
}

// 차트 초기화 및 데이터 가져오기
initializeMonthChart();
fetchMonthData();


// --------------------


function showChart(chartId) {
    // 모든 차트를 숨김
    const charts = document.querySelectorAll('.chart-container');
    charts.forEach(chart => chart.classList.add('hidden'));

    // 클릭된 차트만 표시
    document.getElementById(`${chartId}-container`).classList.remove('hidden');

    // 모든 버튼의 활성화 상태 제거
    const buttons = document.querySelectorAll('.tab-button');
    buttons.forEach(button => button.classList.remove('active'));

    // 현재 버튼 활성화
    const activeButton = document.querySelector(`button[onclick="showChart('${chartId}')"]`);
    activeButton.classList.add('active');
}



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
