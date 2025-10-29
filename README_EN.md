# GuideLayer

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/language-Kotlin-blue.svg" alt="Language">
  <img src="https://img.shields.io/badge/license-MIT-orange.svg" alt="License">
  <img src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat" alt="API">
</p>

<p align="center">
  <strong>An elegant and powerful Android user guide overlay library</strong><br>
  Supporting multi-step guides, text descriptions, custom animations, and flexible UI configurations
</p>

[中文文档](README.md) | **English**

---

## ✨ Features

- 🎯 **Highlight Views**: Support for rectangular, circular, and oval highlight shapes
- 📝 **Text Descriptions**: Built-in title and description text support without extra images
- 🔢 **Multi-step Guides**: Easily create coherent multi-step user guide flows
- 🎨 **Flexible Positioning**: Support for absolute, relative, and preset positioning
- 🎬 **Smooth Animations**: Built-in fade-in/fade-out animations for better UX
- ⏭️ **Navigation Controls**: Previous/Next buttons, step indicators, and skip functionality
- 🎨 **Modern UI**: Beautiful button styles and card designs
- 🔧 **Highly Customizable**: Fluent chain API for flexible step configuration
- 🔙 **Backward Compatible**: Fully preserves single-step API

## 📱 Preview

<img src="https://github.com/tangzhengfeng1024/GuideLayer/raw/main/Screen_recording_20251029_204805-ezgif.com-video-to-gif-converter.gif" alt="功能截图" width="50%" height="50%">


## 🚀 Quick Start

### Installation

Copy the GuideOverlayView.kt file to your project.

### Basic Usage

#### Single Step Guide

```kotlin
val targetView = findViewById<View>(R.id.targetView)

GuideLayer.with(this)
    .highlight(targetView, shape = HighlightShape.CIRCLE, paddingDp = 12f)
    .text("Feature Title", "This is a detailed description of the feature")
    .confirmButton(show = true, text = "Got it")
    .show()
```

#### Multi-step Guide

```kotlin
GuideLayer.with(this)
    // Step 1
    .addStep()
        .highlight(view1)
        .text("Step One", "Introduction to the first feature")
        .showNextButton(true)
        .showStepIndicator(true)
        .showSkipButton(true)
    // Step 2
    .nextStep()
        .highlight(view2)
        .text("Step Two", "Introduction to the second feature")
        .showPreviousButton(true)
        .showNextButton(true)
        .showStepIndicator(true)
        .showSkipButton(true)
    // Step 3
    .nextStep()
        .highlight(view3)
        .text("Step Three", "Introduction to the third feature")
        .showPreviousButton(true)
        .confirmButton(show = true, text = "Finish")
        .showStepIndicator(true)
    // Complete configuration
    .endSteps()
    .onStepChanged { current, total ->
        Log.d("Guide", "Step: $current/$total")
    }
    .onSkipAll {
        Log.d("Guide", "User skipped the guide")
    }
    .show(delayMs = 500)
```

## 📖 Documentation

### Highlight Shapes

```kotlin
enum class HighlightShape {
    RECT,    // Rectangle with rounded corners
    CIRCLE,  // Circle
    OVAL     // Oval
}
```

### Position Types

```kotlin
// Absolute pixel position
Position(PositionType.ABSOLUTE, x = 100f, y = 200f)

// Relative screen percentage (0.0-1.0)
Position(PositionType.RELATIVE, x = 0.5f, y = 0.5f)

// Offset relative to highlighted target
Position(PositionType.RELATIVE_TO_TARGET, x = 0f, y = 100f)

// Preset position
Position(PositionType.PRESET, preset = PresetPosition.BOTTOM_CENTER)
```

### API Methods

#### GuideLayer

| Method | Description |
|--------|-------------|
| `with(activity)` | Create GuideLayer instance |
| `highlight(view, shape, padding, cornerRadius)` | Highlight specified view |
| `overlayImage(resId, position)` | Add overlay image (e.g., arrow) |
| `addStep()` | Start adding multi-step guide |
| `dismissOnTouchAnywhere(enabled)` | Allow dismiss by touching anywhere |
| `confirmButton(show, text)` | Configure confirm button |
| `confirmButtonPosition(position)` | Set confirm button position |
| `onStepChanged(callback)` | Set step change callback |
| `onSkipAll(callback)` | Set skip all callback |
| `startFromStep(index)` | Set starting step index |
| `show(delayMs)` | Show guide |

#### StepBuilder (Multi-step)

| Method | Description |
|--------|-------------|
| `highlight(view, ...)` | Highlight specified view |
| `text(title, description)` | Set text content (title + description) |
| `title(title)` | Set title only |
| `description(description)` | Set description only |
| `textPosition(position)` | Set text position |
| `overlayImage(resId, position)` | Add overlay image |
| `dismissOnTouchAnywhere(enabled)` | Allow dismiss by touching anywhere |
| `confirmButton(show, text)` | Configure confirm button |
| `confirmButtonPosition(position)` | Set confirm button position |
| `showPreviousButton(show)` | Show "Previous" button |
| `showNextButton(show)` | Show "Next" button |
| `showStepIndicator(show)` | Show step indicator |
| `showSkipButton(show)` | Show "Skip" button |
| `callbacks(onShown, onDismissed)` | Set callbacks |
| `nextStep()` | Complete current step, continue to next |
| `endSteps()` | Complete all step configurations |

## 🎨 Advanced Usage

### Custom Text Position

```kotlin
.addStep()
    .highlight(view)
    .text("Title", "Description text")
    .textPosition(Position(PositionType.RELATIVE_TO_TARGET, 0f, 150f))
```

### Using Overlay Images (e.g., Arrows)

```kotlin
.addStep()
    .highlight(view)
    .overlayImage(R.drawable.arrow, Position(PositionType.RELATIVE_TO_TARGET, 50f, -100f))
```

### Step Callbacks and Listeners

```kotlin
.addStep()
    .highlight(view)
    .callbacks(
        onShown = { 
            analytics.track("guide_step_shown")
        },
        onDismissed = { 
            saveProgress()
        }
    )
.nextStep()
    // ... more steps
.endSteps()
.onStepChanged { current, total ->
    updateProgressBar(current, total)
}
.onSkipAll {
    analytics.track("guide_skipped")
}
```

## 💡 Best Practices

1. **Disable Touch Dismiss**: Set `.dismissOnTouchAnywhere(false)` for multi-step guides
2. **Use Step Indicator**: Let users know progress with `.showStepIndicator(true)`
3. **Provide Skip Option**: Respect user choice with `.showSkipButton(true)`
4. **Add Show Delay**: Ensure layout completion with `.show(delayMs = 500)`
5. **Track User Behavior**: Use callbacks to record guide completion
6. **Save Progress**: Allow users to continue from where they left off

## 📋 Requirements

- **Minimum API Level**: 21 (Android 5.0)
- **Programming Language**: Kotlin
- **AndroidX**: Yes


## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

Thanks to all contributors and users!

---

<p align="center">
  If this project helps you, please give it a ⭐️ Star!
</p>

