package engine.materials

import com.cozmicgames.utils.*

class Material : Properties() {
    var colorTexturePath by string { "" }
    var normalTexturePath by string { "" }

    var restitution by float { 0.0f }
    var staticFriction by float { 0.5f }
    var dynamicFriction by float { 0.3f }
    var density by float { 1.0f }

    val color by color { it.set(Color.WHITE) }

    var isLit by boolean { false }
    var castsShadows by boolean { false }
    var receivesShadows by boolean { false }

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false

        if (this === other)
            return true

        if (this::class != other::class)
            return false

        other as Material

        if (colorTexturePath != other.colorTexturePath)
            return false

        if (normalTexturePath != other.normalTexturePath)
            return false

        if (restitution != other.restitution)
            return false

        if (staticFriction != other.staticFriction)
            return false

        if (dynamicFriction != other.dynamicFriction)
            return false

        if (density != other.density)
            return false

        if (color != other.color)
            return false

        if (isLit != other.isLit)
            return false

        if (receivesShadows != other.receivesShadows)
            return false

        if (castsShadows != other.castsShadows)
            return false

        return true
    }
}