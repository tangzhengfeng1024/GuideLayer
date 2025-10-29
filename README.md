# GuideLayer

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/language-Kotlin-blue.svg" alt="Language">
  <img src="https://img.shields.io/badge/license-MIT-orange.svg" alt="License">
  <img src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat" alt="API">
</p>

<p align="center">
  <strong>一个优雅、强大的 Android 用户引导遮罩层库</strong><br>
  支持多步骤引导、文本说明、自定义动画和灵活的UI配置
</p>

**中文** | [English](README_EN.md)

---

## ✨ 特性

- 🎯 **高亮显示**：支持矩形、圆形、椭圆形高亮形状
- 📝 **文本说明**：内置标题和描述文本支持，无需额外图片
- 🔢 **多步骤引导**：轻松创建连贯的多步骤用户引导流程
- 🎨 **灵活定位**：支持绝对定位、相对定位、预设位置等多种定位方式
- 🎬 **流畅动画**：内置淡入淡出动画，提升用户体验
- ⏭️ **导航控制**：上一步/下一步按钮，步骤指示器，跳过功能
- 🎨 **现代UI**：精美的按钮样式和卡片设计
- 🎨 **按钮自定义**：支持自定义按钮颜色、字体大小、圆角等样式
- 🔧 **高度可定制**：链式API，灵活配置每个步骤
- 🔙 **向后兼容**：完全保留单步骤API，升级无忧

## 📱 效果预览

<img src="https://github.com/tangzhengfeng1024/GuideLayer/raw/main/Screen_recording_20251029_204805-ezgif.com-video-to-gif-converter.gif" alt="功能截图" width="50%" height="50%">

## 🚀 快速开始

### 安装

将代码添加到你的项目中：

```kotlin
// 将 GuideOverlayView.kt 相关代码复制到你的项目中
```

### 基础用法

#### 单步骤引导

```kotlin
val targetView = findViewById<View>(R.id.targetView)

GuideLayer.with(this)
    .highlight(targetView, shape = HighlightShape.CIRCLE, paddingDp = 12f)
    .text("功能标题", "这是对该功能的详细说明")
    .confirmButton(show = true, text = "我知道了")
    .show()
```

#### 多步骤引导

```kotlin
GuideLayer.with(this)
    // 第一步
    .addStep()
        .highlight(view1)
        .text("第一步", "介绍第一个功能")
        .showNextButton(true)
        .showStepIndicator(true)
        .showSkipButton(true)
    // 第二步
    .nextStep()
        .highlight(view2)
        .text("第二步", "介绍第二个功能")
        .showPreviousButton(true)
        .showNextButton(true)
        .showStepIndicator(true)
        .showSkipButton(true)
    // 第三步
    .nextStep()
        .highlight(view3)
        .text("第三步", "介绍第三个功能")
        .showPreviousButton(true)
        .confirmButton(show = true, text = "完成")
        .showStepIndicator(true)
        // 自定义按钮样式
        .primaryButtonStyle(ButtonStyle(
            backgroundColor = Color.parseColor("#4CAF50"),
            textColor = Color.WHITE,
            cornerRadiusDp = 20f
        ))
        .secondaryButtonStyle(ButtonStyle(
            backgroundColor = Color.TRANSPARENT,
            textColor = Color.parseColor("#666666"),
            strokeColor = Color.parseColor("#CCCCCC"),
            strokeWidthDp = 1.5f
        ))
    // 完成配置
    .endSteps()
    .onStepChanged { current, total ->
        Log.d("Guide", "步骤: $current/$total")
    }
    .onSkipAll {
        Log.d("Guide", "用户跳过了引导")
    }
    .show(delayMs = 500)
```

## 📖 详细文档

### 高亮形状

```kotlin
enum class HighlightShape {
    RECT,    // 矩形（圆角）
    CIRCLE,  // 圆形
    OVAL     // 椭圆形
}
```

### 位置类型

