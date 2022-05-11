package engine.graphics.ui.layout

import com.cozmicgames.utils.Disposable
import engine.Game
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIContext
import engine.graphics.ui.GUIStyle

class GUILayout : Disposable {
    private val styles = hashMapOf<String, GUIStyle>()
    private val guis = hashMapOf<String, GUI>()
    private val regions = hashMapOf<String, GUIRegion>()

    init {
        addStyle("default") {}
    }

    fun addStyle(name: String, block: GUIStyle.() -> Unit) {
        styles.getOrPut(name) { GUIStyle() }.apply(block)
    }

    fun addRegion(name: String, style: String = "default", block: GUIRegion.() -> Unit) {
        val gui = getGUI(style) ?: requireNotNull(getGUI("default"))
        val region = regions.getOrPut(name) { GUIRegion(gui) }
        region.gui = gui
        region.apply(block)
    }

    fun getGUI(style: String): GUI? {
        if (style !in styles)
            return null

        return guis.getOrPut(style) {
            GUI(GUIContext(Game.renderer), requireNotNull(styles[style]))
        }
    }

    fun render() {
        regions.values.forEach { it.render() }
    }

    override fun dispose() {
        guis.forEach { it.value.dispose() }
    }
}