package engine.graphics

import engine.graphics.font.GlyphLayout
import com.gratedgames.graphics.gpu.GraphicsBuffer
import com.gratedgames.memory.IntBuffer
import com.gratedgames.memory.Struct
import com.gratedgames.memory.StructBuffer
import com.gratedgames.memory.clear
import com.gratedgames.utils.Color
import com.gratedgames.utils.Disposable
import com.gratedgames.utils.maths.Matrix3x2
import com.gratedgames.utils.maths.Vector2
import com.gratedgames.utils.maths.VectorPath
import engine.graphics.sprite.Sprite
import kotlin.math.*

class DrawContext2D(size: Int = 512) : Disposable {
    class Vertex : Struct() {
        var x by float()
        var y by float()
        var u by float()
        var v by float()
        var uMin by float()
        var uMax by float()
        var vMin by float()
        var vMax by float()
        var color by int()

        operator fun component1() = x
        operator fun component2() = y
        operator fun component3() = u
        operator fun component4() = v
        operator fun component5() = uMin
        operator fun component6() = uMax
        operator fun component7() = vMin
        operator fun component8() = vMax
        operator fun component9() = color

        fun transform(matrix: Matrix3x2) {
            matrix.transform(x, y) { nx, ny ->
                x = nx
                y = ny
            }
        }
    }

    var numVertices = 0
        private set

    var numIndices = 0
        private set

    private var vertices = StructBuffer(size, false, supplier = { Vertex() })

    private var indices = IntBuffer(size)

    var currentIndex = 0

    fun vertex(block: (Vertex) -> Unit) {
        block(vertices[numVertices++])
    }

    fun vertex(x: Float, y: Float, u: Float, v: Float, uMin: Float, vMin: Float, uMax: Float, vMax: Float, color: Int) {
        vertex {
            it.x = x
            it.y = y
            it.u = u
            it.v = v
            it.uMin = uMin
            it.uMax = uMax
            it.vMin = vMin
            it.vMax = vMax
            it.color = color
        }
    }

    fun vertex(minX: Float, minY: Float, maxX: Float, maxY: Float, x: Float, y: Float, color: Int, uMin: Float, vMin: Float, uMax: Float, vMax: Float) {
        val localU = (x - minX) / (maxX - minX)
        val localV = (y - minY) / (maxY - minY)

        val u = uMin + localU * (uMax - uMin)
        val v = vMin + localV * (vMax - vMin)

        vertex(x, y, u, v, uMin, vMin, uMax, vMax, color)
    }

    fun index(index: Int) {
        indices[numIndices++] = index
    }

    fun transform(matrix: Matrix3x2) {
        repeat(numVertices) {
            vertices[it].transform(matrix)
        }
    }

    fun draw(context: DrawContext2D) {
        ensureSize(context.numVertices, context.numIndices)

        repeat(context.numVertices) {
            val (x, y, u, v, uMin, vMin, uMax, vMax, color) = context.vertices[it]
            vertex(x, y, u, v, uMin, vMin, uMax, vMax, color)
        }

        repeat(context.numIndices) {
            index(context.indices[it])
        }

        currentIndex += context.currentIndex
    }

    fun reset() {
        numVertices = 0
        vertices.memory.clear()
        numIndices = 0
        indices.memory.clear()
        currentIndex = 0
    }

    fun ensureSize(numVertices: Int, numIndices: Int) {
        vertices.ensureSize(this.numVertices + numVertices + 1)
        indices.ensureSize(this.numIndices + numIndices + 1)
    }

    fun updateBuffers(vertexBuffer: GraphicsBuffer, indexBuffer: GraphicsBuffer) {
        vertexBuffer.setData(vertices.memory, size = numVertices * vertices.structSize)
        indexBuffer.setData(indices.memory, size = numIndices * indices.valueSize)
    }

    override fun dispose() {
        vertices.dispose()
        indices.dispose()
    }
}

