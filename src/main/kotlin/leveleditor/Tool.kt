package leveleditor

import engine.graphics.TextureRegion

object Tools {


    val registered get() = internalRegistered.asIterable()

    private val internalRegistered = arrayListOf<Tool>()

    private fun register(tool: Tool) {
        internalRegistered += tool
    }
}

interface Tool {
    val icon: TextureRegion


}