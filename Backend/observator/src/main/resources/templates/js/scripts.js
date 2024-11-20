// Fetch data from Upbit API
async function fetchBitcoinData() {
    const response = await fetch("https://api.upbit.com/v1/candles/minutes/1?market=KRW-BTC&count=30"); // 1분 단위 데이터
    const data = await response.json();
    return data.map(candle => ({
        time: new Date(candle.timestamp).toLocaleTimeString(), // 시간만 표시
        price: candle.trade_price
    }));
}

// Line chart instance
let lineChart;

// Render or update the line chart
async function updateLineChart() {
    const data = await fetchBitcoinData();

    // If chart instance exists, update it
    if (lineChart) {
        lineChart.data.labels = data.map(item => item.time);
        lineChart.data.datasets[0].data = data.map(item => item.price);
        lineChart.update(); // Update chart
    } else {
        // Create chart for the first time
        const ctx = document.getElementById('btcChart').getContext('2d');
        lineChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.map(item => item.time),
                datasets: [{
                    label: 'Bitcoin Price (KRW)',
                    data: data.map(item => item.price),
                    borderColor: 'rgba(54, 162, 235, 1)',
                    backgroundColor: 'rgba(54, 162, 235, 0.2)',
                    borderWidth: 2,
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        title: {
                            display: true,
                            text: 'Time'
                        }
                    },
                    y: {
                        title: {
                            display: true,
                            text: 'Price (KRW)'
                        },
                        beginAtZero: false
                    }
                }
            }
        });
    }
}

// Call updateLineChart every 5 seconds
setInterval(updateLineChart, 5000);

// Initialize chart on page load
updateLineChart();
