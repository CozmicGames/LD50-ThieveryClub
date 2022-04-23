package common.levels

import engine.Game
import engine.graphics.TextureRegion

class TileType(val name: String, val defaultPath: String, val isSolid: Boolean) {
    class Rule(val top: String, val left: String, val right: String, val bottom: String, val path: String) {
        val texture by Game.textures(path)
    }

    private val internalRules = arrayListOf<Rule>()

    val defaultTexture by Game.textures(defaultPath)
    val rules get() = internalRules.toList()

    fun addRule(top: String, left: String, right: String, bottom: String, path: String) {
        internalRules.add(Rule(top, left, right, bottom, path))
    }

    fun getTexture(tile: Tile): TextureRegion {
        val rule = internalRules.firstOrNull {
            it.top == tile.top?.name && it.left == tile.left?.name && it.right == tile.right?.name && it.bottom == tile.bottom?.name
        } ?: return defaultTexture

        return rule.texture
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
