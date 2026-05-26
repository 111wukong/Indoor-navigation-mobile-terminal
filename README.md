# 室内导航 — 移动终端

室内导航系统的 Android 客户端应用。通过拍摄图片或视频发送至识别服务器进行场景识别，在设备上显示导航方向指示（前进 / 左转 / 右转等）。

配合 [Indoor-navigation-algorithm](https://github.com/111wukong/Indoor-navigation-algorithm) 服务端使用。

## 功能

- 📷 拍照识别 — 拍摄室内场景图片，上传至服务器获取场景分类
- 🎥 实时视频 — 实时视频流识别
- 🧭 方向指示 — 根据识别结果显示导航方向
- 🔌 与识别服务器联动

## 快速开始

```bash
git clone https://github.com/111wukong/Indoor-navigation-mobile-terminal.git
cd Indoor-navigation-mobile-terminal
# 用 Android Studio 打开项目
# 修改 app 中的服务器地址
# 构建并安装到设备
```

## 环境要求

- Android Studio
- Android SDK 21+
- [室内导航算法服务端](https://github.com/111wukong/Indoor-navigation-algorithm) 已运行

## 项目结构

```
├── app/              # Android 应用模块
│   ├── src/
│   │   ├── main/     # 主代码
│   │   └── test/     # 测试
│   └── build.gradle
├── gradle/           # Gradle 配置
├── build.gradle
└── settings.gradle
```

## License

MIT
