package engine.graphics.scene.renderers

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.*
import com.cozmicgames.graphics.gpu.*
import com.cozmicgames.graphics.gpu.pipeline.PipelineDefinition
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.collections.DynamicArray
import com.cozmicgames.utils.collections.getOrPut
import com.cozmicgames.utils.maths.OrthographicCamera
import com.cozmicgames.utils.maths.generatePoissonDiskSamplingPoints
import engine.Game
import engine.graphics.asRegion
import engine.graphics.scene.ObjectRenderable
import engine.graphics.scene.ObjectRenderableBatcher

class ObjectRenderer : Disposable {
    private companion object {
        val VERTEX_LAYOUT = VertexLayout {
            vec2("position")
            vec2("texcoord")
        }
    }

    private val pipeline = PipelineDefinition(
        """
        #section state
        blend add source_alpha one_minus_source_alpha
        
        #section layout
        vec2 position
        vec2 texcoord
        
        #section uniforms
        mat4 uTransform
        bool uFlipX
        bool uFlipY
        sampler2D uColorTexture
        vec4 uColorTextureBounds
        sampler2D uNormalTexture
        vec4 uNormalTextureBounds
        vec4 uColor
        bool uIsLit
        bool uReceivesShadows
        sampler2D uLightMapTexture
        sampler2D uShadowMapTexture
        vec2 uFramebufferSize
        vec4 uAmbientLight
        vec2 uLightMapSize
        
        #section vertex
        
        out vec2 vColorTexcoord;
        out vec2 vNormalTexcoord;
        out vec2 vPosition;

        void main() {
            if (uFlipX) {
                vColorTexcoord.x = uColorTextureBounds.x + 1.0 - texcoord.x * (uColorTextureBounds.z - uColorTextureBounds.x);
                vNormalTexcoord.x = uNormalTextureBounds.x + 1.0 - texcoord.x * (uNormalTextureBounds.z - uNormalTextureBounds.x);
            } else {
                vColorTexcoord.x = uColorTextureBounds.x + texcoord.x * (uColorTextureBounds.z - uColorTextureBounds.x);
                vNormalTexcoord.x = uNormalTextureBounds.x + texcoord.x * (uNormalTextureBounds.z - uNormalTextureBounds.x);
            }
            
            if (uFlipY) {
                vColorTexcoord.y = uColorTextureBounds.y + 1.0 - texcoord.y * (uColorTextureBounds.w - uColorTextureBounds.y);
                vNormalTexcoord.y = uNormalTextureBounds.y + 1.0 - texcoord.y * (uNormalTextureBounds.w - uNormalTextureBounds.y);
            } else {
                vColorTexcoord.y = uColorTextureBounds.y + texcoord.y * (uColorTextureBounds.w - uColorTextureBounds.y);
                vNormalTexcoord.y = uNormalTextureBounds.y + texcoord.y * (uNormalTextureBounds.w - uNormalTextureBounds.y);
            }
            
            gl_Position = uTransform * vec4(position, 0.0, 1.0);
            vPosition = gl_Position.xy;
        }
        
        #section fragment
        
        in vec2 vColorTexcoord;
        in vec2 vNormalTexcoord;
        in vec2 vPosition;
        
        out vec4 outColor;
        
        void main() {
            vec4 color = texture(uColorTexture, vColorTexcoord) * uColor;
            
            if (color.a < 1.0 / 255.0)
                discard;
            
            vec3 diffuse = color.rgb;
            
            if (uIsLit) {
                vec2 framebufferTexcoord = (gl_FragCoord.xy + 0.5) / uFramebufferSize;
                vec4 lightMap = texture(uLightMapTexture, framebufferTexcoord);
                float shadowMap = texture(uShadowMapTexture, framebufferTexcoord).r;
                vec2 normal = normalize(vec2(texture(uNormalTexture, vNormalTexcoord).rg * 2.0 - 1.0));
                
                const vec2 directions[] = {
                    ${generatePoissonDiskSamplingPoints(2.0f, 1.0f, 4).joinToString(",\n") { "vec2(${it.x}, ${it.y})" }}
                };
                
                const float sampleDistance = 24.0; //seems to be okay?
                
                float totalWeight;
                vec2 lightDirection;
                float lightMapBrightness = lightMap.a;
                
                for (int i = 0; i < directions.length; i++) {
                    vec2 sampleTexcoord = framebufferTexcoord + (directions[i] * sampleDistance) / uLightMapSize;
                    float sampleBrightness = texture(uLightMapTexture, sampleTexcoord).a;
                    
                    float weight = abs(lightMapBrightness - sampleBrightness);
                    
                    if (lightMapBrightness > sampleBrightness)
                        lightDirection -= directions[i] * weight;
                    else
                        lightDirection += directions[i] * weight;
                        
                    totalWeight += weight;
                }
                
                lightDirection /= totalWeight;
                
                vec2 finalLightDirection = normalize(vec2(-lightDirection.x, lightDirection.y));
                
                float shadowAmount = uReceivesShadows ? shadowMap : 1.0;
                float lightingAmount = max(dot(normal, finalLightDirection), lightMapBrightness);
                
                diffuse *= uAmbientLight.rgb + lightMap.rgb * shadowAmount * lightingAmount;
            }
            
            outColor = vec4(diffuse, color.a);
        }
    """.trimIndent()
    ).createPipeline()

