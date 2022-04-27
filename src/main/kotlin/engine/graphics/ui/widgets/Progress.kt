package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.extensions.clamp
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled

/**
 * Adds a progress bar to the GUI.
 *
 * @param progress The progress of the progress bar. Must be between 0 and 1.
 * @param width The width of the progress bar. Defaults to [style.elementSize] * 10.
 * @param color The color of the progress bar. Defaults to [style.highlightColor].
 */
fun GUI.progress(progress: Float, width: Float = style.elementSize * 10.0f, color: Color = style.highlightColor, element: GUIElement = getLastElement()): GUIElement {
    val (x, y) = element
    val height = style.elementSizeWithPadding

    currentCommandList.drawRectFilled(x, y, width, height, style.roundedCorners, style.cornerRounding, style.normalColor)

    if (progress > 0.0f)
        currentCommandList.drawRectFilled(x, y, width * progress.clamp(0.0f, 1.0f), height, style.roundedCorners, style.cornerRounding, color)

    return setLastElement(x, y, width, height)
}