fun DrawContext2D.drawRect(x: Float, y: Float, width: Float, height: Float, color: Color = Color.WHITE, rotation: Float = 0.0f, u0: Float = 0.0f, v0: Float = 0.0f, u1: Float = 1.0f, v1: Float = 1.0f) {
    ensureSize(4, 6)

    val colorBits = color.bits

    val x0 = x
    val y0 = y
    val x1 = x + width
    val y1 = y + height

    var p0x = x0
    var p0y = y0
    var p1x = x1
    var p1y = y0
    var p2x = x1
    var p2y = y1
    var p3x = x0
    var p3y = y1

    if (rotation != 0.0f) {
        val halfWidth = (x1 - x0) * 0.5f
        val halfHeight = (y1 - y0) * 0.5f
        val centerX = x0 + halfWidth
        val centerY = y0 + halfHeight

        val cos = cos(rotation)
        val sin = sin(rotation)

        p0x = cos * x0 - sin * y0
        p0y = sin * x0 + cos * y0

        p1x = cos * x1 - sin * y0
        p1y = sin * x1 + cos * y0

        p2x = cos * x1 - sin * y1
        p2y = sin * x1 + cos * y1

        p3x = cos * x0 - sin * y1
        p3y = sin * x0 + cos * y1
    }

    vertex(p0x, p0y, u0, v0, u0, v0, u1, v1, colorBits)
    vertex(p1x, p1y, u1, v0, u0, v0, u1, v1, colorBits)
    vertex(p2x, p2y, u1, v1, u0, v0, u1, v1, colorBits)
    vertex(p3x, p3y, u0, v1, u0, v0, u1, v1, colorBits)

    //vertex(x0, y0, u0, v0, u0, v0, u1, v1, colorBits)
    //vertex(x1, y0, u1, v0, u0, v0, u1, v1, colorBits)
    //vertex(x1, y1, u1, v1, u0, v0, u1, v1, colorBits)
    //vertex(x0, y1, u0, v1, u0, v0, u1, v1, colorBits)

    index(currentIndex)
    index(currentIndex + 1)
    index(currentIndex + 2)
    index(currentIndex)
    index(currentIndex + 2)
    index(currentIndex + 3)

    currentIndex += 4
}

fun DrawContext2D.drawPathFilled(path: VectorPath, color: Color = Color.WHITE, uMin: Float = 0.0f, vMin: Float = 0.0f, uMax: Float = 1.0f, vMax: Float = 1.0f) {
    val vertexCount = path.count
    val indexCount = (vertexCount - 2) * 3

    ensureSize(vertexCount, indexCount)

    val colorBits = color.bits

    repeat(path.count) {
        val point = path[it]

        vertex(path.minX, path.minY, path.maxX, path.maxY, point.x, point.y, colorBits, uMin, vMin, uMax, vMax)

        if (it >= 2) {
            index(currentIndex)
            index(currentIndex + it - 1)
            index(currentIndex + it)
        }
    }

    currentIndex += vertexCount
}

