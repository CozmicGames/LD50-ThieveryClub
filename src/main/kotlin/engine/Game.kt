package engine

import com.cozmicgames.Application
import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.injector
import com.cozmicgames.utils.maths.OrthographicCamera
import common.InitialGameState
import engine.audio.SoundManager
import engine.graphics.*
import engine.graphics.scene.RenderManager
import engine.input.Controls
import engine.physics.Physics
import engine.utils.Rumble

object Game : Application {
    val sounds by Kore.context.injector(true) { SoundManager() }
    val textures by Kore.context.injector(true) { TextureManager() }
    val controls by Kore.context.injector(true) { Controls() }
    val graphics2d by Kore.context.injector(true) { Graphics2D() }
    val physics by Kore.context.injector(true) { Physics() }
    val uiRenderer by Kore.context.injector(true) { Renderer() }
    val canvas by Kore.context.injector(true) { Canvas(renderer = uiRenderer) }
    val rumble by Kore.context.injector(true) { Rumble() }
    val renderer by Kore.context.injector(true) { RenderManager() }
    val camera by Kore.context.injector(true) { OrthographicCamera(Kore.graphics.width, Kore.graphics.height) }

    private lateinit var currentState: GameState

    override fun onCreate() {
        camera.position.setZero()
        camera.update()

        currentState = InitialGameState()
        currentState.onCreate()
    }

    override fun onFrame(delta: Float) {
        val newState = currentState.onFrame(delta)

        if (currentState != newState) {
            currentState.onDestroy()
            newState.onCreate()
            currentState = newState
        }
    }

    override fun onPause() {

    }

    override fun onResume() {

    }

    override fun onResize(width: Int, height: Int) {
        camera.width = width
        camera.height = height
    }

    override fun onDispose() {
        currentState.onDestroy()
    }
}
