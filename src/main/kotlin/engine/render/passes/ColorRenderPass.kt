package engine.render.passes

import com.cozmicgames.graphics.gpu.Texture
import engine.render.RenderPass
import engine.render.standardResolution

class ColorRenderPass(resolution: Resolution = standardResolution(), colorFormat: Texture.Format = Texture.Format.RGBA8_UNORM) : RenderPass(resolution) {
    val color = addColorRenderTarget(colorFormat)
}