package com.jarvis.demo.presentation.utlis

import android.view.View
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import androidx.annotation.ColorInt

fun View.setRoundedBgM3(@ColorInt color: Int, radiusPx: Float) {
    val shape = ShapeAppearanceModel.Builder()
        .setAllCorners(CornerFamily.ROUNDED, radiusPx)
        .build()
    val bg = MaterialShapeDrawable(shape).apply {
        fillColor = android.content.res.ColorStateList.valueOf(color)
    }
    background = bg
}

fun View.setRoundedBgM3(
    @ColorInt color: Int,
    topStart: Float, topEnd: Float, bottomEnd: Float, bottomStart: Float
) {
    val shape = ShapeAppearanceModel.Builder()
        .setTopLeftCorner(CornerFamily.ROUNDED, topStart)
        .setTopRightCorner(CornerFamily.ROUNDED, topEnd)
        .setBottomRightCorner(CornerFamily.ROUNDED, bottomEnd)
        .setBottomLeftCorner(CornerFamily.ROUNDED, bottomStart)
        .build()
    background = MaterialShapeDrawable(shape).apply {
        fillColor = android.content.res.ColorStateList.valueOf(color)
    }
}