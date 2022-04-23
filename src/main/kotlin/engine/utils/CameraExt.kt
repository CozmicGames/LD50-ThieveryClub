package engine.utils

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.safeHeight
import com.cozmicgames.graphics.safeWidth
import com.cozmicgames.utils.maths.Camera
import com.cozmicgames.utils.maths.Vector3

fun Camera.unproject(x: Float, y: Float, z: Float = 0.0f, viewportX: Float = Kore.graphics.safeInsetLeft.toFloat(), viewportY: Float = Kore.graphics.safeInsetBottom.toFloat(), viewportWidth: Float = Kore.graphics.safeWidth.toFloat(), viewportHeight: Float = Kore.graphics.safeHeight.toFloat(), dest: Vector3 = Vector3()): Vector3 {
    val xx = x - viewportX
    val yy = y - viewportY
    val nx = 2.0f * xx / viewportWidth - 1.0f
    val ny = 2.0f * yy / viewportHeight - 1.0f
    val nz = 2.0f * z - 1.0f

    return inverseProjectionView.project(nx, ny, nz) { px, py, pz ->
        dest.set(px, py, pz)
    }
}
