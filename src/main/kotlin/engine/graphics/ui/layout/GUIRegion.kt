package engine.graphics.ui.layout

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import engine.graphics.ui.GUI
import engine.graphics.ui.drawCircleFilled
import engine.graphics.ui.drawRect

class GUIRegion(val gui: GUI, private val ownsGUI: Boolean = false) : Disposable {
    constructor() : this(GUI(), true)

    val constraints = GUIConstraints()

    val animator = GUIAnimator()

    val x get() = constraints.x.getValue(parent, this) + animator.x

    val y get() = constraints.y.getValue(parent, this) + animator.y

    val width get() = constraints.width.getValue(parent, this) * animator.width

    val height get() = constraints.height.getValue(parent, this) * animator.height

    var parent: GUIRegion? = null

    var layoutElements: (GUI) -> Unit = {}

    fun render() {
        gui.begin()
        gui.setLastElement(gui.absolute(x, y))
        layoutElements(gui)
        gui.currentCommandList.drawRect(x, y, width, height, 0, 0.0f, 1.5f, Color.RED)
        gui.currentCommandList.drawCircleFilled(x, y, 5.0f, Color.RED)
        gui.end()
    }

    override fun dispose() {
        if (ownsGUI)
            gui.dispose()
    }
}