package engine.graphics.ui.widgets

import engine.graphics.font.GlyphLayout
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawText

/**
 * Adds a label element.
 *
 * @param text The text of the label.
 */
fun GUI.label(text: String, element: GUIElement = getLastElement()): GUIElement {
    val (x, y) = element
    val layout = GlyphLayout(text, drawableFont)
    currentCommandList.drawText(x, y, layout, style.fontColor, style.backgroundColor)
    return setLastElement(x, y, layout.width, layout.height)
}
