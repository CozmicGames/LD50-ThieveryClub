package engine.graphics.ui.widgets

import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled

/**
 * Adds a checkbox to the GUI.
 *
 * @param isChecked Whether the checkbox is checked or not.
 * @param action The action to perform when the checkbox is clicked. It receives if the checkbox is checked or not.
 */
fun GUI.checkBox(isChecked: Boolean, action: (Boolean) -> Unit): GUIElement {
    val (x, y) = getLastElement()
    val size = style.elementSize

    val rectangle = Rectangle(x, y, size, size)
    val state = getState(rectangle, GUI.ButtonBehaviour.DEFAULT)

    currentCommandList.drawRectFilled(x, y, size, size, style.roundedCorners, style.cornerRounding, if (GUI.State.HOVERED in state) style.hoverColor else style.normalColor)
    val isClicked = GUI.State.ACTIVE in state
    var newChecked = isChecked

    if (isClicked) {
        newChecked = !newChecked
        action(newChecked)
    }

    if (newChecked) {
        val checkMarkX = x + style.elementPadding
        val checkMarkY = y + style.elementPadding
        val checkMarkSize = style.fontSize

        currentCommandList.drawRectFilled(checkMarkX, checkMarkY, checkMarkSize, checkMarkSize, style.roundedCorners, style.cornerRounding, style.highlightColor)
    }

    return setLastElement(x, y, size, size)
}