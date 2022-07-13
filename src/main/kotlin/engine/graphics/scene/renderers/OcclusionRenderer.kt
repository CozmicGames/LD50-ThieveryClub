package engine.graphics.scene.renderers

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.*
import com.cozmicgames.graphics.gpu.*
import com.cozmicgames.graphics.gpu.pipeline.PipelineDefinition
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.maths.OrthographicCamera
import engine.Game
import engine.graphics.asRegion
import engine.graphics.scene.LightRenderable
import engine.graphics.scene.ObjectRenderable
import engine.graphics.scene.ObjectRenderableBatcher
import engine.graphics.scene.RenderManager

class OcclusionRenderer(val manager: RenderManager) : Disposable {
    private companion object {
        val VERTEX_LAYOUT = VertexLayout {
            vec2("position")
        }
    }

    private val pipeline = PipelineDefinition(
        """
        #section state
        blend add source_color one_minus_source_color
        
        #section layout
        vec2 position
        vec2 texcoord
        
        #section uniforms
        mat4 uTransform
        sampler2D uColorTexture
        vec4 uColorTextureBounds
        bool uFlipX
        bool uFlipY
        vec4 uColor
        
        #section vertex
        out vec2 vTexcoord;
        
        void main() {
            if (uFlipX)
                vTexcoord.x = uColorTextureBounds.x + 1.0 - texcoord.x * (uColorTextureBounds.z - uColorTextureBounds.x);
            else
                vTexcoord.x = uColorTextureBounds.x + texcoord.x * (uColorTextureBounds.z - uColorTextureBounds.x);
            
            if (uFlipY)
                vTexcoord.y = uColorTextureBounds.y + 1.0 - texcoord.y * (uColorTextureBounds.w - uColorTextureBounds.y);
            else
                vTexcoord.y = uColorTextureBounds.y + texcoord.y * (uColorTextureBounds.w - uColorTextureBounds.y);
            
            gl_Position = uTransform * vec4(position, 0.0, 1.0);
        }
        
        #section fragment
        in vec2 vTexcoord;
        
        out float outOcclusion;
        
        void main() {
            float alpha = texture(uColorTexture, vTexcoord).a * uColor.a;
            outOcclusion = alpha;
        }
    """.trimIndent()
    ).createPipeline()

    private val defaultColorTexture = Kore.graphics.createTexture2D(Texture.Format.RGBA8_UNORM, Game.graphics2d.pointClampSampler) {
        setImage(Image(1, 1).also {
            it[0, 0] = Color.CLEAR
        })
    }

    private val batcher = ObjectRenderableBatcher()
    private val vertexBuffer = Kore.graphics.createBuffer(GraphicsBuffer.Usage.DYNAMIC)
    private val indexBuffer = Kore.graphics.createBuffer(GraphicsBuffer.Usage.DYNAMIC)
    private val camera = OrthographicCamera(0, 0)

    fun render(objectRenderables: List<ObjectRenderable>, lightRenderable: LightRenderable) {
        camera.position.x = lightRenderable.transform.x
        camera.position.y = lightRenderable.transform.y
        camera.width = manager.singleShadowMapSize
        camera.height = manager.singleShadowMapSize
        camera.update()

        objectRenderables.forEach {
            if (it.material.castsShadows && it.bounds intersects camera.rectangle)
                batcher.submit(it.material, it.drawable, it.flipX, it.flipY)
        }

        Kore.graphics.setPipeline(pipeline)
        Kore.graphics.setVertexBuffer(vertexBuffer, VERTEX_LAYOUT)
        Kore.graphics.setIndexBuffer(indexBuffer)

        pipeline.getMatrixUniform("uTransform")?.update(camera.projectionView)

        batcher.flush { batch ->
            batch.material?.let { material ->
                val colorTexture = Game.textures[material.colorTexturePath] ?: defaultColorTexture.asRegion()

                pipeline.getTexture2DUniform("uColorTexture")?.update(colorTexture.texture)
                pipeline.getFloatUniform("uColorTextureBounds")?.update {
                    it[0] = colorTexture.u0
                    it[1] = colorTexture.v0
                    it[2] = colorTexture.u1
                    it[3] = colorTexture.v1
                }

                pipeline.getFloatUniform("uColor")?.update(material.color)
            }

            pipeline.getBooleanUniform("uFlipX")?.update(batch.flipX)
            pipeline.getBooleanUniform("uFlipY")?.update(batch.flipY)

            batch.builder.updateBuffers(vertexBuffer, indexBuffer)
            Kore.graphics.drawIndexed(Primitive.TRIANGLES, batch.builder.numIndices, 0, IndexDataType.INT)
        }

        Kore.graphics.setVertexBuffer(null, VERTEX_LAYOUT)
        Kore.graphics.setIndexBuffer(null)
        Kore.graphics.setPipeline(null)
    }

    override fun dispose() {
        pipeline.dispose()
        defaultColorTexture.dispose()
        batcher.dispose()
        vertexBuffer.dispose()
        indexBuffer.dispose()
    }
}