package tileseteditor

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import common.levels.TileSet
import engine.GameState

class TileSetEditState : GameState {
    lateinit var tileSet: TileSet

    override fun onCreate() {
        tileSet = TileSet()
    }

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.DARK_GRAY)



        return this
    }

    override fun onDestroy() {

    }
}