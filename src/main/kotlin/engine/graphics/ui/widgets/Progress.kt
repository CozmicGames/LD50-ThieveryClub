package engine.graphics.ui.widgets

import com.cozmicgames.utils.extensions.clamp
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled

fun GUI.progress(progress: Float, width: Float = 100.0f, element: GUIElement = getLastElement()): GUIElement {
    val (x, y) = element
    val height = style.elementSize

    currentCommandList.drawRectFilled(x, y, width, height, style.roundedCorners, style.cornerRounding, style.normalColor)

    if (progress > 0.0f)
        currentCommandList.drawRectFilled(x, y, width * progress.clamp(0.0f, 1.0f), height, style.roundedCorners, style.cornerRounding, style.highlightColor)

    return setLastElement(x, y, width, height)
}