```kotlin
// 绝对像素位置
Position(PositionType.ABSOLUTE, x = 100f, y = 200f)

// 相对屏幕百分比（0.0-1.0）
Position(PositionType.RELATIVE, x = 0.5f, y = 0.5f)

// 相对于高亮目标的偏移
Position(PositionType.RELATIVE_TO_TARGET, x = 0f, y = 100f)

// 预设位置
Position(PositionType.PRESET, preset = PresetPosition.BOTTOM_CENTER)
```

### 预设位置

```kotlin
enum class PresetPosition {
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    CENTER_LEFT, CENTER, CENTER_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
}
```

### 按钮样式

```kotlin
data class ButtonStyle(
    val backgroundColor: Int? = null,           // 背景颜色
    val textColor: Int? = null,                 // 文字颜色
    val textSizeSp: Float? = null,              // 文字大小（sp）
    val cornerRadiusDp: Float? = null,          // 圆角半径（dp）
    val strokeColor: Int? = null,               // 边框颜色
    val strokeWidthDp: Float? = null            // 边框宽度（dp）
)
```

**使用示例：**

```kotlin
// 自定义主要按钮样式（确认、下一步）
.primaryButtonStyle(ButtonStyle(
    backgroundColor = Color.parseColor("#2196F3"),
    textColor = Color.WHITE,
    cornerRadiusDp = 12f
))

// 自定义次要按钮样式（上一步）
.secondaryButtonStyle(ButtonStyle(
    backgroundColor = Color.TRANSPARENT,
    textColor = Color.parseColor("#666666"),
    strokeColor = Color.parseColor("#CCCCCC"),
    strokeWidthDp = 1.5f
))

// 自定义跳过按钮样式
.skipButtonStyle(ButtonStyle(
    backgroundColor = Color.parseColor("#80000000"),
    textColor = Color.parseColor("#CCCCCC"),
    cornerRadiusDp = 20f
))

// 自定义确认按钮样式（单步骤模式）
.confirmButtonStyle(ButtonStyle(
    backgroundColor = Color.parseColor("#FF6B35"),
    textColor = Color.WHITE,
    textSizeSp = 18f,
    cornerRadiusDp = 25f
))
```

### API 方法

#### GuideLayer

| 方法 | 说明 |
|-----|------|
| `with(activity)` | 创建 GuideLayer 实例 |
| `highlight(view, shape, padding, cornerRadius)` | 高亮指定视图 |
| `overlayImage(resId, position)` | 添加覆盖图片（如箭头） |
| `addStep()` | 开始添加多步骤引导 |
| `dismissOnTouchAnywhere(enabled)` | 是否允许点击任意位置关闭 |
| `confirmButton(show, text)` | 配置确认按钮 |
| `confirmButtonPosition(position)` | 设置确认按钮位置 |
| `confirmButtonStyle(style)` | 设置确认按钮样式 |
| `onStepChanged(callback)` | 设置步骤改变回调 |
| `onSkipAll(callback)` | 设置跳过所有步骤回调 |
| `startFromStep(index)` | 设置起始步骤索引 |
| `show(delayMs)` | 显示引导 |

#### StepBuilder（多步骤）

| 方法 | 说明 |
|-----|------|
| `highlight(view, ...)` | 高亮指定视图 |
| `text(title, description)` | 设置文本内容（标题+描述） |
| `title(title)` | 仅设置标题 |
| `description(description)` | 仅设置描述 |
| `textPosition(position)` | 设置文本位置 |
| `overlayImage(resId, position)` | 添加覆盖图片 |
| `dismissOnTouchAnywhere(enabled)` | 是否允许点击任意位置关闭 |
| `confirmButton(show, text)` | 配置确认按钮 |
| `confirmButtonPosition(position)` | 设置确认按钮位置 |
| `showPreviousButton(show)` | 是否显示"上一步"按钮 |
| `showNextButton(show)` | 是否显示"下一步"按钮 |
| `showStepIndicator(show)` | 是否显示步骤指示器 |
| `showSkipButton(show)` | 是否显示"跳过"按钮 |
| `confirmButtonStyle(style)` | 设置确认按钮样式 |
| `primaryButtonStyle(style)` | 设置主要按钮样式（确认、下一步） |
| `secondaryButtonStyle(style)` | 设置次要按钮样式（上一步） |
| `skipButtonStyle(style)` | 设置跳过按钮样式 |
| `prevButtonText(text)` | 设置"上一步"按钮文字 |
| `nextButtonText(text)` | 设置"下一步"按钮文字 |
| `skipButtonText(text)` | 设置"跳过"按钮文字 |
| `callbacks(onShown, onDismissed)` | 设置回调 |
| `nextStep()` | 完成当前步骤，继续配置下一步 |
| `endSteps()` | 完成所有步骤配置 |

