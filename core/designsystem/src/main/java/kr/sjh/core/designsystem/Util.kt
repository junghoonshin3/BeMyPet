package kr.sjh.core.designsystem

import android.content.Context
import android.util.TypedValue
import androidx.compose.ui.unit.Dp

fun Dp.convertDpToPx(context: Context): Int {
    val metrics = context.resources.displayMetrics;
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics).toInt()
}