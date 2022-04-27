package engine.graphics.sprite

import com.cozmicgames.utils.Color
import engine.graphics.Component
import engine.graphics.Drawable
import engine.graphics.Renderer
import engine.graphics.TextureRegion
import engine.utils.Transform

abstract class Sprite(val transform: Transform) : Drawable, Component {
    val color = Color.WHITE.copy()
    val bounds = SpriteBounds()

    var isFlippedX = false
    var isFlippedY = false

    protected var isDirty = true

    abstract val texture: TextureRegion

    init {
        transform.addChangeListener {
            isDirty = true
        }
    }

    override val rectangle get() = bounds.rectangle

    protected abstract fun updateVertices()

    override fun update(delta: Float) {
        if (isDirty) {
            updateVertices()
            isDirty = false
        }
    }

    override fun render(delta: Float, renderer: Renderer) {
        renderer.withFlippedX(isFlippedX) {
            renderer.withFlippedY(isFlippedY) {
                renderer.draw(this)
            }
        }
    }
}