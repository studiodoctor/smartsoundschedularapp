<div align="center">
  
# 📱 Smart Sound Scheduler

### Automatically manage your phone's sound mode based on your schedule

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Design-purple.svg)](https://m3.material.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

<img src="screenshots/app_banner.png" alt="Smart Sound Scheduler Banner" width="100%">

[Features](#-features) • [Screenshots](#-screenshots) • [Installation](#-installation) • [Tech Stack](#-tech-stack) • [Architecture](#-architecture) • [Contributing](#-contributing)

</div>

---

## 🎯 About

**Smart Sound Scheduler** is a powerful Android application that automatically switches your phone between Ring, Silent, and Vibrate modes at scheduled times. Perfect for work meetings, sleep schedules, classes, or any situation where you need your phone to be quiet automatically.

Never forget to silence your phone during meetings or unmute it after work again! Set it once and let the app handle the rest.

---

## ✨ Features

### Core Features
| Feature | Description |
|---------|-------------|
| 🔔 **Multiple Sound Modes** | Switch between Ring, Silent, and Vibrate modes |
| ⏰ **Unlimited Schedules** | Create as many time slots as you need |
| 📅 **Day Selection** | Choose specific days for each schedule |
| 🌙 **Overnight Support** | Schedules that cross midnight work perfectly |
| 🔄 **Auto-Restart** | Schedules persist after device reboot |

### Smart Features
| Feature | Description |
|---------|-------------|
| ⚠️ **Conflict Detection** | Smart algorithm prevents overlapping schedules |
| 💡 **Alternative Suggestions** | Get suggestions when conflicts are detected |
| 📊 **Quick Presets** | One-tap selection for Weekdays, Weekends, or Everyday |
| 🎨 **Color Coding** | Assign colors to easily identify schedules |

### User Experience
| Feature | Description |
|---------|-------------|
| 🎨 **Material 3 Design** | Beautiful, modern UI following latest design guidelines |
| 🌓 **Dark Mode** | Full dark mode support |
| ✨ **Smooth Animations** | Delightful micro-interactions throughout |
| 📱 **Dynamic Colors** | Adapts to your device's wallpaper colors (Android 12+) |

---

## 📸 Screenshots

<div align="center">
<table>
  <tr>
    <td><img src="screenshots/home_screen.png" alt="Home Screen" width="200"/></td>
    <td><img src="screenshots/add_schedule.png" alt="Add Schedule" width="200"/></td>
    <td><img src="screenshots/time_picker.png" alt="Time Picker" width="200"/></td>
    <td><img src="screenshots/settings.png" alt="Settings" width="200"/></td>
  </tr>
  <tr>
    <td align="center"><b>Home Screen</b></td>
    <td align="center"><b>Add Schedule</b></td>
    <td align="center"><b>Time Picker</b></td>
    <td align="center"><b>Settings</b></td>
  </tr>
</table>
</div>

<div align="center">
<table>
  <tr>
    <td><img src="screenshots/conflict_detection.png" alt="Conflict Detection" width="200"/></td>
    <td><img src="screenshots/day_selector.png" alt="Day Selector" width="200"/></td>
    <td><img src="screenshots/dark_mode.png" alt="Dark Mode" width="200"/></td>
    <td><img src="screenshots/empty_state.png" alt="Empty State" width="200"/></td>
  </tr>
  <tr>
    <td align="center"><b>Conflict Detection</b></td>
    <td align="center"><b>Day Selector</b></td>
    <td align="center"><b>Dark Mode</b></td>
    <td align="center"><b>Empty State</b></td>
  </tr>
</table>
</div>

---

## 🚀 Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 26+ (Android 8.0 Oreo)

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/studiodoctor/smartsoundschedularapp.git

# Navigate to project directory
cd smart-sound-scheduler

# Open in Android Studio and sync Gradle
# Or build via command line:
./gradlew assembleDebug
