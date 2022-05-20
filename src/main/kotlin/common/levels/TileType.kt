package common.levels

import engine.Game
import engine.graphics.TextureRegion

class TileType(val tileSet: TileSet, var name: String, defaultPath: String, var isSolid: Boolean) {
    class Rule(val tileType: TileType, path: String, var bottom: String? = null, var top: String? = null, var left: String? = null, var right: String? = null) {
        var path = path
            set(value) {
                if (field == value)
                    return

                texture = Game.textures.getOrAdd(tileType.tileSet.file.child(value))
                field = value
            }

        var texture = Game.textures.getOrAdd(tileType.tileSet.file.child(path))
            private set
    }

    private val internalRules = arrayListOf<Rule>()

    var defaultPath = defaultPath
        set(value) {
            if (field == value)
                return

            defaultTexture = Game.textures.getOrAdd(tileSet.file.child(value))
            field = value
        }

    val rules get() = internalRules.asIterable()

    var defaultTexture = Game.textures.getOrAdd(tileSet.file.child(defaultPath))
        private set

    fun addRule(path: String, bottom: String? = null, top: String? = null, left: String? = null, right: String? = null): Rule {
        val rule = Rule(this, path, bottom, top, left, right)
        internalRules.add(rule)
        return rule
    }

    fun removeRule(rule: Rule) {
        internalRules.remove(rule)
    }

    fun getTexture(tile: Tile): TextureRegion {
        return getTexture(tile.left, tile.right, tile.top, tile.bottom)
    }

    fun getTexture(bottom: TileType? = null, top: TileType? = null, left: TileType? = null, right: TileType? = null): TextureRegion {
        for (rule in rules) {
            if (rule.bottom != null && rule.bottom != bottom?.name)
                continue

            if (rule.top != null && rule.top != top?.name)
                continue

            if (rule.left != null && rule.left != left?.name)
                continue

            if (rule.right != null && rule.right != right?.name)
                continue

            return rule.texture
        }

        return defaultTexture
    }
}
