package game.levels

import com.gratedgames.utils.collections.Array2D
import com.gratedgames.utils.maths.Vector2i

interface LevelGenerator {
    fun generate(tiles: Array2D<Tile>, startPoint: Vector2i)
}