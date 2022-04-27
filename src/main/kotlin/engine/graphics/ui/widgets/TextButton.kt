package engine.graphics.ui.widgets

import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.font.GlyphLayout
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled
import engine.graphics.ui.drawText

/**
 * Adds a text button element.
 *
 * @param text The text of the button.
 * @param action The function to execute when the button is clicked.
 */
fun GUI.textButton(text: String, element: GUIElement = getLastElement(), action: () -> Unit): GUIElement {
    val (x, y) = element

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y

    val layout = GlyphLayout(text, drawableFont)
    val textX = x + style.elementPadding
    val textY = y + style.elementPadding

    rectangle.width = layout.width + 2.0f * style.elementPadding
    rectangle.height = layout.height + 2.0f * style.elementPadding

    val state = getState(rectangle, GUI.ButtonBehaviour.DEFAULT)

    val color = if (GUI.State.ACTIVE in state) {
        action()
        style.highlightColor
    } else if (GUI.State.HOVERED in state)
        style.hoverColor
    else
        style.normalColor

    currentCommandList.drawRectFilled(rectangle.x, rectangle.y, rectangle.width, rectangle.height, style.roundedCorners, style.cornerRounding, color)
    currentCommandList.drawText(textX, textY, layout, style.fontColor, null)

    return setLastElement(x, y, rectangle.width, rectangle.height)
}
