package common

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.input.Keys
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.collections.FixedSizeStack
import com.cozmicgames.utils.injector
import engine.Game
import engine.GameState
import engine.graphics.render.RenderGraph
import engine.graphics.render.onRender
import engine.graphics.render.passes.ColorRenderPass
import engine.graphics.render.present.SimplePresentFunction
import engine.graphics.ui.GUI
import engine.graphics.ui.TextData
import engine.graphics.ui.widgets.*
import game.graphics.LightmapRenderFunction
import game.graphics.MainRenderFunction
import game.graphics.RenderManager
import leveleditor.LevelEditState
import tileseteditor.TileSetEditState
import kotlin.math.min

class InitialGameState : GameState {
    private lateinit var ui: GUI
    private var sizeX = 0.0f
    private var sizeY = 0.0f

    override fun onCreate() {
        Game.physics.doPositionRounding = true

        Game.controls.setActionKeyInput("move_right", Keys.KEY_D)
        Game.controls.setActionKeyInput("move_left", Keys.KEY_A)
        Game.controls.setActionKeyInput("move_jump", Keys.KEY_SPACE)
        Game.controls.setActionKeyInput("move_crouch", Keys.KEY_SHIFT)

        ui = GUI()
    }

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.LIGHT_GRAY)

        var returnState: GameState = this

        ui.begin()
        ui.label("Thievery Club - V0.0.1")

        ui.sameLine {
            ui.label("Width ${10 + (sizeX * 90).toInt()}")
            ui.slider(sizeX) { sizeX = it }
        }

        ui.sameLine {
            ui.label("Height ${10 + (sizeY * 90).toInt()}")
            ui.slider(sizeY) { sizeY = it }
        }

        ui.textButton("New level") {
            returnState = LevelEditState(10 + (sizeX * 90).toInt(), 10 + (sizeY * 90).toInt())
        }

        ui.textButton("New Tileset") {
            returnState = TileSetEditState()
        }

        ui.textButton("UI Test") {
            returnState = TestGameState()
        }

        ui.end()

        return returnState
    }

    override fun onDestroy() {
        ui.dispose()
    }
}

val Game.renderManager by Kore.context.injector(true) { RenderManager() }
val Game.renderGraph by Kore.context.injector(true) { createRenderGraph() }

fun createRenderGraph(): RenderGraph {
    val renderGraph = RenderGraph(SimplePresentFunction("main", 0))
    renderGraph.onRender("lightmap", ColorRenderPass(), LightmapRenderFunction())
    renderGraph.onRender("main", ColorRenderPass(), MainRenderFunction())
    return renderGraph
}
