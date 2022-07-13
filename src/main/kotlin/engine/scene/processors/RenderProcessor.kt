package engine.scene.processors

import engine.Game
import engine.scene.SceneProcessor
import engine.scene.components.*

class RenderProcessor : SceneProcessor() {
    override fun shouldProcess(delta: Float) = true

    override fun process(delta: Float) {
        val scene = scene ?: return

        for (gameObject in scene) {
            val transformComponent = gameObject.getComponent<TransformComponent>() ?: continue

            val lightComponent = gameObject.getComponent<LightComponent>()

            if (lightComponent != null)
                Game.renderer.submitLight(transformComponent.transform, lightComponent.light)

            val spriteComponent = gameObject.getComponent<SpriteComponent>()
            val materialComponent = gameObject.getComponent<MaterialComponent>()

            if (spriteComponent != null && materialComponent != null)
                Game.renderer.submitObject(spriteComponent, materialComponent.material, spriteComponent.isFlippedX, spriteComponent.isFlippedY, spriteComponent.layer)
        }
    }
}