import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { Line, Bar, Doughnut } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  BarElement,
  LineElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Title, Tooltip, Legend);

// Center text plugin for Doughnut chart
// Center text plugin for Doughnut chart
// Center text plugin for Doughnut chart
// Center text plugin for Doughnut chart
// Center text plugin for Doughnut chart only
ChartJS.register({
  id: 'centerText',
  beforeDraw: (chart) => {
    // 도넛 차트에서만 중앙 텍스트를 추가
    if (chart.config.type === 'doughnut') {
      const { ctx, data } = chart;
      const { width, height } = chart;
      ctx.save();
      const fontSize = (height / 150).toFixed(2); // 폰트 크기 조정 가능
      ctx.font = `${fontSize}em sans-serif`;
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';

      const winRateValue = parseFloat(data.datasets[0]?.data[0]) || 0;
      const text = `${winRateValue.toFixed(2)}%`;
      
      const textX = width / 2;
      const textY = height / 2;
      ctx.fillText(text, textX, textY);
      ctx.restore();
    }
  },
});



function LoginPage({ setIsLoggedIn, setUsername }) {
  const navigate = useNavigate();
  const [inputUsername, setInputUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = () => {
    if (inputUsername && password) {
      setUsername(inputUsername);
      setIsLoggedIn(true);
      navigate('/dashboard');
    } else {
      alert('Please enter both username and password');
    }
  };

  return (
    <div style={styles.loginContainer}>
      <h2>Login</h2>
      <input
        type="text"
        placeholder="Username"
        value={inputUsername}
        onChange={(e) => setInputUsername(e.target.value)}
        style={styles.input}
      />
      <input
        type="password"
        placeholder="Password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        style={styles.input}
      />
      <button onClick={handleLogin} style={styles.button}>Log In</button>
    </div>
  );
}

function BitcoinTracker({ isLoggedIn, username }) {
  const [priceData, setPriceData] = useState([]);
  const [predictedPriceData, setPredictedPriceData] = useState([]);
  const [timeData, setTimeData] = useState([]);
  const [volumeData, setVolumeData] = useState([]);
  const [tradeHistory, setTradeHistory] = useState([]);
  const [winRate, setWinRate] = useState(0);
  const [averageHoldingTime, setAverageHoldingTime] = useState(0);

  useEffect(() => {
    const fetchDummyData = () => {
      const dummyPrice = parseFloat((Math.random() * (60000 - 50000) + 50000).toFixed(2));
      const dummyPredictedPrice = parseFloat((dummyPrice * (1 + (Math.random() - 0.5) * 0.02)).toFixed(2));
      const dummyVolume = (Math.random() * 100).toFixed(2);
      const currentTime = new Date().toLocaleTimeString();

      setPriceData((prevPriceData) => [...prevPriceData, dummyPrice]);
      setPredictedPriceData((prevPredictedData) => [...prevPredictedData, dummyPredictedPrice]);
      setTimeData((prevTimeData) => [...prevTimeData, currentTime]);
      setVolumeData((prevVolumeData) => [...prevVolumeData, dummyVolume]);

      const isWin = Math.random() > 0.5;
      setTradeHistory((prevHistory) => [
        ...prevHistory,
        { time: currentTime, isWin, holdingTime: Math.floor(Math.random() * 60) },
      ]);

      const winCount = tradeHistory.filter((trade) => trade.isWin).length;
      setWinRate((winCount / tradeHistory.length) * 100);

      const totalHoldingTime = tradeHistory.reduce((acc, trade) => acc + trade.holdingTime, 0);
      setAverageHoldingTime(totalHoldingTime / tradeHistory.length || 0);
    };

    const interval = setInterval(fetchDummyData, 1000);
    return () => clearInterval(interval);
  }, [tradeHistory]);

  if (!isLoggedIn) {
    return <Navigate to="/" />;
  }

  const priceChartData = {
    labels: timeData,
    datasets: [
      {
        label: 'Bitcoin Price',
        data: priceData,
        fill: false,
        borderColor: 'blue',
        tension: 0.4,
        borderWidth: 1,
        pointRadius: 0,
      },
      {
        label: 'Predicted Bitcoin Price',
        data: predictedPriceData,
        fill: false,
        borderColor: 'darkorange', // 눈에 잘 띄는 색상
        borderWidth: 3, // 선 두께 증가
        pointRadius: 3, // 포인트 크기 증가
        borderDash: [5, 5], // 점선 스타일
        tension: 0.4,
      },
    ],
  };
  
  // 그림자 플러그인 추가
// 도넛 차트 전용 Center text plugin
ChartJS.register({
  id: 'centerText',
  beforeDraw: (chart) => {
    if (chart.config.type === 'doughnut') {
      const { ctx, data } = chart;
      const { width, height } = chart;
      ctx.save();
      const fontSize = (height / 150).toFixed(2);
      ctx.font = `${fontSize}em sans-serif`;
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';

      const winRateValue = parseFloat(data.datasets[0]?.data[0]) || 0;
      const text = `${winRateValue.toFixed(2)}%`;

      const textX = width / 2;
      const textY = height / 2;
      ctx.fillText(text, textX, textY);
      ctx.restore();
    }
  },
});

// 그림자 플러그인 (라인 차트, 바 차트 등에서만 사용)
ChartJS.register({
  id: 'shadowPlugin',
  beforeDatasetsDraw: (chart) => {
    if (chart.config.type !== 'doughnut') { // 도넛 차트 제외
      const { ctx, chartArea: { left, top, width, height } } = chart;
      ctx.save();
      ctx.shadowColor = 'rgba(0, 0, 0, 0.3)';
      ctx.shadowBlur = 10;
      ctx.shadowOffsetX = 5;
      ctx.shadowOffsetY = 5;
      ctx.clearRect(left, top, width, height);
    }
  },
  afterDatasetsDraw: (chart) => {
    if (chart.config.type !== 'doughnut') { // 도넛 차트 제외
      chart.ctx.restore();
    }
  },
});

  

  const volumeChartData = {
    labels: timeData,
    datasets: [
      {
        label: 'Bitcoin Volume',
        data: volumeData,
        backgroundColor: 'rgba(0, 123, 255, 0.5)',
        borderColor: 'blue',
        borderWidth: 1,
      },
    ],
  };

  const winRateData = {
    labels: ['Win', 'Loss'],
    datasets: [
      {
        data: [winRate, 100 - winRate],
        backgroundColor: ['#4caf50', '#f44336'],
      },
    ],
  };

  return (
    <div>
      <div style={styles.header}>
        <h1>{username}님 안녕하세요</h1>
      </div>

      <div style={styles.container}>
        <div style={styles.column}>
          <h2>Bitcoin Price vs Predicted Price</h2>
          <Line data={priceChartData} options={{ scales: { x: { ticks: { maxTicksLimit: 10 } } } }} />

          <h2>Bitcoin Volume</h2>
          <Bar data={volumeChartData} options={{ scales: { x: { ticks: { maxTicksLimit: 10 } } } }} />
        </div>

        <div style={styles.column}>
          <h2>Trade History (Dummy) - Latest 15</h2>
          <table border="1" cellPadding="10" cellSpacing="0">
            <thead>
              <tr>
                <th>Time</th>
                <th>Price (USD)</th>
                <th>Volume</th>
                <th>Action</th>
                <th>Result</th>
              </tr>
            </thead>
            <tbody>
              {tradeHistory.slice(-15).map((trade, index) => (
                <tr key={index}>
                  <td>{trade.time}</td>
                  <td>{priceData[index]}</td>
                  <td>{volumeData[index]}</td>
                  <td>{trade.isWin ? 'Buy' : 'Sell'}</td>
                  <td>{trade.isWin ? 'Win' : 'Lose'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div style={styles.column}>
          <h2>Trade Metrics</h2>
          <p>Win Rate: {winRate.toFixed(2)}%</p>
          <p>Average Holding Time: {averageHoldingTime.toFixed(2)} minutes</p>
          <h3>Win Rate</h3>
          <Doughnut data={winRateData} options={{ plugins: { centerText: true } }} />
        </div>
      </div>
    </div>
  );
}

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [username, setUsername] = useState('');

  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage setIsLoggedIn={setIsLoggedIn} setUsername={setUsername} />} />
        <Route path="/dashboard" element={<BitcoinTracker isLoggedIn={isLoggedIn} username={username} />} />
      </Routes>
    </Router>
  );
}

const styles = {
  loginContainer: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    height: '100vh',
    backgroundColor: '#f0f2f5',
  },
  input: {
    margin: '10px',
    padding: '12px',
    fontSize: '16px',
    width: '250px',
    borderRadius: '4px',
    border: '1px solid #ccc',
  },
  button: {
    padding: '10px 20px',
    fontSize: '16px',
    cursor: 'pointer',
    backgroundColor: '#4caf50',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '20px',
    borderBottom: '2px solid #ccc',
    backgroundColor: '#f0f2f5',
  },
  container: {
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'flex-start',
    flexWrap: 'wrap',
  },
  column: {
    flex: 1,
    minWidth: '300px',
    marginRight: '10px',
    marginBottom: '20px',
  },
};

export default App;


