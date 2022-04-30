package engine.graphics.ui

import com.cozmicgames.Kore
import com.cozmicgames.files.Files
import com.cozmicgames.graphics
import com.cozmicgames.graphics.loadFont
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Corners

/**
 * Represents the style of GUI elements.
 */
open class GUIStyle {
    open var font = Kore.graphics.defaultFont
    open var fontSize = 14.0f
    open var elementSize = 18.0f
    open var backgroundColor = Color(0x171A23FF)
    open var normalColor = Color(0x2A2F3FFF)
    open var highlightColor = Color(0x4FB742FF)
    open var hoverColor = Color(0x43485BFF)
    open var fontColor = Color.WHITE.copy()
    open var cursorColor = Color(0xFFFFFF99.toInt())
    open var strokeThickness = 1.5f
    open var cornerRounding = 1.5f
    open var roundedCorners = Corners.ALL
    open var offsetToNextX = 4.0f
    open var offsetToNextY = 4.0f
    open var scrollSpeed = 5.0f

    val elementPadding get() = (elementSize - fontSize) * 0.5f
}