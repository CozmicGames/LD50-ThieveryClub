package engine.graphics

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.safeHeight
import com.cozmicgames.graphics.safeWidth
import com.cozmicgames.input
import com.cozmicgames.input.Key
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.maths.OrthographicCamera
import com.cozmicgames.utils.maths.Vector2

class Canvas(width: Int = Kore.graphics.safeWidth, height: Int = Kore.graphics.safeHeight, val renderer: Renderer = Renderer()) : Disposable {
    var width = width
        private set

    var height = height
        private set

    var camera = OrthographicCamera(width, height)

    private val components = arrayListOf<Component>()

    fun resize(width: Int, height: Int, resetPosition: Boolean = true) {
        this.width = width
        this.height = height

        camera.width = width
        camera.height = height
        if (resetPosition)
            camera.resetPosition()
        camera.update()
    }

    fun render(delta: Float) {
        renderer.begin(camera)

        components.forEach {
            it.update(delta)

            if (it.rectangle intersects camera.rectangle)
                it.render(delta, renderer)
        }

        renderer.end()
    }

    fun addComponent(component: Component) {
        components += component
    }

    fun removeComponent(component: Component) {
        components.remove(component)
    }

    operator fun plusAssign(component: Component) = addComponent(component)

    operator fun minusAssign(component: Component) = removeComponent(component)

    override fun dispose() {
        components.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}