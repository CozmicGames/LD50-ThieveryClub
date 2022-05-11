package engine.graphics.ui

import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.maths.VectorPath
import engine.graphics.Renderer

class GUIContext(val renderer: Renderer, private val ownsRenderer: Boolean = false) : Disposable {
    constructor() : this(Renderer(), true)

    private val path = VectorPath()

    fun path(block: VectorPath.() -> Unit): VectorPath {
        path.clear()
        block(path)
        return path
    }

    override fun dispose() {
        if (ownsRenderer)
            renderer.dispose()
    }
}