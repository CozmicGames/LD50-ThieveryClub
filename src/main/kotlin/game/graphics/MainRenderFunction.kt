package game.graphics

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.gpu.getTexture2DUniform
import com.cozmicgames.graphics.gpu.pipeline.PipelineDefinition
import com.cozmicgames.graphics.gpu.update
import com.cozmicgames.utils.Color
import common.renderManager
import engine.Game
import engine.graphics.render.RenderFunction
import engine.graphics.render.colorRenderTargetDependency

class MainRenderFunction : RenderFunction() {
    private val lightmapInput = colorRenderTargetDependency("lightmap", 0)

    private val pipeline = PipelineDefinition(
        """
        //TODO()
    """.trimIndent()
    ).createPipeline()

    private val lightmapUniform = requireNotNull(pipeline.getTexture2DUniform("uLightmap"))

    override fun render(delta: Float) {
        Kore.graphics.clear(Color.SKY)
        lightmapUniform.update(lightmapInput.texture)
        Game.renderManager.renderMainPass(delta)
    }
}