package engine.graphics.ui.widgets

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.input
import com.cozmicgames.input.Keys
import com.cozmicgames.utils.maths.Corners
import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.font.GlyphLayout
import engine.graphics.ui.*
import kotlin.math.max
import kotlin.math.sin

/**
 * Adds a text field to the UI.
 *
 * @param textData The text data to use for the text field.
 * @param minWidth The minimum width of the text field. Should be more than 0, so it can be selected if the text is empty. Defaults to [style.elementSize].
 * @param action The action to perform when the text changed. Defaults to a no-op.
 */
fun GUI.textField(textData: TextData, minWidth: Float = style.elementSize, element: GUIElement = getLastElement(), action: () -> Unit = {}): GUIElement {
    val (x, y) = element

    val layout = GlyphLayout(textData.text, drawableFont)
    val rectangle = Rectangle()

    rectangle.x = x
    rectangle.y = y
    rectangle.width = max(layout.width, minWidth) + style.elementPadding * 2.0f
    rectangle.height = layout.height + style.elementPadding * 2.0f

    val state = getState(rectangle, GUI.ButtonBehaviour.REPEATED)

    if (GUI.State.HOVERED in state && GUI.State.ACTIVE in state) {
        currentTextData = textData
        textData.setCursor(max(0, layout.findCursorIndex(touchPosition.x - x + style.elementPadding, touchPosition.y - y + style.elementPadding)))
    } else if (Kore.input.justTouched)
        currentTextData = null

    currentCommandList.drawRectFilled(rectangle.x, rectangle.y, rectangle.width, rectangle.height, style.roundedCorners, style.cornerRounding, style.backgroundColor)
    currentCommandList.drawText(x + style.elementPadding, y + style.elementPadding, layout, style.fontColor)

    if (textData == currentTextData) {
        if (textData.isSelectionActive) {
            val selectionX: Float
            val selectionY: Float
            val selectionWidth: Float

            val cursor0 = textData.getFrontSelectionPosition()
            val cursor1 = textData.getEndSelectionPosition()

            if (cursor0 < layout.count) {
                val quad = layout[cursor0]
                selectionX = quad.x
                selectionY = quad.y
            } else {
                val quad = layout[layout.count - 1]
                selectionX = quad.x + quad.width
                selectionY = quad.y
            }

            selectionWidth = if (cursor1 < layout.count) {
                val quad = layout[cursor1]
                quad.x - selectionX
            } else {
                val quad = layout[layout.count - 1]
                quad.x + quad.width - selectionX
            }

            currentCommandList.drawRectFilled(x + selectionX + style.elementPadding, y + selectionY + style.elementPadding, selectionWidth, style.elementSize, Corners.NONE, 0.0f, style.cursorColor)
        } else if (sin(Kore.graphics.statistics.runTime * 5.0f) > 0.0f) {
            val cursorX: Float
            val cursorY: Float

            if (layout.count > 0) {
                if (textData.cursor < layout.count) {
                    val quad = layout[textData.cursor]
                    cursorX = quad.x
                    cursorY = quad.y
                } else {
                    val quad = layout[layout.count - 1]
                    cursorX = quad.x + quad.width
                    cursorY = quad.y
                }
            } else {
                cursorX = 0.0f
                cursorY = 0.0f
            }

            currentCommandList.drawRectFilled(x + cursorX + style.elementPadding, y + cursorY + style.elementPadding, 1.0f, style.elementSize, Corners.NONE, 0.0f, style.cursorColor)
        }
    }

    if (textData.hasChanged) {
        action()
        textData.hasChanged = false
    }

    return setLastElement(x, y, rectangle.width, rectangle.height)
}