## 🎨 高级用法

### 自定义文本位置

```kotlin
.addStep()
    .highlight(view)
    .text("标题", "描述文本")
    .textPosition(Position(PositionType.RELATIVE_TO_TARGET, 0f, 150f))
```

### 使用覆盖图片（如箭头）

```kotlin
.addStep()
    .highlight(view)
    .overlayImage(R.drawable.arrow, Position(PositionType.RELATIVE_TO_TARGET, 50f, -100f))
```

### 步骤回调和监听

```kotlin
.addStep()
    .highlight(view)
    .callbacks(
        onShown = { 
            // 步骤显示时执行
            analytics.track("guide_step_shown")
        },
        onDismissed = { 
            // 步骤关闭时执行
            saveProgress()
        }
    )
.nextStep()
    // ... 更多步骤
.endSteps()
.onStepChanged { current, total ->
    // 全局步骤改变监听
    updateProgressBar(current, total)
}
.onSkipAll {
    // 用户点击跳过
    analytics.track("guide_skipped")
}
```

### 从指定步骤开始

```kotlin
val lastStep = preferences.getInt("last_guide_step", 0)

GuideLayer.with(this)
    .addStep()
        // 步骤 1
    .nextStep()
        // 步骤 2
    .nextStep()
        // 步骤 3
    .endSteps()
    .startFromStep(lastStep)  // 从保存的步骤继续
    .show()
```

## 🎯 使用场景

### 新手引导

为新用户提供应用功能介绍和操作指导。

```kotlin
GuideLayer.with(this)
    .addStep()
        .highlight(menuButton)
        .text("菜单", "点击这里打开应用菜单")
        .showNextButton(true)
    .nextStep()
        .highlight(searchBar)
        .text("搜索", "在这里搜索你想要的内容")
        .showNextButton(true)
    .endSteps()
    .show()
```

### 新功能介绍

向用户展示应用的新功能。

```kotlin
GuideLayer.with(this)
    .addStep()
        .highlight(newFeatureButton)
        .text("新功能！", "我们添加了一个强大的新功能")
        .confirmButton(show = true, text = "试试看")
    .endSteps()
    .show()
```

### 操作提示

在特定场景下提示用户如何操作。

```kotlin
GuideLayer.with(this)
    .highlight(submitButton)
    .text("提交表单", "填写完成后点击这里提交")
    .show()
```

## 💡 最佳实践

1. **禁用触摸关闭**：多步骤引导中建议设置 `.dismissOnTouchAnywhere(false)`
2. **使用步骤指示器**：让用户知道进度 `.showStepIndicator(true)`
3. **提供跳过选项**：尊重用户选择 `.showSkipButton(true)`
4. **添加延迟显示**：确保页面布局完成 `.show(delayMs = 500)`
5. **追踪用户行为**：使用回调记录引导完成情况
6. **保存进度**：允许用户从上次离开的地方继续

## 🔧 技术实现

- **自定义View**：通过 `FrameLayout` 实现遮罩层
- **PorterDuff模式**：使用 `CLEAR` 模式挖空高亮区域
- **链式API**：Builder 模式提供流畅的API
- **动画效果**：View 属性动画实现淡入淡出
- **灵活定位**：多种定位系统满足不同需求

## 📋 要求

- **最低 API 级别**: 21 (Android 5.0)
- **编程语言**: Kotlin
- **AndroidX**: 是

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 开发指南

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 Pull Request


## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。



## 📧 联系方式

如有问题或建议，欢迎：
- 发送邮件到：tangzhengfeng1024@gmail.com

---

<p align="center">
  如果这个项目对你有帮助，请给它一个 ⭐️ Star！
</p>

