package engine.graphics.ui.gizmos

import com.cozmicgames.Kore
import com.cozmicgames.input
import com.cozmicgames.input.InputListener
import com.cozmicgames.input.Keys
import com.cozmicgames.input.addListener
import com.cozmicgames.input.removeListener
import com.cozmicgames.log
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.maths.*
import engine.graphics.begin
import engine.graphics.ui.GUIContext
import engine.scene.GameObject
import engine.scene.Scene
import engine.scene.components.TransformComponent
import engine.utils.Transform
import engine.utils.unproject

class Gizmos(val context: GUIContext = GUIContext()) : Disposable {
    enum class TransformMode {
        TRANSLATION,
        ROTATION,
        SCALE
    }

    private var isActive = false
    private lateinit var camera: Camera

    private var currentScrollAmount = 0.0f

    private val inputListener = object : InputListener {
        override fun onScroll(x: Float, y: Float) {
            currentScrollAmount += y
        }
    }

    var zoomFactor = 0.1f

    init {
        Kore.input.addListener(inputListener)
    }

    fun selectGameObject(scene: Scene, filter: (GameObject) -> Boolean = { true }, callback: (GameObject) -> Unit) {
        if (!Kore.input.justTouchedDown)
            return

        val temp = camera.unproject(Kore.input.x.toFloat(), Kore.input.y.toFloat())
        val touchPosition = Vector2(temp.x, temp.y)

        for (gameObject in scene) {
            if (!filter(gameObject))
                continue

            val transform = gameObject.getComponent<TransformComponent>()?.transform ?: continue
            val boundsPath = getBoundsPath(transform)

            if (touchPosition in boundsPath) {
                callback(gameObject)
                return
            }
        }
    }

    fun editTransform(transform: Transform, mode: TransformMode) = when (mode) {
        TransformMode.TRANSLATION -> editTranslation(transform)
        TransformMode.ROTATION -> editRotation(transform)
        TransformMode.SCALE -> editScale(transform)
    }

    private fun getBoundsPath(transform: Transform): VectorPath {
        val path = VectorPath()
        transform.transform(-0.5f, -0.5f, path::add)
        transform.transform(0.5f, -0.5f, path::add)
        transform.transform(0.5f, 0.5f, path::add)
        transform.transform(-0.5f, 0.5f, path::add)
        return path
    }

    fun editTranslation(transform: Transform) {
        require(isActive)

        val boundsPath = getBoundsPath(transform)

        val temp = camera.unproject(Kore.input.x.toFloat(), Kore.input.y.toFloat())
        val touchPosition = Vector2(temp.x, temp.y)

        if (touchPosition in boundsPath) {
            if (Kore.input.isTouched) {
                transform.x += Kore.input.deltaX
                transform.y += Kore.input.deltaY
            }
        }

        context.renderer.drawPathStroke(boundsPath, 1.5f, true, Color.RED)
    }

    fun editRotation(transform: Transform) {
        require(isActive)

        val boundsPath = getBoundsPath(transform)

        context.renderer.drawPathStroke(boundsPath, 1.5f, true, Color.RED)

        if (!Kore.input.isTouched)
            return

        val temp = camera.unproject(Kore.input.x.toFloat(), Kore.input.y.toFloat())
        val touchPosition = Vector2(temp.x, temp.y)

        camera.unproject(Kore.input.lastX.toFloat(), Kore.input.lastY.toFloat(), dest = temp)
        val lastTouchPosition = Vector2(temp.x, temp.y)

        val angle = touchPosition.angle(lastTouchPosition)

        transform.rotation -= angle
    }

    fun editScale(transform: Transform) {
        require(isActive)

        val boundsPath = getBoundsPath(transform)

        context.renderer.drawPathStroke(boundsPath, 1.5f, true, Color.RED)

        val temp = camera.unproject(Kore.input.x.toFloat(), Kore.input.y.toFloat())
        val touchPosition = Vector2(temp.x, temp.y)

        if (Kore.input.isTouched) {
            camera.unproject(Kore.input.lastX.toFloat(), Kore.input.lastY.toFloat(), dest = temp)
            val lastTouchPosition = Vector2(temp.x, temp.y)

            val distance = touchPosition.copy().sub(lastTouchPosition)

            val m = Matrix2x2()
            m.setRotation(transform.rotation)
            m.transpose()
            m.transform(distance)

            transform.scaleX += distance.x
            transform.scaleY += distance.y
        }
    }

    fun begin(camera: Camera) {
        if (isActive)
            Kore.log.fail(this::class, "Gizmos are already active.")

        if (Kore.input.isKeyDown(Keys.KEY_CONTROL)) {
            if (camera is OrthographicCamera)
                camera.zoom += currentScrollAmount * zoomFactor

            currentScrollAmount = 0.0f

            camera.position.x -= Kore.input.deltaX
            camera.position.y -= Kore.input.deltaY
            camera.update()
        }

        context.renderer.begin(camera)
        this.camera = camera
        isActive = true
    }

    fun end() {
        if (!isActive)
            Kore.log.fail(this::class, "Gizmos are not active.")

        context.renderer.end()
        isActive = false
    }

    override fun dispose() {
        context.dispose()
        Kore.input.removeListener(inputListener)
    }
}


