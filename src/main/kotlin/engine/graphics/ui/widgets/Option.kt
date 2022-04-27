package engine.graphics.ui.widgets

import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawCircleFilled

fun GUI.option(option: Int, selectedOption: Int, element: GUIElement = getLastElement(), action: (Int) -> Unit): GUIElement {
    val (x, y) = element
    val size = style.elementSize

    val rectangle = Rectangle(x, y, size, size)
    val state = getState(rectangle, GUI.ButtonBehaviour.DEFAULT)

    currentCommandList.drawCircleFilled(x + size * 0.5f, y + size * 0.5f, size * 0.5f, if (GUI.State.HOVERED in state) style.hoverColor else style.normalColor)
    val isClicked = GUI.State.ACTIVE in state
    var newChecked = option == selectedOption

    if (isClicked) {
        newChecked = !newChecked
        action(option)
    }

    if (newChecked) {
        val middleX = x + style.elementPadding
        val middleY = y + style.elementPadding
        val middleSize = size - style.elementPadding * 2.0f

        currentCommandList.drawCircleFilled(middleX + middleSize * 0.5f, middleY + middleSize * 0.5f, middleSize * 0.5f, style.highlightColor)
    }

    return setLastElement(x, y, size, size)
}
