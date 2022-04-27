package engine.graphics.ui.widgets

import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled

fun GUI.checkBox(checked: Boolean, element: GUIElement = getLastElement(), action: (Boolean) -> Unit): GUIElement {
    val (x, y) = element
    val size = style.elementSize

    val rectangle = Rectangle(x, y, size, size)
    val state = getState(rectangle, GUI.ButtonBehaviour.DEFAULT)

    currentCommandList.drawRectFilled(x, y, size, size, style.roundedCorners, style.cornerRounding, if (GUI.State.HOVERED in state) style.hoverColor else style.normalColor)
    val isClicked = GUI.State.ACTIVE in state
    var newChecked = checked

    if (isClicked) {
        newChecked = !newChecked
        action(newChecked)
    }

    if (newChecked) {
        val checkMarkX = x + style.elementPadding
        val checkMarkY = y + style.elementPadding
        val checkMarkSize = size - style.elementPadding * 2.0f

        currentCommandList.drawRectFilled(checkMarkX, checkMarkY, checkMarkSize, checkMarkSize, style.roundedCorners, style.cornerRounding, style.highlightColor)
    }

    return setLastElement(x, y, size, size)
}