package game.levels

import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.Properties
import com.cozmicgames.utils.Reflection
import com.cozmicgames.utils.collections.Array2D

class Level : Disposable {
    companion object {
        const val TILE_SIZE = 16.0f
    }

    private var tiles: Array2D<Tile>? = null

    var width = 0
        private set

    var height = 0
        private set

    fun generate(width: Int, height: Int, generator: LevelGenerator) {
        this.width = width
        this.height = height
        tiles = Array2D(width, height)
        generator.generate(this)
    }

    fun getTile(x: Int, y: Int): Tile? {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return null

        return tiles?.get(x, y)
    }

    fun setTile(x: Int, y: Int, tile: Tile?) {
        tiles?.get(x, y)?.dispose()
        tiles?.set(x, y, tile)
        tile?.initialize(x, y, this)
    }

    fun update(delta: Float) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                getTile(x, y)?.update(delta)
            }
        }
    }

    fun write(): Properties {
        val properties = Properties()

        properties.setInt("width", width)
        properties.setInt("height", height)

        if (tiles == null)
            return properties

        val tilesProperties = arrayListOf<Properties>()

        for (x in 0 until width) {
            for (y in 0 until height) {
                val tile = getTile(x, y)
                if (tile != null) {
                    val tileProperties = Properties()
                    tileProperties.setInt("x", x)
                    tileProperties.setInt("y", y)
                    tileProperties.setString("type", Reflection.getClassName(tile::class))
                    tile.write(tileProperties)
                    tilesProperties += tileProperties
                }
            }
        }

        properties.setPropertiesArray("tiles", tilesProperties.toTypedArray())

        return properties
    }

    fun clear() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                getTile(x, y)?.dispose()
            }
        }
        tiles = null
    }

    fun read(properties: Properties) {
        clear()

        width = properties.getInt("width") ?: 0
        height = properties.getInt("height") ?: 0

        if (width == 0 || height == 0)
            return

        tiles = Array2D(width, height)

        val tilesProperties = properties.getPropertiesArray("tiles") ?: return

        for (tileProperties in tilesProperties) {
            val x = tileProperties.getInt("x") ?: continue
            val y = tileProperties.getInt("y") ?: continue
            val typeName = tileProperties.getString("type") ?: continue
            val type = Reflection.getClassByName(typeName) ?: continue
            val tile = Reflection.createInstance(type) as? Tile ?: continue
            tile.read(tileProperties)
            setTile(x, y, tile)
        }
    }

    override fun dispose() {
        clear()
    }
}