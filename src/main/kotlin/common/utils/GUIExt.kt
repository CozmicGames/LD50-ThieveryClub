package common.utils

import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRect
import engine.graphics.ui.drawRectFilled
import java.lang.Float.min

fun GUI.plusButton(width: Float = style.elementSize, height: Float = style.elementSize, element: GUIElement = getLastElement(), action: () -> Unit): GUIElement {
    val (x, y) = element

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = width
    rectangle.height = height

    val state = getState(rectangle, GUI.ButtonBehaviour.DEFAULT)
    val plusThickness = min(width, height) * 0.2f
    val borderSize = min(width, height) / style.elementSize

    val color = if (GUI.State.ACTIVE in state) {
        action()
        style.highlightColor
    } else if (GUI.State.HOVERED in state)
        style.hoverColor
    else
        style.normalColor

    currentCommandList.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, style.roundedCorners, style.cornerRounding, borderSize, color)
    currentCommandList.drawRectFilled(rectangle.centerX - plusThickness * 0.5f, rectangle.y + plusThickness, plusThickness, rectangle.height - plusThickness * 2.0f, style.roundedCorners, style.cornerRounding, color)
    currentCommandList.drawRectFilled(rectangle.x + plusThickness, rectangle.centerY - plusThickness * 0.5f, rectangle.width - plusThickness * 2.0f, plusThickness, style.roundedCorners, style.cornerRounding, color)

    return setLastElement(x, y, width, height)
}

fun GUI.minusButton(width: Float = style.elementSize, height: Float = style.elementSize, element: GUIElement = getLastElement(), action: () -> Unit): GUIElement {
    val (x, y) = element

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = width
    rectangle.height = height

    val state = getState(rectangle, GUI.ButtonBehaviour.DEFAULT)
    val plusThickness = min(width, height) * 0.2f
    val borderSize = min(width, height) / style.elementSize

    val color = if (GUI.State.ACTIVE in state) {
        action()
        style.highlightColor
    } else if (GUI.State.HOVERED in state)
        style.hoverColor
    else
        style.normalColor

    currentCommandList.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, style.roundedCorners, style.cornerRounding, borderSize, color)
    currentCommandList.drawRectFilled(rectangle.x + plusThickness, rectangle.centerY - plusThickness * 0.5f, rectangle.width - plusThickness * 2.0f, plusThickness, style.roundedCorners, style.cornerRounding, color)

    return setLastElement(x, y, width, height)
}
