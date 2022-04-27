package engine.graphics.font

import com.cozmicgames.graphics.gpu.Texture2D
import com.cozmicgames.utils.Disposable
import engine.graphics.shaders.DefaultShader
import engine.graphics.shaders.Shader

interface DrawableFont : Disposable {
    companion object {
        fun defaultChars() = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890 \"!`?'.,;:()[]{}<>|/@\\^\$-%+=#_&~*"
    }

    val drawableCharacters: String
    val size: Float

    val requiredShader: Shader get() = DefaultShader
    val texture: Texture2D

    operator fun get(char: Char): Glyph
}