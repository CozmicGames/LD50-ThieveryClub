package engine.graphics

import com.cozmicgames.input.Key
import com.cozmicgames.utils.maths.Rectangle

interface Component {
    val rectangle: Rectangle

    fun render(delta: Float, renderer: Renderer) {}

    fun update(delta: Float) {}
}