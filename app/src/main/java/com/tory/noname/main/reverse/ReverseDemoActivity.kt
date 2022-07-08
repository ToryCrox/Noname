package com.tory.noname.main.reverse

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import com.tory.library.base.BaseActivity
import com.tory.library.component.base.*
import com.tory.library.extension.*
import com.tory.noname.R
import kotlinx.android.synthetic.main.activity_reverse_demo.*

/**
 * - Author: tory
 * - Date: 2022/6/23
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
class ReverseDemoActivity : BaseActivity() {

    override fun getLayoutId(): Int = R.layout.activity_reverse_demo

    val listAdapter = NormalModuleAdapter()

    override fun initView(savedInstanceState: Bundle?) {

        val itemSpace = ItemSpace(edgeH = 20.dp(), spaceV = 10.dp(), spaceH = 10.dp())

        listAdapter.register(gridSize = 2, itemSpace = itemSpace) {
            ReverseCardView(this, onItemClick = { model, position ->
                showBackView(model, position)
            })
        }

        listAdapter.register(gridSize = 2, itemSpace = itemSpace){
            ReversBackView(this, onItemClick = { model, position ->
                showFrontView(model, position)
            })
        }


        recyclerView.layoutManager = listAdapter.getGridLayoutManager(this)
        recyclerView.adapter = listAdapter
        val cameraDistance = resources.displayMetrics.density * 16000
        recyclerView.itemAnimator = ReverseItemAnimator(cameraDistance)


        val list = List(10) {
            ReverseCardModel()
        }
        listAdapter.setItems(list)

//        reverseCardView.clickThrottle
//        {
//            Toast.makeText(this, "点击正面", Toast.LENGTH_SHORT).show()
//        }
//        btn.clickThrottle
//        {
//            reverseCardView.doAction(HomeReverseCardModel())
//        }
    }

    private fun showFrontView(model: ReverseBackModel, position: Int) {
        listAdapter.updateItem(position, model.cardModel)
    }

    private fun showBackView(model: ReverseCardModel, position: Int) {
        listAdapter.updateItem(position, ReverseBackModel(model))
    }


}

class ReverseCardModel

class ReverseCardView constructor(
    context: Context, attrs: AttributeSet? = null, val onItemClick: OnViewItemClick<ReverseCardModel>
) : AbsModuleView<ReverseCardModel>(context, attrs) {

    val reverseSubView = ReverseSubModuleView(this)

    init {

        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, 150.dp())
        val view = View(context)
        view.background = GradientDrawable().also {
            it.setColor(Color.GRAY)
            it.cornerRadius = 2.dp().toFloat()
        }
        addViewKt(view, widthFull = true, heightFull = true)
        val textView = AppCompatTextView(context)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        textView.setTextColor(Color.WHITE)
        textView.text = "点击反转卡片"
        addViewKt(textView, gravity = Gravity.CENTER)

        addSubModuleViews(reverseSubView)

        clickThrottle {
            val model = data ?: return@clickThrottle
            onItemClick(model, layoutPosition)
        }
    }

    override fun onChanged(model: ReverseCardModel) {
        super.onChanged(model)
    }

    fun doAction(model: HomeReverseCardModel) {
        reverseSubView.doToggleReverse()
    }
}

class ReverseBackModel(
    val cardModel: ReverseCardModel
)

class ReversBackView constructor(
    context: Context, attrs: AttributeSet? = null,
    val onItemClick: OnViewItemClick<ReverseBackModel>
) : AbsModuleView<ReverseBackModel>(context, attrs) {

    val reverseBtn = ImageView(context)
    val backView = View(context)

    init {
        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, 150.dp())
        backView.background = GradientDrawable().also {
            it.setColor(Color.CYAN)
            it.cornerRadius = 2.dp().toFloat()
        }
        addViewKt(backView, widthFull = true, heightFull = true)
        val textView = AppCompatTextView(context)
        textView.text = "测试"
        addViewKt(textView, gravity = Gravity.CENTER)
        reverseBtn.setImageResource(R.drawable.ic_mall_feed_item_reverse_action)
        addViewKt(reverseBtn)
        clickThrottle {
            val model = data ?: return@clickThrottle
            onItemClick(model, layoutPosition)
        }
    }

}

class ReverseSubModuleView<T>(val containerView: AbsModuleView<T>) : ISubModuleView<T> {

    companion object {
        const val STATE_NONE = 0
        const val STATE_FRONT = 1
        const val STATE_BACK = 2
    }

    val context = containerView.context

    override val parent: IModuleView<T>
        get() = containerView

    private var reverseHolder: ReverseHolder? = null

    private var reverseModel: HomeReverseCardModel? = null

    override fun update(model: T) {

    }

    var isReversed = false

    fun doToggleReverse() {
        val model = HomeReverseCardModel()
        val holder = reverseHolder ?: createReverseView(model).also {
            reverseHolder = it
        }
        if (isReversed) {
            animateFront(holder)
        } else {
            animateBack(holder)
        }
        isReversed = !isReversed

    }

    private fun createReverseView(model: HomeReverseCardModel): ReverseHolder {
        val holder = ReverseHolder(containerView)
        holder.bind(model)
        holder.frontView.frontImage.setImageBitmap(containerView.drawToBitmap())
        holder.backView.clickThrottle {
            Toast.makeText(context, "点击反面", Toast.LENGTH_SHORT).show()
        }
        return holder
    }

    private fun animateBack(holder: ReverseHolder) {
        holder.maskShow()
        holder.frontView.frontImage.isVisible = true
        holder.backView.isVisible = true
        val cameraDistance = context.resources.displayMetrics.density * 16000
        holder.frontView.cameraDistance = cameraDistance
        holder.backView.cameraDistance = cameraDistance

        val a1 = createAnimator(holder.frontView, 0f, 180f, 1f, 0f)
        val a2 = createAnimator(holder.backView, -180f, 0f, 0f, 1f)
        a1.setDuration(500L)
        a2.setDuration(500L)
        val animator = AnimatorSet()
        animator.playTogether(a1, a2)
        animator.doOnEnd {
            holder.maskHide()
        }
        animator.start()
    }

    private fun animateFront(holder: ReverseHolder) {
        holder.maskShow()
        holder.frontView.frontImage.isVisible = true
        holder.backView.isVisible = true
        val cameraDistance = context.resources.displayMetrics.density * 16000
        holder.frontView.cameraDistance = cameraDistance
        holder.backView.cameraDistance = cameraDistance

        val a1 = createAnimator(holder.backView, 0f, 180f, 1f, 0f)
        val a2 = createAnimator(holder.frontView, -180f, 0f, 0f, 1f)
        a1.setDuration(500L)
        a2.setDuration(500L)
        val animator = AnimatorSet()
        animator.playTogether(a1, a2)
        animator.doOnEnd {
            holder.frontView.frontImage.isVisible = false
            holder.backView.isVisible = false
            holder.maskHide()
        }
        animator.start()
    }

    private fun createAnimator(
        target: View,
        startRotationY: Float, endRotationY: Float,
        startAlpha: Float, endAlpha: Float
    ): ObjectAnimator {
        val holder1 = PropertyValuesHolder.ofFloat(View.ROTATION_Y, startRotationY, endRotationY)
        val holder2 = PropertyValuesHolder.ofFloat(View.ALPHA, startAlpha, endAlpha)
        return ObjectAnimator.ofPropertyValuesHolder(target, holder1, holder2)
    }

    class ReverseHolder(val parent: FrameLayout) {
        val context = parent.context
        val frontView = FrontReverseView(context)
        val backView = BackReverseView(context)
        val maskView: View = View(context)

        init {
            maskView.setBackgroundColor(Color.WHITE)
            parent.addViewKt(maskView, widthFull = true, heightFull = true)
            parent.addViewKt(frontView, widthFull = true, heightFull = true)
            parent.addViewKt(backView, widthFull = true, heightFull = true)
        }

        fun maskShow() {
            maskView.isVisible = true
        }

        fun maskHide() {
            maskView.isVisible = false
        }

        fun bind(model: HomeReverseCardModel) {
            // todo: fill model
        }

        fun recycle() {
            parent.removeView(frontView)
            parent.removeView(backView)
        }
    }

    class FrontReverseView constructor(
        context: Context, attrs: AttributeSet? = null
    ) : FrameLayout(context, attrs) {

        val reverseBtn = ImageView(context)
        val frontImage = ImageView(context)

        init {
            reverseBtn.adjustViewBounds = true
            addViewKt(frontImage, widthFull = true, heightFull = true)
            reverseBtn.setImageResource(R.drawable.ic_mall_feed_item_reverse_action)
            addViewKt(reverseBtn)
        }

    }

    class BackReverseView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
    ) : FrameLayout(context, attrs) {

        val reverseBtn = ImageView(context)
        val backView = View(context)

        init {
            backView.background = GradientDrawable().also {
                it.setColor(Color.CYAN)
                it.cornerRadius = 2.dp().toFloat()
            }
            addViewKt(backView, widthFull = true, heightFull = true)
            val textView = AppCompatTextView(context)
            textView.text = "测试"
            addViewKt(textView, gravity = Gravity.CENTER)
            reverseBtn.setImageResource(R.drawable.ic_mall_feed_item_reverse_action)
            addViewKt(reverseBtn)
        }
    }

}

data class HomeReverseCardModel(
    val title: String? = null,
    val dynamicCardId: String? = null,
    val iconUrl: String? = null,
    val parentCspuId: String? = null,
    val parentPosition: Int = 0
)