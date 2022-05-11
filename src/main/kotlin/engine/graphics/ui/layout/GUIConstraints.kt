package engine.graphics.ui.layout

import com.cozmicgames.utils.maths.Rectangle

abstract class GUIConstraint {
    internal enum class Type {
        NONE,
        X,
        Y,
        WIDTH,
        HEIGHT
    }

    internal var type = Type.NONE

    abstract fun getValue(parent: Rectangle, child: Rectangle): Float
}

fun absolute(value: Float, mirror: Boolean = false) = object : GUIConstraint() {
    override fun getValue(parent: Rectangle, child: Rectangle) = when (type) {
        Type.X -> if (mirror) parent.maxX - (value + child.width) else parent.minX + value
        Type.Y -> if (mirror) parent.maxY - (value + child.height) else parent.minY + value
        Type.WIDTH, Type.HEIGHT -> value
        else -> throw UnsupportedOperationException()
    }
}

fun center() = object : GUIConstraint() {
    override fun getValue(parent: Rectangle, child: Rectangle) = when (type) {
        Type.X -> parent.minX + (parent.width - child.width) * 0.5f
        Type.Y -> parent.minY + (parent.height - child.height) * 0.5f
        else -> throw UnsupportedOperationException()
    }
}

fun relative(value: Float) = object : GUIConstraint() {
    override fun getValue(parent: Rectangle, child: Rectangle) = when (type) {
        Type.X -> parent.minX + parent.width * value
        Type.Y -> parent.minY + parent.height * value
        Type.WIDTH -> parent.width * value
        Type.HEIGHT -> parent.height * value
        else -> throw UnsupportedOperationException()
    }
}

fun aspect(value: Float = 1.0f) = object : GUIConstraint() {
    override fun getValue(parent: Rectangle, child: Rectangle) = when (type) {
        Type.WIDTH -> child.height * value
        Type.HEIGHT -> child.width / value
        else -> throw UnsupportedOperationException()
    }
}

fun fill() = object : GUIConstraint() {
    override fun getValue(parent: Rectangle, child: Rectangle) = when (type) {
        Type.WIDTH -> parent.width
        Type.HEIGHT -> parent.height
        else -> throw UnsupportedOperationException()
    }
}

fun add(a: GUIConstraint, b: GUIConstraint) = object : GUIConstraint() {
    override fun getValue(parent: Rectangle, child: Rectangle): Float {
        a.type = type
        b.type = type
        return a.getValue(parent, child) + b.getValue(parent, child)
    }
}

fun subtract(a: GUIConstraint, b: GUIConstraint) = object : GUIConstraint() {
    override fun getValue(parent: Rectangle, child: Rectangle): Float {
        a.type = type
        b.type = type
        return a.getValue(parent, child) - b.getValue(parent, child)
    }
}

fun multiply(a: GUIConstraint, b: GUIConstraint) = object : GUIConstraint() {
    override fun getValue(parent: Rectangle, child: Rectangle): Float {
        a.type = type
        b.type = type
        return a.getValue(parent, child) * b.getValue(parent, child)
    }
}

fun divide(a: GUIConstraint, b: GUIConstraint) = object : GUIConstraint() {
    override fun getValue(parent: Rectangle, child: Rectangle): Float {
        a.type = type
        b.type = type
        return a.getValue(parent, child) / b.getValue(parent, child)
    }
}

operator fun GUIConstraint.plus(other: GUIConstraint) = add(this, other)

operator fun GUIConstraint.minus(other: GUIConstraint) = subtract(this, other)

operator fun GUIConstraint.times(other: GUIConstraint) = multiply(this, other)

operator fun GUIConstraint.div(other: GUIConstraint) = divide(this, other)

class GUIConstraints {
    companion object {
        private val DEFAULT_X = absolute(0.0f).apply { type = GUIConstraint.Type.X }
        private val DEFAULT_Y = absolute(0.0f).apply { type = GUIConstraint.Type.Y }
        private val DEFAULT_WIDTH = fill().apply { type = GUIConstraint.Type.WIDTH }
        private val DEFAULT_HEIGHT = fill().apply { type = GUIConstraint.Type.HEIGHT }
    }

    var x: GUIConstraint = DEFAULT_X
        set(value) {
            value.type = GUIConstraint.Type.X
            field = value
        }

    var y: GUIConstraint = DEFAULT_Y
        set(value) {
            value.type = GUIConstraint.Type.Y
            field = value
        }

    var width: GUIConstraint = DEFAULT_WIDTH
        set(value) {
            value.type = GUIConstraint.Type.WIDTH
            field = value
        }

    var height: GUIConstraint = DEFAULT_HEIGHT
        set(value) {
            value.type = GUIConstraint.Type.HEIGHT
            field = value
        }
}

fun constraints(block: GUIConstraints.() -> Unit): GUIConstraints {
    val constraints = GUIConstraints()
    constraints.block()
    return constraints
}
