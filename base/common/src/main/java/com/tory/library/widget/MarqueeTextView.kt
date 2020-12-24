package com.tory.library.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.tory.library.R
import com.tory.library.log.LogUtils
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Author: tory
 * Date: 12/22/20
 * Email: xutao@theduapp.com
 * Description:
 */
class MarqueeTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mSpaceText: String = ""
    private var mText: String = "";
    private var mDrawText: String = ""
    private val mTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mMarqueeSpeed: Int = 10
    private var mMarqueeDelay: Int = 1500
    private var mMarqueeEnable: Boolean = true

    private var mScrollValue: Float = 0f
    private var mTotalScroll: Float = 0f

    private var mMarqueeAnimator: Animator? = null

    //是否可以滚动，即宽度是否超出
    private var mAvailableMarquee: Boolean = false
    private var mPendingMeasureText: Boolean = false

    private var mLifecycleOwner: LifecycleOwner? = null

    //是否为resume状态
    private val isLifecycleResume: Boolean
        get() {
            val owner = mLifecycleOwner
            return owner == null || owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
    private val lifecycleEventObserver = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_RESUME) {
                log("onResume")
                startMarquee()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                log("onPause")
                stopMarquee()
            }
        }
    }

    init {

        val array = context.obtainStyledAttributes(attrs, R.styleable.MarqueeTextView)

        val textColor = array.getColor(R.styleable.MarqueeTextView_marquee_textColor, Color.BLACK)
        val textSize = array.getDimensionPixelSize(R.styleable.MarqueeTextView_marquee_textSize, dp2px(12f))
        val textStyle = array.getInt(R.styleable.MarqueeTextView_marquee_textStyle, 0)
        mTextPaint.setColor(textColor)
        mTextPaint.textSize = textSize.toFloat()
        mTextPaint.style = Paint.Style.FILL
        setTextStyle(textStyle)
        mMarqueeSpeed = array.getInt(R.styleable.MarqueeTextView_marquee_speed, mMarqueeSpeed)
        mMarqueeDelay = array.getInt(R.styleable.MarqueeTextView_marquee_delay, mMarqueeDelay)
        setSpace(array.getDimensionPixelOffset(R.styleable.MarqueeTextView_marquee_space, dp2px(50f)))
        setMarqueeEnable(array.getBoolean(R.styleable.MarqueeTextView_marquee_enable, mMarqueeEnable))

        val text = array.getString(R.styleable.MarqueeTextView_marquee_text).orEmpty()
        setText(text)
        array.recycle()

        if (context is LifecycleOwner) {
            mLifecycleOwner = context
            context.lifecycle.addObserver(lifecycleEventObserver)
        }
    }

    /**
     * 设置字体样式
     * TEXT_STYLE_NORMAL, TEXT_STYLE_BOLD, TEXT_STYLE_ITALIC
     */
    fun setTextStyle(textStyle: Int) {
        val isBold = textStyle and TEXT_STYLE_BOLD != 0
        val isItalic = textStyle and TEXT_STYLE_ITALIC != 0
        val fontStyle = if (isBold && isItalic) {
            Typeface.BOLD_ITALIC
        } else if (isBold) {
            Typeface.BOLD
        } else if (isItalic) {
            Typeface.ITALIC
        } else {
            Typeface.NORMAL
        }
        mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, fontStyle))
    }

    fun setSpace(space: Int) {
        val spaceWidth = mTextPaint.measureText(" ")
        if (spaceWidth <= 0) return
        val count = (space / spaceWidth.toFloat()).roundToInt()
        val sb = StringBuilder()
        repeat(count){ sb.append(" ")}
        mSpaceText = sb.toString()
    }

    fun setMarqueeEnable(enable: Boolean) {
        if (mMarqueeEnable == enable) return
        mMarqueeEnable = enable
        postMarqueeText()
    }


    /**
     * 设置文字
     */
    fun setText(text: String) {
        if (mText == text) {
            return
        }
        mText = text
        mDrawText = text

        log("setText text:$text")
        postMarqueeText()
    }

    private fun reset() {
        mScrollValue = 0f
        mTotalScroll = 0f
        stopMarquee()
    }

    private fun postMarqueeText() {
        if (width > 0) {
            mPendingMeasureText = false
            measureAndMarqueeText()
        } else {
            mPendingMeasureText = true
            reset()
        }
        invalidate()
    }

    private fun measureAndMarqueeText() {
        reset()

        val textWidth = mTextPaint.measureText(mText)
        val contentWidth = width - paddingLeft - paddingRight
        mAvailableMarquee = textWidth > contentWidth
        log("measureAndMarqueeText mAvailableMarquee:$mAvailableMarquee, " +
                "textWidth:$textWidth, contentWidth:$contentWidth")
        if (!mAvailableMarquee) return
        if (!mMarqueeEnable) {
            //不可滚动，添加省略号
            val ellipsizeText = "..."
            val sb = StringBuilder()
            val text = mText
            var index = 0
            while (index < text.length) {
                sb.append(text.get(index))
                sb.append(ellipsizeText)
                val w = mTextPaint.measureText(sb, 0, sb.length)
                if (w > contentWidth) {
                    break
                }
                sb.delete(sb.length - ellipsizeText.length, sb.length)
                index++
            }
            mDrawText = text.substring(0, index) + ellipsizeText
            return
        } else {
            mDrawText = mText + mSpaceText + mText
            mTotalScroll = mTextPaint.measureText(mDrawText, 0, mText.length + mSpaceText.length)
            log("measureAndMarqueeText mTotalScroll:$mTotalScroll")
            startMarquee()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val fm = mTextPaint.fontMetrics
        val height = fm.bottom - fm.top
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                height.roundToInt() + paddingTop + paddingBottom, MeasureSpec.EXACTLY))
    }

    private fun getTextWidth(): Int {
        return mTextPaint.measureText(mText).roundToInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mPendingMeasureText) {
            mPendingMeasureText = false
            measureAndMarqueeText()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return


        val x = paddingLeft.toFloat()
        val y = paddingTop + Math.abs(mTextPaint.fontMetrics.top)

        val drawText = mDrawText

        canvas.save()
        //对画布进行裁剪，不超过padding位置
        canvas.clipRect(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
        //设置滚动距离
        canvas.translate(-mScrollValue, 0f)
        canvas.drawText(drawText, x, y, mTextPaint)
        canvas.restore()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (isVisible) {
            startMarquee()
        } else {
            stopMarquee()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startMarquee()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopMarquee()
    }


    private fun startMarquee() {
        if (!mAvailableMarquee || !mMarqueeEnable || !isVisible
                || !isAttachedToWindow || !isLifecycleResume) {
            return
        }
        stopMarquee()

        log("startMarquee mScrollValue:$mScrollValue, mTotalScroll:$mTotalScroll")
        val duration = mMarqueeSpeed * (mTotalScroll - mScrollValue)
        val animator = ValueAnimator.ofFloat(mScrollValue, mTotalScroll)
        animator.setDuration(duration.roundToLong())
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener {
            mScrollValue = it.animatedValue as Float
            //log("marquee: mScrollValue:$mScrollValue")
            invalidate()
        }

        val isCancel = AtomicBoolean(false)
        animator.addListener(onCancel = {
            isCancel.set(true)
        }, onEnd = {
            if (!isCancel.get()) {
                postMarqueeText()
            } else {
                log("startMarquee isCanceled")
            }
        })
        if (mScrollValue == 0f) {
            animator.startDelay = mMarqueeDelay.toLong()
        }

        animator.start()
        mMarqueeAnimator = animator
    }

    private fun stopMarquee() {
        log("stopMarquee")
        mMarqueeAnimator?.cancel()
        mMarqueeAnimator = null
    }

    private fun log(msg: String) {
        LogUtils.d("MarqueeTextView", "$msg")
    }

    fun attachLifecycle(owner: LifecycleOwner) {
        if (mLifecycleOwner == owner) {
            return
        }
        mLifecycleOwner?.lifecycle?.removeObserver(lifecycleEventObserver)
        mLifecycleOwner = owner
        owner.lifecycle.addObserver(lifecycleEventObserver)
    }

    fun dp2px(dpValue: Float): Int {
        val scale = resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun isMarquee(): Boolean {
        return mMarqueeAnimator?.isRunning == true
    }

    companion object {
        const val TEXT_STYLE_NORMAL = 0
        const val TEXT_STYLE_BOLD = 1
        const val TEXT_STYLE_ITALIC = 2
    }
}