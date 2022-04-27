package engine.graphics.ui.widgets

import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectMultiColor
import engine.utils.Gradient
import engine.utils.GradientColor

/**
 * TODO: Finish
 */
fun GUI.gradient(gradient: Gradient, width: Float = 100.0f, height: Float = 50.0f, element: GUIElement = getLastElement()): GUIElement {
    val (x, y) = element

    lateinit var last: GradientColor
    var isFirst = true

    for (color in gradient) {
        if (isFirst)
            isFirst = false
        else {
            val sliceX = color.stop * width
            val sliceWidth = (color.stop - last.stop) * width
            currentCommandList.drawRectMultiColor(sliceX, y, sliceWidth, height, last.color, last.color, color.color, color.color)
        }

        last = color
    }

    return setLastElement(x, y, width, height)
}
