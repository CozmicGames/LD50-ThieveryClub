package game

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.graphics
import com.cozmicgames.input.Keys
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Properties
import com.cozmicgames.utils.use
import engine.Game
import engine.GameState
import game.levels.Level
import game.levels.generators.TestGenerator
import game.player.Player

class InitialGameState : GameState {
    lateinit var level: Level
    lateinit var player: Player

    override fun onCreate() {
        level = Level()
        level.generate(50, 10, TestGenerator())
        player = Player(300.0f, 300.0f)

        Game.controls.setActionKeyInput("move_right", Keys.KEY_D)
        Game.controls.setActionKeyInput("move_left", Keys.KEY_A)
        Game.controls.setActionKeyInput("move_jump", Keys.KEY_SPACE)
        Game.controls.setActionKeyInput("move_crouch", Keys.KEY_SHIFT)
    }

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.SKY)

        level.update(delta)
        player.update(delta)

        Game.canvas.render(delta)

        return this
    }

    override fun onDestroy() {
        level.dispose()
        player.dispose()
    }
}