fun DrawContext2D.drawPathStroke(path: VectorPath, thickness: Float, closed: Boolean = true, color: Color = Color.WHITE, extrusionOffset: Float = 0.5f, uMin: Float = 0.0f, vMin: Float = 0.0f, uMax: Float = 1.0f, vMax: Float = 1.0f) {
    val pointsCount = path.count
    val indexCount = pointsCount * 6
    val vertexCount = pointsCount * 2

    ensureSize(vertexCount, indexCount)

    val colorBits = color.bits

    val direction = Vector2()
    val normal = Vector2()

    fun computeDirection(x0: Float, y0: Float, x1: Float, y1: Float) {
        direction.x = x0 - x1
        direction.y = y0 - y1
        val factor = 1.0f / sqrt(direction.x * direction.x + direction.y * direction.y)
        direction.x *= factor
        direction.y *= factor
    }

    fun computeNormal(x0: Float, y0: Float, x1: Float, y1: Float): Float {
        normal.x = x0 + x1
        normal.y = y0 + y1
        val factor = 1.0f / sqrt(normal.x * normal.x + normal.y * normal.y)
        normal.x *= factor
        normal.y *= factor
        return thickness / (normal.x * -y0 + normal.y * x0)
    }

    repeat(pointsCount) {
        if (closed) {
            val prevPointIndex = if (it - 1 >= 0) it - 1 else pointsCount - 1
            val pointIndex = it
            val nextPointIndex = if (it + 1 == pointsCount) 0 else it + 1

            val prevPoint = path[prevPointIndex]
            val point = path[pointIndex]
            val nextPoint = path[nextPointIndex]

            computeDirection(prevPoint.x, prevPoint.y, point.x, point.y)
            val toPrevX = direction.x
            val toPrevY = direction.y

            computeDirection(nextPoint.x, nextPoint.y, point.x, point.y)
            val toNextX = direction.x
            val toNextY = direction.y

            val extrudeLength = computeNormal(toPrevX, toPrevY, toNextX, toNextY)

            vertex(path.minX, path.minY, path.maxX, path.maxY, point.x + normal.x * extrudeLength * extrusionOffset, point.y + normal.y * extrudeLength * extrusionOffset, colorBits, uMin, vMin, uMax, vMax)
            vertex(path.minX, path.minY, path.maxX, path.maxY, point.x - normal.x * extrudeLength * (1.0f - extrusionOffset), point.y - normal.y * extrudeLength * (1.0f - extrusionOffset), colorBits, uMin, vMin, uMax, vMax)

            val prevPointDrawIndex = prevPointIndex * 2
            val pointDrawIndex = pointIndex * 2

            index(currentIndex + prevPointDrawIndex + 1)
            index(currentIndex + prevPointDrawIndex)
            index(currentIndex + pointDrawIndex + 1)
            index(currentIndex + prevPointDrawIndex)
            index(currentIndex + pointDrawIndex)
            index(currentIndex + pointDrawIndex + 1)
        } else {
            when (it) {
                0 -> {
                    val pointIndex = it
                    val nextPointIndex = it + 1

                    val point = path[pointIndex]
                    val nextPoint = path[nextPointIndex]

                    computeNormal(point.x, point.y, nextPoint.x, nextPoint.y)
                    val extrudeLength = thickness * 0.5f

                    vertex(path.minX, path.minY, path.maxX, path.maxY, point.x + normal.x * extrudeLength, point.y + normal.y * extrudeLength, colorBits, uMin, vMin, uMax, vMax)
                    vertex(path.minX, path.minY, path.maxX, path.maxY, point.x - normal.x * extrudeLength, point.y - normal.y * extrudeLength, colorBits, uMin, vMin, uMax, vMax)
                }
                pointsCount - 1 -> {
                    val prevPointIndex = it - 1
                    val pointIndex = it

                    val prevPoint = path[prevPointIndex]
                    val point = path[pointIndex]

                    computeNormal(prevPoint.x, prevPoint.y, point.x, point.y)
                    val extrudeLength = thickness * 0.5f

                    vertex(path.minX, path.minY, path.maxX, path.maxY, point.x + normal.x * extrudeLength, point.y + normal.y * extrudeLength, colorBits, uMin, vMin, uMax, vMax)
                    vertex(path.minX, path.minY, path.maxX, path.maxY, point.x - normal.x * extrudeLength, point.y - normal.y * extrudeLength, colorBits, uMin, vMin, uMax, vMax)

                    val prevPointDrawIndex = prevPointIndex * 2
                    val pointDrawIndex = pointIndex * 2

                    index(currentIndex + prevPointDrawIndex + 1)
                    index(currentIndex + prevPointDrawIndex)
                    index(currentIndex + pointDrawIndex + 1)
                    index(currentIndex + prevPointDrawIndex)
                    index(currentIndex + pointDrawIndex)
                    index(currentIndex + pointDrawIndex + 1)
                }
                else -> {
                    val prevPointIndex = it - 1
                    val pointIndex = it
                    val nextPointIndex = it + 1

                    val prevPoint = path[prevPointIndex]
                    val point = path[pointIndex]
                    val nextPoint = path[nextPointIndex]

                    computeDirection(prevPoint.x, prevPoint.y, point.x, point.y)
                    val toPrevX = direction.x
                    val toPrevY = direction.y

                    computeDirection(nextPoint.x, nextPoint.y, point.x, point.y)
                    val toNextX = direction.x
                    val toNextY = direction.y

                    val extrudeLength = computeNormal(toPrevX, toPrevY, toNextX, toNextY)

                    vertex(path.minX, path.minY, path.maxX, path.maxY, point.x + normal.x * extrudeLength, point.y + normal.y * extrudeLength, colorBits, uMin, vMin, uMax, vMax)
                    vertex(path.minX, path.minY, path.maxX, path.maxY, point.x - normal.x * extrudeLength, point.y - normal.y * extrudeLength, colorBits, uMin, vMin, uMax, vMax)

                    val prevPointDrawIndex = prevPointIndex * 2
                    val pointDrawIndex = pointIndex * 2

                    index(currentIndex + prevPointDrawIndex + 1)
                    index(currentIndex + prevPointDrawIndex)
                    index(currentIndex + pointDrawIndex + 1)
                    index(currentIndex + prevPointDrawIndex)
                    index(currentIndex + pointDrawIndex)
                    index(currentIndex + pointDrawIndex + 1)
                }
            }
        }
    }

    currentIndex += pointsCount * 2
}

