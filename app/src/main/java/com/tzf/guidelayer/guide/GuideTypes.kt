package com.tzf.guidelayer.guide

import androidx.annotation.DrawableRes

enum class HighlightShape { RECT, CIRCLE, OVAL }

enum class PositionType {
    ABSOLUTE,      // 绝对像素位置
    RELATIVE,      // 相对屏幕百分比 (0.0-1.0)
    RELATIVE_TO_TARGET,  // 相对于高亮目标
    PRESET         // 预设位置
}

enum class PresetPosition {
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    CENTER_LEFT, CENTER, CENTER_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
}

data class Position(
    val type: PositionType,
    val x: Float = 0f,           // ABSOLUTE: 像素值, RELATIVE: 百分比, RELATIVE_TO_TARGET: 偏移像素
    val y: Float = 0f,           // ABSOLUTE: 像素值, RELATIVE: 百分比, RELATIVE_TO_TARGET: 偏移像素
    val preset: PresetPosition? = null,  // PRESET 类型时使用
    val targetIndex: Int = 0     // RELATIVE_TO_TARGET 类型时使用的目标索引
)

data class HighlightTarget(
    val view: android.view.View,
    val shape: HighlightShape = HighlightShape.RECT,
    val paddingPx: Float = 16f,
    val cornerRadiusPx: Float = 24f
)

data class OverlayImage(
    @DrawableRes val resId: Int,
    val position: Position
)

/**
 * 按钮样式配置
 */
data class ButtonStyle(
    val backgroundColor: Int? = null,           // 背景颜色，为null时使用默认
    val textColor: Int? = null,                 // 文字颜色，为null时使用默认
    val textSizeSp: Float? = null,              // 文字大小（sp），为null时使用默认
    val cornerRadiusDp: Float? = null,          // 圆角半径（dp），为null时使用默认
    val strokeColor: Int? = null,               // 边框颜色（次要按钮使用），为null时使用默认
    val strokeWidthDp: Float? = null            // 边框宽度（dp），为null时使用默认
)

/**
 * 引导步骤配置
 */
data class GuideStep(
    val highlights: List<HighlightTarget> = emptyList(),
    val images: List<OverlayImage> = emptyList(),
    val title: String? = null,                  // 步骤标题
    val description: String? = null,            // 步骤描述
    val textPosition: Position = Position(PositionType.PRESET, preset = PresetPosition.BOTTOM_CENTER),
    val textMaxWidthRatio: Float = 0.8f,        // 文本最大宽度占屏幕比例 (0.0-1.0)
    val dismissOnTouchAnywhere: Boolean = true,
    val showConfirmButton: Boolean = true,
    val confirmText: String = "我知道了",
    val confirmButtonPosition: Position = Position(PositionType.PRESET, preset = PresetPosition.BOTTOM_CENTER),
    val confirmButtonStyle: ButtonStyle? = null, // 确认按钮样式，为null时使用默认
    val showPrevButton: Boolean = false,
    val showNextButton: Boolean = false,
    val showStepIndicator: Boolean = false,
    val showSkipButton: Boolean = false,        // 显示"跳过"按钮
    val prevButtonText: String = "上一步",
    val nextButtonText: String = "下一步",
    val skipButtonText: String = "跳过",
    val primaryButtonStyle: ButtonStyle? = null,  // 主要按钮样式（确认、下一步），为null时使用默认
    val secondaryButtonStyle: ButtonStyle? = null, // 次要按钮样式（上一步），为null时使用默认
    val skipButtonStyle: ButtonStyle? = null,     // 跳过按钮样式，为null时使用默认
    val onShown: (() -> Unit)? = null,
    val onDismissed: (() -> Unit)? = null
)
