package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Corners
import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled
import engine.graphics.ui.drawRectMultiColor

fun GUI.colorEdit(color: Color, element: GUIElement = getLastElement()): GUIElement {
    val (x, y) = element

    val hsv = color.toHSV()
    var newAlpha = color.a
    val rectangle = Rectangle()
    val size = 100.0f
    var totalWidth = 0.0f
    var totalHeight = 0.0f

    val commands = recordCommands {
        rectangle.x = x + style.elementPadding
        rectangle.y = y + style.elementPadding + 15.0f
        rectangle.width = size
        rectangle.height = size

        currentCommandList.drawRectMultiColor(rectangle.x + rectangle.width * 0.5f, rectangle.y + rectangle.height * 0.5f, rectangle.width, rectangle.height, Color.WHITE, color, color, Color.WHITE)
        currentCommandList.drawRectMultiColor(rectangle.x + rectangle.width * 0.5f, rectangle.y + rectangle.height * 0.5f, rectangle.width, rectangle.height, Color.CLEAR, Color.CLEAR, Color.BLACK, Color.BLACK)

        var state = getState(rectangle, GUI.ButtonBehaviour.REPEATED)

        var crossHairColor = style.normalColor
        if (GUI.State.HOVERED in state) {
            if (GUI.State.ACTIVE in state) {
                hsv[1] = (touchPosition.x - rectangle.x) / rectangle.width
                hsv[2] = 1.0f - (touchPosition.y - rectangle.y) / rectangle.height
                crossHairColor = style.highlightColor
            } else
                crossHairColor = style.hoverColor
        }

        val crossHairX = rectangle.x + hsv[1] * rectangle.width
        val crossHairY = rectangle.y + (1.0f - hsv[2]) * rectangle.height

        currentCommandList.drawRectFilled(crossHairX - 5.0f, crossHairY - 1.0f, 10.0f, 2.0f, Corners.NONE, 0.0f, crossHairColor)
        currentCommandList.drawRectFilled(crossHairX - 1.0f, crossHairY - 5.0f, 2.0f, 10.0f, Corners.NONE, 0.0f, crossHairColor)

        rectangle.x += 105.0f
        rectangle.width = 20.0f

        val hueBarX = rectangle.x + style.elementPadding
        val hueBarY = rectangle.y
        val hueBarWidth = rectangle.width - style.elementPadding * 2.0f
        val hueBarSubHeight = rectangle.height / Color.HUE_COLORS.size

        Color.HUE_COLORS.forEachIndexed { index, c0 ->
            val c1 = Color.HUE_COLORS[if (index < Color.HUE_COLORS.lastIndex) index + 1 else 0]
            currentCommandList.drawRectMultiColor(hueBarX + hueBarWidth * 0.5f, hueBarY + index * hueBarSubHeight + hueBarSubHeight * 0.5f, hueBarWidth, hueBarSubHeight, c0, c0, c1, c1)
        }

        state = getState(rectangle, GUI.ButtonBehaviour.REPEATED)

        var hueLineColor = style.normalColor
        if (GUI.State.HOVERED in state) {
            if (GUI.State.ACTIVE in state) {
                hsv[0] = (touchPosition.y - rectangle.y) / rectangle.height * 360.0f
                hueLineColor = style.highlightColor
            } else
                hueLineColor = style.hoverColor
        }

        val hueLineY = rectangle.y + (hsv[0] / 360.0f) * rectangle.height - 0.5f - 2.0f
        currentCommandList.drawRectFilled(rectangle.x, hueLineY, rectangle.width, 4.0f, Corners.NONE, 0.0f, hueLineColor)

        rectangle.x += 25.0f
        rectangle.width = 20.0f

        val alphaBarX = rectangle.x + style.elementPadding
        val alphaBarY = rectangle.y
        val alphaBarWidth = rectangle.width - style.elementPadding * 2.0f
        val alphaBarHeight = rectangle.height

        currentCommandList.drawRectMultiColor(alphaBarX + alphaBarWidth * 0.5f, alphaBarY + alphaBarHeight * 0.5f, alphaBarWidth, alphaBarHeight, Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK)

        state = getState(rectangle, GUI.ButtonBehaviour.REPEATED)

        var alphaLineColor = style.normalColor
        if (GUI.State.HOVERED in state) {
            if (GUI.State.ACTIVE in state) {
                newAlpha = 1.0f - (touchPosition.y - rectangle.y) / rectangle.height
                alphaLineColor = style.highlightColor
            } else
                alphaLineColor = style.hoverColor
        }

        val alphaLineY = rectangle.y + (1.0f - color.a) * rectangle.height - 0.5f - 2.0f
        currentCommandList.drawRectFilled(rectangle.x, alphaLineY, rectangle.width, 4.0f, Corners.NONE, 0.0f, alphaLineColor)

        totalWidth = rectangle.x + rectangle.width - (x + style.elementPadding)
        totalHeight = rectangle.y + rectangle.height - (y + style.elementPadding)
        currentCommandList.drawRectFilled(x + style.elementPadding, y + style.elementPadding, totalWidth, 12.0f, style.roundedCorners, style.cornerRounding, color)
    }

    color.fromHSV(hsv)
    color.a = newAlpha

    currentCommandList.drawRectFilled(x, y, totalWidth + style.elementPadding * 2.0f, totalHeight + style.elementPadding * 2.0f, style.roundedCorners, style.cornerRounding, style.backgroundColor)
    currentCommandList.addCommandList(commands)

    return setLastElement(x, y, totalWidth + style.elementPadding * 2.0f, totalHeight + style.elementPadding * 2.0f)
}