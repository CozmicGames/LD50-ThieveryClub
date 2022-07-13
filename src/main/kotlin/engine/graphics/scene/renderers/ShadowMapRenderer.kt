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

class ShadowMapRenderer(private val manager: RenderManager) : Disposable {
    companion object {
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
        
        #section uniforms
        vec2 uPosition
        float uSize
        mat4 uTransform
        float uResolution
        float uSoftShadows
        sampler2D uShadowTexture
        
        #section vertex
        out vec2 vTexcoord;

        void main() {
            vTexcoord = position * 0.5 + 0.5;
            gl_Position = uTransform * vec4(uPosition + position * uSize, 0.0, 1.0);
        }
        
        #section fragment
        in vec2 vTexcoord;
        
        out float outShadowMap;
        
        float sampleShadowTexture(vec2 location, float r) {
            return step(r, texture(uShadowTexture, vec2(location.x, location.y)).r);
        }
        
        void main() {
            vec2 normalizedTexcoord = vTexcoord * 2.0 - 1.0;
            float theta = atan(normalizedTexcoord.y, normalizedTexcoord.x);
            float r = length(normalizedTexcoord);
            float polarCoord = (theta + PI) / (2.0 * PI);

            vec2 sampleLocation = vec2(polarCoord, 0.0);

            float blur = (1.0 / uResolution)  * smoothstep(0.0, 1.0, r);

            float sum = 0.0;

            float center = sampleShadowTexture(sampleLocation, r);

            sum += sampleShadowTexture(vec2(sampleLocation.x - 4.0 * blur, sampleLocation.y), r) * 0.05;
            sum += sampleShadowTexture(vec2(sampleLocation.x - 3.0 * blur, sampleLocation.y), r) * 0.09;
            sum += sampleShadowTexture(vec2(sampleLocation.x - 2.0 * blur, sampleLocation.y), r) * 0.12;
            sum += sampleShadowTexture(vec2(sampleLocation.x - 1.0 * blur, sampleLocation.y), r) * 0.15;
            sum += center * 0.16;
            sum += sampleShadowTexture(vec2(sampleLocation.x + 1.0 * blur, sampleLocation.y), r) * 0.15;
            sum += sampleShadowTexture(vec2(sampleLocation.x + 2.0 * blur, sampleLocation.y), r) * 0.12;
            sum += sampleShadowTexture(vec2(sampleLocation.x + 3.0 * blur, sampleLocation.y), r) * 0.09;
            sum += sampleShadowTexture(vec2(sampleLocation.x + 4.0 * blur, sampleLocation.y), r) * 0.05;

            float amount = mix(center, sum, uSoftShadows);

            outShadowMap = amount * smoothstep(1.0, 0.0, r);
        }
    """.trimIndent()
    ).createPipeline()

    private val vertexBuffer = Kore.graphics.createBuffer(GraphicsBuffer.Usage.STATIC)
    private val indexBuffer = Kore.graphics.createBuffer(GraphicsBuffer.Usage.STATIC)

    init {
        Memory.of(
            -1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
        ).use {
            vertexBuffer.setData(it)
        }

        Memory.of(0, 1, 2, 2, 3, 0).use {
            indexBuffer.setData(it)
        }
    }

    fun render(camera: OrthographicCamera, lightRenderable: LightRenderable, shadowTexture: Texture2D) {
        Kore.graphics.setPipeline(pipeline)
        Kore.graphics.setVertexBuffer(vertexBuffer, VERTEX_LAYOUT)
        Kore.graphics.setIndexBuffer(indexBuffer)

        pipeline.getTexture2DUniform("uShadowTexture")?.update(shadowTexture)
        pipeline.getFloatUniform("uResolution")?.update(manager.singleShadowMapSize.toFloat())
        pipeline.getFloatUniform("uSoftShadows")?.update(if (manager.softShadows) 1.0f else 0.0f)
        pipeline.getMatrixUniform("uTransform")?.update(camera.projectionView)

        pipeline.getFloatUniform("uPosition")?.update(lightRenderable.transform.x, lightRenderable.transform.y)
        pipeline.getFloatUniform("uSize")?.update(manager.singleShadowMapSize * manager.singleShadowMapUpscale)

        Kore.graphics.drawIndexed(Primitive.TRIANGLES, 6, 0, IndexDataType.INT)

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