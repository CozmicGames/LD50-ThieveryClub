package engine.graphics.ui.immediate

import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.maths.VectorPath
import engine.graphics.Renderer

class ImmediateUIContext(val renderer: Renderer, private val ownsRenderer: Boolean) : Disposable {
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