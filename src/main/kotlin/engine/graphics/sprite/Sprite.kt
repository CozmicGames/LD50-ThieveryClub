package engine.graphics.sprite

import engine.graphics.Component
import engine.graphics.Drawable
import engine.graphics.Renderer
import engine.utils.Transform

class Sprite(val transform: Transform) : Drawable, Component {
    private var isDirty = true

    val bounds = DrawableBounds()
    var isFlippedX = false
    var isFlippedY = false

    override val rectangle get() = bounds.rectangle

    override val vertices = arrayOf(
        Drawable.Vertex(-0.5f, -0.5f, 0.0f, 0.0f),
        Drawable.Vertex(0.5f, -0.5f, 1.0f, 0.0f),
        Drawable.Vertex(0.5f, 0.5f, 1.0f, 1.0f),
        Drawable.Vertex(-0.5f, 0.5f, 0.0f, 1.0f)
    )

    override val indices = arrayOf(0, 1, 2, 0, 2, 3)

    init {
        transform.addChangeListener {
            isDirty = true
        }
    }

    private fun updateVertices() {
        transform.transform(-0.5f, -0.5f) { x, y ->
            vertices[0].x = x
            vertices[0].y = y
        }

        transform.transform(0.5f, -0.5f) { x, y ->
            vertices[1].x = x
            vertices[1].y = y
        }

        transform.transform(0.5f, 0.5f) { x, y ->
            vertices[2].x = x
            vertices[2].y = y
        }

        transform.transform(-0.5f, 0.5f) { x, y ->
            vertices[3].x = x
            vertices[3].y = y
        }

        bounds.update(vertices[0].x, vertices[0].y, vertices[1].x, vertices[1].y, vertices[2].x, vertices[2].y, vertices[3].x, vertices[3].y)
    }

    override fun update(delta: Float) {
        if (isDirty) {
            updateVertices()
            isDirty = false
        }
    }

    override fun render(delta: Float, renderer: Renderer) {
        renderer.withFlippedX(isFlippedX) {
            renderer.withFlippedY(isFlippedY) {
                //renderer.draw(this)
            }
        }
    }
}