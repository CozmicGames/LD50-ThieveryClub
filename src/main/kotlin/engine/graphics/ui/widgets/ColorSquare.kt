package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled

/**
 * Adds a color square element.
 *
 * @param color The color.
 * @param size The size of the square. Defaults to [style.elementSizeWithPadding].
 */
fun GUI.colorSquare(color: Color, size: Float = style.elementSizeWithPadding, element: GUIElement = getLastElement()): GUIElement {
    val (x, y) = element
    currentCommandList.drawRectFilled(x, y, size, size, style.roundedCorners, style.cornerRounding, color)
    return setLastElement(x, y, size, size)
}
