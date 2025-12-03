<div align="center">

# 📱 Smart Sound Scheduler

### Automatically manage your phone's sound mode based on your schedule

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Design-purple.svg)](https://m3.material.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-FFDD00?style=flat&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/studiodoctor)

<img src="screenshots/1.jpg" alt="Smart Sound Scheduler Banner" width="400">

</div>

## 🎯 About

**Smart Sound Scheduler** is a powerful Android application that automatically switches your phone between Ring, Silent, and Vibrate modes at scheduled times. Perfect for work meetings, sleep schedules, classes, or any situation where you need your phone to be quiet automatically.

Never forget to silence your phone during meetings or unmute it after work again! Set it once and let the app handle the rest.

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

## 📸 Screenshots

| Screen 1 | Screen 2 | Screen 3 | Screen 4 | Screen 5 |
|----------|----------|----------|----------|----------|
| <img src="screenshots/1.jpg" width="150"/> | <img src="screenshots/2.jpg" width="150"/> | <img src="screenshots/3.jpg" width="150"/> | <img src="screenshots/4.jpg" width="150"/> | <img src="screenshots/5.jpg" width="150"/> |

## 🚀 Installation

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 26+ (Android 8.0 Oreo)

### Clone and Build

Clone the repository:

    git clone https://github.com/studiodoctor/smartsoundschedularapp.git

Navigate to project directory:

    cd smartsoundschedularapp

Build the project:

    ./gradlew assembleDebug

Or open in Android Studio and sync Gradle.

### Direct APK Download

Download the latest APK from the [Releases](https://github.com/studiodoctor/smartsoundschedularapp/releases) page.

## 🛠 Tech Stack

| Technology | Purpose |
|------------|---------|
| Kotlin 2.0 | Primary programming language |
| Jetpack Compose | Modern declarative UI toolkit |
| Material 3 | Design system and components |
| MVVM | Architecture pattern |
| Hilt | Dependency injection |
| Room | Local database |
| Kotlin Coroutines | Asynchronous programming |
| Kotlin Flow | Reactive data streams |
| Navigation Compose | In-app navigation |
| DataStore | Preferences storage |

## ⚙️ Permissions

| Permission | Purpose |
|------------|---------|
| ACCESS_NOTIFICATION_POLICY | Change Do Not Disturb settings |
| RECEIVE_BOOT_COMPLETED | Restart service after device boot |
| FOREGROUND_SERVICE | Run background scheduler |
| SCHEDULE_EXACT_ALARM | Schedule precise time triggers |
| MODIFY_AUDIO_SETTINGS | Change ringer mode |
| VIBRATE | Vibration feedback |
| POST_NOTIFICATIONS | Show service notifications |

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/AmazingFeature`
3. Commit changes: `git commit -m 'Add AmazingFeature'`
4. Push to branch: `git push origin feature/AmazingFeature`
5. Open a Pull Request

## 📋 Roadmap

- [x] Basic scheduling functionality
- [x] Conflict detection
- [x] Material 3 design
- [x] Dark mode support
- [x] Boot persistence
- [ ] Widget support
- [ ] Calendar integration
- [ ] Location-based triggers
- [ ] Backup & restore
- [ ] Multiple languages

## 📄 License

MIT License - Copyright (c) 2024 Studio Doctor

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software.

## 💖 Support

If you find this project helpful, please consider supporting its development!

<a href="https://www.buymeacoffee.com/studiodoctor" target="_blank">
<img src="https://cdn.buymeacoffee.com/buttons/v2/default-violet.png" alt="Buy Me A Coffee" height="60">
</a>

Your support helps me add new features, fix bugs, and stay caffeinated while coding!

### Other Ways to Support

- ⭐ Star this repository
- 🐛 Report bugs
- 💡 Suggest features
- 📢 Share with others
- 🍴 Contribute code

## 👨‍💻 Author

**Studio Doctor**

GitHub: [@studiodoctor](https://github.com/studiodoctor)

Support: [Buy Me a Coffee](https://buymeacoffee.com/studiodoctor)

---

<div align="center">

**Made with ❤️ and Kotlin**

If this project helped you, please give it a ⭐!

</div>
