package engine.graphics

import com.cozmicgames.Kore
import com.cozmicgames.files.FileHandle
import com.cozmicgames.graphics
import com.cozmicgames.graphics.Image
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.graphics.loadImage
import com.cozmicgames.log
import com.cozmicgames.utils.Disposable
import engine.Game
import kotlin.reflect.KProperty

class TextureManager : Disposable {
    inner class Getter(private val fileHandle: FileHandle, filter: Texture.Filter) {
        init {
            if (fileHandle !in this@TextureManager)
                add(fileHandle, filter)
        }

        operator fun getValue(thisRef: Any, property: KProperty<*>) = get(fileHandle) ?: Game.graphics2d.missingTexture.asRegion()
    }

    data class TextureKey(val filter: Texture.Filter)

    private val textures = hashMapOf<TextureKey, TextureAtlas>()
    private val keys = hashMapOf<String, TextureKey>()

    fun add(fileHandle: FileHandle, filter: Texture.Filter = Texture.Filter.NEAREST) {
        val image = if (fileHandle.exists)
            Kore.graphics.loadImage(fileHandle)
        else {
            Kore.log.error(this::class, "Texture file not found: $fileHandle")
            return
        }

        if (image == null) {
            Kore.log.error(this::class, "Failed to load texture file: $fileHandle")
            return
        }

        add(fileHandle.toString(), image, filter)
    }

    operator fun contains(fileHandle: FileHandle) = fileHandle.toString() in keys

    operator fun contains(name: String) = name in keys

    fun add(file: String, image: Image, filter: Texture.Filter = Texture.Filter.NEAREST) {
        val key = TextureKey(filter)
        val atlas = getAtlas(key)
        atlas.add(file to image)
        keys[file] = key
    }

    fun remove(file: String) {
        val key = keys[file] ?: return
        textures[key]?.remove(file)
    }

    operator fun get(fileHandle: FileHandle) = get(fileHandle.toString())

    operator fun get(name: String): TextureRegion? {
        val key = keys[name] ?: return null
        val texture = textures[key] ?: return null
        return texture[name]
    }

    fun getOrAdd(fileHandle: FileHandle, filter: Texture.Filter = Texture.Filter.NEAREST): TextureRegion {
        if (fileHandle !in this)
            add(fileHandle, filter)

        return this[fileHandle] ?: Game.graphics2d.missingTexture.asRegion()
    }

    fun getAtlas(key: TextureKey): TextureAtlas {
        return textures.getOrPut(key) {
            TextureAtlas().also {
                it.texture.setFilter(key.filter, key.filter)
            }
        }
    }

    override fun dispose() {
        textures.forEach { (_, texture) ->
            texture.dispose()
        }
    }

    operator fun invoke(fileHandle: FileHandle, filter: Texture.Filter = Texture.Filter.NEAREST) = Getter(fileHandle, filter)
}
