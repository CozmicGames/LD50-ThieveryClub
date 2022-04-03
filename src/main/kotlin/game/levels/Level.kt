package game.levels

import com.gratedgames.utils.Disposable
import com.gratedgames.utils.collections.Array2D
import com.gratedgames.utils.maths.Vector2i

class Level(val width: Int, val height: Int, generator: LevelGenerator) : Disposable {
    companion object {
        const val TILE_SIZE = 16.0f
    }

    val startPoint = Vector2i()

    private val tiles = Array2D<Tile>(width, height)

    init {
        generator.generate(tiles, startPoint)
    }

    fun getTile(x: Int, y: Int): Tile? {
        return tiles[x, y]
    }

    fun update(delta: Float) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                getTile(x, y)?.update(delta)
            }
        }
    }

    override fun dispose() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                getTile(x, y)?.dispose()
            }
        }
        tiles.clear()
    }
}