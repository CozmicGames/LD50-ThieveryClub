package common.levels

import engine.Game
import engine.graphics.TextureRegion

class TileType(var name: String, defaultPath: String, var isSolid: Boolean) {
    class Rule(val top: String?, val left: String?, val right: String?, val bottom: String?, val path: String) {
        val texture by Game.textures(path)
    }

    var defaultPath = defaultPath
        set(value) {
            if (field == value)
                return

            defaultTexture = Game.textures.getOrAdd(value)
            field = value
        }

    val rules = arrayListOf<Rule>()

    var defaultTexture = Game.textures.getOrAdd(defaultPath)
        private set

    fun getTexture(tile: Tile): TextureRegion {
        for (rule in rules) {
            if (rule.top != null && rule.top != tile.top?.name)
                continue

            if (rule.left != null && rule.left != tile.left?.name)
                continue

            if (rule.right != null && rule.right != tile.right?.name)
                continue

            if (rule.bottom != null && rule.bottom != tile.bottom?.name)
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
