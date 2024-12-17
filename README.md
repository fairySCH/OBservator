# **OBservator 🚀**

Welcome to **OBservator**!  
This service uses a machine learning model to predict cryptocurrency price fluctuations and automatically execute trades.  
Using the **LSTM Neural Network**, the system analyzes market data and provides real-time actionable trading insights.

👉 [**_Access the Service_**](https://observator.co.kr/login) _(Temporary open until December 24, 2024)_

---

## 📜 **Key Features**

### **End-to-End Workflow**

- **Data Collection**: Automatically collects Orderbook and Ticks data through the Upbit API and preprocesses it.
- **Feature Extraction**: Extracts key features such as price spread, mid-price, order book imbalance, price volatility, and buy/sell ratios.
- **Model Training**: Trains the LSTM model with hyperparameter tuning and early stopping for optimization.
- **Automated Trading**: Executes **BUY/SELL** orders in real-time based on model predictions and user-defined thresholds.

### **User-Customized Features**

- **Sign-Up & Login**: Users can sign up and log in through the website and register their Upbit API key for system integration.
- **Real-Time Dashboard**: The dashboard provides cryptocurrency data, including current prices, order books, trade history, and related news.
- **Threshold Settings**: Users can adjust thresholds on the dashboard to fine-tune their automated trading strategies.
- **Auto-Trading Control**: Users can easily start/stop auto-trading with a simple button click.
- **24-Hour Model Updates**: The system retrains the model every 24 hours with new data and updates to the most performant model for improved accuracy.

### **Modular & Scalable**

- Each component is designed for independent execution.
- Parameters like trading conditions, data inputs, and model configurations are dynamically adjustable.

### **Real-Time Execution**

- Real-time data streaming via continuous socket connections.
- Supports immediate threshold adjustments for effective trading strategies.

### **Logging & Debugging**

- Detailed logging across all scripts for easy traceability and debugging.
- Stores test losses and performance metrics for further analysis.

---

## **Implementation Results**

### **1. Login Screen**

<img src="Doc/imgs/login_1.png" height=300> <img src="Doc/imgs/login_2.png" height=300>  

---

### **2. Sign-Up Screen & Terms Popup**

<img src="Doc/imgs/signup_1.png" height=300> <img src="Doc/imgs/signup_2.png" height=300>  

---

### **3. Home Screen**

<img src="Doc/imgs/home.png" height=300>  

---

### **4. Profile Screen & Upbit API Guide Popup**

<img src="Doc/imgs/profile_1.png" height=300> <img src="Doc/imgs/profile_2.png" height=300> <img src="Doc/imgs/profile_3.png" height=300>  

---

### **5. Dashboard Screen**

<img src="Doc/imgs/dashboard_1.png" height=300> <img src="Doc/imgs/dashboard_2.png" height=300>  

---

## **Development Environment**

### **Operating System**

![windows](https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white) ![mac](https://img.shields.io/badge/mac%20os-000000?style=for-the-badge&logo=apple&logoColor=white) ![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white)

### **Code Editor**

![vsCode](https://img.shields.io/badge/Visual_Studio_Code-0078D4?style=for-the-badge&logo=visual%20studio%20code&logoColor=white)

### **Collaboration Tools**

![notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white) ![google Cloud](https://img.shields.io/badge/Google_Cloud-4285F4?style=for-the-badge&logo=google-cloud&logoColor=white) ![github](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white) ![slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white) ![naver mybox](https://img.shields.io/badge/Naver%20Mybox-6079F6?style=for-the-badge&logo=naver&logoColor=white)

---

## **Programming Languages & Frameworks**

### **Frontend**

![html5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white) ![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E) ![css](https://img.shields.io/badge/CSS-239120?&style=for-the-badge&logo=css3&logoColor=white)

### **Backend**

![java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)  
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)  
![Amazon S3](https://img.shields.io/badge/Amazon%20S3-FF9900?style=for-the-badge&logo=amazons3&logoColor=white) ![Amazon RDS](https://img.shields.io/badge/Amazon%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white)

### **Machine Learning**

![python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white) ![PyTorch](https://img.shields.io/badge/PyTorch-%23EE4C2C.svg?style=for-the-badge&logo=PyTorch&logoColor=white)  
![Anaconda](https://img.shields.io/badge/Anaconda-%2344A833.svg?style=for-the-badge&logo=anaconda&logoColor=white)  

---

## **System Architecture**

![SystemDiagram](./Doc/Diagrams/시스템구성도.png)

The **OBservator** system architecture comprises the **Client**, **AWS Cloud**, **WSL**, and **Upbit Exchange** components.

---

## **Expected Effects**

<img src="Doc/imgs/기대효과.png" height=300>

1. **Lower Barrier to Entry**: Simplifies cryptocurrency trading for beginners by automating complex decisions.
2. **Reduced Loss Rates**: Automation removes emotional biases, enabling consistent and rational trading strategies.
3. **24/7 Trading**: Automatic execution ensures no missed opportunities in a volatile market.
4. **Scalability**: Designed to integrate various cryptocurrencies and exchanges.

---

## **Project Deliverables**

| **Category** | **Deliverables** |
| :----------: | :--------------: |
| **Reports**  | [Project Plan](/Doc/Reports/수행계획서_S7_행복해조.pdf) 🔹 [Final Report](/Doc/Reports/최종보고서_S7_행복해조.pdf) |
| **Slides**   | [Proposal Slides](/Doc/Presentation/제안발표_S7_행복해조.pdf) 🔹 [Midterm Slides](/Doc/Presentation/중간발표_S7_행복해조.pdf) 🔹 [Final Slides](/Doc/Presentation/최종발표_S7_행복해조.pdf) |
| **Diagrams** | [Use Case](/Doc/useCaseDiagramExplain.md) 🔹 [Sequence](/Doc/sequenceDiagramExplain.md) 🔹 [System Block](/Doc/systemBlockDiagramExplain.md) 🔹 [Activity](/Doc/activityDiagramExplain.md) |
| **Other**    | [Patent Document](/Doc/특허명세서_S7_행복해조_최종.pdf) 🔹 [Meeting Minutes](/Doc/meeting_minutes.md) |

---

## 🤝 **Team & Members**

> **Team Name**: S7_HappyJo  

| **Name**      | **Role**                         |
| :-----------: | :------------------------------: |
| **Daehan Jin**| 👨🏻‍🏫 Mentor 👨🏻‍🏫                  |
| **Minjae Kim**| 👑 Machine Learning & Leader 👑  |
| **Boseong Lee**| 📈 Frontend 📈                   |
| **Iljun Kwon**| 🖥️ Backend 🖥️                   |
| **Seungho Lee**| 🌐 Cloud Server & FE/BE Support 🌐 |

---

## **License**

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
