package common.levels

import com.cozmicgames.files.FileHandle
import com.cozmicgames.utils.Properties

class TileSet(val file: FileHandle) {
    private val internalTileTypes = arrayListOf<TileType>()

    val tileTypes get() = internalTileTypes.toList()

    fun add(name: String, defaultPath: String = "default.png", isSolid: Boolean = false): TileType {
        val tileType = TileType(this, name, defaultPath, isSolid)
        internalTileTypes.add(tileType)
        return tileType
    }

    fun remove(tileType: TileType) {
        internalTileTypes.remove(tileType)
    }

    operator fun get(name: String): TileType? {
        return internalTileTypes.find { it.name == name }
    }

    fun clear() {
        internalTileTypes.clear()
    }

    fun read(properties: Properties) {
        clear()
        val tileTypesProperties = properties.getPropertiesArray("tileTypes") ?: return
        for (tileTypeProperties in tileTypesProperties) {
            val name = tileTypeProperties.getString("path") ?: continue
            val defaultPath = tileTypeProperties.getString("defaultPath") ?: continue
            val isSolid = tileTypeProperties.getBoolean("isSolid") ?: continue

            val tileType = TileType(this, name, defaultPath, isSolid)

            val rulesProperties = tileTypeProperties.getPropertiesArray("rules") ?: continue
            for (ruleProperties in rulesProperties) {

                val path = ruleProperties.getString("path") ?: continue
                val bottom = ruleProperties.getString("bottom") ?: continue
                val top = ruleProperties.getString("top") ?: continue
                val left = ruleProperties.getString("left") ?: continue
                val right = ruleProperties.getString("right") ?: continue

                tileType.addRule(path, bottom, top, left, right)
            }

            internalTileTypes.add(tileType)
        }
    }

    fun write(): Properties {
        val properties = Properties()
        val tileTypesProperties = arrayListOf<Properties>()

        for (tileType in internalTileTypes) {
            val tileTypeProperties = Properties()
            tileTypeProperties.setString("name", tileType.name)
            tileTypeProperties.setBoolean("isSolid", tileType.isSolid)
            tileTypeProperties.setString("defaultPath", tileType.defaultPath)

            val tileTypeRulesProperties = arrayListOf<Properties>()
            for (tileTypeRule in tileType.rules) {
                val tileTypeRuleProperties = Properties()

                tileTypeRule.bottom?.let { tileTypeRuleProperties.setString("bottom", it) }
                tileTypeRule.top?.let { tileTypeRuleProperties.setString("top", it) }
                tileTypeRule.left?.let { tileTypeRuleProperties.setString("left", it) }
                tileTypeRule.right?.let { tileTypeRuleProperties.setString("right", it) }
                tileTypeRuleProperties.setString("path", tileTypeRule.path)

                tileTypeRulesProperties.add(tileTypeRuleProperties)
            }
            tileTypeProperties.setPropertiesArray("rules", tileTypeRulesProperties.toTypedArray())
            tileTypesProperties.add(tileTypeProperties)
        }

        properties.setPropertiesArray("tileTypes", tileTypesProperties.toTypedArray())
        return properties
    }
}
