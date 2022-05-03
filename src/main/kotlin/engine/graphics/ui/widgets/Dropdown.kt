package engine.graphics.ui.widgets

import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.font.GlyphLayout
import engine.graphics.ui.*
import kotlin.math.max

fun GUI.dropdown(title: String, isOpen: Boolean, minWidth: Float? = null, action: (Boolean) -> Unit): GUIElement {
    val (x, y) = getLastElement()

    val layout = GlyphLayout(title, drawableFont)
    val textX = x + style.elementPadding
    val textY = y + style.elementPadding

    val requiredWidth = layout.width + style.contentSize + style.elementPadding * 3.0f
    val width = if (minWidth == null) requiredWidth else max(requiredWidth, minWidth)
    val height = layout.height + style.elementPadding * 2.0f

    val dropdownX = width - style.elementPadding - style.contentSize
    val dropDownY = y + style.elementPadding

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = width
    rectangle.height = height

    val state = getState(rectangle, GUI.ButtonBehaviour.DEFAULT)

    val color = if (GUI.State.ACTIVE in state)
        style.highlightColor
    else if (GUI.State.HOVERED in state)
        style.hoverColor
    else
        style.normalColor

    currentCommandList.drawRectFilled(x, y, width, height, style.roundedCorners, style.cornerRounding, color)
    currentCommandList.drawText(textX, textY, layout, style.fontColor)

    val isClicked = GUI.State.ACTIVE in state
    var newOpen = isOpen

    if (isClicked) {
        newOpen = !newOpen
        action(newOpen)
    }

    if (newOpen) {
        val triangleX0 = dropdownX
        val triangleY0 = dropDownY + style.contentSize

        val triangleX1 = dropdownX + style.contentSize
        val triangleY1 = dropDownY + style.contentSize

        val triangleX2 = dropdownX + style.contentSize * 0.5f
        val triangleY2 = dropDownY

        currentCommandList.drawTriangleFilled(triangleX0, triangleY0, triangleX1, triangleY1, triangleX2, triangleY2, style.fontColor)
    } else {
        val triangleX0 = dropdownX
        val triangleY0 = dropDownY

        val triangleX1 = dropdownX + style.contentSize
        val triangleY1 = dropDownY

        val triangleX2 = dropdownX + style.contentSize * 0.5f
        val triangleY2 = dropDownY + style.contentSize

        currentCommandList.drawTriangleFilled(triangleX0, triangleY0, triangleX1, triangleY1, triangleX2, triangleY2, style.fontColor)
    }

    return setLastElement(x, y, width, height)
}