fun DrawContext2D.draw(drawable: Drawable, u0: Float = 0.0f, v0: Float = 0.0f, u1: Float = 1.0f, v1: Float = 1.0f, color: Color) {
    ensureSize(drawable.vertices.size, drawable.indices.size)

    val colorBits = color.bits

    drawable.vertices.forEach {
        val u = u0 + it.u * (u1 - u0)
        val v = v0 + it.v * (v1 - v0)
        vertex(it.x, it.y, u, v, u0, v0, u1, v1, colorBits)
    }

    drawable.indices.forEach {
        index(currentIndex + it)
    }

    currentIndex += drawable.vertices.size
}

fun DrawContext2D.drawSprite(sprite: Sprite) {
    draw(sprite, sprite.texture.u0, sprite.texture.v0, sprite.texture.u1, sprite.texture.v1, sprite.color)
}

fun DrawContext2D.drawGlyphs(glyphLayout: GlyphLayout, x: Float, y: Float, color: Color = Color.WHITE) {
    for (quad in glyphLayout)
        drawRect(quad.x + x, quad.y + y, quad.width, quad.height, color, 0.0f, quad.u0, quad.v0, quad.u1, quad.v1)
}

fun DrawContext2D.drawTriangle(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, color0: Color, color1: Color = color0, color2: Color = color0) {
    ensureSize(3, 3)

    val minX = min(x0, min(x1, x2))
    val minY = min(y0, min(y1, y2))
    val maxX = max(x0, min(x1, x2))
    val maxY = max(y0, min(y1, y2))

    vertex(minX, minY, maxX, maxY, x0, y0, color0.bits, 0.0f, 0.0f, 1.0f, 1.0f)
    vertex(minX, minY, maxX, maxY, x1, y1, color1.bits, 0.0f, 0.0f, 1.0f, 1.0f)
    vertex(minX, minY, maxX, maxY, x2, y2, color2.bits, 0.0f, 0.0f, 1.0f, 1.0f)

    index(currentIndex)
    index(currentIndex + 1)
    index(currentIndex + 2)

    currentIndex += 3
}

fun DrawContext2D.drawRect(x: Float, y: Float, width: Float, height: Float, color00: Color, color01: Color = color00, color11: Color = color00, color10: Color = color00) {
    ensureSize(4, 6)

    val halfWidth = width * 0.5f
    val halfHeight = height * 0.5f

    val x0 = x - halfWidth
    val y0 = y - halfHeight
    val x1 = x + halfWidth
    val y1 = y - halfHeight
    val x2 = x + halfWidth
    val y2 = y + halfHeight
    val x3 = x - halfWidth
    val y3 = y + halfHeight

    vertex(x0, y0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, color00.bits)
    vertex(x1, y1, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, color01.bits)
    vertex(x2, y2, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, color11.bits)
    vertex(x3, y3, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, color10.bits)

    index(currentIndex)
    index(currentIndex + 1)
    index(currentIndex + 2)
    index(currentIndex)
    index(currentIndex + 2)
    index(currentIndex + 3)

    currentIndex += 4
}