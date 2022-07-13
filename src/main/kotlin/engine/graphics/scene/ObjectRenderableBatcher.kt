package engine.graphics.scene

import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.collections.Pool
import com.cozmicgames.utils.collections.Resettable
import engine.graphics.Drawable
import engine.materials.Material

class ObjectRenderableBatcher : Disposable {
    class RenderBatch : Resettable, Disposable {
        var material: Material? = null
        var flipX = false
        var flipY = false
        val builder = ObjectRenderableDrawDataBuilder()

        override fun reset() {
            material = null
            flipX = false
            flipY = false
            builder.reset()
        }

        override fun dispose() {
            builder.dispose()
        }
    }

    private val drawBatchPool = Pool(supplier = { RenderBatch() })
    private val batches = arrayListOf<RenderBatch>()

    fun submit(material: Material, drawable: Drawable, flipX: Boolean, flipY: Boolean) {
        val lastBatch = batches.lastOrNull()

        if (lastBatch != null && material == lastBatch.material && flipX == lastBatch.flipX && flipY == lastBatch.flipY)
            lastBatch.builder.draw(drawable)
        else {
            val batch = drawBatchPool.obtain()
            batch.material = material
            batch.flipX = flipX
            batch.flipY = flipY
            batch.builder.draw(drawable)
            batches += batch
        }
    }

    fun flush(block: (RenderBatch) -> Unit) {
        if (batches.isEmpty())
            return

        batches.forEach {
            block(it)
            drawBatchPool.free(it)
        }
        batches.clear()
    }

    override fun dispose() {
        flush {}
        drawBatchPool.dispose()
    }
}