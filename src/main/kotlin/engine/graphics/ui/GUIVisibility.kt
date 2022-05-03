package engine.graphics.ui

import com.cozmicgames.utils.collections.PriorityList

/**
 * Reports if a point is contained within any area of bounds.
 * This is used to determine if the point of touch is handled by a GUI layer.
 *
 * There is lots to be optimized here, but it works for now.
 */
class GUIVisibility {
    class Node(val x: Float, val y: Float, val width: Float, val height: Float) : Comparable<Node> {
        override fun compareTo(other: Node): Int {
            return if (x < other.x) -1 else if (x > other.x) 1 else 0
        }
    }

    private val nodes = PriorityList<Node>()

    private var minX = Float.MAX_VALUE
    private var minY = Float.MAX_VALUE
    private var maxX = -Float.MAX_VALUE
    private var maxY = -Float.MAX_VALUE

    fun add(x: Float, y: Float, width: Float, height: Float) {
        nodes.add(Node(x, y, width, height))

        if (x < minX)
            minX = x

        if (y < minY)
            minY = y

        if (x + width > maxX)
            maxX = x + width

        if (y + height > maxY)
            maxY = y + height
    }

    fun contains(x: Float, y: Float): Boolean {
        if (nodes.isEmpty())
            return false

        if (x < minX || x > maxX || y < minY || y > maxY)
            return false

        for (node in nodes) {
            if (x < node.x)
                return false

            if (node.x <= x && node.x + node.width >= x && node.y <= y && node.y + node.height >= y)
                return true
        }

        return false
    }

    fun reset() {
        nodes.clear()
        minX = 0.0f
        minY = 0.0f
        maxX = 0.0f
        maxY = 0.0f
    }
}