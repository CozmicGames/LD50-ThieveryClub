package common.levels

import com.cozmicgames.Kore
import com.cozmicgames.files
import engine.Game
import engine.graphics.TextureRegion

class TileType(var name: String, defaultPath: String, var isSolid: Boolean) {
    class Rule(path: String, var top: String? = null, var left: String? = null, var right: String? = null, var bottom: String? = null) {
        var path = path
            set(value) {
                if (field == value)
                    return

                texture = Game.textures.getOrAdd(Kore.files.absolute(value))
                field = value
            }

        var texture = Game.textures.getOrAdd(Kore.files.absolute(path))
            private set
    }

    var defaultPath = defaultPath
        set(value) {
            if (field == value)
                return

            defaultTexture = Game.textures.getOrAdd(Kore.files.absolute(value))
            field = value
        }

    val rules = arrayListOf<Rule>()

    var defaultTexture = Game.textures.getOrAdd(Kore.files.absolute(defaultPath))
        private set

    fun getTexture(tile: Tile): TextureRegion {
        return getTexture(tile.left, tile.right, tile.top, tile.bottom)
    }

    fun getTexture(left: TileType? = null, right: TileType? = null, top: TileType? = null, bottom: TileType? = null): TextureRegion {
        for (rule in rules) {
            if (rule.top != null && rule.top != top?.name)
                continue

            if (rule.left != null && rule.left != left?.name)
                continue

            if (rule.right != null && rule.right != right?.name)
                continue

            if (rule.bottom != null && rule.bottom != bottom?.name)
                continue

            return rule.texture
        }

        return defaultTexture
    }
}

//TODO: Remove this
fun createTestTileSet(): TileSet {
    val ground = TileType("ground", "images/ground.png", true)
    val dirt = TileType("dirt", "images/dirt.png", true)
    val grass = TileType("grass", "images/grass.png", true)
    val stone = TileType("stone", "images/stone.png", true)

    val tileSet = TileSet()
    tileSet.add(ground)
    tileSet.add(dirt)
    tileSet.add(grass)
    tileSet.add(stone)
    return tileSet
}
