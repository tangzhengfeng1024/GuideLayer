package com.tzf.guidelayer.guide

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes

class GuideLayer private constructor(private val activity: Activity) {
    private val highlights = mutableListOf<HighlightTarget>()
    private val images = mutableListOf<OverlayImage>()
    private var dismissAnywhere = true
    private var showConfirm = true
    private var confirmText = "我知道了"
    private var confirmButtonPosition = Position(PositionType.PRESET, preset = PresetPosition.BOTTOM_CENTER)
    private var confirmButtonStyle: ButtonStyle? = null
    private var onShown: (() -> Unit)? = null
    private var onDismissed: (() -> Unit)? = null

    // 多步骤支持
    private val steps = mutableListOf<GuideStep>()
    private var currentStepBuilder: StepBuilder? = null
    private var startStepIndex = 0
    private var onStepChanged: ((Int, Int) -> Unit)? = null
    private var onSkipAll: (() -> Unit)? = null

    companion object {
        fun with(activity: Activity) = GuideLayer(activity)
    }

    /**
     * 步骤构建器
     */
    inner class StepBuilder {
        private val highlights = mutableListOf<HighlightTarget>()
        private val images = mutableListOf<OverlayImage>()
        private var title: String? = null
        private var description: String? = null
        private var textPosition = Position(PositionType.PRESET, preset = PresetPosition.BOTTOM_CENTER)
        private var textMaxWidthRatio = 0.8f
        private var dismissAnywhere = true
        private var showConfirm = true
        private var confirmText = "我知道了"
        private var confirmButtonPosition = Position(PositionType.PRESET, preset = PresetPosition.BOTTOM_CENTER)
        private var showPrev = false
        private var showNext = true
        private var showIndicator = true
        private var showSkip = false
        private var onShown: (() -> Unit)? = null
        private var onDismissed: (() -> Unit)? = null
        private var confirmButtonStyle: ButtonStyle? = null
        private var primaryButtonStyle: ButtonStyle? = null
        private var secondaryButtonStyle: ButtonStyle? = null
        private var skipButtonStyle: ButtonStyle? = null
        private var prevButtonText = "上一步"
        private var nextButtonText = "下一步"
        private var skipButtonText = "跳过"

        fun highlight(
            view: View,
            shape: HighlightShape = HighlightShape.RECT,
            paddingDp: Float = 8f,
            cornerRadiusDp: Float = 12f
        ): StepBuilder {
            highlights += HighlightTarget(
                view, shape,
                paddingDp * density(activity),
                cornerRadiusDp * density(activity)
            )
            return this
        }

        fun overlayImage(@DrawableRes resId: Int, xDp: Float, yDp: Float): StepBuilder {
            images += OverlayImage(resId, Position(PositionType.ABSOLUTE, xDp * density(activity), yDp * density(activity)))
            return this
        }

        fun overlayImage(@DrawableRes resId: Int, position: Position): StepBuilder {
            images += OverlayImage(resId, position)
            return this
        }

        fun dismissOnTouchAnywhere(enabled: Boolean): StepBuilder {
            dismissAnywhere = enabled
            return this
        }

        fun confirmButton(show: Boolean, text: String = "我知道了"): StepBuilder {
            showConfirm = show
            confirmText = text
            return this
        }

        fun confirmButtonPosition(position: Position): StepBuilder {
            confirmButtonPosition = position
            return this
        }

        fun showPreviousButton(show: Boolean): StepBuilder {
            showPrev = show
            return this
        }

        fun showNextButton(show: Boolean): StepBuilder {
            showNext = show
            return this
        }

        fun showStepIndicator(show: Boolean): StepBuilder {
            showIndicator = show
            return this
        }

        fun showSkipButton(show: Boolean): StepBuilder {
            showSkip = show
            return this
        }

        /**
         * 设置步骤标题
         */
        fun title(title: String): StepBuilder {
            this.title = title
            return this
        }

        /**
         * 设置步骤描述
         */
        fun description(description: String): StepBuilder {
            this.description = description
            return this
        }

        /**
         * 设置文本内容（标题+描述的便捷方法）
         */
        fun text(title: String, description: String? = null): StepBuilder {
            this.title = title
            this.description = description
            return this
        }

        /**
         * 设置文本位置
         */
        fun textPosition(position: Position): StepBuilder {
            this.textPosition = position
            return this
        }

        /**
         * 设置文本最大宽度比例 (0.0-1.0)
         */
        fun textMaxWidthRatio(ratio: Float): StepBuilder {
            this.textMaxWidthRatio = ratio.coerceIn(0.1f, 1.0f) // 限制在合理范围内
            return this
        }

        fun callbacks(onShown: (() -> Unit)? = null, onDismissed: (() -> Unit)? = null): StepBuilder {
            this.onShown = onShown
            this.onDismissed = onDismissed
            return this
        }

        /**
         * 设置确认按钮样式
         */
        fun confirmButtonStyle(style: ButtonStyle?): StepBuilder {
            this.confirmButtonStyle = style
            return this
        }

        /**
         * 设置主要按钮样式（确认、下一步）
         */
        fun primaryButtonStyle(style: ButtonStyle?): StepBuilder {
            this.primaryButtonStyle = style
            return this
        }

        /**
         * 设置次要按钮样式（上一步）
         */
        fun secondaryButtonStyle(style: ButtonStyle?): StepBuilder {
            this.secondaryButtonStyle = style
            return this
        }

        /**
         * 设置跳过按钮样式
         */
        fun skipButtonStyle(style: ButtonStyle?): StepBuilder {
            this.skipButtonStyle = style
            return this
        }

        /**
         * 设置上一步按钮文字
         */
        fun prevButtonText(text: String): StepBuilder {
            this.prevButtonText = text
            return this
        }

        /**
         * 设置下一步按钮文字
         */
        fun nextButtonText(text: String): StepBuilder {
            this.nextButtonText = text
            return this
        }

        /**
         * 设置跳过按钮文字
         */
        fun skipButtonText(text: String): StepBuilder {
            this.skipButtonText = text
            return this
        }

        internal fun build(): GuideStep {
            return GuideStep(
                highlights = highlights.toList(),
                images = images.toList(),
                title = title,
                description = description,
                textPosition = textPosition,
                textMaxWidthRatio = textMaxWidthRatio,
                dismissOnTouchAnywhere = dismissAnywhere,
                showConfirmButton = showConfirm,
                confirmText = confirmText,
                confirmButtonPosition = confirmButtonPosition,
                confirmButtonStyle = confirmButtonStyle,
                showPrevButton = showPrev,
                showNextButton = showNext,
                showStepIndicator = showIndicator,
                showSkipButton = showSkip,
                prevButtonText = prevButtonText,
                nextButtonText = nextButtonText,
                skipButtonText = skipButtonText,
                primaryButtonStyle = primaryButtonStyle,
                secondaryButtonStyle = secondaryButtonStyle,
                skipButtonStyle = skipButtonStyle,
                onShown = onShown,
                onDismissed = onDismissed
            )
        }

        /**
         * 完成当前步骤并继续添加下一步
         */
        fun nextStep(): StepBuilder {
            steps.add(build())
            return StepBuilder()
        }

        /**
         * 完成所有步骤配置并返回 GuideLayer
         */
        fun endSteps(): GuideLayer {
            steps.add(build())
            return this@GuideLayer
        }
    }

