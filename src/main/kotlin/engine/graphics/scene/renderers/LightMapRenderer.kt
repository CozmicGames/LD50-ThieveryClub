package engine.graphics.scene.renderers

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.IndexDataType
import com.cozmicgames.graphics.Primitive
import com.cozmicgames.graphics.gpu.*
import com.cozmicgames.graphics.gpu.pipeline.PipelineDefinition
import com.cozmicgames.graphics.setVertexBuffer
import com.cozmicgames.memory.Memory
import com.cozmicgames.memory.of
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.maths.OrthographicCamera
import com.cozmicgames.utils.use
import engine.graphics.scene.LightRenderable
import engine.graphics.scene.RenderManager

class LightMapRenderer(private val manager: RenderManager) : Disposable {
    companion object {
        val VERTEX_LAYOUT = VertexLayout {
            vec2("position")
        }
    }

    private val pipeline = PipelineDefinition(
        """
        #section state
        blend add source_alpha one_minus_source_alpha
        cull back
    
        #section layout
        vec2 position
        
        #section uniforms
        vec2 uLightPosition
        vec3 uLightColor
        float uLightRange
        mat4 uTransform
        
        #section vertex
        in vec2 position;
        
        out vec2 vPosition;

        void main() {
            vPosition = uLightPosition + position * uLightRange;
            gl_Position = uTransform * vec4(uLightPosition + position * uLightRange, 0.0, 1.0);
        }
        
        #section fragment
        in vec2 vPosition;
        
        out vec4 outLightDirection;
        out vec4 outLightMap;
        
        void main() {
            vec2 lightDirection = uLightPosition - vPosition;
            float distance = length(lightDirection);
            float attenuation = max(1.0 - ((distance * distance) / (uLightRange * uLightRange)), 0.0);
            attenuation *= attenuation;
            
            outLightMap = vec4(uLightColor.rgb, attenuation);
        }
    """.trimIndent()
    ).createPipeline()

    private val vertexBuffer = Kore.graphics.createBuffer(GraphicsBuffer.Usage.STATIC)
    private val indexBuffer = Kore.graphics.createBuffer(GraphicsBuffer.Usage.STATIC)

    init {
        Memory.of(-1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f).use {
            vertexBuffer.setData(it)
        }

        Memory.of(0, 1, 2, 2, 3, 0).use {
            indexBuffer.setData(it)
        }
    }

    fun render(camera: OrthographicCamera, lightRenderables: List<LightRenderable>) {
        Kore.graphics.setPipeline(pipeline)
        Kore.graphics.setVertexBuffer(vertexBuffer, VERTEX_LAYOUT)
        Kore.graphics.setIndexBuffer(indexBuffer)

        pipeline.getMatrixUniform("uTransform")?.update(camera.projectionView)

        lightRenderables.forEach {
            pipeline.getFloatUniform("uLightPosition")?.update(it.transform.x, it.transform.y)
            pipeline.getFloatUniform("uLightColor")?.update { data ->
                data[0] = it.light.color.r * it.light.intensity
                data[1] = it.light.color.g * it.light.intensity
                data[2] = it.light.color.b * it.light.intensity
            }
            pipeline.getFloatUniform("uLightRange")?.update(it.light.range)

            Kore.graphics.drawIndexed(Primitive.TRIANGLES, 6, 0, IndexDataType.INT)
        }

        Kore.graphics.setVertexBuffer(null, VERTEX_LAYOUT)
        Kore.graphics.setIndexBuffer(null)
        Kore.graphics.setPipeline(null)
    }

    override fun dispose() {
        vertexBuffer.dispose()
        indexBuffer.dispose()
        pipeline.dispose()
    }
}