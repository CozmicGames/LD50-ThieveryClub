package engine.graphics.scene

import com.cozmicgames.graphics.gpu.GraphicsBuffer
import com.cozmicgames.memory.IntBuffer
import com.cozmicgames.memory.Struct
import com.cozmicgames.memory.StructBuffer
import com.cozmicgames.memory.clear
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.maths.Matrix3x2
import engine.graphics.Drawable

class OcclusionDrawDataBuilder(size: Int = 512) : Disposable {
    class Vertex : Struct() {
        var x by float()
        var y by float()

        operator fun component1() = x
        operator fun component2() = y

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

    var currentIndex = 0

    private var vertices = StructBuffer(size, false, supplier = { Vertex() })

    private var indices = IntBuffer(size)

    fun vertex(block: (Vertex) -> Unit) {
        block(vertices[numVertices++])
    }

    fun vertex(x: Float, y: Float) {
        vertex {
            it.x = x
            it.y = y
        }
    }

    fun index(index: Int) {
        indices[numIndices++] = index
    }

    fun transform(matrix: Matrix3x2) {
        repeat(numVertices) {
            vertices[it].transform(matrix)
        }
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

    fun draw(drawable: Drawable) {
        ensureSize(drawable.vertices.size, drawable.indices.size)

        drawable.vertices.forEach {
            vertex(it.x, it.y)
        }

        drawable.indices.forEach {
            index(currentIndex + it)
        }

        currentIndex += drawable.vertices.size
    }

    override fun dispose() {
        vertices.dispose()
        indices.dispose()
    }
}