    fun highlight(
        view: View,
        shape: HighlightShape = HighlightShape.RECT,
        paddingDp: Float = 8f,
        cornerRadiusDp: Float = 12f
    ): GuideLayer {
        highlights += HighlightTarget(
            view, shape,
            paddingDp * density(activity),
            cornerRadiusDp * density(activity)
        )
        return this
    }

    fun overlayImage(@DrawableRes resId: Int, xDp: Float, yDp: Float): GuideLayer {
        images += OverlayImage(resId, Position(PositionType.ABSOLUTE, xDp * density(activity), yDp * density(activity)))
        return this
    }

    fun overlayImage(@DrawableRes resId: Int, position: Position): GuideLayer {
        images += OverlayImage(resId, position)
        return this
    }

    fun dismissOnTouchAnywhere(enabled: Boolean): GuideLayer {
        dismissAnywhere = enabled
        return this
    }

    fun confirmButton(show: Boolean, text: String = "我知道了"): GuideLayer {
        showConfirm = show
        confirmText = text
        return this
    }

    fun confirmButtonPosition(position: Position): GuideLayer {
        confirmButtonPosition = position
        return this
    }

    fun confirmButtonStyle(style: ButtonStyle?): GuideLayer {
        confirmButtonStyle = style
        return this
    }

    fun callbacks(onShown: (() -> Unit)? = null, onDismissed: (() -> Unit)? = null): GuideLayer {
        this.onShown = onShown
        this.onDismissed = onDismissed
        return this
    }

    /**
     * 开始添加多步骤引导
     * 示例：
     * ```
     * GuideLayer.with(activity)
     *     .addStep()
     *         .highlight(view1)
     *         .confirmButton(show = false)
     *         .showNextButton(true)
     *     .nextStep()
     *         .highlight(view2)
     *         .showPreviousButton(true)
     *         .showNextButton(true)
     *     .endSteps()
     *     .show()
     * ```
     */
    fun addStep(): StepBuilder {
        return StepBuilder()
    }

    /**
     * 设置步骤改变回调
     */
    fun onStepChanged(callback: (currentStep: Int, totalSteps: Int) -> Unit): GuideLayer {
        this.onStepChanged = callback
        return this
    }

    /**
     * 设置跳过所有步骤回调
     */
    fun onSkipAll(callback: () -> Unit): GuideLayer {
        this.onSkipAll = callback
        return this
    }

    /**
     * 设置开始步骤索引（从0开始）
     */
    fun startFromStep(index: Int): GuideLayer {
        this.startStepIndex = index
        return this
    }

    fun show(delayMs: Long = 0L) {
        val showAction = {
            val root = activity.findViewById<ViewGroup>(android.R.id.content)
            val overlay = GuideOverlayView(activity).apply {
                // 多步骤模式
                if (steps.isNotEmpty()) {
                    setSteps(steps, startStepIndex, this@GuideLayer.onStepChanged, this@GuideLayer.onSkipAll)
                }
                // 单步骤模式（向后兼容）
                else {
                    setData(highlights, images, dismissAnywhere, showConfirm, confirmText, confirmButtonPosition, confirmButtonStyle)
                    onShown = this@GuideLayer.onShown
                    onDismissed = this@GuideLayer.onDismissed
                }
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            root.addView(overlay)
        }
        if (delayMs > 0) {
            activity.window.decorView.postDelayed(showAction, delayMs)
        } else {
            showAction()
        }
    }

    private fun density(ctx: android.content.Context) = ctx.resources.displayMetrics.density
}
