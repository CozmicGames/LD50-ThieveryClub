package leveleditor

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.graphics
import com.cozmicgames.input
import com.cozmicgames.input.Keys
import com.cozmicgames.input.MouseButtons
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.use
import common.levels.Level
import common.levels.TileSet
import common.levels.TileType
import common.levels.createTestTileSet
import engine.Game
import engine.GameState
import engine.graphics.Canvas
import engine.graphics.render
import engine.graphics.ui.GUI
import engine.graphics.ui.TextData
import engine.graphics.ui.widgets.selectableImage
import engine.graphics.ui.widgets.textButton
import engine.graphics.ui.widgets.textField
import engine.utils.unproject

class LevelEditState(val width: Int, val height: Int, val backgroundColor: Color = Color.SKY) : GameState {
    companion object {
        private var id = 0
    }

    private var selectedTileType: TileType? = null

    private lateinit var ui: GUI
    private lateinit var textData: TextData
    private lateinit var levelCanvas: Canvas
    private lateinit var tileSet: TileSet
    private lateinit var level: Level

    private var name = "level${id++}"

    override fun onCreate() {
        ui = GUI()
        textData = TextData(name) {}
        levelCanvas = Canvas()
        tileSet = createTestTileSet() //TODO: load from file
        level = Level(levelCanvas, tileSet)
        level.initialize(width, height)
    }

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(backgroundColor)

        level.update()

        if (Kore.input.isButtonDown(MouseButtons.MIDDLE) || (Kore.input.isKeyDown(Keys.KEY_CONTROL) && (Kore.input.isButtonDown(MouseButtons.LEFT) || Kore.input.isButtonDown(MouseButtons.RIGHT)))) {
            levelCanvas.camera.position.x -= Kore.input.deltaX
            levelCanvas.camera.position.y -= Kore.input.deltaY
            levelCanvas.camera.update()
        }

        levelCanvas.render(delta)

        val worldMousePosition = levelCanvas.camera.unproject(Kore.input.x.toFloat(), Kore.input.y.toFloat())
        val mouseTileX = (worldMousePosition.x.toInt() / Level.TILE_SIZE).toInt()
        val mouseTileY = (worldMousePosition.y.toInt() / Level.TILE_SIZE).toInt()

        val gridColor = Color(0.5f, 0.5f, 0.5f, 0.5f)

        Game.renderer.render(levelCanvas.camera) {
            repeat(width) { tileX ->
                repeat(height) { tileY ->
                    val isHoveredTile = tileX == mouseTileX && tileY == mouseTileY

                    if (level.getTile(tileX, tileY) == null || isHoveredTile) {
                        val x = tileX * Level.TILE_SIZE
                        val y = tileY * Level.TILE_SIZE

                        val color = if (isHoveredTile) Color.WHITE else gridColor

                        it.drawPathStroke(it.path {
                            rect(x, y, Level.TILE_SIZE, Level.TILE_SIZE)
                        }, 1.0f, true, color, 0.0f)
                    }
                }
            }
        }

        ui.begin()
        selectType(tileSet)

        if (!Kore.input.isKeyDown(Keys.KEY_CONTROL))
            setTile(mouseTileX, mouseTileY)

        ui.sameLine {
            ui.textButton("Clear") {
                level.clear()
            }

            ui.textField(textData) {
                name = textData.text
            }

            ui.textButton("Save") {
                val properties = level.write()
                Kore.files.local("$name.txt").write(false).use {
                    it.writeString(properties.write())
                }
            }
        }

        ui.end()

        return this
    }

    private fun setTile(tileX: Int, tileY: Int) {
        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height)
            return

        if (Kore.input.isButtonDown(MouseButtons.LEFT))
            level.setTile(tileX, tileY, selectedTileType)

        if (Kore.input.isButtonDown(MouseButtons.RIGHT))
            level.setTile(tileX, tileY, null)
    }

    private fun selectType(set: TileSet) {
        ui.sameLine {
            set.tileTypes.forEach {
                ui.selectableImage(it.defaultTexture, 32.0f, 32.0f, selectedTileType == it) {
                    selectedTileType = it
                }
            }
        }
    }

    override fun onDestroy() {
        ui.dispose()
    }
}