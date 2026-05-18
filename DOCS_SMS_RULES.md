# 快递取件码识别规则文档 | SMS Parsing Rules Documentation

Parcel Tracker 插件使用基于 JSON 的规则引擎来解析短信。您可以根据需要自定义这些规则，以支持更多快递服务。

The Parcel Tracker plugin uses a JSON-based rule engine to parse SMS messages. You can customize these rules to support additional courier services.

## 规则文件结构 | Rule File Structure

规则文件是一个包含规则数组的 JSON 对象：

The rules file is a JSON object containing an array of rules:

```json
{
  "rules": [
    {
      "provider": "服务商名称",
      "priority": 10,
      "match_keywords": ["关键字1", "关键字2"],
      "rules": {
        "pickup_code": "取件码正则表达式",
        "location": "位置正则表达式"
      }
    }
  ]
}
```

### 字段说明 | Field Descriptions

- **provider**: 快递服务商的名称（用于后台管理）。
  - The name of the courier service provider.
- **priority**: 优先级。当多条规则匹配时，优先级高的规则（数值大）将先被执行。
  - Priority level. When multiple rules match, the one with the higher value is executed first.
- **match_keywords**: 只有当短信包含这些**所有**关键字时，该规则才会被尝试匹配。
  - The rule will only be attempted if the SMS contains **all** of these keywords.
- **rules**: 具体的正则表达式定义。
  - Specific regex definitions.
    - **pickup_code**: 用于提取取件码。必须包含一个捕获组 `(...)` 来指定取件码内容。
      - Regex for extracting the pickup code. Must include a capture group `(...)` for the code itself.
    - **location** (可选): 用于提取取件位置或驿站名称。
      - (Optional) Regex for extracting the station name or location.

## 示例 | Examples

### 菜鸟驿站示例 | Cainiao Example

```json
{
  "provider": "菜鸟驿站",
  "priority": 10,
  "match_keywords": ["菜鸟驿站", "取件码"],
  "rules": {
    "pickup_code": "(?:取件码|取件码为)\\s*[:：]?\\s*([A-Z0-9-]+)",
    "location": "地址[:：]\\s*([^，。！\\s]+)"
  }
}
```

**匹配短信：** 【菜鸟驿站】您的快递已到，取件码为：A-1234，地址：天猫小店门口。
**提取结果：** 取件码: `A-1234`, 位置: `天猫小店门口`

---

### 丰巢示例 | Hive Box Example

```json
{
  "provider": "丰巢",
  "priority": 10,
  "match_keywords": ["丰巢", "取件码"],
  "rules": {
    "pickup_code": "(?:取件码|取件码为)\\s*[:：]?\\s*([0-9]{6,8})",
    "location": "位于\\s*([^，。！\\s]+)"
  }
}
```

**匹配短信：** 【丰巢】凭取件码 123456 到位于 1 号楼下的丰巢快递柜取件。
**提取结果：** 取件码: `123456`, 位置: `1 号楼下的丰巢快递柜`

## 注意事项 | Important Notes

1. **转义字符**: 在 JSON 字符串中编写正则表达式时，反斜杠 `\ ` 需要双重转义，即使用 `\\ `。
   - **Backslashes**: When writing regex in JSON strings, backslashes must be double-escaped (e.g., `\\s` instead of `\s`).
2. **捕获组**: `pickup_code` 必须包含且仅包含一个有效捕获组。如果您需要使用非捕获组，请使用 `(?:...)`。
   - **Capture Groups**: `pickup_code` must contain exactly one effective capture group. Use non-capturing groups `(?:...)` where needed.
