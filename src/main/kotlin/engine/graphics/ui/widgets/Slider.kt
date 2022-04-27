package engine.graphics.ui.widgets

import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawCircleFilled
import engine.graphics.ui.drawRectFilled

fun GUI.slider(amount: Float, width: Float = 100.0f, element: GUIElement = getLastElement(), action: (Float) -> Unit): GUIElement {
    val (x, y) = element

    val handleRadius = style.elementSize * 0.5f

    val sliderHeight = style.elementSize / 3.0f
    val sliderWidth = width - handleRadius * 2.0f
    val sliderX = x + handleRadius
    val sliderY = y + sliderHeight

    var handleX = x + handleRadius + sliderWidth * amount
    val handleY = y + handleRadius

    currentCommandList.drawRectFilled(sliderX, sliderY, sliderWidth, sliderHeight, style.roundedCorners, style.cornerRounding, style.normalColor)

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = width
    rectangle.height = style.elementSize

    val state = getState(rectangle, GUI.ButtonBehaviour.REPEATED)

    if (GUI.State.HOVERED in state) {
        val color = if (GUI.State.ACTIVE in state) {
            val newAmount = (touchPosition.x - x) / width
            handleX = x + handleRadius + sliderWidth * newAmount

            action(newAmount)
            style.highlightColor
        } else
            style.hoverColor

        currentCommandList.drawCircleFilled(handleX, handleY, handleRadius, color)
    } else
        currentCommandList.drawCircleFilled(handleX, handleY, handleRadius, style.normalColor)

    return setLastElement(x, y, width, style.elementSize)
}
