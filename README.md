# BTC Signal Pro - منصة التحليل والبيانات اللحظية لزوج BTC/USDT

تطبيق أندرويد متكامل وموجّه لنظام **Android 15 (API Level 35)** مع دعم أدنى **Min SDK 26**، مبني بلغة **Kotlin** مع واجهات **Jetpack Compose (Material Design 3)**. تم تصميم التطبيق لرصد، تحليل، وتوليد إشارات فنية احتمالية فورية فائقة الدقة لزوج العملات **BTC/USDT** عبر منصات **Binance** و **MEXC**.

---

## الفهرس (Table of Contents)

1. [المميزات الرئيسية (Key Features)](#المميزات-الرئيسية-key-features)
2. [هيكل المجلدات والمشروع (Project Structure)](#هيكل-المجلدات-والمشروع-project-structure)
3. [المعادلات الرياضية ومحرك التحليل (Mathematical Engine)](#المعادلات-الرياضية-ومحرك-التحليل-mathematical-engine)
4. [هندسة النظام وخط تدفق البيانات (Architecture & Data Pipeline)](#هندسة-النظام-وخط-تدفق-البيانات-architecture--data-pipeline)
5. [شرح الملفات المصدرية الرئيسية (Source Code Walkthrough)](#شرح-الملفات-المصدرية-الرئيسية-source-code-walkthrough)
6. [خط أنابيب البناء المستمر (CI/CD Pipeline)](#خط-أنابيب-البناء-المستمر-cicd-pipeline)
7. [طريقة التثبيت والتشغيل (Build & Run Instructions)](#طريقة-التثبيت-والتشغيل-build--run-instructions)

---

## المميزات الرئيسية (Key Features)

- **تدفق بيانات لحظي عالي التردد (High-Frequency Real-Time WebSocket):**
  - ربط مباشر ومزدوج مع خوادم WebSocket العامة لمنصتي **Binance Spot** و **MEXC Spot**.
  - بث فوري لعمق سجل الأوامر (Order Book Depth 20) بتردد 100 ملي ثانية.
  - شريط الصفقات المجمعة الفورية (Aggregate Trades Tape) مع تمييز صفقات الشراء والبيع العنيفة (Aggressive Taker Flow).
  - تحديثات التيكر اللحظي (24h Ticker) مع حركة الأسعار اللحظية.

- **أنابيب البيانات متعددة الأطر الزمنية (Multi-Timeframe OHLCV Pipeline):**
  - جلب شموع التداول التاريخية عبر واجهات برمجة التطبيقات REST endpoints لـ:
    - إطار **الدقيقة (1m)** للمضاربة السريعة (Scalping).
    - إطار **5 دقائق (5m)** لصفقات السوينغ السريعة (Day Trading).
    - إطار **30 دقيقة (30m)** لتحديد الاتجاه الهيكلي (Trend Direction).
  - تشكيل الشمعة اللحظية وتحديثها المستمر مع كل صفقة أو تكة سعر جديدة وإعادة حساب المؤشرات تلقائياً.

- **محرك المؤشرات الفنية الرياضي (Quantitative Technical Engine):**
  - **مؤشر القوة النسبية (RSI - 14 Periods):** حساب نقي باستخدام طريقة وايلدر (Wilder's Smoothing) لتحديد مناطق ذروة الشراء والبيع والزخم.
  - **مؤشر الماكد (MACD 12, 26, 9):** خط MACD، خط الإشارة (Signal Line)، والهيستوجرام مع كشف التقاطعات الصاعدة والهابطة.
  - **المتوسطات المتحركة الأسية (EMA 9, EMA 21, EMA 50):** حساب شريط المتوسطات لتحديد نسق الاتجاه (Bullish/Bearish Stack Alignment).
  - **نطاقات بولينجر (Bollinger Bands 20, 2.0σ):** الخط العلوي، المتوسط البسيط SMA20، الخط السفلي، ونسبة عرض النطاق (Bandwidth %) ومؤشر الموقع (%B).
  - **متوسط المدى الحقيقي (ATR - 14 Periods):** لحساب تقلبات السوق اللحظية وضبط مستويات الخطر.

- **مؤشر الاحتمالية الخوارزمي وإدارة المخاطر (Probabilistic Signal & Risk Matrix):**
  - مؤشر ترجيح احتمالي (0% إلى 100%) للاتجاه الصاعد (Bullish) مقابل الهابط (Bearish).
  - تصنيف الإشارات: `STRONG BUY`, `BUY`, `NEUTRAL`, `SELL`, `STRONG SELL`.
  - مصفوفة إدارة المخاطر الآلية:
    - حساب نقاط الدعم الديناميكي (Dynamic Support) والمقاومة الديناميكية (Dynamic Resistance).
    - نقطة الدخول المقترحة (Entry Price).
    - وقف الخسارة الديناميكي (Stop-Loss - SL) بناءً على ATR ومستويات الدعم/المقاومة.
    - أهداف جني الأرباح متعددة المستويات: TP1 (نسبة 1:1.5)، TP2 (نسبة 1:2.5)، TP3 (نسبة 1:4.0).

- **واجهة مستخدم احترافية بالوضع الداكن (Dark-Mode Crypto Terminal UI):**
  - وميض التكة اللحظي (Millisecond Tick Flash Animation): وميض أخضر نيون عند ارتفاع السعر وأحمر مرجاني عند انخفاضه مع انتقال ناعم.
  - رسم بياني للشموع اليابانية (Candlestick Canvas Chart) مع خطوط المتوسطات المتحركة ونطاقات بولينجر وتدقيق الشموع باللمس والسحب.
  - سلم عمق الأوامر (Order Book Depth Ladder) وحساب الفارق السعري (Spread) ونسبة أحجام الشراء للبيع.
  - نافذة حوارية تشرح أسباب الإشارة وتوافق الأطر الزمنية ونسبة الثقة.

---

## هيكل المجلدات والمشروع (Project Structure)

```text
├── .github/
│   └── workflows/
│       └── build-apk.yml                 # خط أنابيب الأتمتة للبناء والإصدار
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   │   ├── engine/
│   │   │   │   │   │   └── IndicatorEngine.kt       # محرك الحسابات الرياضية للمؤشرات والإشارات
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── MarketModels.kt          # نماذج بيانات السوق، الشموع، وسجل الأوامر
│   │   │   │   │   │   └── SignalModels.kt          # نماذج الإشارات، درجات الاحتمال، والمخاطر
│   │   │   │   │   ├── network/
│   │   │   │   │   │   ├── CryptoRestClient.kt      # عميل REST لجلب الشموع التاريخية
│   │   │   │   │   │   └── CryptoWebSocketClient.kt # عميل WebSocket اللحظي عالي التردد
│   │   │   │   │   └── repository/
│   │   │   │   │       └── CryptoRepository.kt      # المستودع المركزي لربط البيانات والمؤشرات
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── CandleStickChart.kt      # رسم الشموع اليابانية عبر Compose Canvas
│   │   │   │   │   │   ├── IndicatorCards.kt        # بطاقات ومقاييس RSI، MACD، EMA، وBollinger
│   │   │   │   │   │   ├── OrderBookView.kt         # سلم عمق سجل الأوامر وشريط الصفقات اللحظية
│   │   │   │   │   │   ├── RiskMatrixCard.kt        # مصفوفة إدارة المخاطر والدعم/المقاومة
│   │   │   │   │   │   └── SignalBreakdownDialog.kt # نافذة شرح تفاصيل عوامل الإشارة الفنية
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt                 # لوحة الألوان الخاصة بمنصات التداول
│   │   │   │   │   │   ├── Theme.kt                 # ثيم Material Design 3 الداكن
│   │   │   │   │   │   └── Type.kt                  # إعدادات الخطوط
│   │   │   │   │   ├── CryptoDashboardScreen.kt     # الشاشة الرئيسية لمنصة التداول
│   │   │   │   │   └── CryptoViewModel.kt           # إدارة حالة التطبيق ووميض التكة
│   │   │   │   └── MainActivity.kt                  # نقطة الدخول للتطبيق
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_launcher_background.xml   # خلفية أيقونة التطبيق التكيفية
│   │   │   │   │   └── ic_launcher_foreground.xml   # الشعار والشكل التكيفي لأيقونة التطبيق
│   │   │   │   ├── values/
│   │   │   │   │   └── strings.xml                  # الموارد النصية
│   │   │   │   └── AndroidManifest.xml              # تصاريح الإنترنت وإعدادات التطبيق
│   │   └── test/java/com/example/
│   │       ├── ExampleRobolectricTest.kt            # اختبار بيئة التشغيل
│   │       └── IndicatorEngineTest.kt               # اختبارات دقة الحسابات الرياضية للمؤشرات
│   └── build.gradle.kts                             # إعدادات بناء التطبيق والتبعيات
├── gradle/
│   └── libs.versions.toml                           # كتالوج إصدارات الحزم والتبعيات
├── build.gradle.kts                                 # إعدادات Gradle الجذرية
├── metadata.json                                    # بيانات التطبيق على منصة AI Studio
└── settings.gradle.kts                              # تعريف المشروع والوحدات
```

---

## المعادلات الرياضية ومحرك التحليل (Mathematical Engine)

تمت برمجة جميع المؤشرات الفنية في ملف `IndicatorEngine.kt` بشكل مستقل ودون الاعتماد على مكتبات خارجية، لضمان أعلى أداء ممكن:

### 1. مؤشر القوة النسبية (RSI - 14 Periods)
تستخدم خوارزمية ويليس وايلدر (Wilder's Smoothing):
$$\Delta P = P_t - P_{t-1}$$
$$\text{Gain} = \max(\Delta P, 0), \quad \text{Loss} = \max(-\Delta P, 0)$$
لأول 14 فترة:
$$\text{AvgGain}_0 = \frac{\sum \text{Gains}}{14}, \quad \text{AvgLoss}_0 = \frac{\sum \text{Losses}}{14}$$
للفترات اللاحقة:
$$\text{AvgGain}_t = \frac{(\text{AvgGain}_{t-1} \times 13) + \text{Gain}_t}{14}$$
$$\text{AvgLoss}_t = \frac{(\text{AvgLoss}_{t-1} \times 13) + \text{Loss}_t}{14}$$
$$\text{RS} = \frac{\text{AvgGain}_t}{\text{AvgLoss}_t}, \quad \text{RSI} = 100 - \left( \frac{100}{1 + \text{RS}} \right)$$

### 2. المتوسطات المتحركة الأسية (EMA)
معامل الترجيح الزمني $k$:
$$k = \frac{2}{N + 1}$$
$$\text{EMA}_t = (P_t \times k) + (\text{EMA}_{t-1} \times (1 - k))$$
حيث يتم حساب:
- $\text{EMA}_9$: الاتجاه فائق السرعة اللحظي.
- $\text{EMA}_{21}$: خط الاتجاه المتوسط الأساسي.
- $\text{EMA}_{50}$: خط الاتجاه العام الهيكلي.

### 3. مؤشر الماكد (MACD 12, 26, 9)
$$\text{MACD Line} = \text{EMA}_{12}(\text{Close}) - \text{EMA}_{26}(\text{Close})$$
$$\text{Signal Line} = \text{EMA}_9(\text{MACD Line})$$
$$\text{Histogram} = \text{MACD Line} - \text{Signal Line}$$

### 4. نطاقات بولينجر (Bollinger Bands 20, 2.0σ)
$$\mu = \frac{1}{20} \sum_{i=0}^{19} P_{t-i} \quad (\text{Middle Band / SMA20})$$
$$\sigma = \sqrt{\frac{1}{20} \sum_{i=0}^{19} (P_{t-i} - \mu)^2}$$
$$\text{Upper Band} = \mu + (2.0 \times \sigma)$$
$$\text{Lower Band} = \mu - (2.0 \times \sigma)$$
$$\text{Bandwidth \%} = \frac{\text{Upper} - \text{Lower}}{\mu} \times 100$$
$$\%B = \frac{P_t - \text{Lower}}{\text{Upper} - \text{Lower}}$$

### 5. متوسط المدى الحقيقي وإدارة المخاطر (ATR & Risk Matrix)
$$\text{TR} = \max\left( \text{High}_t - \text{Low}_t, \, |\text{High}_t - \text{Close}_{t-1}|, \, |\text{Low}_t - \text{Close}_{t-1}| \right)$$
$$\text{ATR}_{14} = \text{Wilder}(\text{TR}, 14)$$

**في الصفقات الصاعدة (Bullish Setup):**
- $\text{Stop-Loss (SL)} = \max(P_t - 1.5 \times \text{ATR}, \, \text{Dynamic Support} \times 0.998)$
- $\text{Risk Distance} = P_t - \text{SL}$
- $\text{Take-Profit 1 (TP1)} = P_t + (1.5 \times \text{Risk Distance})$
- $\text{Take-Profit 2 (TP2)} = P_t + (2.5 \times \text{Risk Distance})$
- $\text{Take-Profit 3 (TP3)} = P_t + (4.0 \times \text{Risk Distance})$

---

## هندسة النظام وخط تدفق البيانات (Architecture & Data Pipeline)

يعتمد التطبيق نمط **MVVM (Model-View-ViewModel)** المعزز بـ Kotlin Coroutines و StateFlows:

1. **التهيئة والتحميل التاريخي (Initial REST Bootstrapping):**
   - عند بدء التطبيق، يستدعي `CryptoRepository` واجهة REST لـ Binance لجلب آخر 100 شمعة لكل من أطر 1m و 5m و 30m.
   - في حال تعذر الاتصال بأحد الخوادم، يتم التحويل التلقائي لخوادم MEXC البديلة.
2. **الاتصال اللحظي المستمر (WebSocket Real-Time Feed):**
   - يتم فتح اتصال فوري ومستمر عبر OkHttp WebSocket مع:
     - Binance: `wss://stream.binance.com:9443/stream?streams=btcusdt@ticker/btcusdt@depth20@100ms/btcusdt@aggTrade`
     - أو MEXC: `wss://wbs.mexc.com/ws`
   - يتم استقبال التحديثات وتوزيعها عبر SharedFlow إلى التيكر، سجل الأوامر، والصفقات المنفذة.
3. **تحديث الشموع وحساب المؤشرات التلقائي (Reactive Candle Update):**
   - مع وصول كل صفقة، يتم تحديث الشمعة الحالية الجارية (Open, High, Low, Close, Volume).
   - عند انتهاء وقت الشمعة، يتم إغلاقها وبدء شمعة جديدة في الذاكرة.
   - يعاد احتساب مؤشرات RSI، MACD، EMAs، Bollinger Bands، والاحتمال الإجمالي فورياً وإرسال الحالة المحدثة إلى واجهة المستخدم.

---

## شرح الملفات المصدرية الرئيسية (Source Code Walkthrough)

### 1. `IndicatorEngine.kt`
الملف المسؤول عن جميع العمليات الحسابية:
- دالة `calculateIndicators(candles)`: تحسب جميع المؤشرات الفنية.
- دالة `calculateRSI(prices, period)`: تحسب القوة النسبية بدقة.
- دالة `calculateMACD(prices, ...)`: تولد خطوط الماكد والهيستوجرام.
- دالة `calculateEMASeries(prices, period)`: تولد سلسلة المتوسطات الأسية.
- دالة `calculateBollingerBands(prices, ...)`: تحسب النطاقات ومؤشرات الضغط (Squeeze).
- دالة `calculateRiskManagement(...)`: تنتج أهداف الدخول والخروج ونسب المخاطرة إلى العائد.
- دالة `evaluateSignal(...)`: تزن إشارات الاتجاه، الزخم، التقاطعات، والتذبذب لإنتاج درجة الاحتمالية من 0 إلى 100%.

### 2. `CryptoWebSocketClient.kt`
الملف المسؤول عن تدفق بيانات الويب سوكيت:
- يتعامل مع بروتوكولات Binance و MEXC.
- يرسل إشارات النبض (Ping/Pong Heartbeats) كل 15-20 ثانية للحفاظ على استقرار الاتصال.
- يدعم إعادة الاتصال التلقائي (Exponential Backoff Auto-Reconnect).
- يفصل البيانات الواردة إلى:
  - `tickerFlow`: تحديثات السعر والتغير اليومي.
  - `orderBookFlow`: تحديثات عمق العرض والطلب.
  - `tradeFlow`: تدفق الصفقات اللحظية المنفذة.

### 3. `CandleStickChart.kt`
مكون الرسم البياني عالي الأداء المبني باستخدام Jetpack Compose Canvas:
- يرسم الشموع اليابانية الصاعدة والخامدة مع الفتائل العلوية والسفلية.
- يرسم منحنيات EMA 9 (أزرق سماوي)، EMA 21 (أصفر)، و EMA 50 (بنفسجي).
- يرسم خط السعر الحالي المتحرك في الزمن الحقيقي.
- يدعم التفاعل اللمسي (Touch Scrubbing) لقراءة تفاصيل أي شمعة سابقة.

### 4. `CryptoDashboardScreen.kt` & `CryptoViewModel.kt`
شاشة التداول الرئيسية:
- تدير وميض التكة اللحظي (Tick Flash Animation) باستخدام `animateColorAsState` لتبديل الألوان إلى الأخضر أو الأحمر اللحظي لمدة 350 ملي ثانية ثم التلاشي الناعم.
- توفر مقاييس واضحة لاحتمالية الصعود والهبوط ومصفوفة إدارة المخاطر.
- تتيح التبديل الفوري بين الأطر الزمنية 1m و 5m و 30m بضغطة زر واحدة.

---

## خط أنابيب البناء المستمر (CI/CD Pipeline)

الملف `.github/workflows/build-apk.yml` مجهز ومعد بالكامل للأتمتة عند الدفع إلى GitHub:

```yaml
name: Build & Release Android APK

on:
  push:
    branches: [ "main", "master" ]
    tags:
      - "v*"
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build:
    name: Build Android APK
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3

      - name: Accept Android Licenses
        run: yes | sdkmanager --licenses || true

      - name: Grant Execute Permission for Gradle Wrapper
        run: chmod +x gradlew || true

      - name: Build Debug & Release APK
        run: |
          ./gradlew assembleDebug --stacktrace
          ./gradlew assembleRelease --stacktrace || ./gradlew assembleDebug

      - name: Locate Compiled APK
        id: find_apk
        run: |
          APK_PATH=$(find app/build/outputs/apk -name "*.apk" | head -n 1)
          echo "Found APK at: $APK_PATH"
          echo "apk_path=$APK_PATH" >> $GITHUB_OUTPUT

      - name: Upload APK as Workflow Artifact
        uses: actions/upload-artifact@v4
        with:
          name: btc-signal-pro-apk
          path: ${{ steps.find_apk.outputs.apk_path }}
          retention-days: 30

      - name: Create GitHub Release and Upload APK
        if: startsWith(github.ref, 'refs/tags/v') || github.event_name == 'workflow_dispatch'
        uses: softprops/action-gh-release@v2
        with:
          files: ${{ steps.find_apk.outputs.apk_path }}
          name: BTC Signal Pro Release ${{ github.ref_name }}
          body: |
            ## BTC Signal Pro - Android Release
            
            - Real-time BTC/USDT spot ticker via Binance and MEXC public WebSockets.
            - Order Book Depth (live bids/asks volume ladder).
            - Real-time Aggregate Trades tape with buyer/seller pressure metrics.
            - Multi-timeframe OHLCV data pipeline (1m, 5m, 30m).
            - Quantitative indicator engine: RSI(14), MACD(12,26,9), EMA(9, 21, 50), Bollinger Bands(20, 2).
            - Probabilistic sentiment score & automated Risk Matrix.
          draft: false
          prerelease: false
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## طريقة التثبيت والتشغيل (Build & Run Instructions)

### المتطلبات الأساسية:
- **Android Studio Ladybug (2024.2+)** أو أحدث.
- **Java Development Kit (JDK 17)**.
- **Android SDK 35 (Android 15)**.

### أوامر البناء عبر الطرفية (Gradle CLI):

1. **تشغيل الاختبارات البرمجية:**
   ```bash
   gradle :app:testDebugUnitTest
   ```

2. **بناء حزمة التطبيق التجريبية (Debug APK):**
   ```bash
   gradle assembleDebug
   ```
   *موقع الملف الناتج:* `app/build/outputs/apk/debug/app-debug.apk`

3. **بناء حزمة التطبيق النهائية (Release APK):**
   ```bash
   gradle assembleRelease
   ```
   *موقع الملف الناتج:* `app/build/outputs/apk/release/app-release.apk`

---

## الترخيص (License)
هذا المشروع مفتوح المصدر ومطور وفق أعلى معايير هندسة البرمجيات لتطبيقات التداول والتحليل المالي اللحظي على نظام أندرويد.
