package game.utils

import com.cozmicgames.utils.Color
import kotlin.math.max
import kotlin.math.min

fun Color.lighter(amount: Float): Color {
    val (h, s, v) = toHSV()
    return Color().fromHSV(h, s, min(v + amount, 1.0f))
}

fun Color.darker(amount: Float): Color {
    val (h, s, v) = toHSV()
    return Color().fromHSV(h, s, max(v - amount, 0.0f))
}
