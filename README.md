# AmpTrack ⚡

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF.svg?logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-757575.svg)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**AmpTrack** is a modern, privacy-first, 100% offline electrical utility application built for Android. Designed for electrical engineers, technicians, hobbyists, and students, AmpTrack simplifies current calculations ($I = P / V$) with high precision, flexible unit conversions, history tracking, and customizable display preferences.

---

## ✨ Features

- ⚡ **Accurate Current Calculations:** Instant calculation of electrical current using $I = \frac{P}{V}$ powered by high-precision `BigDecimal` arithmetic.
- 📐 **Flexible Units:**
  - **Power:** Watts ($W$), Kilowatts ($kW$)
  - **Voltage:** Millivolts ($mV$), Volts ($V$), Kilovolts ($kV$)
  - **Current Output:** Microamperes ($\mu A$), Milliamperes ($mA$), Amperes ($A$), Kiloamperes ($kA$)
- 🔄 **Smart Auto-Scaling & Formatting:** Automatically adapts current output units based on magnitude or restricts output strictly to Amperes.
- 🎯 **Quick-Value Chips:** One-tap presets for common power ($10W$, $100W$, $1000W$, $2000W$) and voltage ($5V$, $12V$, $24V$, $120V$, $230V$, $240V$) values.
- 📜 **Calculation History:** Automatically stores up to 50 previous calculations locally with full search, deletion, and quick-reload capabilities.
- 🔍 **Formula Transparency:** Displays step-by-step breakdown of how power and voltage inputs were converted and computed.
- ⚙️ **Customizable Settings:**
  - Decimal precision selection (1, 2, or 3 decimal places).
  - Default power and voltage unit preferences.
  - Haptic feedback toggles.
  - Dark mode, light mode, and system default themes.
- 📱 **Responsive Design:** Adaptive layout optimized for phone, landscape, and tablet screen sizes.
- 🔒 **100% Offline & Private:** Operates entirely offline with zero network permissions or external data collection.

---

## 🏗️ Architecture & Tech Stack

AmpTrack follows **Modern Android Architecture (MVVM)** principles with Kotlin and Jetpack Compose:

- **Language:** [Kotlin](https://kotlinlang.org/) (2.2+)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Navigation:** [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **State Management:** `StateFlow` and Unidirectional Data Flow (UDF)
- **Local Persistence:** [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Domain Logic:** Standalone `CalculatorEngine` using `BigDecimal` with strict input validation
- **Testing:** Unit testing with JUnit 4

---

## 🛠️ Getting Started

### Prerequisites

- **Android Studio:** Ladybug / 2024.2.1 or newer
- **JDK:** Java 11 or higher
- **Android SDK:** Minimum SDK 24 (Android 7.0), Target SDK 37

### Build & Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/abdullah-ateeq/AmpTrack.git
   cd AmpTrack
   ```

2. **Open in Android Studio:**
   Open Android Studio and select **Open** -> Navigate to the cloned `AmpTrack` directory.

3. **Build and Run:**
   - Run via Android Studio or run the Gradle build task:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 📄 Safety Disclaimer

AmpTrack is designed as an educational and reference calculation utility. Always consult official electrical safety codes, certified equipment specifications, and licensed professionals when working with high-voltage or high-current electrical systems.

---

## 📜 License

This project is open source and available under the [MIT License](LICENSE).
