package engine.graphics.ui.widgets

import com.cozmicgames.utils.maths.Corners
import com.cozmicgames.utils.maths.convertRange
import engine.graphics.ui.*
import kotlin.math.max
import kotlin.math.min

/**
 * Adds a plot to the screen.
 * A plot is a graph that can be used to display data.
 *
 * @param values The values to plot.
 * @param type The type of plot to display.
 * @param width The width of the plot. Defaults to [style.elementSize] * 10.
 * @param height The height of the plot. Defaults to the same as [width].
 * @param min The minimum value of the plot. If null, the minimum value in [values] will be used. Defaults to null.
 * @param max The maximum value of the plot. If null, the maximum value in [values] will be used. Defaults to null.
 *
 * @see [PlotType].
 */
fun GUI.plot(values: Iterable<Float>, type: GUI.PlotType, width: Float = style.elementSize * 10.0f, height: Float = width, min: Float? = null, max: Float? = null): GUIElement {
    val (x, y) = getLastElement()

    var count = 0
    var minValue = Float.MAX_VALUE
    var maxValue = -Float.MAX_VALUE

    values.forEach {
        minValue = min(minValue, it)
        maxValue = max(maxValue, it)
        count++
    }

    val usedMin = min ?: minValue
    val usedMax = max ?: maxValue

    currentCommandList.drawRectFilled(x, y, width, height, style.roundedCorners, style.cornerRounding, style.normalColor)

    val slotWidth = (width - style.elementPadding * 2.0f) / count
    val slotMaxHeight = height - style.elementPadding * 2.0f
    var slotX = x + style.elementPadding

    fun getSlotHeight(value: Float) = value.convertRange(usedMin, usedMax, 0.0f, 1.0f) * slotMaxHeight

    when (type) {
        GUI.PlotType.POINTS -> {
            slotX += slotWidth * 0.5f
            values.forEach {
                val slotY = y + style.elementPadding + (slotMaxHeight - getSlotHeight(it))
                currentCommandList.drawCircleFilled(slotX, slotY, style.elementSize * 0.33f, style.highlightColor)
                slotX += slotWidth
            }
        }
        GUI.PlotType.BARS -> {
            values.forEach {
                val slotHeight = getSlotHeight(it)
                currentCommandList.drawRectFilled(slotX + 1.0f, y + style.elementPadding + slotMaxHeight - slotHeight, slotWidth - 2.0f, slotHeight, Corners.NONE, 0.0f, style.highlightColor)
                slotX += slotWidth
            }
        }
        GUI.PlotType.LINES -> {
            slotX += slotWidth * 0.5f
            var lastValue = 0.0f
            var isFirst = true
            values.forEach {
                val slotY = y + style.elementPadding + (slotMaxHeight - getSlotHeight(it))

                if (isFirst)
                    isFirst = false
                else {
                    val lastSlotX = slotX - slotWidth
                    val lastSlotY = y + style.elementPadding + (slotMaxHeight - getSlotHeight(lastValue))
                    currentCommandList.drawLine(lastSlotX, lastSlotY, slotX, slotY, style.elementSize * 0.25f, style.highlightColor)
                }

                currentCommandList.drawCircleFilled(slotX, slotY, style.elementSize * 0.33f, style.highlightColor)
                slotX += slotWidth
                lastValue = it
            }
        }
    }

    return setLastElement(x, y, width, height)
}
