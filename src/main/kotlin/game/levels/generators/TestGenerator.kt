package game.levels.generators

import com.gratedgames.utils.collections.Array2D
import com.gratedgames.utils.maths.Vector2i
import game.levels.LevelGenerator
import game.levels.Tile
import game.levels.tiles.GroundTile

class TestGenerator : LevelGenerator {
    override fun generate(tiles: Array2D<Tile>, startPoint: Vector2i) {
        for (x in 0 until tiles.width) {
            repeat(4) {
                tiles[x, it] = GroundTile(x, it)
            }
        }

        repeat(5) {
            tiles[5 + it, 5] = GroundTile(5 + it, 5)
            tiles[5 + it, 6] = GroundTile(5 + it, 6)
        }

        repeat(5) {
            tiles[20 + it, 5] = GroundTile(20 + it, 5)
        }
    }
}