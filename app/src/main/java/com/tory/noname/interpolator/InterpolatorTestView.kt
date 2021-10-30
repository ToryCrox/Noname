package com.tory.noname.interpolator

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.tory.library.extension.dp
import com.tory.noname.utils.MaterialColor
import android.graphics.DashPathEffect

import android.graphics.PathEffect
import android.view.animation.Interpolator
import androidx.interpolator.view.animation.*

class InterpolatorTestView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    private val interpolatorInfoList = mutableListOf<InterpolatorInfo>()

    private val zeroPoint = PointF()
    private var xLength: Float = 0f
    private var yLength: Float = 0f

    private val paint =  Paint()
    private val dashPathEffect: PathEffect = DashPathEffect(floatArrayOf(2.dp().toFloat(), 2.dp().toFloat()), 1f)

    private val dotCount = 200

    init {

        val interpolatorList: Array<Interpolator> = arrayOf(
            FastOutSlowInInterpolator(),
            //PathInterpolatorCompat.create(0.4f, 0f, 0.2f, 1f),
            FastOutLinearInInterpolator(),
            LinearOutSlowInInterpolator()
            //PathInterpolatorCompat.create(0.42f, 0f, 0.58f, 1f)
        )

        for ((index, interpolator) in interpolatorList.withIndex()) {
            val dots = List(dotCount + 1) {
                val input = it.toFloat() / dotCount
                val result = interpolator.getInterpolation(input)
                PointF(input, result)
            }

            val info = InterpolatorInfo(dots,
                MaterialColor.values()[index * 2].color,
                interpolator.javaClass.simpleName)
            interpolatorInfoList.add(info)
        }

        paint.textSize = 12.dp().toFloat()
        paint.isAntiAlias = true

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        zeroPoint.x = 10.dp().toFloat()
        zeroPoint.y = (h - 10.dp()).toFloat()
        xLength = w - 10.dp() - zeroPoint.x
        yLength = zeroPoint.y - 10.dp()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return
        drawGrid(canvas)
        drawCurves(canvas)

        drawInfos(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        paint.setColor(MaterialColor.grey.color)
        canvas.drawLine(zeroPoint.x, zeroPoint.y, zeroPoint.x + xLength, zeroPoint.y, paint)
        canvas.drawLine(zeroPoint.x, zeroPoint.y, zeroPoint.x, zeroPoint.y - yLength, paint)
        paint.setPathEffect(dashPathEffect)
        canvas.drawLine(zeroPoint.x, zeroPoint.y - yLength, zeroPoint.x + xLength, zeroPoint.y - yLength, paint)
        canvas.drawLine(zeroPoint.x + xLength, zeroPoint.y, zeroPoint.x + xLength, zeroPoint.y - yLength, paint)
        paint.setPathEffect(null)
    }

    private fun drawCurves(canvas: Canvas) {
        for (info in interpolatorInfoList) {
            paint.setColor(info.color)

            var lastDot = info.dots.first()
            for ((index, dot) in info.dots.withIndex()) {
                if (index == 0){
                    continue
                }
                val startX = zeroPoint.x + lastDot.x * xLength
                val startY = zeroPoint.y - lastDot.y * yLength
                val endX = zeroPoint.x + dot.x * xLength
                val endY = zeroPoint.y - dot.y * yLength
                canvas.drawLine(startX, startY, endX, endY, paint)
                lastDot = dot
            }
        }
    }


    private fun drawInfos(canvas: Canvas) {

        val left = zeroPoint.x + 10.dp()
        var top = 10.dp().toFloat()

        val fm = paint.fontMetrics
        val textHeight = fm.bottom - fm.top
        top += textHeight
        for (info in interpolatorInfoList) {
            paint.setColor(info.color)
            canvas.drawText(info.name, left, top, paint)
            top += textHeight * 1.2f
        }
    }


    class InterpolatorInfo(
        val dots: List<PointF>,
        val color: Int,
        val name: String
    )

}