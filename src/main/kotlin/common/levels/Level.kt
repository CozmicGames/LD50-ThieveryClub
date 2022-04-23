package common.levels

import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.Properties
import com.cozmicgames.utils.collections.Array2D
import engine.graphics.Canvas

class Level(val canvas: Canvas, val tileSet: TileSet) : Disposable {
    companion object {
        const val TILE_SIZE = 16.0f
    }

    private var tiles: Array2D<Tile>? = null
    private var isDirty = true

    var isActive = false

    var width = 0
        private set

    var height = 0
        private set

    fun initialize(width: Int, height: Int) {
        this.width = width
        this.height = height
        tiles = Array2D(width, height)
    }

    fun activate() {
        isActive = true
        for (x in 0 until width) {
            for (y in 0 until height) {
                tiles?.get(x, y)?.activate()
            }
        }
    }

    fun deactivate() {
        isActive = false
        for (x in 0 until width) {
            for (y in 0 until height) {
                tiles?.get(x, y)?.deactivate()
            }
        }
    }

    fun updateBodies() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                tiles?.get(x, y)?.updateBody()
            }
        }
    }

    fun getTile(x: Int, y: Int): TileType? {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return null

        return tiles?.get(x, y)?.type
    }

    fun setTile(x: Int, y: Int, type: TileType?) {
        tiles?.get(x, y)?.dispose()

        if (type != null) {
            val tile = Tile(type)
            tile.initialize(x, y, this)
            tiles?.set(x, y, tile)
        } else
            tiles?.set(x, y, null)

        isDirty = true
    }

    fun update() {
        if (isDirty) {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    tiles?.get(x, y)?.updateSprite()
                }
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
                val type = getTile(x, y)
                if (type != null) {
                    val tileProperties = Properties()
                    tileProperties.setInt("x", x)
                    tileProperties.setInt("y", y)
                    tileProperties.setString("type", type.name)
                    tilesProperties += tileProperties
                }
            }
        }

        properties.setPropertiesArray("tiles", tilesProperties.toTypedArray())

        return properties
    }

    fun read(properties: Properties) {
        clear()

        val width = properties.getInt("width") ?: 0
        val height = properties.getInt("height") ?: 0

        if (width == 0 || height == 0)
            return

        initialize(width, height)

        val tilesProperties = properties.getPropertiesArray("tiles") ?: return

        for (tileProperties in tilesProperties) {
            val x = tileProperties.getInt("x") ?: continue
            val y = tileProperties.getInt("y") ?: continue
            val typeName = tileProperties.getString("type") ?: continue
            val type = tileSet[typeName] ?: continue
            setTile(x, y, type)
        }

        updateBodies()
    }

    fun clear() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                setTile(x, y, null)
            }
        }
    }

    override fun dispose() {
        clear()
    }
}