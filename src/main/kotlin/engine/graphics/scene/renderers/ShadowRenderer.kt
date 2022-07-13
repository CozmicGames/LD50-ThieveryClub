package engine.graphics.scene.renderers

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.*
import com.cozmicgames.graphics.gpu.*
import com.cozmicgames.graphics.gpu.pipeline.PipelineDefinition
import com.cozmicgames.memory.Memory
import com.cozmicgames.memory.of
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.maths.OrthographicCamera
import com.cozmicgames.utils.use
import engine.graphics.scene.*

class ShadowRenderer(private val manager: RenderManager) : Disposable {
    companion object {
        val VERTEX_LAYOUT = VertexLayout {
            vec2("position")
        }
    }

    private val pipeline = PipelineDefinition(
        """
        #section uniforms
        float uShadowMapSize
        float uShadowMapUpscale
        mat4 uTransform
        sampler2D uOcclusionTexture
        float uTest
        
        #section layout
        vec2 position
        
        #section vertex
        out vec2 vPosition;

        void main() {
            vPosition = position;
            gl_Position = uTransform * vec4(position.x * uShadowMapSize, position.y, 0.0, 1.0);
        }
        
        #section fragment
        in vec2 vPosition;
                            
        out float outDistance;
                
        void main() {
            const float THRESHOLD = 0.75;
            
            float distance = 1.0;
            
            for (float y = 0.0; y < uShadowMapSize; y += 1.0) {
                vec2 normalizedDirection = vec2(vPosition.x, y / uShadowMapSize) * 2.0 - 1.0;
                float theta = PI * 1.5 + normalizedDirection.x * PI;
                float r = normalizedDirection.y + 1.0;
   
                vec2 polarCoord = vec2(-r * sin(theta), -r * cos(theta)) * 0.5 + 0.5;

                float occlusion = texture(uOcclusionTexture, vec2(1.0 - polarCoord.x, polarCoord.y)).r;

                float occlusionDistance = y / uShadowMapSize / uShadowMapUpscale;

                if (occlusion > THRESHOLD)
                    distance = min(distance, occlusionDistance);
            }
            
            outDistance = distance;
        }
    """.trimIndent()
    ).createPipeline()

    private val batcher = ObjectRenderableBatcher()
    private val camera = OrthographicCamera(0, 0)
    private val vertexBuffer = Kore.graphics.createBuffer(GraphicsBuffer.Usage.STATIC)
    private val indexBuffer = Kore.graphics.createBuffer(GraphicsBuffer.Usage.STATIC)

    init {
        Memory.of(
            -1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f).use {
            vertexBuffer.setData(it)
        }

        Memory.of(0, 1, 2, 2, 3, 0).use {
            indexBuffer.setData(it)
        }
    }

    fun render(occlusionTexture: Texture2D) {
        camera.position.x = 0.0f
        camera.position.y = 0.0f
        camera.width = manager.singleShadowMapSize
        camera.height = 1
        camera.update()

        Kore.graphics.setPipeline(pipeline)
        Kore.graphics.setVertexBuffer(vertexBuffer, VERTEX_LAYOUT)
        Kore.graphics.setIndexBuffer(indexBuffer)

        pipeline.getFloatUniform("uShadowMapSize")?.update(manager.singleShadowMapSize.toFloat())
        pipeline.getFloatUniform("uShadowMapUpscale")?.update(manager.singleShadowMapUpscale)
        pipeline.getMatrixUniform("uTransform")?.update(camera.projectionView)
        pipeline.getTexture2DUniform("uOcclusionTexture")?.update(occlusionTexture)

        Kore.graphics.drawIndexed(Primitive.TRIANGLES, 6, 0, IndexDataType.INT)

        Kore.graphics.setViewport(null)
        Kore.graphics.setPipeline(null)
        Kore.graphics.setVertexBuffer(null, VERTEX_LAYOUT)
        Kore.graphics.setIndexBuffer(null)
    }

    override fun dispose() {
        pipeline.dispose()
        batcher.dispose()
        vertexBuffer.dispose()
        indexBuffer.dispose()
    }
}