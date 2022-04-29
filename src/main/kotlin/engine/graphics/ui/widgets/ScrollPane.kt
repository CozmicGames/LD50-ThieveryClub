package engine.graphics.ui.widgets

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.input
import com.cozmicgames.utils.maths.Corners
import com.cozmicgames.utils.maths.Rectangle
import com.cozmicgames.utils.maths.Vector2
import engine.graphics.ui.*

/**
 * Adds a scroll pane to the GUI.
 * A scroll pane is a container of GUI elements that can be scrolled.
 *
 * @param width The width of the scroll pane. If null, the width of the scroll pane is calculated automatically to fit all elements.
 * @param height The height of the scroll pane. If null, the height of the scroll pane is calculated automatically to fit all elements.
 * @param scroll The current scroll position of the scroll pane. This function will update the scroll position automatically.
 */
fun GUI.scrollPane(width: Float? = null, height: Float? = null, scroll: Vector2, block: () -> Unit): GUIElement {
    val (x, y) = getLastElement()

    if (width == null)
        scroll.x = 0.0f

    if (height == null)
        scroll.y = 0.0f

    val contentX = x + style.elementPadding
    val contentY = y + style.elementPadding

    val scissorRectangle = Rectangle(x, y, width ?: (Kore.graphics.width - x), height ?: (Kore.graphics.height - y))
    val previousScissorRectangle = currentScissorRectangle
    currentScissorRectangle = scissorRectangle

    lateinit var content: GUIElement

    val commands = recordCommands {
        transient {
            setLastElement(absolute(contentX - scroll.x, contentY - scroll.y))
            content = group { block() }
        }
    }

    currentScissorRectangle = previousScissorRectangle


    val contentWidth: Float
    val contentHeight: Float
    val paneWidth: Float
    val paneHeight: Float

    if (width == null) {
        contentWidth = content.width
        paneWidth = contentWidth + style.elementPadding * 2.0f
    } else {
        contentWidth = width - style.elementPadding * 2.0f
        paneWidth = width
    }

    if (height == null) {
        contentHeight = content.height
        paneHeight = contentHeight + style.elementPadding * 2.0f
    } else {
        contentHeight = height - style.elementPadding * 2.0f
        paneHeight = height
    }

    var totalWidth = paneWidth
    var totalHeight = paneHeight

    val scrollbarCommands = GUICommandList()

    if (paneWidth - style.elementPadding * 2.0f < content.width) {
        val scrollbarX = x
        val scrollbarY = y + contentHeight + style.elementPadding * 2.0f
        val scrollbarWidth = paneWidth
        val scrollbarHeight = style.elementSize * 0.5f

        val scrollbarGripX = scrollbarX + scroll.x * paneWidth / content.width
        val scrollbarGripWidth = (paneWidth / content.width) * scrollbarWidth

        val rectangle = Rectangle()
        rectangle.x = scrollbarGripX
        rectangle.y = scrollbarY
        rectangle.width = scrollbarGripWidth
        rectangle.height = scrollbarHeight

        val scrollbarGripState = getState(rectangle, GUI.ButtonBehaviour.REPEATED)

        val scrollbarGripColor = if (GUI.State.ACTIVE in scrollbarGripState && GUI.State.HOVERED in scrollbarGripState) {
            scroll.x += Kore.input.deltaX * content.width / paneWidth
            if (scroll.x < 0.0f)
                scroll.x = 0.0f

            if (scroll.x > content.width - paneWidth)
                scroll.x = content.width - paneWidth

            style.highlightColor
        } else if (GUI.State.HOVERED in scrollbarGripState)
            style.hoverColor
        else
            style.backgroundColor

        scrollbarCommands.drawRectFilled(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, style.roundedCorners, style.cornerRounding, style.hoverColor)
        scrollbarCommands.drawRectFilled(scrollbarGripX, scrollbarY, scrollbarGripWidth, scrollbarHeight, style.roundedCorners, style.cornerRounding, scrollbarGripColor)

        totalHeight += scrollbarHeight
    }

    if (paneHeight - style.elementPadding * 2.0f < content.height) {
        val scrollbarX = x + contentWidth + style.elementPadding * 2.0f
        val scrollbarY = y
        val scrollbarWidth = style.elementSize * 0.5f
        val scrollbarHeight = paneHeight

        val scrollBarGripY = scrollbarY + scroll.y * paneHeight / content.height
        val scrollbarGripHeight = (paneHeight / content.height) * scrollbarHeight

        val rectangle = Rectangle()
        rectangle.x = scrollbarX
        rectangle.y = scrollBarGripY
        rectangle.width = scrollbarWidth
        rectangle.height = scrollbarGripHeight

        val scrollbarGripState = getState(rectangle, GUI.ButtonBehaviour.REPEATED)

        val scrollbarGripColor = if (GUI.State.ACTIVE in scrollbarGripState && GUI.State.HOVERED in scrollbarGripState) {
            scroll.y -= Kore.input.deltaY * content.height / paneHeight
            if (scroll.y < 0.0f)
                scroll.y = 0.0f

            if (scroll.y > content.height - paneHeight)
                scroll.y = content.height - paneHeight

            style.highlightColor
        } else if (GUI.State.HOVERED in scrollbarGripState)
            style.hoverColor
        else
            style.backgroundColor

        scrollbarCommands.drawRectFilled(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, style.roundedCorners, style.cornerRounding, style.hoverColor)
        scrollbarCommands.drawRectFilled(scrollbarX, scrollBarGripY, scrollbarWidth, scrollbarGripHeight, style.roundedCorners, style.cornerRounding, scrollbarGripColor)

        totalWidth += scrollbarWidth
    }

    currentCommandList.drawRectFilled(x, y, totalWidth, totalHeight, Corners.NONE, 0.0f, style.backgroundColor)

    currentCommandList.pushScissor(contentX, contentY, contentWidth, contentHeight)
    currentCommandList.addCommandList(commands)
    currentCommandList.popScissor()

    if (!scrollbarCommands.isEmpty)
        currentCommandList.addCommandList(scrollbarCommands)

    return setLastElement(x, y, totalWidth, totalHeight)
}
