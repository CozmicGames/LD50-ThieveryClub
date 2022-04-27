package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.TextureRegion
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawImage
import engine.graphics.ui.drawRect

/**
 * Adds a selectable image to the GUI.
 *
 * @param texture The texture to use for the image.
 * @param width The width of the image. Defaults to [style.elementSizeWithPadding].
 * @param height The height of the image. Defaults to the same as [width].
 * @param isSelected Whether the image is selected.
 * @param action The action to perform when the image is clicked.
 */
fun GUI.selectableImage(texture: TextureRegion, width: Float = style.elementSizeWithPadding, height: Float = width, isSelected: Boolean, element: GUIElement = getLastElement(), action: () -> Unit): GUIElement {
    val (x, y) = element

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = width
    rectangle.height = height

    val state = getState(rectangle, GUI.ButtonBehaviour.DEFAULT)

    currentCommandList.drawImage(x, y, width, height, texture, Color.WHITE)

    if (GUI.State.ACTIVE in state)
        action()

    if (GUI.State.HOVERED in state && !isSelected)
        currentCommandList.drawRect(x, y, width, height, style.roundedCorners, style.cornerRounding, style.strokeThickness, style.hoverColor)
    else if (isSelected)
        currentCommandList.drawRect(x, y, width, height, style.roundedCorners, style.cornerRounding, style.strokeThickness, style.highlightColor)

    return setLastElement(x, y, width, height, false)
}
