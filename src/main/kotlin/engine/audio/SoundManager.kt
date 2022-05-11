package engine.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.audio.Sound
import com.cozmicgames.files
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.Files
import com.cozmicgames.files.extension
import com.cozmicgames.log
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.extensions.extension
import com.cozmicgames.utils.use
import kotlin.reflect.KProperty

class SoundManager : Disposable {
    inner class Getter(val file: FileHandle) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrAdd(file)
    }

    private val sounds = hashMapOf<FileHandle, Sound>()

    fun add(file: FileHandle) {
        if (!file.exists) {
            Kore.log.error(this::class, "Sound file not found: $file")
            return
        }

        val sound = file.read().use {
            Kore.audio.readSound(it, file.extension)
        }

        if (sound == null) {
            Kore.log.error(this::class, "Failed to load sound file: $file")
            return
        }

        add(file, sound)
    }

    operator fun contains(file: FileHandle) = file in sounds

    fun add(file: FileHandle, sound: Sound) {
        sounds[file] = sound
    }

    fun remove(file: FileHandle) {
        sounds.remove(file)
    }

    operator fun get(file: FileHandle): Sound? {
        return sounds[file]
    }

    fun getOrAdd(file: FileHandle): Sound {
        if (file !in this)
            add(file)

        return requireNotNull(this[file])
    }

    override fun dispose() {
        sounds.forEach { (_, sound) ->
            if (sound is Disposable)
                sound.dispose()
        }
    }

    operator fun invoke(file: FileHandle) = Getter(file)
}