package com.tzf.guidelayer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tzf.guidelayer.guide.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 等待视图布局完成后显示引导
        findViewById<View>(R.id.main).post {
            // 演示多步骤引导
            showMultiStepGuide()

            // 演示单步骤引导（原有功能，已注释）
            // showSingleStepGuide()

            // 演示自定义按钮样式（已注释）
//             showCustomButtonStyleGuide()
        }
    }

    /**
     * 演示自定义按钮样式（单步骤模式）
     */
    private fun showCustomButtonStyleGuide() {
        val tvHello = findViewById<View>(R.id.tvHello)

        GuideLayer.with(this)
            .highlight(tvHello, shape = HighlightShape.CIRCLE, paddingDp = 20f)
            .overlayImage(R.drawable.ic_arrow_curve, Position(PositionType.RELATIVE_TO_TARGET, 0f, 250f))
            .dismissOnTouchAnywhere(false)
            .confirmButton(show = true, text = "体验自定义按钮")
            .confirmButtonPosition(Position(PositionType.RELATIVE_TO_TARGET, -100f, 400f))
            .confirmButtonStyle(ButtonStyle(
                backgroundColor = Color.parseColor("#FF6B35"), // 橙色背景
                textColor = Color.WHITE,                        // 白色文字
                textSizeSp = 18f,                              // 18sp文字大小
                cornerRadiusDp = 25f                           // 25dp圆角
            ))
            .callbacks(
                onShown = { Log.d("GuideLayer", "自定义按钮样式引导已显示") },
                onDismissed = { Log.d("GuideLayer", "自定义按钮样式引导已关闭") }
            )
            .show(delayMs = 300)
    }

    /**
     * 演示单步骤引导（向后兼容的原有用法）
     */
    private fun showSingleStepGuide() {
        val hello = findViewById<View>(R.id.tvHello)

        GuideLayer.with(this)
            .highlight(hello, shape = HighlightShape.CIRCLE, paddingDp = 12f)
            // 使用新的位置指定系统 - 相对位置（屏幕中心偏右上方）
            .overlayImage(R.drawable.ic_arrow_curve, Position(PositionType.RELATIVE_TO_TARGET, 0f, 250f))
            .dismissOnTouchAnywhere(true)           // 默认 true，可改为 false 只允许点"我知道了"
            .confirmButton(show = true, text = "我知道了")
            // 设置确认按钮位置为顶部居中
            .confirmButtonPosition(Position(PositionType.RELATIVE_TO_TARGET, -100f, 400f))
            .callbacks(
                onShown = { Log.d("GuideLayer", "单步骤引导已显示") },
                onDismissed = { Log.d("GuideLayer", "单步骤引导已关闭") }
            )
            .show(delayMs = 300)                     // 支持延迟出现
    }

    /**
     * 演示多步骤引导（展示新功能）
     */
    private fun showMultiStepGuide() {
        val tvTitle = findViewById<View>(R.id.tvTitle)
        val tvHello = findViewById<View>(R.id.tvHello)
        val btnAction = findViewById<View>(R.id.btnAction)

        GuideLayer.with(this)
            // 第一步：高亮标题，使用文本说明
            .addStep()
                .highlight(tvTitle, shape = HighlightShape.RECT, paddingDp = 16f, cornerRadiusDp = 12f)
                .text("欢迎标题", "这是页面的主标题，显示欢迎信息")  // 新功能：文本说明
                .overlayImage(R.drawable.ic_arrow_curve, Position(PositionType.RELATIVE_TO_TARGET, 0f, 250f))
                .textPosition(Position(PositionType.RELATIVE_TO_TARGET, 0f, 500f))
                .textMaxWidthRatio(0.7f) // 设置最大宽度为屏幕的70%
                .dismissOnTouchAnywhere(true)      // 禁止点击任意位置关闭
                .confirmButton(show = false)         // 不显示确认按钮
                .showNextButton(true)                // 显示"下一步"按钮
                .showStepIndicator(true)             // 显示步骤指示器 (1/3)
                .showSkipButton(true)                // 新功能：显示"跳过"按钮
                .callbacks(
                    onShown = { Log.d("GuideLayer", "步骤1: 标题引导已显示") },
                    onDismissed = { Log.d("GuideLayer", "步骤1: 标题引导已关闭") }
                )
            // 第二步：高亮中间文本，使用文本说明
            .nextStep()
                .highlight(tvHello, shape = HighlightShape.CIRCLE, paddingDp = 20f)
                .title("核心内容")                     // 新功能：只显示标题
                .description("这里显示主要的欢迎信息和内容")  // 新功能：只显示描述
                .textPosition(Position(PositionType.RELATIVE_TO_TARGET, 0f, 550f))
                .dismissOnTouchAnywhere(true)
                .overlayImage(R.drawable.ic_arrow_curve, Position(PositionType.RELATIVE_TO_TARGET, 0f, 300f))
                .confirmButton(show = false)
                .showPreviousButton(true)            // 显示"上一步"按钮
                .showNextButton(true)                // 显示"下一步"按钮
                .showStepIndicator(true)             // 显示步骤指示器 (2/3)
                .showSkipButton(true)                // 显示"跳过"按钮
                .callbacks(
                    onShown = { Log.d("GuideLayer", "步骤2: Hello World 引导已显示") },
                    onDismissed = { Log.d("GuideLayer", "步骤2: Hello World 引导已关闭") }
                )
            // 第三步：高亮按钮，使用文本说明
            .nextStep()
                .highlight(btnAction, shape = HighlightShape.OVAL, paddingDp = 12f)
                .text("操作按钮", "点击这个按钮执行相应操作")
                .textPosition(Position(PositionType.RELATIVE_TO_TARGET, 0f, -500f))
                .overlayImage(R.drawable.ic_arrow_curve, Position(PositionType.RELATIVE_TO_TARGET, 0f, -200f))
                .dismissOnTouchAnywhere(false)
                .confirmButton(show = true, text = "完成")  // 显示"完成"按钮（改进的UI样式）
                .showPreviousButton(true)                  // 显示"上一步"按钮
                .showNextButton(false)                     // 不显示"下一步"按钮
                .showStepIndicator(true)                   // 显示步骤指示器 (3/3)
                .showSkipButton(true)                      // 显示"跳过"按钮
                .confirmButtonStyle(ButtonStyle(
                    backgroundColor = Color.parseColor("#4CAF50"), // 绿色背景
                    textColor = Color.WHITE,
                    textSizeSp = 16f,
                    cornerRadiusDp = 20f
                ))
                .primaryButtonStyle(ButtonStyle(
                    backgroundColor = Color.parseColor("#2196F3"), // 蓝色背景
                    textColor = Color.WHITE,
                    cornerRadiusDp = 12f
                ))
                .secondaryButtonStyle(ButtonStyle(
                    backgroundColor = Color.TRANSPARENT,  // 透明背景
                    textColor = Color.parseColor("#666666"),
                    strokeColor = Color.parseColor("#CCCCCC"),
                    strokeWidthDp = 1.5f,
                    cornerRadiusDp = 12f
                ))
                .skipButtonStyle(ButtonStyle(
                    backgroundColor = Color.parseColor("#80000000"), // 半透明黑色
                    textColor = Color.parseColor("#CCCCCC"),
                    cornerRadiusDp = 20f
                ))
                .callbacks(
                    onShown = { Log.d("GuideLayer", "步骤3: 按钮引导已显示") },
                    onDismissed = { Log.d("GuideLayer", "步骤3: 按钮引导已关闭") }
                )
            // 完成所有步骤配置
            .endSteps()
            // 设置步骤改变回调
            .onStepChanged { currentStep, totalSteps ->
                Log.d("GuideLayer", "当前步骤: $currentStep/$totalSteps")
            }
            // 新功能：跳过所有步骤回调
            .onSkipAll {
                Log.d("GuideLayer", "用户跳过了引导")
            }
            // 可选：从指定步骤开始（默认从第0步开始）
            // .startFromStep(0)
            // 显示引导（带淡入动画）
            .show(delayMs = 500)
    }
}