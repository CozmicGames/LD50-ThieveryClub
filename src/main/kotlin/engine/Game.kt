package engine

import com.cozmicgames.Application
import com.cozmicgames.Kore
import com.cozmicgames.utils.injector
import engine.physics.Physics
import engine.audio.SoundManager
import engine.graphics.Canvas
import engine.graphics.Graphics2D
import engine.graphics.Renderer
import engine.graphics.TextureManager
import engine.input.Controls
import engine.utils.Rumble
import game.InitialGameState

object Game : Application {
    val sounds by Kore.context.injector(true) { SoundManager() }
    val textures by Kore.context.injector(true) { TextureManager() }
    val controls by Kore.context.injector(true) { Controls() }
    val graphics2d by Kore.context.injector(true) { Graphics2D() }
    val physics by Kore.context.injector(true) { Physics() }
    val renderer by Kore.context.injector(true) { Renderer() }
    val canvas by Kore.context.injector(true) { Canvas(renderer = renderer) }
    val rumble by Kore.context.injector(true) { Rumble() }

    private lateinit var currentState: GameState

    override fun onCreate() {
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

    }

    override fun onDispose() {
        currentState.onDestroy()
    }
}
