package engine.graphics.ui.widgets

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.input
import com.cozmicgames.utils.maths.Rectangle
import com.cozmicgames.utils.maths.Vector2
import engine.graphics.ui.*
import kotlin.math.min

/**
 * Adds a scrollable area to the GUI.
 * A scroll pane is a container of GUI elements that can be scrolled.
 *
 * @param maxWidth The maximum width of the scroll pane. If null, the width of the scroll pane is calculated automatically to fit all elements.
 * @param maxHeight The maximum height of the scroll pane. If null, the height of the scroll pane is calculated automatically to fit all elements.
 * @param scroll The current scroll position of the scroll pane. This function will update the scroll position automatically.
 */
fun GUI.scrollArea(maxWidth: Float? = null, maxHeight: Float? = null, scroll: Vector2, block: () -> Unit): GUIElement {
    val (x, y) = getLastElement()

    if (maxWidth == null)
        scroll.x = 0.0f

    if (maxHeight == null)
        scroll.y = 0.0f

    val contentX = x + style.elementPadding
    val contentY = y + style.elementPadding

    val scissorRectangle = Rectangle(x, y, maxWidth ?: (Kore.graphics.width - x), maxHeight ?: (Kore.graphics.height - y))
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
    val areaWidth: Float
    val areaHeight: Float

    if (maxWidth == null) {
        contentWidth = content.width
        areaWidth = contentWidth + style.elementPadding * 2.0f
    } else {
        areaWidth = min(content.width + style.elementPadding * 2.0f, maxWidth)
        contentWidth = areaWidth - style.elementPadding * 2.0f

        if (scroll.x < 0.0f)
            scroll.x = 0.0f

        if (scroll.x > content.width - areaWidth)
            scroll.x = content.width - areaWidth
    }

    if (maxHeight == null) {
        contentHeight = content.height
        areaHeight = contentHeight + style.elementPadding * 2.0f
    } else {
        areaHeight = min(content.height + style.elementPadding * 2.0f, maxHeight)
        contentHeight = areaHeight - style.elementPadding * 2.0f

        if (scroll.y < 0.0f)
            scroll.y = 0.0f

        if (scroll.y > content.height - areaHeight)
            scroll.y = content.height - areaHeight
    }

    var totalWidth = areaWidth
    var totalHeight = areaHeight

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = totalWidth
    rectangle.height = totalHeight

    if (GUI.State.HOVERED in getState(rectangle)) {
        if (maxWidth != null) scroll.x += currentScrollAmount.x
        if (maxHeight != null) scroll.y += currentScrollAmount.y
    }

    val scrollbarCommands = GUICommandList()

    if (areaWidth - style.elementPadding * 2.0f < content.width) {
        val scrollbarX = x
        val scrollbarY = y + contentHeight + style.elementPadding * 2.0f
        val scrollbarWidth = areaWidth
        val scrollbarHeight = style.elementSize * 0.5f

        val scrollbarGripX = scrollbarX + scroll.x * areaWidth / content.width
        val scrollbarGripWidth = (areaWidth / content.width) * scrollbarWidth

        val scrollbarState = getState(rectangle.apply {
            this.x = scrollbarX
            this.y = scrollbarY
            this.width = scrollbarWidth
            this.height = scrollbarHeight
        }, GUI.ButtonBehaviour.NONE)

        val scrollbarGripState = getState(rectangle.apply {
            this.x = scrollbarGripX
            this.y = scrollbarY
            this.width = scrollbarGripWidth
            this.height = scrollbarHeight
        }, GUI.ButtonBehaviour.REPEATED)

        val scrollbarColor = if (GUI.State.HOVERED in scrollbarState) style.hoverColor else style.normalColor

        val scrollbarGripColor = if (GUI.State.ACTIVE in scrollbarGripState && GUI.State.HOVERED in scrollbarGripState) {
            scroll.x += Kore.input.deltaX * content.width / areaWidth
            style.highlightColor
        } else if (GUI.State.HOVERED in scrollbarGripState)
            style.normalColor
        else
            style.backgroundColor

        scrollbarCommands.drawRectFilled(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, style.roundedCorners, style.cornerRounding, scrollbarColor)
        scrollbarCommands.drawRectFilled(scrollbarGripX, scrollbarY, scrollbarGripWidth, scrollbarHeight, style.roundedCorners, style.cornerRounding, scrollbarGripColor)

        totalHeight += scrollbarHeight
    } else
        scroll.x = 0.0f

    if (areaHeight - style.elementPadding * 2.0f < content.height) {
        val scrollbarX = x + contentWidth + style.elementPadding * 2.0f
        val scrollbarY = y
        val scrollbarWidth = style.elementSize * 0.5f
        val scrollbarHeight = areaHeight

        val scrollbarGripY = scrollbarY + scroll.y * areaHeight / content.height
        val scrollbarGripHeight = (areaHeight / content.height) * scrollbarHeight

        val scrollbarState = getState(rectangle.apply {
            this.x = scrollbarX
            this.y = scrollbarY
            this.width = scrollbarWidth
            this.height = scrollbarHeight
        }, GUI.ButtonBehaviour.NONE)

        val scrollbarGripState = getState(rectangle.apply {
            this.x = scrollbarX
            this.y = scrollbarGripY
            this.width = scrollbarWidth
            this.height = scrollbarGripHeight
        }, GUI.ButtonBehaviour.REPEATED)

        val scrollbarColor = if (GUI.State.HOVERED in scrollbarState) style.hoverColor else style.normalColor

        val scrollbarGripColor = if (GUI.State.ACTIVE in scrollbarGripState && GUI.State.HOVERED in scrollbarGripState) {
            scroll.y -= Kore.input.deltaY * content.height / areaHeight
            style.highlightColor
        } else if (GUI.State.HOVERED in scrollbarGripState)
            style.normalColor
        else
            style.backgroundColor

        scrollbarCommands.drawRectFilled(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, style.roundedCorners, style.cornerRounding, scrollbarColor)
        scrollbarCommands.drawRectFilled(scrollbarX, scrollbarGripY, scrollbarWidth, scrollbarGripHeight, style.roundedCorners, style.cornerRounding, scrollbarGripColor)

        totalWidth += scrollbarWidth
    } else
        scroll.y = 0.0f

    currentCommandList.pushScissor(contentX, contentY, contentWidth, contentHeight)
    currentCommandList.addCommandList(commands)
    currentCommandList.popScissor()

    if (!scrollbarCommands.isEmpty)
        currentCommandList.addCommandList(scrollbarCommands)

    return setLastElement(x, y, totalWidth, totalHeight)
}
