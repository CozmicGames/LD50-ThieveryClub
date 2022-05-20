package common

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.input
import com.cozmicgames.input.Keys
import com.cozmicgames.utils.Color
import engine.GameState
import engine.graphics.ui.layout.*
import engine.graphics.ui.widgets.textButton

class TestGameState : GameState {
    val layout = GUILayout()

    var c = true

    override fun onCreate() {
        layout.addRegion("test1") {
            with(constraints) {
                x = absolute(0.0f)
                y = center()
                width = relative(0.2f)
                height = relative(0.8f)
            }

            layoutElements = {
                it.textButton("Hello") {
                    println("Test1")
                }
            }
        }

        layout.addRegion("test2") {
            with(constraints) {
                x = absolute(0.0f, true)
                y = center()
                width = relative(0.2f)
                height = relative(0.8f)
            }

            layoutElements = {
                it.textButton("Hello 2") {
                    println("Test2")
                }
            }
        }

        layout.addRegion("test3") {
            with(constraints) {
                x = center()
                y = relative(0.8f)
                width = relative(0.75f)
                height = relative(0.2f)
            }

            layoutElements = {
                it.textButton("Hello 3") {
                    println("Test3")
                }
            }
        }
    }

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.LIGHT_GRAY)
        layout.render(delta)

        if (Kore.input.isKeyJustDown(Keys.KEY_ESCAPE)) {
            c = !c

            if (c) {
                layout.getRegion("test1")?.resetToNormal()
                layout.getRegion("test2")?.resetToNormal()
                layout.getRegion("test3")?.resetToNormal()
            } else {
                layout.getRegion("test1")?.slideOutLeft()
                layout.getRegion("test2")?.slideOutRight()
                layout.getRegion("test3")?.slideOutBottom()
            }
        }

        return this
    }

    override fun onDestroy() {
        layout.dispose()
    }
}