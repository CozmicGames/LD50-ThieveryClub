package engine.graphics.ui.layout

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Rectangle
import com.cozmicgames.utils.maths.Vector2
import engine.Game
import engine.graphics.render
import engine.graphics.ui.GUI
import engine.graphics.ui.drawRectFilled
import engine.graphics.ui.widgets.scrollArea
import engine.graphics.ui.widgets.scrollPane

class GUIRegion(internal var gui: GUI) {
    companion object {
        internal val DEFAULT_CONSTRAINTS = GUIConstraints()
        internal val DEFAULT_PARENT = Rectangle()
            get() {
                field.x = Kore.graphics.safeInsetLeft.toFloat()
                field.y = Kore.graphics.safeInsetTop.toFloat()
                field.width = Kore.graphics.width.toFloat()
                field.height = Kore.graphics.height.toFloat()
                return field
            }
    }

    val rectangle = Rectangle()
        get() {
            field.x = constraints.x.getValue(parent, field)
            field.y = constraints.y.getValue(parent, field)
            field.width = constraints.width.getValue(parent, field)
            field.height = constraints.height.getValue(parent, field)
            return field
        }

    var parent: Rectangle = DEFAULT_PARENT

    var constraints: GUIConstraints = DEFAULT_CONSTRAINTS

    var layout: (GUI) -> Unit = {}

    private val scrollAmount = Vector2()

    fun render() {
        val rectangle = this.rectangle

        gui.begin()
        gui.setLastElement(gui.absolute(rectangle.x, rectangle.y))
        gui.scrollPane(rectangle.width, rectangle.height, scrollAmount) {
            layout(gui)
        }
        gui.end()
    }
}