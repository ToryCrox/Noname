package com.tory.noname.interpolator

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.tory.library.base.BaseActivity
import com.tory.noname.R
import java.lang.StringBuilder
import java.util.*
import kotlin.math.abs

class InterpolatorTestActivity: BaseActivity() {

    override fun getLayoutId(): Int = R.layout.activity_interpolator_test

    override fun initView(savedInstanceState: Bundle?) {

        val interpolatorHelper = TableInterpolatorHelper()
        findViewById<View>(R.id.btn1).setOnClickListener {
            val count = 1000000
            val list = List(count) {
                val input = Random().nextFloat()
                val result = interpolatorHelper.interpolator.getInterpolation(input)
                val calInput = interpolatorHelper.getInputByResult(result)
                val diff = calInput - input
                DtoInfo(input, result, calInput, diff)
            }
            val diffSum = list.sumByDouble { abs(it.diff).toDouble() }

            Log.d("TestTag", "diff: ${(diffSum / count) * 100}")
            val maxDto = list.maxBy { abs(it.diff) }
            Log.d("TestTag", "maxDto: $maxDto")
        }

        findViewById<View>(R.id.btn2).setOnClickListener {
            val interpolator = FastOutSlowInInterpolator()
            val interpolator2 = PathInterpolatorCompat.create(0.4f, 0f, 0.2f, 1f)
            val count = 1000000
            val list = List(count) {
                val input = Random().nextFloat()
                val result = interpolator.getInterpolation(input)
                val result2 = interpolator2.getInterpolation(input)
                val diff = result2 - result
                Dto2Info(input, result, result2, diff)
            }

            val diffSum = list.sumByDouble { abs(it.diff).toDouble() }

            Log.d("TestTag", "interpolator 区别 diff: ${(diffSum / count) * 100}")
            val maxDto = list.maxBy { abs(it.diff) }
            Log.d("TestTag", "interpolator maxDto: $maxDto")
        }

        val interpolator = PathInterpolatorCompat.create(0.42f, 0f, 0.58f, 1f)
        val sb = StringBuilder()
        repeat(201) {
            val input = it / 200f
            val result = interpolator.getInterpolation(input)
            sb.append("${"%.6f".format(result)}f, ")
            if (it % 10 == 9) {
                sb.append("\n")
            }
        }

        Log.d("TTTTTT0", sb.toString())
    }



    data class DtoInfo(
        val input: Float,
        val result: Float,
        val calInput: Float,
        val diff: Float
    )

    data class Dto2Info(
        val input: Float,
        val result: Float,
        val result2: Float,
        val diff: Float
    )
}