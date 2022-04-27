package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import engine.graphics.TextureRegion
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawImage
import engine.graphics.ui.drawRect

/**
 * Adds an image element.
 *
 * @param texture The texture of the image.
 * @param width The width of the image. Defaults to [style.elementSize].
 * @param height The height of the image. Defaults to [style.elementSize].
 * @param color The color of the image. Defaults to [style.normalColor].
 * @param borderThickness The thickness of the border. Defaults to 4.0f.
 */
fun GUI.image(texture: TextureRegion, width: Float = style.elementSize, height: Float = style.elementSize, color: Color = Color.WHITE, borderThickness: Float = 4.0f, element: GUIElement = getLastElement()): GUIElement {
    val (x, y) = element

    currentCommandList.drawImage(x, y, width, height, texture, color)
    if (borderThickness > 0.0f)
        currentCommandList.drawRect(x, y, width, height, style.roundedCorners, style.cornerRounding, borderThickness, style.normalColor)

    return setLastElement(x, y, width, height)
}
