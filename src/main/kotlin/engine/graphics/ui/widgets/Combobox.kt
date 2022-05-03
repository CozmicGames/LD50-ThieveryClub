package engine.graphics.ui.widgets

import com.cozmicgames.utils.maths.Corners
import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.font.GlyphLayout
import engine.graphics.ui.*
import kotlin.math.max

fun GUI.combobox(data: ComboboxData<*>, maxDropOutHeight: Float? = null): GUIElement {
    val layout = GlyphLayout()
    var maxItemWidth = 0.0f
    var itemsHeight = 0.0f

    data.forEach {
        layout.update(it.toString(), drawableFont)
        maxItemWidth = max(maxItemWidth, layout.width)
        itemsHeight += layout.height + style.elementPadding * 2.0f
    }

    val requiresScrollbar = maxDropOutHeight != null && itemsHeight > maxDropOutHeight

    if (requiresScrollbar)
        maxItemWidth += style.scrollbarSize + style.elementPadding

    val element = dropdown(data.selectedItem.toString(), data.isOpen, maxItemWidth) {
        data.isOpen = it

        if (it) {
            if (currentComboBoxData != data)
                currentComboBoxData?.isOpen = false

            currentComboBoxData = data
        } else
            currentComboBoxData = null
    }

    maxItemWidth = max(maxItemWidth, element.width)

    if (requiresScrollbar)
        maxItemWidth -= style.scrollbarSize + style.elementPadding

    if (data.isOpen) {
        val previousSameLine =

        layerUp {
            transient {
                scrollPane(maxHeight = maxDropOutHeight, scroll = data.scrollAmount, backgroundColor = style.normalColor) {
                    repeat(data.size) {
                        comboboxElement(data, it, maxItemWidth)
                    }
                }
            }
        }
    }

    return setLastElement(element)
}

private fun GUI.comboboxElement(data: ComboboxData<*>, index: Int, itemWidth: Float) {
    val (x, y) = getLastElement()

    val itemLayout = GlyphLayout(data[index].toString(), drawableFont)

    val rectangle = Rectangle()

    rectangle.x = x
    rectangle.y = y
    rectangle.width = itemWidth + style.elementPadding * 2.0f
    rectangle.height = itemLayout.height + style.elementPadding + 2.0f

    val textX = x + style.elementPadding
    val textY = y + style.elementPadding

    val state = getState(rectangle, GUI.ButtonBehaviour.DEFAULT)

    val color = if (GUI.State.ACTIVE in state) {
        data.selectedIndex = index
        data.isOpen = false
        style.highlightColor
    } else if (GUI.State.HOVERED in state)
        style.hoverColor
    else
        null

    if (color != null)
        currentCommandList.drawRectFilled(rectangle.x, rectangle.y, rectangle.width, rectangle.height, Corners.NONE, 0.0f, color)

    currentCommandList.drawText(textX, textY, itemLayout, style.fontColor)

    setLastElement(x, y, rectangle.width, rectangle.height)
}
