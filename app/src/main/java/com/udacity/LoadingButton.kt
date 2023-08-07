package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var textHeight = resources.getDimension(R.dimen.default_text_size)
    private var buttonText: String
    private var buttonBackgroundColor = resources.getColor(R.color.colorPrimary)
    private var buttonColorWhileLoading = resources.getColor(R.color.colorPrimaryDark)
    private var loadingCircleColor = resources.getColor(R.color.colorAccent)
    private var loadingProgressBar: Float = 0.0f
    private var loadingProgressCircle: Float = 0.0f

    private var valueAnimator = ValueAnimator()

    var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Loading -> {
                buttonText = resources.getString(R.string.button_loading)
                valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                valueAnimator.duration = 5000
                valueAnimator.addUpdateListener {
                    loadingProgressBar = valueAnimator.animatedFraction
                    loadingProgressCircle = valueAnimator.animatedValue as Float * 360f
                    invalidate() // don't fully understand why it needs to be here but if I put it outside of the addUpdateListener it doesn't animate correctly
                }
                valueAnimator.disableViewDuringAnimation(this) // Disable the button during the animation
                valueAnimator.repeatCount = 1000
                valueAnimator.start()

                // Testing only: Transition to "ButtonState.Completed" before having the downloader implemented
//                Handler().postDelayed({
//                    buttonState = ButtonState.Completed
//                }, valueAnimator.duration)

            }
            ButtonState.Completed -> {

                buttonBackgroundColor = resources.getColor(R.color.colorPrimary)
                buttonText = resources.getString(R.string.button_name)
                requestLayout()
                invalidate()
                loadingProgressCircle = 0.0f
                loadingProgressBar = 0.0f
                valueAnimator.enableViewAfterAnimation(this)
                valueAnimator.cancel()

            }

            ButtonState.Clicked -> {
                // What to do? Maybe not necessary?
                // Not clear from the initial Spec
            }

        }


    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)

    }


    init {
        isClickable = true
        buttonText = resources.getString(R.string.button_name)
        buttonBackgroundColor = resources.getColor(R.color.colorPrimary)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton)

        buttonColorWhileLoading = typedArray.getColor(
            R.styleable.LoadingButton_buttonColorWhileLoading,
            resources.getColor(R.color.colorPrimaryDark)
        )

        loadingCircleColor = typedArray.getColor(
            R.styleable.LoadingButton_colorOfLoadingCircle,
            resources.getColor(R.color.colorAccent)
        )
        typedArray.recycle()
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawColor(buttonBackgroundColor)
        drawProgressBar(canvas)
        drawButtonText(canvas)
        drawProgressCircle(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    private fun drawButtonText(canvas: Canvas?) {
        paint.color = resources.getColor(R.color.white)
        canvas?.drawText(
            buttonText,
            (widthSize / 2).toFloat(),
            ((heightSize + textHeight / 2) / 2),
            paint
        )
    }


    private fun drawProgressBar(canvas: Canvas?) {
        paint.color = buttonColorWhileLoading
        val progressRect = Rect(0, 0, (loadingProgressBar * widthSize).toInt(), heightSize)
        canvas?.drawRect(progressRect, paint)
    }

    private fun drawProgressCircle(canvas: Canvas?){
        paint.color = loadingCircleColor
        val centerX = (widthSize / 2).toFloat() + paint.measureText(buttonText)
        val centerY = heightSize / 2f
        val radius = minOf(widthSize, heightSize) / 4f

        val startAngle = 0f
        val sweepAngle = loadingProgressCircle

        canvas?.drawArc(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            startAngle,
            sweepAngle,
            true,
            paint
        )

    }

    private fun ValueAnimator.disableViewDuringAnimation(view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                view.isEnabled = false
            }
        })
    }

    private fun ValueAnimator.enableViewAfterAnimation(view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.isEnabled = true
            }
        })
    }


}