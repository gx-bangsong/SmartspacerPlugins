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

---

## 出行建议提取 | Travel Suggestions

“出行建议提取”插件可以自动解析您的短信，或通过手动粘贴，自动识别火车、高铁和飞机票的出行信息，并优雅地展示在 Smartspace 卡片上，避免您因找不到票务信息而手忙脚乱。

### 主要功能

- **智能短信解析**：内置高度精准的正则提取模块（基于 `shared-sms-parser` 独立解析库），完美适配 12306 订票、各航空公司出票短信。
- **手动粘贴解析**：提供可视化“方案 B”核对弹窗，粘贴短信后一键解析，在表单中核对并编辑车次、出发地、目的地、时间和座位号等字段后安全入库。
- **点击一键跳转**：点击行程卡片可直接复制行程详情到剪贴板，并可自定义联动拉起 12306、航旅纵横或系统地图等软件。
- **精确出发通知**：使用 `AlarmManager` 机制，在出发前 30 分钟弹出高优先级系统通知，提醒您做好出行准备。
- **优雅 Dismiss**：在 Smartspace 上侧滑清除行程卡，将自动把该行程标记为已出行。

---

The **Travel Suggestions** plugin automatically parses ticket notifications from your SMS or via manual paste, extracting trains, high-speed rail, and flight details to display directly on your Smartspace.

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

## 饮水提醒 | Water Reminder

“饮水提醒”插件是一个帮助您养成良好饮水习惯的智能工具。它会在您设定的时间段内，根据您的每日目标 and 水杯容量，智能地提醒您按时饮水，并跟踪您的饮水进度。

---

## 用药提醒 | Medication Reminder

“用药提醒”插件是一个简单而可靠的工具，旨在确保您不会忘记在正确的时间服用药物。您可以轻松添加多种药物，并为每种药物设置灵活的提醒时间表。

---

## 食物保质期提醒 | Food Shelf Life Reminder

“食物保质期提醒”插件帮助您跟踪家中食物的有效期，有效减少浪费。只需简单几步，即可为您储藏的食物设置到期提醒。

---

## 和风天气生活指数 | QWeather Indices

“和风天气生活指数”插件能将您关心的各种生活指数清晰地展示在您的 Smartspace 上。它利用和风天气 API，提供精准、丰富的生活建议，帮助您更好地规划每一天。

**请注意：** 使用本插件前，您需要先拥有一个和风天气的 API 密钥。

### 主要功能

- **丰富的指数选择**：您可以根据自己的需求，多选关心和风天气生活指数，如运动、洗车、穿衣、钓鱼、紫外线等。
- **智能摘要显示**：插件会将您选择的众多指数智能地分类、汇总成两条简洁明了的摘要信息：“活动建议”（如“宜：洗车 | 不宜：运动”）和“状态摘要”（如“穿衣：炎热”），有效避免了信息过长被截断的问题。
- **自定义配置**：
    - **API 密钥**：填入您自己的和风天气 API 密钥。
    - **API Host**：支持自定义数据 API 地址（如付费版、商业版密钥可填写 `api.qweather.com`）。注：城市查询服务 (GeoAPI) 会自动路由到官方专用的 `geoapi.qweather.com`，您无需担心因更改数据 API 主机而导致城市查询或天气检索失败。
    - **城市名称**：输入您希望查询的城市。
