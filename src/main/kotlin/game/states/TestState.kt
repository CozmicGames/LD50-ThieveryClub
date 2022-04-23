package game.states

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import engine.Game
import engine.GameState
import game.levels.Level
import game.levels.generators.TestGenerator
import game.player.Player
import leveleditor.LevelEditState

class TestState: GameState {
    lateinit var level: Level
    lateinit var player: Player

    override fun onCreate() {
        level = Level()
        level.generate(50, 10, TestGenerator())
        player = Player(300.0f, 300.0f)
    }

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.SKY)

        level.update(delta)
        player.update(delta)

        Game.canvas.render(delta)

        return LevelEditState(50, 20)
    }

    override fun onDestroy() {
        level.dispose()
        player.dispose()
    }
}