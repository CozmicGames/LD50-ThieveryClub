package common

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.randomFloat
import engine.Game
import engine.GameState
import engine.graphics.ui.GUI
import engine.graphics.ui.gizmos.Gizmos
import engine.graphics.ui.widgets.*
import engine.scene.GameObject
import engine.scene.Scene
import engine.scene.components.*
import engine.scene.processors.RenderProcessor

class SceneGameState : GameState {
    val scene = Scene()
    val gizmos = Gizmos()
    val gui = GUI()

    var selectedMode = Gizmos.TransformMode.TRANSLATION
    var selectedGameObject: GameObject? = null

    fun createObject(): GameObject {
        return scene.addGameObject {
            addComponent<TransformComponent> {
                transform.x = randomFloat() * Kore.graphics.width - Kore.graphics.width * 0.5f
                transform.y = randomFloat() * Kore.graphics.height - Kore.graphics.height * 0.5f
                transform.scaleX = 32.0f + randomFloat() * 64.0f
                transform.scaleY = 32.0f + randomFloat() * 64.0f
                transform.rotation = randomFloat() * 360.0f
            }
            addComponent<MaterialComponent> {
                material.colorTexturePath = "images/grass.png"
                material.isLit = true
                material.castsShadows = true
                material.receivesShadows = false
            }
            addComponent<SpriteComponent> {
                layer = 1
            }
        }
    }

    fun createBackground() {
        repeat(50) { x ->
            repeat(20) { y ->
                scene.addGameObject {
                    addComponent<TransformComponent> {
                        transform.x = x * 32.0f - 50 * 32.0f * 0.5f
                        transform.y = y * 32.0f - 20 * 32.0f * 0.5f
                        transform.scaleX = 32.0f
                        transform.scaleY = 32.0f
                    }
                    addComponent<MaterialComponent> {
                        material.colorTexturePath = "images/ground.png"
                        material.normalTexturePath = "images/NormalMap.png"
                        material.isLit = true
                        material.receivesShadows = true
                    }
                    addComponent<SpriteComponent> {
                        layer = 0
                    }
                }
            }
        }
    }

    fun createLight(): GameObject {
        return scene.addGameObject {
            addComponent<TransformComponent> {
                transform.x = randomFloat() * Kore.graphics.width - Kore.graphics.width * 0.5f
                transform.y = randomFloat() * Kore.graphics.height - Kore.graphics.height * 0.5f
                transform.scaleX = 50.0f
                transform.scaleY = 50.0f
            }
            addComponent<LightComponent> {
                light.range = 200.0f
                light.intensity = 2.0f
            }
            addComponent<MaterialComponent> {
                material.colorTexturePath = "images/light_icon.png"
                material.isLit = false
                material.receivesShadows = false
                material.castsShadows = false
            }
            addComponent<SpriteComponent> {
                layer = 1
            }
        }
    }

    override fun onCreate() {
        scene.addSceneProcessor(RenderProcessor())
        Game.renderer.ambientLightIntensity = 0.5f
        Game.textures.add(Kore.files.asset("images/grass.png"))
        Game.textures.add(Kore.files.asset("images/ground.png"))
        Game.textures.add(Kore.files.asset("images/NormalMap.png"))
        Game.textures.add(Kore.files.asset("images/light_icon.png"))

        createBackground()
    }

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.LIGHT_GRAY)

        scene.update(delta)
        Game.renderer.render(Game.camera)

        gui.begin()
        gui.sameLine {
            gui.selectableImage(Game.textures.getOrAdd(Kore.files.asset("images/translate_gizmo.png")), 40.0f, 40.0f, selectedMode == Gizmos.TransformMode.TRANSLATION) { selectedMode = Gizmos.TransformMode.TRANSLATION }
            gui.selectableImage(Game.textures.getOrAdd(Kore.files.asset("images/rotate_gizmo.png")), 40.0f, 40.0f, selectedMode == Gizmos.TransformMode.ROTATION) { selectedMode = Gizmos.TransformMode.ROTATION }
            gui.selectableImage(Game.textures.getOrAdd(Kore.files.asset("images/scale_gizmo.png")), 40.0f, 40.0f, selectedMode == Gizmos.TransformMode.SCALE) { selectedMode = Gizmos.TransformMode.SCALE }
        }
        gui.textButton("Add light") { createLight() }
        gui.textButton("Add object") { createObject() }
        selectedGameObject?.let {
            gui.textButton("Remove") {
                scene.removeGameObject(it)
                selectedGameObject = null
            }
        }

        gui.slider(Game.renderer.lightMapScale) { Game.renderer.lightMapScale = it }
        gui.slider(Game.renderer.shadowMapScale) { Game.renderer.shadowMapScale = it }

        //gui.image(Game.renderer.occlusionFramebuffer[Framebuffer.Attachment.COLOR0]!!.asRegion(), 256.0f, 256.0f)
        //gui.image(Game.renderer.shadowFramebuffer[Framebuffer.Attachment.COLOR0]!!.asRegion(), 256.0f, 16.0f)

        gui.end()

        gizmos.begin(Game.camera)
        gizmos.selectGameObject(scene, filter = {
            val layer = it.getComponent<SpriteComponent>()?.layer ?: 0
            layer > 0
        }) {
            selectedGameObject = it
        }

        selectedGameObject?.getComponent<TransformComponent>()?.transform?.let {
            gizmos.editTransform(it, selectedMode)
        }
        gizmos.end()

        return this
    }

    override fun onDestroy() {
        gui.dispose()
        scene.dispose()
        gizmos.dispose()
    }
}