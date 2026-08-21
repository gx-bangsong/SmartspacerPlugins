# Smartspacer 插件合集

![Smartspacer Logo](https://i.imgur.com/CfHF7Dkl.png)

这是一个为 [Smartspacer](https://github.com/KieronQuinn/Smartspacer) 开发的非官方插件合集，旨在通过提供实用的生活提醒功能，增强您的 Smartspacer 使用体验。所有插件均可通过 Smartspacer 应用内的插件仓库进行安装，也可以在本项目的 [Releases](https://github.com/KieronQuinn/SmartspacerPlugins/releases) 页面手动下载。

---

# Smartspacer Plugins

This repository contains an unofficial collection of plugins developed for [Smartspacer](https://github.com/KieronQuinn/Smartspacer), designed to enhance your Smartspacer experience by providing useful lifestyle reminders. All plugins are available for installation through the Plugin Repository within the Smartspacer app, or you can manually download them from the [Releases](https://github.com/KieronQuinn/SmartspacerPlugins/releases) page.

## 目录 | Table of Contents

- [出行建议提取 (Travel Suggestions)](#出行建议提取--travel-suggestions)
- [考勤打卡提醒 (Check-In Reminder)](#考勤打卡提醒--check-in-reminder)
- [快递取件提醒 (Parcel Tracker)](#快递取件提醒--parcel-tracker)
- [饮水提醒 (Water Reminder)](#饮水提醒--water-reminder)
- [用药提醒 (Medication Reminder)](#用药提醒--medication-reminder)
- [食物保质期提醒 (Food Shelf Life Reminder)](#食物保质期提醒--food-shelf-life-reminder)
- [和风天气生活指数 (QWeather Indices)](#和风天气生活指数--qweather-indices)
- [Live Updates 支持与兼容性 (Live Updates Support)](#live-updates-支持与兼容性-live-updates-support)

---

## 出行建议提取 | Travel Suggestions

“出行建议提取”插件可以自动解析您的短信，或通过手动粘贴，自动识别火车、高铁和飞机票的出行信息，并优雅地展示在 Smartspace 卡片上，避免您因找不到票务信息而手忙脚乱。

### 主要功能

- **智能短信解析**：内置高度精准的正则提取模块（基于 `shared-sms-parser` 独立解析库），完美适配 12306 订票、各航空公司出票短信。
- **手动粘贴解析**：提供可视化“方案 B”核对弹窗，粘贴短信后一键解析，在表单中核对并编辑车次、出发地、目的地、时间和座位号等字段后安全入库。
- **点击一键跳转**：点击行程卡片可直接复制行程详情到剪贴板，并可自定义联动拉起 12306、航旅纵横或系统地图等软件。
- **精确出发通知**：使用 `AlarmManager` 机制，在出发前 30 分钟弹出高优先级系统通知，提醒您做好出行准备；进入出发窗口后，通知会自动升级为置顶的 **Live Update**（含系统倒计时、出发站/到达站与座位信息），并提供“查看行程”与“标记已出行”操作。
- **系统分享解析**：从微信、浏览器、备忘录等任何应用分享车票/航班文本 → 在 Sharesheet 中选择 **“解析出行信息”** → 应用内完成解析 → 弹出可编辑核对表单 → 确认后保存并安排出发提醒。
- **优雅 Dismiss**：在 Smartspace 上点击该target，将自动把该行程标记为已出行。

---

The **Travel Suggestions** plugin automatically parses ticket notifications from your SMS or via manual paste, extracting trains, high-speed rail, and flight details to display directly on your Smartspace.

### 使用步骤：从其他应用分享文本 → 解析出行信息 → 核对并保存

1. 在任意应用中（微信、浏览器、备忘录、短信等）选中包含车票/航班信息的文本，点击“分享”；
2. 在系统 Sharesheet 中选择 **“解析出行信息”**（本插件）；
3. 应用立即显示“正在解析出行信息”的 Live Update 通知与解析进度；
4. 解析成功后，同一张通知原地更新为行程摘要，并弹出可编辑的核对表单（车次/航班、出发站、到达站、时间、座位均可修改）；
5. 点击 **“核对并保存”** 后，行程才写入数据库、刷新 Smartspace 卡片并安排出发前提醒；或点击 **“取消”** 放弃（不写入任何数据）。

> 隐私说明：分享的文本只用于本地解析，不会写入日志或上传；解析过程中的临时草稿只保存核对所需的最少字段（不含乘客姓名与原始短信全文），30 分钟后自动清理。

---

## 考勤打卡提醒 | Check-In Reminder

“考勤打卡”插件是一个用于每日打卡状态跟踪、智能提醒并快速拉起办公软件的多合一考勤小助手。

### 主要功能

- **智能打卡逻辑 (方案 A)**：Smartspace 桌面卡片动态流转状态：“今日未打卡” $\rightarrow$ 点击记录上班时间，显示“上班已打卡 09:15” $\rightarrow$ 再次点击记录下班时间，并支持多次点击实时更新下班打卡。
- **打卡联动 App**：配置您日常打卡使用的应用（企业微信、钉钉、飞书、飞连），点击 Smartspace 卡片后，在本地 Room 记录时间的同时，自动帮您拉起该考勤应用。
- **双重闹铃提醒**：支持自定义上班与下班时间提醒。如果在指定打卡时间前半小时仍未打卡，将触发系统通知（内容支持自定义配置，默认为“上班时间请记得打卡”）。
- **打卡记录轨迹**：在设置页以列表形式直观展示您的所有打卡历史。支持手动补卡与随时删除记录，管理更轻松。

---

The **Check-In Reminder** plugin acts as your personal attendance assistant, managing clock-in/out states, scheduling dual-track reminder alarms, and automatically launching corporate tools like DingTalk, WeCom, Feishu, or Feilian.

---

## 快递取件提醒 | Parcel Tracker

“快递取件提醒”插件通过扫描您的短信，自动识别并提取快递取件码信息，将其直接展示在 Smartspace 上，让您无需翻找短信即可轻松取件。

### 主要功能

- **全自动识别**：实时监控新收到的快递短信，或扫描历史短信，自动提取取件码。
- **智能规则引擎**：内置针对菜鸟、丰巢等主流快递服务的识别规则，并支持通过自定义 JSON 文件扩展识别逻辑。
- **关键信息展示**：在 Smartspace 上直接展示取件码（如 3-4-1024）和快递站名称，一目了然。
- **自动过期清理**：支持设定自动清理时长（默认 24 小时），确保已取出的快递不会长期占据显示空间。
- **自定义规则**：对于特殊的快递短信格式，您可以通过 [取件码识别规则文档](DOCS_SMS_RULES.md) 自定义匹配逻辑。

---

The **Parcel Tracker** plugin scans your SMS messages to automatically identify and extract pickup codes, displaying them directly on your Smartspace for quick and easy access.

> **实验性选项（默认关闭）**：设置页提供“取件码实时更新（Promoted Live Update）”。官方明确说明普通包裹跟踪不适合 promoted 通知，因此该选项默认关闭；开启后取件码通知会以 Live Update 置顶显示（需要 Android 16 QPR1/36.1+，且系统允许该应用的 promoted 通知），提供“已取件”与“停止实时显示”操作。用户移除/取消置顶后不会被自动重新发布。

### Features

- **Automated Recognition**: Monitors incoming SMS messages in real-time or scans your inbox to extract pickup codes automatically.
- **Intelligent Rule Engine**: Pre-configured with rules for major courier services (e.g., Cainiao, Hive Box) and supports extensions via custom JSON files.
- **Essential Information**: Displays the pickup code (e.g., 3-4-1024) and the station name directly on Smartspace.
- **Automatic Expiry**: Configurable cleanup duration (defaulting to 24 hours) ensures that old pickup codes don't clutter your view.
- **Customizable Rules**: For unique SMS formats, you can define your own matching logic by following the [SMS Rules Documentation](DOCS_SMS_RULES.md).

---

## 饮水提醒 | Water Reminder

“饮水提醒”插件是一个帮助您养成良好饮水习惯的智能工具。它会在您设定的时间段内，根据您的每日目标和水杯容量，智能地提醒您按时饮水，并跟踪您的饮水进度。

### 主要功能

- **个性化饮水目标**：您可以设定从 500ml 到 5000ml 的每日饮水总量。
- **自定义水杯容量**：根据您常用的水杯，设定 100ml 到 1000ml 的单次饮水量。
- **智能提醒周期**：设定每日开始和结束提醒的时间，插件将只在活跃时段内打扰您。
- **多种显示模式**：根据您的喜好，选择不同的 Smartspace 显示样式。
- **进度自动重置**：可选择在每日提醒开始时自动清零饮水记录。
- **智能调整**：启用后，插件会根据您的饮水进度动态调整下一次提醒的时间。
- **暂停提醒**：需要暂时中断？可以轻松将下一次提醒推迟 5 到 60 分钟。

---

The **Water Reminder** plugin is a smart tool to help you build and maintain healthy hydration habits. It intelligently reminds you to drink water at regular intervals based on your daily goal and cup size, all within a time window you define, while also tracking your progress.

### Features

- **Personalized Daily Goal**: Set your daily water intake goal, from 500ml to 5000ml.
- **Custom Cup Size**: Define the volume of your typical cup, from 100ml to 1000ml, for accurate tracking.
- **Smart Reminder Schedule**: Specify start and end times for reminders, ensuring the plugin is only active when you are.
- **Multiple Display Modes**: Choose from various display styles to customize how the reminder appears on Smartspace.
- **Automatic Progress Reset**: Optionally configure your daily progress to reset automatically at the start of your reminder schedule.
- **Smart Adjustments**: When enabled, the plugin dynamically adjusts the next reminder time based on your hydration progress.
- **Snooze Functionality**: Need a break? Easily snooze the next reminder for 5 to 60 minutes.

---

## 用药提醒 | Medication Reminder

“用药提醒”插件是一个简单而可靠的工具，旨在确保您不会忘记在正确的时间服用药物。您可以轻松添加多种药物，并为每种药物设置灵活的提醒时间表。

### 主要功能

- **药物管理**：轻松添加和管理您的所有药物。
- **剂量说明**：为每种药物记录具体的剂量信息（例如，“饭后一粒”）。
- **灵活的用药周期**：设定药物的开始日期和可选的结束日期，支持长期服药和短期疗程。
- **精确的提醒时间**：您可以为每种药物添加多个每日的精确服药时间点（例如，08:00, 14:00, 20:00）。
- **自动计算下一次剂量**：插件会自动计算并显示下一次需要服药的时间，让您一目了然。

---

The **Medication Reminder** plugin is a simple and reliable tool designed to ensure you never forget to take your medication at the right time. You can easily add multiple medications and set up flexible reminder schedules for each one.

### Features

- **Medication Management**: Easily add and manage all your medications.
- **Dosage Information**: Record specific dosage instructions for each medication (e.g., "One pill after meals").
- **Flexible Scheduling**: Set a start date and an optional end date for each medication, supporting both long-term and short-term treatments.
- **Precise Reminder Times**: Add multiple specific times of day for each medication, ensuring accurate reminders (e.g., 08:00, 14:00, 20:00).
- **Automatic Next Dose Calculation**: The plugin automatically calculates and displays the time for your next dose, keeping you informed at a glance.

---

## 食物保质期提醒 | Food Shelf Life Reminder

“食物保质期提醒”插件帮助您跟踪家中食物的有效期，有效减少浪费。只需简单几步，即可为您储藏的食物设置到期提醒。

### 主要功能

- **物品追踪**：添加您购买的食物或其他有时效性物品的名称。
- **储存方式记录**：记录每件物品的储存方法（例如，“冷藏”、“避光干燥”）。
- **灵活的保质期设定**：以天为单位，轻松设置物品的保质期。
- **快捷填充**：为常见保质期（如 3 个月、12 个月）提供一键填充功能，简化输入过程。
- **自动到期日计算**：插件会根据您输入的保质期，自动计算并记录准确的到期日期。

---

The **Food Shelf Life Reminder** plugin helps you keep track of the expiration dates of your food items at home, effectively reducing waste. Set up expiry reminders for your stored goods in just a few simple steps.

### Features

- **Item Tracking**: Add the names of food items or other perishable goods you've purchased.
- **Storage Method Logging**: Record the storage method for each item (e.g., "Refrigerated," "Store in a cool, dry place").
- **Flexible Shelf Life**: Easily set the shelf life for your items in days.
- **Quick-Fill Buttons**: Use one-tap buttons for common shelf lives (e.g., 3 months, 12 months) to speed up data entry.
- **Automatic Expiry Calculation**: The plugin automatically calculates and records the exact expiration date based on the shelf life you provide.

---

## 和风天气生活指数 | QWeather Indices

“和风天气生活指数”插件能将您关心的各种生活指数清晰地展示在您的 Smartspace 上。它利用和风天气 API，提供精准、丰富的生活建议，帮助您更好地规划每一天。

**请注意：** 使用本插件前，您需要先拥有一个和风天气的 API 密钥。

### 主要功能

- **丰富的指数选择**：您可以根据自己的需求，多选关心和风天气生活指数，如运动、洗车、穿衣、钓鱼、紫外线等。
- **智能摘要显示**：插件会将您选择的众多指数智能地分类、汇总成两条简洁明了的摘要信息：“活动建议”（如“宜：洗车 | 不宜：运动”）和“状态摘要”（如“穿衣：炎热”），有效避免了信息过长被截断的问题。
- **自定义配置**：
    - **API 密钥**：填入您自己的和风天气 API 密钥。
    - **API Host**：和风天气API要求。
    - **城市名称**：输入您希望查询的城市。

---

The **QWeather Indices** plugin brings a variety of lifestyle indices to your Smartspace, keeping you well-informed. Powered by the QWeather API, it provides accurate and rich daily-life advice to help you better plan your day.

**Please Note:** You need a QWeather API Key to use this plugin.

### Features

- **Rich Index Selection**: Choose from a wide range of QWeather's lifestyle indices that matter to you, such as sports, car washing, dressing, fishing, UV index, and more.
- **Smart Summary Display**: The plugin intelligently categorizes and consolidates the selected indices into two concise summary lines: "Activity Suggestions" (e.g., "Suitable for: Car Wash | Unsuitable for: Sports") and "Status Summary" (e.g., "Dressing: Hot"). This effectively prevents text from being truncated.
- **Custom Configuration**:
    - **API Key**: Enter your personal QWeather API key.
    - **API Host**: API provider's commend.
      - *Warning*: If you set a custom host (e.g. `api.qweather.com` for subscription keys), the plugin will also route GeoAPI city lookup to this domain. Since QWeather's GeoAPI is only hosted on `geoapi.qweather.com`, the lookup city request will fail with a 404 error. Only configure this if your custom proxy also handles and routes GeoAPI endpoints correctly, otherwise leave it empty.
    - **City Name**: Specify the city for which you want to retrieve weather data.

---

## Live Updates 支持与兼容性 | Live Updates Support

本合集对部分插件启用了 Android 官方的 **promoted ongoing notification（Live Updates）** 支持。Live Update 是系统层面的置顶持续通知（官方文档：[Create live update notifications](https://developer.android.com/develop/ui/views/notifications/live-update)、[Progress-centric notifications](https://developer.android.com/about/versions/16/features/progress-centric-notifications)），不是 LiveData / 热更新，也不替代 Smartspacer Target 刷新。

实现方式：通过 **androidx.core 1.17.0** 的兼容 API（`NotificationCompat.Builder#setRequestPromotedOngoing`、`NotificationCompat.ProgressStyle`、`BuildCompat.isAtLeastB_1()`）在 compileSdk 36 下编译；运行时仅在 `BuildCompat.isAtLeastB_1()`（Android 16 QPR1 / 36.1+）为真时才真正请求提升，其余设备自动降级。

### 支持版本

| Android 版本 | 行为 |
|---|---|
| Android 10–15（API 29–35） | 全部降级为普通通知；功能不受影响 |
| Android 16 基础版（API 36.0） | 36.0 不包含 opt-in 的 promoted API，插件经 `BuildCompat.isAtLeastB_1()` 检测后自动降级为普通 ongoing 通知（符合官方行为；官方 Live Updates 需要 36.1/QPR） |
| Android 16 36.1/QPR 及更新 | 满足条件时按 Live Update 置顶显示；以 `NotificationManager.canPostPromotedNotifications()` 与 `Notification.hasPromotableCharacteristics()` 运行时检测为准 |

### 场景 × 是否符合官方 Live Update 条件

| 场景 | 用户主动发起 | ongoing | 时间敏感 | 有明确结束 | 是否 Live Update |
|---|---|---|---|---|---|
| 出行：分享文本“解析出行信息”（PARSING/REVIEW） | ✅ | ✅ | ✅ | ✅（确认/取消/失败） | ✅ promoted Live Update（进度样式 + 原地更新） |
| 出行：已保存行程进入出发窗口（T-30） | ✅（购票时已确认） | ✅ | ✅ | ✅（出发/标记已出行/删除） | ✅ promoted Live Update（系统倒计时） |
| 出行：短信自动解析结果通知 | ❌（后台短信非用户主动） | — | — | — | ❌ 普通高优先级通知（进入窗口后升级为 Live Update） |
| 快递取件码 | ❌ | ❌ | ❌ | — | ❌ 默认普通通知；设置页实验性开关（默认关闭）可开启 |
| 考勤 / 饮水 / 用药 / 食物提醒 | 定时静态提醒 | ❌ | ❌ | — | ❌ 普通通知（带结束操作） |

### 平台使用限制

- Live Update 需要 `android.permission.POST_PROMOTED_NOTIFICATIONS`（非运行时权限，随安装授予）、Android 16 QPR1（36.1）及以上系统，并由用户开启该应用的 promoted 通知；被用户关闭、OEM 未实现或系统不支持时自动降级为普通通知。
- 同一业务实体始终使用同一 notification ID 原地更新，并设置 `setOnlyAlertOnce(true)`，不会重复响铃或堆叠卡片。
- 用户移除（dismiss/unpin）后，通知不会被后续的轮询/重启/重新调度自动重新发布。
- 锁屏显示使用 `VISIBILITY_PRIVATE` + 脱敏 public version，不展示乘客姓名、证件号或完整短信原文。
