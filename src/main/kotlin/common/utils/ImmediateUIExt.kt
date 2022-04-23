package common.utils

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.TextureRegion
import engine.graphics.ui.immediate.*

fun ImmediateUI.selectableImage(texture: TextureRegion, width: Float = style.elementSize, height: Float = style.elementSize, isSelected: Boolean, element: ImmediateUI.Element = getLastElement(), action: () -> Unit): ImmediateUI.Element {
    val (x, y) = element

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = width
    rectangle.height = height

    val state = getState(rectangle, ImmediateUI.ButtonBehaviour.DEFAULT)

    currentCommandList.drawImage(x, y, width, height, texture, Color.WHITE)

    if (ImmediateUI.State.ACTIVE in state)
        action()

    if (ImmediateUI.State.HOVERED in state && !isSelected)
        currentCommandList.drawRect(x, y, width, height, style.roundedCorners, style.cornerRounding, 3.0f, style.hoverColor)
    else if (isSelected)
        currentCommandList.drawRect(x, y, width, height, style.roundedCorners, style.cornerRounding, 3.0f, style.highlightColor)

    return setLastElement(x, y, width, height, false)
}
