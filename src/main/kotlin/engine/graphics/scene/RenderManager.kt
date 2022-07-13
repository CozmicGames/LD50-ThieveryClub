package engine.graphics.scene

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.gpu.Framebuffer
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.graphics.gpu.Viewport
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.collections.Pool
import com.cozmicgames.utils.maths.OrthographicCamera
import engine.Game
import engine.graphics.Drawable
import engine.graphics.scene.renderers.*
import engine.materials.Material
import engine.utils.Transform

class RenderManager : Disposable {
    private var shouldResizeShadowFramebuffers = true
    private var shouldResizeScreenspaceFramebuffers = true
    private var currentScreenWidth = Kore.graphics.width
    private var currentScreenHeight = Kore.graphics.height

    private val lightRenderablePool = Pool(supplier = { LightRenderable() })
    private val objectRenderablePool = Pool(supplier = { ObjectRenderable() })

    private val lightRenderables = arrayListOf<LightRenderable>()
    private val objectRenderables = arrayListOf<ObjectRenderable>()

    private val occlusionRenderer = OcclusionRenderer(this)
    private val shadowRenderer = ShadowRenderer(this)
    private val lightMapRenderer = LightMapRenderer(this)
    private val shadowMapRenderer = ShadowMapRenderer(this)
    private val objectRenderer = ObjectRenderer()

    private val premultipliedAmbientLightColor = Color()

    private val occlusionFramebuffer = Kore.graphics.createFramebuffer()
    private val shadowFramebuffer = Kore.graphics.createFramebuffer()
    private val lightMapFramebuffer = Kore.graphics.createFramebuffer()
    private val shadowMapFramebuffer = Kore.graphics.createFramebuffer()

    var singleShadowMapSize = 256
        set(value) {
            if (field == value)
                return

            field = value
            shouldResizeShadowFramebuffers = true
        }

    var singleShadowMapUpscale = 1.0f
        set(value) {
            if (field == value)
                return

            field = value
            shouldResizeShadowFramebuffers = true
        }

    var softShadows = true

    val ambientLightColor = Color.WHITE.copy()

    var ambientLightIntensity = 0.3f

    var lightMapScale = 1.0f
        set(value) {
            field = value
            shouldResizeScreenspaceFramebuffers = true
        }

    var shadowMapScale = 1.0f
        set(value) {
            field = value
            shouldResizeScreenspaceFramebuffers = true
        }

    init {
        occlusionFramebuffer.addAttachment(Framebuffer.Attachment.COLOR0, Texture.Format.R8_UNORM, Game.graphics2d.pointClampSampler)
        shadowFramebuffer.addAttachment(Framebuffer.Attachment.COLOR0, Texture.Format.R8_UNORM, Game.graphics2d.linearRepeatSampler)

        lightMapFramebuffer.addAttachment(Framebuffer.Attachment.COLOR0, Texture.Format.RGBA16_FLOAT, Game.graphics2d.linearClampSampler)
        shadowMapFramebuffer.addAttachment(Framebuffer.Attachment.COLOR0, Texture.Format.R8_UNORM, Game.graphics2d.linearClampSampler)

        currentScreenWidth = Kore.graphics.width
        currentScreenHeight = Kore.graphics.height

        Kore.addResizeListener { width, height ->
            shouldResizeScreenspaceFramebuffers = true
            currentScreenWidth = width
            currentScreenHeight = height
        }
    }

    fun submitLight(transform: Transform, light: Light) {
        val renderable = lightRenderablePool.obtain()
        renderable.transform = transform
        renderable.light = light
        lightRenderables.add(renderable)
    }

    fun submitObject(drawable: Drawable, material: Material, flipX: Boolean, flipY: Boolean, layer: Int) {
        val renderable = objectRenderablePool.obtain()
        renderable.drawable = drawable
        renderable.material = material
        renderable.flipX = flipX
        renderable.flipY = flipY
        renderable.layer = layer
        renderable.updateBounds()
        objectRenderables.add(renderable)
    }

    fun render(camera: OrthographicCamera, targetFramebuffer: Framebuffer? = null) {
        if (shouldResizeShadowFramebuffers) {
            occlusionFramebuffer.update(singleShadowMapSize, singleShadowMapSize)
            shadowFramebuffer.update(singleShadowMapSize, 1)
            shouldResizeShadowFramebuffers = false
        }

        if (shouldResizeScreenspaceFramebuffers) {
            lightMapFramebuffer.update((currentScreenWidth * lightMapScale).toInt(), (currentScreenHeight * lightMapScale).toInt())
            shadowMapFramebuffer.update((currentScreenWidth * shadowMapScale).toInt(), (currentScreenHeight * shadowMapScale).toInt())
            shouldResizeScreenspaceFramebuffers = false
        }

        Kore.graphics.setFramebuffer(shadowMapFramebuffer)
        Kore.graphics.clear(Color.CLEAR)

        lightRenderables.forEach {
            if (it.light.castsShadows) {
                Kore.graphics.setFramebuffer(occlusionFramebuffer)
                Kore.graphics.setViewport(Viewport(0, 0, singleShadowMapSize, singleShadowMapSize))
                Kore.graphics.clear(Color.CLEAR)

                occlusionRenderer.render(objectRenderables, it)

                Kore.graphics.setFramebuffer(shadowFramebuffer)
                Kore.graphics.setViewport(Viewport(0, 0, singleShadowMapSize, 1))
                Kore.graphics.clear(Color.WHITE)

                val occlusionTexture = requireNotNull(occlusionFramebuffer[Framebuffer.Attachment.COLOR0])
                shadowRenderer.render(occlusionTexture)

                Kore.graphics.setFramebuffer(shadowMapFramebuffer)
                Kore.graphics.setViewport(Viewport(0, 0, shadowMapFramebuffer.width, shadowMapFramebuffer.height))

                val shadowTexture = requireNotNull(shadowFramebuffer[Framebuffer.Attachment.COLOR0])
                shadowMapRenderer.render(camera, it, shadowTexture)
            }
        }

        Kore.graphics.setFramebuffer(lightMapFramebuffer)
        Kore.graphics.setViewport(Viewport(0, 0, lightMapFramebuffer.width, lightMapFramebuffer.height))
        Kore.graphics.clear(Color.CLEAR)
        lightMapRenderer.render(camera, lightRenderables)

        Kore.graphics.setFramebuffer(targetFramebuffer)
        Kore.graphics.setViewport(targetFramebuffer?.let { Viewport(0, 0, it.width, it.height) })

        val lightMapTexture = requireNotNull(lightMapFramebuffer[Framebuffer.Attachment.COLOR0])
        val shadowMapTexture = requireNotNull(shadowMapFramebuffer[Framebuffer.Attachment.COLOR0])
        premultipliedAmbientLightColor.set(ambientLightColor.r * ambientLightIntensity, ambientLightColor.g * ambientLightIntensity, ambientLightColor.b * ambientLightIntensity, 1.0f)
        objectRenderer.render(camera, objectRenderables, lightMapTexture, shadowMapTexture, premultipliedAmbientLightColor)

        lightRenderables.forEach(lightRenderablePool::free)
        lightRenderables.clear()

        objectRenderables.forEach(objectRenderablePool::free)
        objectRenderables.clear()
    }

    override fun dispose() {
        occlusionFramebuffer.dispose()
        shadowFramebuffer.dispose()
        lightMapFramebuffer.dispose()
        shadowMapFramebuffer.dispose()

        occlusionRenderer.dispose()
        shadowRenderer.dispose()
        lightMapRenderer.dispose()
        shadowMapRenderer.dispose()
        objectRenderer.dispose()
    }
}