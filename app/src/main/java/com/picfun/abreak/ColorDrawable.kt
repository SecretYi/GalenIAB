package com.picfun.abreak

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * @author Secret
 * @since 2019/10/8
 */
class ColorDrawable:Drawable(){

    private var circlePaint:Paint = Paint()

    init {
        circlePaint.style = Paint.Style.FILL
        circlePaint.color = Color.RED
        circlePaint.strokeWidth = 5f
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(bounds.exactCenterX(),bounds.exactCenterY(),bounds.width().div(2f),circlePaint)
        circlePaint.style = Paint.Style.FILL_AND_STROKE

        canvas.drawCircle(bounds.exactCenterX(),bounds.exactCenterY(),bounds.width().div(2f),circlePaint)
    }

    override fun setAlpha(alpha: Int) {
        circlePaint.alpha = alpha
        invalidateSelf()
    }

    override fun getOpacity() = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
        circlePaint.colorFilter = colorFilter
        invalidateSelf()
    }

}

