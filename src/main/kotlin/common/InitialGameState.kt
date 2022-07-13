package common

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.input.Keys
import com.cozmicgames.utils.Color
import engine.Game
import engine.GameState
import engine.graphics.ui.GUI
import engine.graphics.ui.widgets.*
import leveleditor.LevelEditState
import tileseteditor.TileSetEditState

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

        ui.textButton("Scene Test") {
            returnState = SceneGameState()
        }

        ui.end()

        return returnState
    }

    override fun onDestroy() {
        ui.dispose()
    }
}