    private val renderLists = DynamicArray<MutableList<ObjectRenderable>>()
    private val batcher = ObjectRenderableBatcher()
    private val vertexBuffer = Kore.graphics.createBuffer(GraphicsBuffer.Usage.DYNAMIC)
    private val indexBuffer = Kore.graphics.createBuffer(GraphicsBuffer.Usage.DYNAMIC)

    private val defaultColorTexture = Kore.graphics.createTexture2D(Texture.Format.RGBA8_UNORM, Game.graphics2d.pointClampSampler) {
        setImage(Image(1, 1).also {
            it[0, 0] = Color.WHITE
        })
    }

    private val defaultNormalTexture = Kore.graphics.createTexture2D(Texture.Format.RGBA8_UNORM, Game.graphics2d.pointClampSampler) {
        setImage(Image(1, 1).also {
            it[0, 0] = Color.BLUE
        })
    }

    fun render(camera: OrthographicCamera, renderables: List<ObjectRenderable>, lightMapTexture: Texture2D, shadowMapTexture: Texture2D, ambientLight: Color) {
        for (renderable in renderables) {
            if (!(renderable.bounds intersects camera.rectangle))
                continue

            val renderList = this.renderLists.getOrPut(renderable.layer) { arrayListOf() }
            renderList.add(renderable)
        }

        Kore.graphics.setPipeline(pipeline)
        pipeline.getMatrixUniform("uTransform")?.update(camera.projectionView)
        pipeline.getTexture2DUniform("uLightMapTexture")?.update(lightMapTexture)
        pipeline.getTexture2DUniform("uShadowMapTexture")?.update(shadowMapTexture)
        pipeline.getFloatUniform("uFramebufferSize")?.update(Kore.graphics.width.toFloat(), Kore.graphics.height.toFloat())
        pipeline.getFloatUniform("uAmbientLight")?.update(ambientLight)
        pipeline.getFloatUniform("uLightMapSize")?.update(lightMapTexture.width.toFloat(), lightMapTexture.height.toFloat())

        Kore.graphics.setVertexBuffer(vertexBuffer, VERTEX_LAYOUT)
        Kore.graphics.setIndexBuffer(indexBuffer)

        renderLists.forEach { renderList ->
            for (renderable in renderList)
                batcher.submit(renderable.material, renderable.drawable, renderable.flipX, renderable.flipY)

            renderList.clear()

            batcher.flush { batch ->
                batch.material?.let { material ->
                    val colorTexture = Game.textures[material.colorTexturePath] ?: defaultColorTexture.asRegion()
                    val normalTexture = Game.textures[material.normalTexturePath] ?: defaultNormalTexture.asRegion()

                    pipeline.getTexture2DUniform("uColorTexture")?.update(colorTexture.texture)
                    pipeline.getFloatUniform("uColorTextureBounds")?.update {
                        it[0] = colorTexture.u0
                        it[1] = colorTexture.v0
                        it[2] = colorTexture.u1
                        it[3] = colorTexture.v1
                    }

                    pipeline.getTexture2DUniform("uNormalTexture")?.update(normalTexture.texture)
                    pipeline.getFloatUniform("uNormalTextureBounds")?.update {
                        it[0] = normalTexture.u0
                        it[1] = normalTexture.v0
                        it[2] = normalTexture.u1
                        it[3] = normalTexture.v1
                    }

                    pipeline.getFloatUniform("uColor")?.update(material.color)
                    pipeline.getBooleanUniform("uIsLit")?.update(material.isLit)
                    pipeline.getBooleanUniform("uReceivesShadows")?.update(material.receivesShadows)
                }

                pipeline.getBooleanUniform("uFlipX")?.update(batch.flipX)
                pipeline.getBooleanUniform("uFlipY")?.update(batch.flipY)

                batch.builder.updateBuffers(vertexBuffer, indexBuffer)
                Kore.graphics.drawIndexed(Primitive.TRIANGLES, batch.builder.numIndices, 0, IndexDataType.INT)
            }
        }

        Kore.graphics.setVertexBuffer(null, VERTEX_LAYOUT)
        Kore.graphics.setIndexBuffer(null)
    }

    override fun dispose() {
        defaultColorTexture.dispose()
        defaultNormalTexture.dispose()
        vertexBuffer.dispose()
        indexBuffer.dispose()
    }
}