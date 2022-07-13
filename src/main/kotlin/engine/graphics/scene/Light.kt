package engine.graphics.scene

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.UUID
import com.cozmicgames.utils.maths.Vector2
import engine.scene.GameObject
import kotlin.math.sqrt

class Light {
    val color = Color.WHITE.copy()
    var intensity = 1.0f
    var range = 16.0f
    var castsShadows = true
}