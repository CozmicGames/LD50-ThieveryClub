package engine.scene.components

import com.cozmicgames.utils.Properties
import engine.graphics.scene.Light
import engine.scene.Component

class LightComponent : Component() {
    val light = Light()

    override fun read(properties: Properties) {
        properties.getFloatArray("color")?.let {
            light.color.r = it.getOrElse(0) { 1.0f }
            light.color.g = it.getOrElse(1) { 1.0f }
            light.color.b = it.getOrElse(2) { 1.0f }
        }

        properties.getFloat("intensity")?.let { light.intensity = it }
        properties.getFloat("range")?.let { light.range = it }
        properties.getBoolean("castsShadows")?.let { light.castsShadows = it }
    }

    override fun write(properties: Properties) {
        properties.setFloatArray("color", arrayOf(light.color.r, light.color.g, light.color.b))
        properties.setFloat("intensity", light.intensity)
        properties.setFloat("range", light.range)
        properties.setBoolean("castsShadows", light.castsShadows)
    }
}