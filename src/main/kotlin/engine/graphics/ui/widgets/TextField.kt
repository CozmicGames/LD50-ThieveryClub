package engine.graphics.ui.widgets

import com.cozmicgames.Kore
import com.cozmicgames.input
import com.cozmicgames.input.Keys
import com.cozmicgames.utils.maths.Corners
import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.font.GlyphLayout
import engine.graphics.ui.*
import kotlin.math.max
import kotlin.math.sin

fun GUI.textField(textData: TextData, minWidth: Float = style.elementSize, element: GUIElement = getLastElement(), action: () -> Unit): GUIElement {
    val (x, y) = element

    val layout = GlyphLayout(textData.text, drawableFont)
    val rectangle = Rectangle()

    rectangle.x = x
    rectangle.y = y
    rectangle.width = max(layout.width, minWidth)
    rectangle.height = layout.height

    val state = getState(rectangle, GUI.ButtonBehaviour.REPEATED)

    if (GUI.State.HOVERED in state && GUI.State.ACTIVE in state) {
        currentTextData = textData
        textData.setCursor(max(0, layout.findCursorIndex(touchPosition.x - x, touchPosition.y - y)))
    } else if (Kore.input.justTouched)
        currentTextData = null

    currentCommandList.drawRectFilled(rectangle.x, rectangle.y, rectangle.width, rectangle.height, style.roundedCorners, style.cornerRounding, style.backgroundColor)
    currentCommandList.drawText(x, y, layout, style.fontColor, null)

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

            if (cursor1 < layout.count) {
                val quad = layout[cursor1]
                selectionWidth = quad.x - selectionX
            } else {
                val quad = layout[layout.count - 1]
                selectionWidth = quad.x + quad.width - selectionX
            }

            currentCommandList.drawRectFilled(x + selectionX, y + selectionY, selectionWidth, style.elementSize, Corners.NONE, 0.0f, style.cursorColor)
        } else if (sin(time * 5.0f) > 0.0f) {
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

            currentCommandList.drawRectFilled(x + cursorX, y + cursorY, 1.0f, style.elementSize, Corners.NONE, 0.0f, style.cursorColor)
        }
    }

    if (textData.hasChanged) {
        action()
        textData.hasChanged = false
    }

    return setLastElement(x, y, rectangle.width, rectangle.height)
}
