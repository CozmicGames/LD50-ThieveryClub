package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import engine.graphics.font.GlyphLayout
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled
import engine.graphics.ui.drawText

/**
 * Adds a label element.
 *
 * @param text The text of the label.
 * @param backgroundColor The background color of the label. Defaults to [style.backgroundColor]. If null, no background is drawn.
 */
fun GUI.label(text: String, backgroundColor: Color? = style.backgroundColor): GUIElement {
    val (x, y) = getLastElement()
    val layout = GlyphLayout(text, drawableFont)
    val textX = x + style.elementPadding
    val textY = y + style.elementPadding
    val textWidth = layout.width + 2.0f * style.elementPadding
    val textHeight = layout.height + 2.0f * style.elementPadding

    if (backgroundColor != null)
        currentCommandList.drawRectFilled(x, y, textWidth, textHeight, style.roundedCorners, style.cornerRounding, backgroundColor)

    currentCommandList.drawText(textX, textY, layout, style.fontColor)
    return setLastElement(x, y, textWidth, textHeight)
}
