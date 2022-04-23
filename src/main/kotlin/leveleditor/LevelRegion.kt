package leveleditor

import com.cozmicgames.utils.collections.Array2D
import com.cozmicgames.utils.maths.Vector2i
import common.levels.Level
import common.levels.TileType
import kotlin.math.min

class LevelRegion(val level: Level, val x: Int, val y: Int, val width: Int, val height: Int) {
    fun getTiles(): Array2D<TileType?> {
        return Array2D(width, height) { x, y ->
            level.getTile(x + this.x, y + this.y)
        }
    }

    fun setTiles(width: Int = this.width, height: Int = this.height, getTile: (Int, Int) -> TileType?) {
        repeat(min(width, this.width)) { x ->
            repeat(min(height, this.height)) { y ->
                level.setTile(x + this.x, y + this.y, getTile(x, y))
            }
        }
    }

    fun setTiles(tiles: Array2D<TileType?>) {
        setTiles(tiles.width, tiles.height) { x, y ->
            tiles[x, y]
        }
    }

    fun setTiles(width: Int = this.width, height: Int = this.height, tile: TileType) {
        setTiles(width, height) { _, _ ->
            tile
        }
    }

    fun copy(level: Level = this.level, x: Int = this.x, y: Int = this.y, width: Int = this.width, height: Int = this.height): LevelRegion {
        return LevelRegion(level, x, y, width, height)
    }

    override fun toString(): String {
        return "LevelRegion(level=$level, x=$x, y=$y, width=$width, height=$height)"
    }

    override fun hashCode(): Int {
        var result = level.hashCode()
        result = 31 * result + x
        result = 31 * result + y
        result = 31 * result + width
        result = 31 * result + height
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is LevelRegion) return false

        if (level != other.level) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (width != other.width) return false
        if (height != other.height) return false
        return true
    }
}

fun LevelRegion.setTiles(region: LevelRegion) = setTiles(region.getTiles())

fun Level.getRegion(x: Int, y: Int, width: Int, height: Int) = LevelRegion(this, x, y, width, height)

fun Level.getRegion(a: Vector2i, b: Vector2i) = getRegion(a.x, a.y, (b.x - a.x) + 1, (b.y - a.y) + 1)
