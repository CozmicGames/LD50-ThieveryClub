package game.graphics

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import common.renderManager
import engine.Game
import engine.graphics.render.RenderFunction

class LightmapRenderFunction : RenderFunction() {
    override fun render(delta: Float) {
        Kore.graphics.clear(Color.BLACK)
        Game.renderManager.renderLightMapPass(delta)
    }
}