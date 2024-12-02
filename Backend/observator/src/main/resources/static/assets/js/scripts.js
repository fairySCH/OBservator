document.addEventListener("DOMContentLoaded", function () {
    initializeWebSocketConnections();
    initializeUIElements();
});

// 실시간 시장 데이터에 대한 WebSocket 연결
function initializeWebSocketConnections() {
    // Upbit WebSocket 설정
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

    // bitFlyer WebSocket 설정
    const bitflyerWebSocket = new WebSocket("wss://ws.lightstream.bitflyer.com/json-rpc");
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
    const binanceWebSocket = new WebSocket("wss://stream.binance.com:9443/ws/btcusdt@ticker");
    binanceWebSocket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        const price = parseFloat(data.c);
        document.getElementById("binance-price").textContent = `${(price * 1400).toLocaleString()} KRW`; // USD-KRW 환율 적용 (1400 가정)
    };

    // Coinbase WebSocket 설정
    const coinbaseWebSocket = new WebSocket("wss://ws-feed.pro.coinbase.com");
    coinbaseWebSocket.onopen = () => {
        coinbaseWebSocket.send(JSON.stringify({
            type: "subscribe",
            channels: [{ name: "ticker", product_ids: ["BTC-USD"] }]
        }));
    };

    coinbaseWebSocket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        if (data.type === "ticker") {
            const price = parseFloat(data.price);
            document.getElementById("coinbase-price").textContent = `${(price * 1400).toLocaleString()} KRW`; // USD-KRW 환율 적용 (1400 가정)
        }
    };

    // 다른 암호화폐 가격을 가져오기 위한 WebSocket 설정
    const otherPricesWebSocket = new WebSocket("wss://api.upbit.com/websocket/v1");
    otherPricesWebSocket.onopen = () => {
        const payload = [
            { "ticket": "test" },
            { "type": "ticker", "codes": ["KRW-BTC", "KRW-ETH", "KRW-XRP"] }
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
}

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

// 차트 데이터를 업데이트하는 함수
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

// UI 요소 초기화
function initializeUIElements() {
    // 슬라이더 및 페이지 상의 UI 초기화 관련 코드
    const ThresholdSlider = document.getElementById("Threshold-slider");
    const hiddenThresholdLevel = document.getElementById("hidden-Threshold-level");
    const ThresholdLevelDisplay = document.getElementById("Threshold-level-display");

    function updateThresholdLevelDisplay(value) {
        if (value != 10) {
            ThresholdLevelDisplay.textContent = "0.0" + value + "%";
            hiddenThresholdLevel.value = parseFloat("0.000" + value);
        } else {
            ThresholdLevelDisplay.textContent = "0.1%";
            hiddenThresholdLevel.value = 0.001;
        }
    }

    ThresholdSlider.addEventListener("input", (e) => {
        updateThresholdLevelDisplay(e.target.value);
    });
    updateThresholdLevelDisplay(ThresholdSlider.value);
}

// 암호화폐 가격 데이터를 업데이트하는 함수
function updateCryptoPrices(data) {
    const tableBody = document.getElementById("crypto-table");
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
        nameCell.textContent = data.code;
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
