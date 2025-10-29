package com.tzf.guidelayer.guide

import android.R
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlin.math.max
import kotlin.math.min

class GuideOverlayView(context: Context) : FrameLayout(context) {
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
        color = Color.parseColor("#99000000") 
    }
    
    // Paint for cutting transparent holes in the mask
    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        color = Color.BLACK // Use black with full alpha for proper DST_OUT
        alpha = 255
    }
    
    // Bitmap for offscreen rendering to avoid black rectangles
    private var maskBitmap: Bitmap? = null
    private var maskCanvas: Canvas? = null

    private val highlights = mutableListOf<HighlightTarget>()
    private val overlayImages = mutableListOf<OverlayImage>()

    var dismissOnTouchAnywhere: Boolean = true
    var onShown: (() -> Unit)? = null
    var onDismissed: (() -> Unit)? = null

    private var showConfirmButton = false
    private var confirmText = "我知道了"
    private var confirmButtonPosition = Position(PositionType.PRESET, preset = PresetPosition.BOTTOM_CENTER)

    // 多步骤支持
    private val steps = mutableListOf<GuideStep>()
    private var currentStepIndex = 0
    private var onStepChanged: ((Int, Int) -> Unit)? = null // (currentStep, totalSteps)
    private var onSkipAll: (() -> Unit)? = null
    private var showPrevButton = false
    private var showNextButton = false
    private var showStepIndicator = false
    private var showSkipButton = false

    // 按钮样式
    private var confirmButtonStyle: ButtonStyle? = null
    private var primaryButtonStyle: ButtonStyle? = null
    private var secondaryButtonStyle: ButtonStyle? = null
    private var skipButtonStyle: ButtonStyle? = null
    private var prevButtonText = "上一步"
    private var nextButtonText = "下一步"
    private var skipButtonText = "跳过"

    // 文本说明
    private var stepTitle: String? = null
    private var stepDescription: String? = null
    private var textPosition = Position(PositionType.PRESET, preset = PresetPosition.BOTTOM_CENTER)
    private var textMaxWidthRatio = 0.8f

    init {
        setWillNotDraw(false)
        // Use hardware layer for better performance and to avoid black rectangles
        setLayerType(LAYER_TYPE_HARDWARE, null)
        isClickable = true
        // 初始设置为透明，等待淡入动画
        alpha = 0f
    }

    fun setData(
        highlights: List<HighlightTarget>,
        images: List<OverlayImage>,
        dismissAnywhere: Boolean,
        showConfirm: Boolean,
        confirmText: String,
        confirmButtonPosition: Position = Position(PositionType.PRESET, preset = PresetPosition.BOTTOM_CENTER),
        confirmButtonStyle: ButtonStyle? = null
    ) {
        this.highlights.clear()
        this.highlights.addAll(highlights)
        this.overlayImages.clear()
        this.overlayImages.addAll(images)
        this.dismissOnTouchAnywhere = dismissAnywhere
        this.showConfirmButton = showConfirm
        this.confirmText = confirmText
        this.confirmButtonPosition = confirmButtonPosition
        this.confirmButtonStyle = confirmButtonStyle
        // 延迟到布局完成后再创建，避免 width/height 为 0 导致定位错误
        post {
            updateOverlays()
        }
    }

    /**
     * 设置多步骤引导
     */
    fun setSteps(
        steps: List<GuideStep>,
        startIndex: Int = 0,
        onStepChanged: ((Int, Int) -> Unit)? = null,
        onSkipAll: (() -> Unit)? = null
    ) {
        this.steps.clear()
        this.steps.addAll(steps)
        this.currentStepIndex = startIndex.coerceIn(0, steps.size - 1)
        this.onStepChanged = onStepChanged
        this.onSkipAll = onSkipAll
        
        // 延迟到布局完成后再创建，避免 width/height 为 0 导致定位错误
        post {
            showCurrentStep()
        }
    }

    /**
     * 显示当前步骤
     */
    private fun showCurrentStep() {
        if (steps.isEmpty() || currentStepIndex >= steps.size) return

        val step = steps[currentStepIndex]

        // 更新当前步骤的数据
        highlights.clear()
        highlights.addAll(step.highlights)
        overlayImages.clear()
        overlayImages.addAll(step.images)
        stepTitle = step.title
        stepDescription = step.description
        textPosition = step.textPosition
        textMaxWidthRatio = step.textMaxWidthRatio
        dismissOnTouchAnywhere = step.dismissOnTouchAnywhere
        showConfirmButton = step.showConfirmButton
        confirmText = step.confirmText
        confirmButtonPosition = step.confirmButtonPosition
        confirmButtonStyle = step.confirmButtonStyle
        showPrevButton = step.showPrevButton
        showNextButton = step.showNextButton
        showStepIndicator = step.showStepIndicator
        showSkipButton = step.showSkipButton
        prevButtonText = step.prevButtonText
        nextButtonText = step.nextButtonText
        skipButtonText = step.skipButtonText
        primaryButtonStyle = step.primaryButtonStyle
        secondaryButtonStyle = step.secondaryButtonStyle
        skipButtonStyle = step.skipButtonStyle

        // 重新创建覆盖层，避免闪烁
        updateOverlays()

        // 回调
        step.onShown?.invoke()
        onStepChanged?.invoke(currentStepIndex + 1, steps.size)
    }

    /**
     * 前往上一步
     */
    fun previousStep() {
        if (currentStepIndex > 0) {
            val prevStep = steps[currentStepIndex]
            prevStep.onDismissed?.invoke()
            currentStepIndex--
            showCurrentStep()
        }
    }

    /**
     * 前往下一步
     */
    fun nextStep() {
        if (currentStepIndex < steps.size - 1) {
            val prevStep = steps[currentStepIndex]
            prevStep.onDismissed?.invoke()
            currentStepIndex++
            showCurrentStep()
        } else {
            // 最后一步，关闭引导
            dismiss()
        }
    }

    /**
     * 跳转到指定步骤
     */
    fun goToStep(index: Int) {
        if (index in 0 until steps.size && index != currentStepIndex) {
            val prevStep = steps[currentStepIndex]
            prevStep.onDismissed?.invoke()
            currentStepIndex = index
            showCurrentStep()
        }
    }

    /**
     * 更新覆盖层，避免闪烁
     */
    private fun updateOverlays() {
        // 创建新的视图列表
        val newViews = mutableListOf<View>()
        val viewPositions = mutableMapOf<View, Position>()

        // Add text description (title + description)
        if (stepTitle != null || stepDescription != null) {
            val textContainer = createTextContainer()
            // textContainer已经在内部处理了最大宽度，这里使用WRAP_CONTENT
            val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            textContainer.layoutParams = lp
            newViews.add(textContainer)
            viewPositions[textContainer] = textPosition
        }

        // Add overlay images
        overlayImages.forEach { item ->
            val iv = ImageView(context)
            iv.setImageDrawable(ContextCompat.getDrawable(context, item.resId))
            val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            iv.layoutParams = lp
            newViews.add(iv)
            viewPositions[iv] = item.position
        }

        // 多步骤模式：添加导航按钮和步骤指示器
        if (steps.isNotEmpty()) {
            val multiStepViewPairs = createMultiStepControls()
            multiStepViewPairs.forEach { (view, position) ->
                newViews.add(view)
                position?.let { viewPositions[view] = it }
            }
        }
        // 单步骤模式：添加确认按钮
        else if (showConfirmButton) {
            val buttonStyle = confirmButtonStyle ?: primaryButtonStyle
            val tv = TextView(context).apply {
                text = confirmText
                setTextColor(buttonStyle?.textColor ?: Color.WHITE)
                textSize = buttonStyle?.textSizeSp ?: 16f
                setPadding(dp(24), dp(12), dp(24), dp(12))
                background = createButtonBackground(buttonStyle, true)
                setOnClickListener { dismiss() }
            }
            val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            tv.layoutParams = lp
            newViews.add(tv)
            viewPositions[tv] = confirmButtonPosition
        }

        // 一次性替换所有子视图，避免闪烁
        removeAllViews()
        newViews.forEach { addView(it) }

        // 立即定位所有视图，避免闪烁
        viewPositions.forEach { (view, position) ->
            positionChild(view, position)
        }

        // 强制重绘遮罩
        invalidate()
    }

    /**
     * 创建覆盖层（保留向后兼容，已弃用）
     */
    @Deprecated("Use updateOverlays() instead to avoid flickering")
    private fun createOverlays() {
        updateOverlays()
    }

    /**
     * 创建文本容器（标题+描述）
     * 使用FrameLayout包装LinearLayout来实现最大宽度限制
     */
    private fun createTextContainer(): FrameLayout {
        // 计算最大宽度
        val maxWidth = (context.resources.displayMetrics.widthPixels * textMaxWidthRatio).toInt()

        return FrameLayout(context).apply {
            // 创建内容容器（背景+文本）
            val contentContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(16), dp(20), dp(16))
                background = createTextCardBackground()  // 使用新的文本卡片背景

                // 添加标题
                stepTitle?.let { title ->
                    val titleView = TextView(context).apply {
                        text = title
                        setTextColor(Color.WHITE)
                        textSize = 18f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    addView(titleView)

                    // 如果有描述，添加间距
                    if (stepDescription != null) {
                        addView(View(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                dp(8)
                            )
                        })
                    }
                }

                // 添加描述
                stepDescription?.let { desc ->
                    val descView = TextView(context).apply {
                        text = desc
                        setTextColor(Color.parseColor("#E0E0E0"))
                        textSize = 14f
                        // 移除maxWidth，让它随内容自动换行
                    }
                    addView(descView)
                }
            }

            // 将内容容器添加到FrameLayout中，并设置最大宽度
            addView(contentContainer, LayoutParams(maxWidth, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            })
        }
    }
    
    /**
     * 创建文本卡片背景（更明亮的颜色）
     */
    private fun createTextCardBackground(): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(12).toFloat()
            setColor(Color.parseColor("#E62196F3"))  // 半透明蓝色背景
        }
    }

    /**
     * 创建卡片背景
     */
    private fun createCardBackground(): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(12).toFloat()
            setColor(Color.parseColor("#DD333333"))  // 半透明深灰色
        }
    }

    /**
     * 创建多步骤控制按钮，返回视图和位置的配对列表
     */
    private fun createMultiStepControls(): List<Pair<View, Position?>> {
        val viewPairs = mutableListOf<Pair<View, Position?>>()
        val isFirstStep = currentStepIndex == 0
        val isLastStep = currentStepIndex == steps.size - 1

        // 创建底部按钮容器
        val buttonContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(20), dp(12), dp(20), dp(12))
            background = createCardBackground()
        }

        // 上一步按钮
        if (showPrevButton && !isFirstStep) {
            val prevButton = createStepButton(prevButtonText, isPrimary = false) { previousStep() }
            buttonContainer.addView(prevButton)

            // 添加间距
            val spacer = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(12), 1)
            }
            buttonContainer.addView(spacer)
        }

        // 步骤指示器
        if (showStepIndicator) {
            val indicator = TextView(context).apply {
                text = "${currentStepIndex + 1}/${steps.size}"
                setTextColor(Color.WHITE)
                textSize = 14f
                setPadding(dp(12), dp(8), dp(12), dp(8))
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                gravity = Gravity.CENTER
            }
            buttonContainer.addView(indicator, lp)
        } else if (showPrevButton && (showNextButton || showConfirmButton)) {
            // 添加弹性间距
            val spacer = View(context)
            val lp = LinearLayout.LayoutParams(0, 1).apply {
                weight = 1f
            }
            buttonContainer.addView(spacer, lp)
        }

        // 下一步或完成按钮
        if (isLastStep) {
            if (showConfirmButton) {
                val confirmButton = createStepButton(confirmText, isPrimary = true) { dismiss() }
                buttonContainer.addView(confirmButton)
            }
        } else if (showNextButton) {
            val nextButton = createStepButton(nextButtonText, isPrimary = true) { nextStep() }
            buttonContainer.addView(nextButton)
        }

        // 如果没有任何按钮，且有确认按钮，显示确认按钮
        if (!showPrevButton && !showNextButton && showConfirmButton) {
            val confirmButton = createStepButton(confirmText, isPrimary = true) {
                if (isLastStep) dismiss() else nextStep()
            }
            buttonContainer.removeAllViews()
            buttonContainer.addView(confirmButton)
        }

        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        buttonContainer.layoutParams = lp
        viewPairs.add(Pair(buttonContainer, confirmButtonPosition))

        // 添加跳过按钮（右上角）
        if (showSkipButton) {
            val skipButton = createSkipButton()
            val skipLp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = dp(16)
                rightMargin = dp(16)
            }
            skipButton.layoutParams = skipLp
            // 跳过按钮直接使用 gravity 定位，不需要 positionChild 处理
            viewPairs.add(Pair(skipButton, null))
        }

        return viewPairs
    }

    /**
     * 创建步骤按钮
     */
    private fun createStepButton(text: String, isPrimary: Boolean = true, onClick: () -> Unit): TextView {
        val buttonStyle = when {
            isPrimary && primaryButtonStyle != null -> primaryButtonStyle
            !isPrimary && secondaryButtonStyle != null -> secondaryButtonStyle
            else -> null
        }

        return TextView(context).apply {
            this.text = text
            textSize = buttonStyle?.textSizeSp ?: 15f
            setTextColor(buttonStyle?.textColor ?: if (isPrimary) Color.WHITE else Color.parseColor("#E0E0E0"))
            setPadding(dp(20), dp(10), dp(20), dp(10))
            background = createButtonBackground(buttonStyle, isPrimary)
            setOnClickListener { onClick() }
        }
    }

    /**
     * 创建跳过按钮
     */
    private fun createSkipButton(): TextView {
        val buttonStyle = skipButtonStyle

        return TextView(context).apply {
            text = skipButtonText
            textSize = buttonStyle?.textSizeSp ?: 14f
            setTextColor(buttonStyle?.textColor ?: Color.WHITE)
            setPadding(dp(16), dp(8), dp(16), dp(8))
            background = createSkipButtonBackground(buttonStyle)
            setOnClickListener {
                onSkipAll?.invoke()
                dismiss()
            }
        }
    }

    /**
     * 创建通用按钮背景
     */
    private fun createButtonBackground(buttonStyle: ButtonStyle?, isPrimary: Boolean): Drawable {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = buttonStyle?.cornerRadiusDp?.let { dp(it.toInt()).toFloat() }
                          ?: if (isPrimary) dp(8).toFloat() else dp(8).toFloat()

            // 设置背景色
            buttonStyle?.backgroundColor?.let { setColor(it) }
                ?: if (isPrimary) {
                    setColor(Color.parseColor("#2196F3"))
                } else {
                    setColor(Color.TRANSPARENT)
                }

            // 设置边框（次要按钮使用）
            if (!isPrimary || buttonStyle?.strokeColor != null) {
                val strokeColor = buttonStyle?.strokeColor ?: Color.parseColor("#E0E0E0")
                val strokeWidth = buttonStyle?.strokeWidthDp?.let { dp(it.toInt()) } ?: dp(1)
                setStroke(strokeWidth, strokeColor)
            }
        }
        return drawable
    }

    /**
     * 创建主要按钮背景（蓝色）
     */
    private fun createPrimaryButtonBackground(): Drawable {
        return createButtonBackground(null, true)
    }

    /**
     * 创建次要按钮背景（灰色边框）
     */
    private fun createSecondaryButtonBackground(): Drawable {
        return createButtonBackground(null, false)
    }

    /**
     * 创建跳过按钮背景
     */
    private fun createSkipButtonBackground(buttonStyle: ButtonStyle? = null): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = buttonStyle?.cornerRadiusDp?.let { dp(it.toInt()).toFloat() } ?: dp(16).toFloat()
            setColor(buttonStyle?.backgroundColor ?: Color.parseColor("#55000000"))
        }
    }

    /**
     * 创建按钮背景（已弃用，保留向后兼容）
     */
    @Deprecated("Use createPrimaryButtonBackground or createSecondaryButtonBackground")
    private fun createButtonBackground(): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(8).toFloat()
            setColor(Color.parseColor("#F0F0F0"))
        }
    }

    private fun positionChild(child: View, position: Position) {
        // 立即设置初始位置，避免闪烁
        updateChildPosition(child, position)

        // 如果视图还没有完成布局，监听布局完成事件进行重新定位
        if (child.width == 0 || child.height == 0) {
            child.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (child.width > 0 && child.height > 0) {
                        child.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        updateChildPosition(child, position)
                    }
                }
            })
        }
    }

    private fun updateChildPosition(child: View, position: Position) {
        val lp = child.layoutParams as LayoutParams
        var left = 0
        var top = 0

        // 使用视图的实际尺寸，如果还没有测量则使用估算值
        val childWidth = if (child.width > 0) child.width else estimateChildWidth(child)
        val childHeight = if (child.height > 0) child.height else estimateChildHeight(child)

        when (position.type) {
            PositionType.ABSOLUTE -> {
                left = position.x.toInt()
                top = position.y.toInt()
            }
            PositionType.RELATIVE -> {
                left = (position.x * width - 0f).toInt()
                top = (position.y * height - 0f).toInt()
            }
            PositionType.RELATIVE_TO_TARGET -> {
                if (position.targetIndex >= 0 && position.targetIndex < highlights.size) {
                    val target = highlights[position.targetIndex]
                    val rect = targetRect(target.view, target.paddingPx)
                    val cx = rect.centerX() + position.x
                    val cy = rect.centerY() + position.y
                    left = (cx - childWidth / 2f).toInt()
                    top = (cy - childHeight / 2f).toInt()
                } else {
                    left = position.x.toInt()
                    top = position.y.toInt()
                }
            }
            PositionType.PRESET -> {
                val p = computePresetLeftTop(position.preset ?: PresetPosition.BOTTOM_CENTER, childWidth, childHeight)
                left = p.x
                top = p.y
            }
        }

        // 边界裁剪，避免超出屏幕
        left = left.coerceIn(0, max(0, width - childWidth))
        top = top.coerceIn(0, max(0, height - childHeight))

        lp.leftMargin = left
        lp.topMargin = top
        lp.gravity = Gravity.NO_GRAVITY
        child.layoutParams = lp
    }

    /**
     * 估算子视图宽度（当视图还没有测量时使用）
     */
    private fun estimateChildWidth(child: View): Int {
        return when (child) {
            is TextView -> {
                // 估算文本视图宽度
                val text = child.text?.toString() ?: ""
                val paint = Paint().apply {
                    textSize = child.textSize
                    typeface = child.typeface
                }
                val textWidth = paint.measureText(text)
                val padding = child.paddingLeft + child.paddingRight
                (textWidth + padding).toInt().coerceAtLeast(dp(100)) // 最小宽度
            }
            is ImageView -> {
                // 估算图片视图宽度
                dp(48) // 默认图标大小
            }
            is LinearLayout -> {
                // 估算线性布局宽度
                dp(200) // 默认中等宽度
            }
            else -> dp(150) // 默认宽度
        }
    }

    /**
     * 估算子视图高度（当视图还没有测量时使用）
     */
    private fun estimateChildHeight(child: View): Int {
        return when (child) {
            is TextView -> {
                // 估算文本视图高度
                val lineHeight = child.textSize * 1.2f // 估算行高
                val lines = child.text?.toString()?.count { it == '\n' }?.plus(1) ?: 1
                val padding = child.paddingTop + child.paddingBottom
                (lineHeight * lines + padding).toInt().coerceAtLeast(dp(40))
            }
            is ImageView -> {
                // 估算图片视图高度
                dp(48) // 默认图标大小
            }
            is LinearLayout -> {
                // 估算线性布局高度
                dp(60) // 默认按钮高度
            }
            else -> dp(50) // 默认高度
        }
    }

    private fun computePresetLeftTop(preset: PresetPosition, w: Int, h: Int): Point {
        val margin = dp(16)
        val left = when (preset) {
            PresetPosition.TOP_LEFT, PresetPosition.CENTER_LEFT, PresetPosition.BOTTOM_LEFT -> margin
            PresetPosition.TOP_CENTER, PresetPosition.CENTER, PresetPosition.BOTTOM_CENTER -> (width - w) / 2
            PresetPosition.TOP_RIGHT, PresetPosition.CENTER_RIGHT, PresetPosition.BOTTOM_RIGHT -> width - margin - w
        }
        val top = when (preset) {
            PresetPosition.TOP_LEFT, PresetPosition.TOP_CENTER, PresetPosition.TOP_RIGHT -> margin
            PresetPosition.CENTER_LEFT, PresetPosition.CENTER, PresetPosition.CENTER_RIGHT -> (height - h) / 2
            PresetPosition.BOTTOM_LEFT, PresetPosition.BOTTOM_CENTER, PresetPosition.BOTTOM_RIGHT -> height - margin - h
        }
        return Point(left, top)
    }

    private fun calculatePosition(position: Position): PointF {
        return when (position.type) {
            PositionType.ABSOLUTE -> PointF(position.x, position.y)

            PositionType.RELATIVE -> PointF(
                position.x * width,
                position.y * height
            )

            PositionType.RELATIVE_TO_TARGET -> {
                if (position.targetIndex < 0 || position.targetIndex >= highlights.size) {
                    PointF(position.x, position.y) // fallback to absolute
                } else {
                    val target = highlights[position.targetIndex]
                    val targetRect = targetRect(target.view, target.paddingPx)
                    PointF(
                        targetRect.centerX() + position.x,
                        targetRect.centerY() + position.y
                    )
                }
            }

            PositionType.PRESET -> {
                position.preset?.let { preset ->
                    calculatePresetPosition(preset)
                } ?: PointF(0f, 0f)
            }
        }
    }

    private fun calculatePresetPosition(preset: PresetPosition): PointF {
        val margin = dp(16).toFloat()
        return when (preset) {
            PresetPosition.TOP_LEFT -> PointF(margin, margin)
            PresetPosition.TOP_CENTER -> PointF(width / 2f, margin)
            PresetPosition.TOP_RIGHT -> PointF(width - margin, margin)
            PresetPosition.CENTER_LEFT -> PointF(margin, height / 2f)
            PresetPosition.CENTER -> PointF(width / 2f, height / 2f)
            PresetPosition.CENTER_RIGHT -> PointF(width - margin, height / 2f)
            PresetPosition.BOTTOM_LEFT -> PointF(margin, height - margin)
            PresetPosition.BOTTOM_CENTER -> PointF(width / 2f, height - margin)
            PresetPosition.BOTTOM_RIGHT -> PointF(width - margin, height - margin)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 添加淡入效果
        post {
            val fadeIn = ValueAnimator.ofFloat(0f, 1f)
            fadeIn.duration = 300
            fadeIn.addUpdateListener { animator ->
                alpha = animator.animatedValue as Float
            }
            fadeIn.start()
            onShown?.invoke()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Recreate bitmap when size changes
        if (w > 0 && h > 0) {
            maskBitmap?.recycle()
            maskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            maskCanvas = Canvas(maskBitmap!!)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (width == 0 || height == 0 || maskBitmap == null) return
        
        val bitmap = maskBitmap ?: return
        val offscreenCanvas = maskCanvas ?: return
        
        // Clear the bitmap first
        bitmap.eraseColor(Color.TRANSPARENT)
        
        // Draw the dim mask on offscreen canvas
        offscreenCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), maskPaint)
        
        // Cut holes for highlights using DST_OUT
        highlights.forEach { h ->
            val rect = targetRect(h.view, h.paddingPx)
            when (h.shape) {
                HighlightShape.RECT -> {
                    val r = h.cornerRadiusPx
                    offscreenCanvas.drawRoundRect(rect, r, r, holePaint)
                }
                HighlightShape.CIRCLE -> {
                    val cx = (rect.left + rect.right) / 2f
                    val cy = (rect.top + rect.bottom) / 2f
                    val radius = max(rect.width(), rect.height()) / 2f
                    offscreenCanvas.drawCircle(cx, cy, radius, holePaint)
                }
                HighlightShape.OVAL -> {
                    offscreenCanvas.drawOval(rect, holePaint)
                }
            }
        }
        
        // Draw the final result to the actual canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Clean up bitmap resources
        maskBitmap?.recycle()
        maskBitmap = null
        maskCanvas = null
    }

    private fun targetRect(view: View, padding: Float): RectF {
        // Use window-relative coordinates for both the target view and the overlay itself.
        // This is more robust with edge-to-edge and status bar configurations than
        // mixing screen coordinates with view-local coordinates, which could cause
        // misalignment and odd highlight shapes.
        val targetWindowLocation = IntArray(2)
        view.getLocationInWindow(targetWindowLocation)

        val selfWindowLocation = IntArray(2)
        getLocationInWindow(selfWindowLocation)

        val left = (targetWindowLocation[0] - selfWindowLocation[0]).toFloat() - padding
        val top = (targetWindowLocation[1] - selfWindowLocation[1]).toFloat() - padding
        val right = left + view.width + padding * 2
        val bottom = top + view.height + padding * 2

        return RectF(
            max(0f, left),
            max(0f, top),
            min(width.toFloat(), right),
            min(height.toFloat(), bottom)
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (dismissOnTouchAnywhere && event.action == MotionEvent.ACTION_UP) {
            // 在多步骤模式下，点击空白区域前进到下一步；单步骤模式下关闭引导
            if (steps.isNotEmpty()) {
                nextStep()
            } else {
                dismiss()
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    fun dismiss() {
        // 添加淡出动画
        val fadeOut = ValueAnimator.ofFloat(1f, 0f)
        fadeOut.duration = 250
        fadeOut.addUpdateListener { animator ->
            alpha = animator.animatedValue as Float
        }
        fadeOut.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                (parent as? ViewGroup)?.removeView(this@GuideOverlayView)
                onDismissed?.invoke()
            }
        })
        fadeOut.start()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun roundedRectDrawable(color: Int, radius: Float): Drawable {
        val r = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
        return ShapeDrawable(object : RoundRectShape(r, null, null) {})
            .apply { paint.color = color }
    }
}

