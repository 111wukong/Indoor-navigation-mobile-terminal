# 室内导航 — 移动终端

室内导航系统的 Android 客户端。拍摄图片或视频，发送至识别服务器进行场景识别，并在设备上显示导航方向指示（前进/左转/右转等）。

## 功能特性

- **图片识别** — 选择或拍摄照片，获取导航方向
- **视频识别** — 逐帧处理视频文件，实时更新方向
- **实时相机** — 摄像头实时画面，持续场景识别
- **方向叠加** — 导航箭头直接绘制在图片上

## 系统架构

```
┌─────────────────────────────────────────────────────┐
│              Android App (Java)                      │
│                                                      │
│  MainActivity  ──→  ChosePicture  ──→  HTTPAPI      │
│       │               ChoseVideo   ──→    POST       │
│       │               ShiBie (相机)    发送图片数据   │
│       └──────────────────────────────────────┘       │
│                         │                            │
│                         ▼                            │
│              识别服务器 (REST API)                    │
└─────────────────────────────────────────────────────┘
```

## 项目结构

```
app/
├── build.gradle
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/myapplication/
│   │   ├── MainActivity.java        # 主入口，图片/视频处理
│   │   ├── ChosePicture.java        # 图片选择
│   │   ├── ChoseVideo.java          # 视频选择
│   │   ├── ShiBie.java              # 实时相机识别
│   │   ├── Login.java               # 用户登录
│   │   ├── Feedback.java            # 意见反馈
│   │   ├── Menu.java                # 菜单辅助
│   │   └── HTTPAPI.java             # HTTP 请求工具
│   ├── res/
│   │   ├── layout/                  # 界面布局 (XML)
│   │   ├── drawable/                # 图片和图标
│   │   ├── values/                  # 字符串、颜色、主题
│   │   └── xml/                     # 安全配置等
│   └── AndroidManifest.xml
build.gradle                          # 根构建配置
settings.gradle                       # 项目设置
gradle.properties                     # Gradle 属性
gradlew / gradlew.bat                 # Gradle 包装器
```

## 系统要求

- Android SDK 24+
- 目标 SDK 31
- 需要运行识别服务器（默认地址：`http://172.20.3.9:8086/`）
- 实时模式需要相机权限

## 构建与运行

### Android Studio

1. 用 Android Studio 打开此项目
2. 等待 Gradle 同步完成
3. 连接设备或启动模拟器，运行

### 命令行

```bash
# 构建调试 APK
./gradlew assembleDebug

# 安装到已连接的设备
./gradlew installDebug
```

## 配置

识别服务器地址在 `MainActivity.java` 中配置：

```java
String baseUrl = "http://172.20.3.9:8086/";
```

构建前请根据实际部署情况修改此地址。

## 依赖库

| 库 | 用途 |
|----|------|
| OpenCV 4.5.3 | 图像处理，相机帧捕捉 |
| JavaCV 1.4.2 | 视频帧提取 |
| FFmpeg 4.0.1 | 视频编码支持 |
| AndroidX | 现代 Android 支持库 |

## 识别服务器

本应用通过 REST API 向识别服务器发送图片数据：

- **接口：** `POST {baseUrl}/rest/api/navigation`
- **请求体：** `image={base64编码的图片}`
- **响应：** JSON，包含 `data.resultdata.direct` 字段（如 `"forward"`、`"turn_left"`）

识别服务器源码：[Indoor-navigation-algorithm](https://github.com/111wukong/Indoor-navigation-algorithm)

## 许可证

MIT
