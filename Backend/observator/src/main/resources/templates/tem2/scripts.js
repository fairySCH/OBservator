  
// Fetch scheduled orders and display them in the table
async function fetchScheduledOrders() {
    try {
        const response = await fetch("/api/scheduledOrders"); // Replace with actual API endpoint
        const data = await response.json();

        const tableBody = document.querySelector("#scheduledOrdersTable tbody");
        tableBody.innerHTML = "";

        data.forEach(order => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td style="padding: 10px; border: 1px solid #dddddd;">${order.time}</td>
                <td style="padding: 10px; border: 1px solid #dddddd;">${order.action}</td>
                <td style="padding: 10px; border: 1px solid #dddddd;">${order.amount}</td>
            `;
            tableBody.appendChild(row);
        });
    } catch (error) {
        console.error("Failed to fetch scheduled orders:", error.message);
    }
}

// Periodically update the scheduled orders table
setInterval(fetchScheduledOrders, 10000); // Update every 10 seconds
fetchScheduledOrders(); // Initial call

///위에까지 스케중 오더



async function fetchSpecialMarketData() {
    try {
        // 업비트 데이터 가져오기
        const tickerResponse = await fetch("https://api.upbit.com/v1/ticker?markets=KRW-BTC");
        const tickerData = await tickerResponse.json();
        const ticker = tickerData[0];

        const orderbookResponse = await fetch("https://api.upbit.com/v1/orderbook?markets=KRW-BTC");
        const orderbookData = await orderbookResponse.json();
        const orderbook = orderbookData[0].orderbook_units;

        // 거래 정보 업데이트
        document.getElementById("special-trade-volume").textContent = `${ticker.acc_trade_volume_24h.toFixed(3)} BTC`;
        document.getElementById("special-trade-amount").textContent = `${(ticker.acc_trade_price_24h / 1e8).toFixed(3)} 백만원`;
        document.getElementById("special-year-high").textContent = `${ticker.high_price.toLocaleString()} `;
        document.getElementById("special-year-low").textContent = `${ticker.low_price.toLocaleString()}`;
        document.getElementById("special-day-high").textContent = `${ticker.high_price.toLocaleString()}`;
        document.getElementById("special-day-low").textContent = `${ticker.low_price.toLocaleString()}`;

        // 누적호가창 업데이트
        const orderbookBody = document.getElementById("special-orderbook-body");
        orderbookBody.innerHTML = ""; // 기존 내용 삭제

        // 호가 주문량의 최대값 계산
        const maxVolume = Math.max(...orderbook.map((item) => Math.max(item.ask_size, item.bid_size)));

        orderbook.forEach((item, index) => {
            const type = index < orderbook.length / 2 ? "Sell" : "Buy";
            const price = type === "Sell" ? item.ask_price : item.bid_price;
            const volume = type === "Sell" ? item.ask_size : item.bid_size;

            // 비율 계산
            const percentage = (volume / maxVolume) * 100;

            const row = `
                <tr style="background-color: ${type === "Buy" ? "rgba(40, 167, 69, 0.1)" : "rgba(220, 53, 69, 0.1)"};">
                    <td style="font-weight: bold; color: ${type === "Buy" ? "#28a745" : "#dc3545"};">${price.toLocaleString()}</td>
                    <td>${volume.toFixed(3)}</td>
                    <td style=" text-align: right;">${(price * volume).toLocaleString()}</td>
                    <td style="width: 100px; text-align: left;">
                        <div style="height: 20px; background-color: ${type === "Buy" ? "#28a745" : "#dc3545"}; width: ${percentage}%;"></div>
                    </td>
                    <td>${percentage.toFixed(2)}%</td>
                </tr>
            `;

            orderbookBody.innerHTML += row;
        });
    } catch (error) {
        console.error("Failed to fetch market or orderbook data:", error);
    }
}

// 1초마다 데이터 갱신
fetchSpecialMarketData();
setInterval(fetchSpecialMarketData, 2000);


// 1초마다 데이터 갱신
fetchSpecialMarketData();
setInterval(fetchSpecialMarketData, 2000);



        let cumulativeData = [];
        let volumeData = [];
        let predictedData = [];
        
        // Fetch Bitcoin data
        async function fetchBitcoinData() {
            const response = await fetch("https://api.upbit.com/v1/candles/minutes/1?market=KRW-BTC&count=1");
            const data = await response.json();
            return {
                time: new Date(data[0].timestamp).toLocaleTimeString(),
                price: data[0].trade_price,
                volume: data[0].candle_acc_trade_volume
            };
        }
        
        // 업비트 Orderbook data
        async function fetchOrderBook() {
            const response = await fetch("https://api.upbit.com/v1/orderbook?markets=KRW-BTC");
            const data = await response.json();
            return data[0].orderbook_units.slice(0, 15);
        }
        
        // Update Order Book
        async function updateOrderBook() {
            const orderBook = await fetchOrderBook();
            const orderbookBody = document.getElementById("orderbook-body");
            orderbookBody.innerHTML = "";
        
            orderBook.forEach((order, index) => {
                const row = document.createElement("tr");
                row.className = index < 7 ? "sell" : "buy";
                row.innerHTML = `
                    <td>${index < 7 ? "Sell" : "Buy"}</td>
                    <td>${index < 7 ? order.ask_price : order.bid_price}</td>
                    <td>${index < 7 ? order.ask_size : order.bid_size}</td>
                `;
                orderbookBody.appendChild(row);
            });
        }
        
        // Generate dummy predicted data
        function generateDummyPredictedData(currentPrice) {
            const fluctuation = (Math.random() - 0.5) * 0.02 * currentPrice; // ±2% fluctuation
            return currentPrice + fluctuation;
        }
        
        // Chart 업데이트
        let lineChart;
        async function updateChart() {
            const dataPoint = await fetchBitcoinData();
            cumulativeData.push(dataPoint);
            volumeData.push(dataPoint.volume);
            predictedData.push({
                time: dataPoint.time,
                price: generateDummyPredictedData(dataPoint.price)
            });
        
            if (!lineChart) {
                const ctx = document.getElementById("btcChart").getContext("2d");
                lineChart = new Chart(ctx, {
                    type: "line",
                    data: {
                        labels: cumulativeData.map((item) => item.time),
                        datasets: [
                            {
                                label: "Bitcoin Price (KRW)",
                                data: cumulativeData.map((item) => item.price),
                                borderColor: "rgba(54, 162, 235, 1)",
                                borderWidth: 2,
                                tension: 0.4, // Smoother curve
                                pointRadius: 0
                            },
                            {
                                label: "Volume",
                                data: volumeData,
                                type: "bar",
                                backgroundColor: "rgba(153, 102, 255, 0.2)",
                                borderColor: "rgba(153, 102, 255, 1)",
                                borderWidth: 1,
                                yAxisID: "volume-axis"
                            },
                            {
                                label: "Predicted Price (KRW)",
                                data: predictedData.map((item) => item.price),
                                borderColor: "rgba(255, 99, 132, 1)",
                                borderWidth: 2,
                                tension: 0.4, // Smoother curve
                                pointRadius: 0 // No points for smoother visualization
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
                                    text: "Time"
                                }
                            },
                            y: {
                                title: {
                                    display: true,
                                    text: "Price (KRW)"
                                }
                            },
                            "volume-axis": {
                                position: "right",
                                title: {
                                    display: true,
                                    text: "Volume"
                                },
                                grid: {
                                    drawOnChartArea: false
                                }
                            }
                        }
                    }
                });
            } else {
                lineChart.data.labels = cumulativeData.map((item) => item.time);
                lineChart.data.datasets[0].data = cumulativeData.map((item) => item.price);
                lineChart.data.datasets[1].data = volumeData;
                lineChart.data.datasets[2].data = predictedData.map((item) => item.price);
                lineChart.update();
            }
        }
        
        // Initialize and update chart and order book every 1 seconds
        setInterval(() => {
            updateChart();
            updateOrderBook();
        }, 1000);
        
        // Risk slider
        document.getElementById("risk-slider").addEventListener("input", (e) => {
            document.getElementById("risk-level-display").textContent = e.target.value;
        });
        
        // 초 상단 제목 
        
        // https://docs.upbit.com/reference/%EC%B4%88second-%EC%BA%94%EB%93%A4
        // API 확인 : 멘토님추천 
        
        async function fetchHeaderData() {
            const response = await fetch("https://api.upbit.com/v1/ticker?markets=KRW-BTC");
            const data = await response.json();
            const priceData = data[0];
        
            document.querySelector(".current-price").textContent = priceData.trade_price.toLocaleString() + " KRW";
            document.querySelector(".change-percent").textContent = (priceData.signed_change_rate * 100).toFixed(2) + "%";
            document.querySelector(".change-amount").textContent = (priceData.signed_change_price > 0 ? "▲ " : "▼ ") + priceData.signed_change_price.toLocaleString();
            document.querySelector(".volume").textContent = priceData.acc_trade_volume_24h.toFixed(3) + " BTC";
            document.querySelector(".trade-amount").textContent = priceData.acc_trade_price_24h.toLocaleString() + " KRW";
        }
        
        // 메인 그래프 
        async function fetchRealTimePrice() {
            try {
                const response = await fetch("https://api.upbit.com/v1/ticker?markets=KRW-BTC");
                const data = await response.json();
                const priceData = data[0];
        
                // 가격 업데이트
                const priceElement = document.querySelector(".current-price");
                const currentPrice = parseFloat(priceData.trade_price).toLocaleString() + " KRW";
                priceElement.textContent = currentPrice;
        
                // 변동률 및 금액 업데이트
                const changePercent = (priceData.signed_change_rate * 500).toFixed(2) + "%";
                const changeAmount = (priceData.signed_change_price > 0 ? "▲ " : "▼ ") + parseFloat(priceData.signed_change_price).toLocaleString();
                document.querySelector(".change-percent").textContent = changePercent;
                document.querySelector(".change-amount").textContent = changeAmount;
        
                // 거래량 및 거래대금 업데이트
                const volume = parseFloat(priceData.acc_trade_volume_24h).toFixed(3) + " BTC";
                const tradeAmount = parseFloat(priceData.acc_trade_price_24h).toLocaleString() + " KRW";
                document.querySelector(".volume").textContent = volume;
                document.querySelector(".trade-amount").textContent = tradeAmount;
        
                // 가격 변동에 따른 색상 업데이트
                priceElement.style.color = priceData.signed_change_price > 0 ? "#28a745" : "#dc3545";
            } catch (error) {
                console.error("Failed to fetch Bitcoin price data:", error);
            }
        }
        
        async function fetchPrices() {
                    try {
                        
                        const prices = {
                            bitflyer: Math.floor(Math.random() * (137000000 - 135000000) + 135000000),
                            binance: Math.floor(Math.random() * (137000000 - 135000000) + 135000000),
                            coinbase: Math.floor(Math.random() * (137000000 - 135000000) + 135000000),
                        };
        
                        // 각 거래소의 가격 업데이트
                        document.getElementById("bitflyer-price").textContent = prices.bitflyer.toLocaleString();
                        document.getElementById("binance-price").textContent = prices.binance.toLocaleString();
                        document.getElementById("coinbase-price").textContent = prices.coinbase.toLocaleString();
                    } catch (error) {
                        console.error("가격 데이터를 가져오는 중 오류가 발생했습니다:", error);
                    }
                }
        
        // 1초마다 가격 업데이트
        setInterval(fetchRealTimePrice, 1000);
        fetchRealTimePrice();
        
        // 5초마다 업데이트
        setInterval(fetchHeaderData, 5000);
        fetchHeaderData();
        
        // Initialize
        updateChart();
        updateOrderBook();
        
        // 가격 데이터를 5초마다 업데이트
        setInterval(fetchPrices, 1000);
        
        // 초기 데이터 로드
        fetchPrices();
        
        const API_BASE_URL = "https://api.upbit.com/v1";

        async function fetchCryptoPrices() {
            try {
                const marketResponse = await fetch(`${API_BASE_URL}/market/all`);
                const marketData = await marketResponse.json();

                const krwMarkets = marketData
                    .filter(market => market.market.startsWith("KRW-") && market.market !== "KRW-BTC")
                    .slice(0, 33); // 상위 35개 가져오기

                const marketNames = krwMarkets.map(market => market.market);

                const tickerResponse = await fetch(`${API_BASE_URL}/ticker?markets=${marketNames.join(",")}`);
                const tickerData = await tickerResponse.json();

                const tableBody = document.getElementById("crypto-table");
                tableBody.innerHTML = "";

                tickerData.forEach(coin => {
                    const row = document.createElement("tr");
                    row.className = "crypto-table-row";

                    const nameCell = document.createElement("td");
                    nameCell.textContent = krwMarkets.find(market => market.market === coin.market).korean_name;
                    nameCell.className = "crypto-table-row-cell";
                    row.appendChild(nameCell);

                    const priceCell = document.createElement("td");
                    priceCell.textContent = `${coin.trade_price.toLocaleString()} KRW`;
                    priceCell.className = "crypto-table-row-cell";
                    row.appendChild(priceCell);

                    const changeCell = document.createElement("td");
                    const changeRate = (coin.signed_change_rate * 100).toFixed(2);
                    changeCell.textContent = `${changeRate}%`;
                    changeCell.className = `crypto-table-row-cell ${changeRate > 0 ? "crypto-positive" : "crypto-negative"}`;
                    row.appendChild(changeCell);

                    const volumeCell = document.createElement("td");
                    volumeCell.textContent = `${coin.acc_trade_volume_24h.toFixed(2)}`;
                    volumeCell.className = "crypto-table-row-cell";
                    row.appendChild(volumeCell);

                    tableBody.appendChild(row);
                });
            } catch (error) {
                console.error("Error fetching crypto prices:", error);
            }
        }

        fetchCryptoPrices();
        setInterval(fetchCryptoPrices, 1000);