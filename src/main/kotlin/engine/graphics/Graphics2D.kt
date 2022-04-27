package engine.graphics

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.graphics.safeHeight
import com.cozmicgames.graphics.safeWidth
import com.cozmicgames.memory.Memory
import com.cozmicgames.memory.of
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.maths.OrthographicCamera
import com.cozmicgames.utils.use

class Graphics2D : Disposable {
    val blankTexture = Kore.graphics.createTexture2D(Texture.Format.RGBA8_UNORM) {
        Memory.of(Color.WHITE.bits).use {
            setImage(1, 1, it, Texture.Format.RGBA8_UNORM)
        }
    }

    val missingTexture = Kore.graphics.createTexture2D(Texture.Format.RGBA8_UNORM) {
        Memory.of(Color.MAGENTA.bits, Color.BLACK.bits, Color.BLACK.bits, Color.MAGENTA.bits).use {
            setImage(2, 2, it, Texture.Format.RGBA8_UNORM)
        }
    }

    val defaultCamera = OrthographicCamera(Kore.graphics.safeWidth, Kore.graphics.safeHeight)

    private val resizeListener = { width: Int, height: Int ->
        defaultCamera.width = width
        defaultCamera.height = height
        defaultCamera.resetPosition()
        defaultCamera.update()
    }

    init {
        Kore.addResizeListener(resizeListener)
    }

    override fun dispose() {
        Kore.removeResizeListener(resizeListener)
        blankTexture.dispose()
        missingTexture.dispose()
    }
}
