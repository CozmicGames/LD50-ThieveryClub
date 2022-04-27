package engine.graphics.ui.widgets

import com.cozmicgames.utils.maths.Corners
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled

/**
 * Adds a separator element.
 * The separator is a horizontal line the width of the given [srcElement].
 *
 * @param element The element.
 */
fun GUI.separator(srcElement: GUIElement, element: GUIElement = getLastElement()) = separator(srcElement.width, element)

/**
 * Adds a separator element.
 * The separator is a horizontal line of the given [width].
 *
 * @param width The width of the separator. Defaults to [style.elementSize] * 10.
 * @param element The element. Defaults to the last element.
 */
fun GUI.separator(width: Float = style.elementSize * 10.0f, element: GUIElement = getLastElement()): GUIElement {
    val (x, y) = element
    val separatorX = x + style.elementPadding
    val separatorY = y + style.elementSizeWithPadding * 0.4f
    val separatorWidth = width - style.elementPadding * 2.0f
    val separatorHeight = style.elementSizeWithPadding * 0.2f
    currentCommandList.drawRectFilled(separatorX, separatorY, separatorWidth, separatorHeight, Corners.NONE, 0.0f, style.normalColor)
    return setLastElement(x, y, width, style.elementSizeWithPadding)
}
