package engine.graphics.scene

import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.Drawable
import engine.materials.Material

class ObjectRenderable {
    lateinit var material: Material
    lateinit var drawable: Drawable
    var flipX = false
    var flipY = false
    var layer = 0

    val bounds = Rectangle()

    fun updateBounds() {
        bounds.infinite()
        drawable.vertices.forEach {
            bounds.merge(it.x, it.y)
        }
    }
}