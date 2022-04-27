package tileseteditor

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import common.levels.TileSet
import common.levels.TileType
import common.utils.plusButton
import engine.GameState
import engine.graphics.asRegion
import engine.graphics.ui.GUI
import engine.graphics.ui.TextData
import engine.graphics.ui.widgets.*

class TileSetEditState : GameState {
    private lateinit var ui: GUI
    private lateinit var tileSet: TileSet
    private lateinit var tileTypeNameTextData: TextData
    private lateinit var tileTypeDefaultPathTextData: TextData

    private var selectedTileType: TileType? = null

    override fun onCreate() {
        ui = GUI()
        tileSet = TileSet()
        tileTypeNameTextData = TextData { selectedTileType?.name = tileTypeNameTextData.text }
        tileTypeDefaultPathTextData = TextData { selectedTileType?.defaultPath = tileTypeDefaultPathTextData.text }

        Kore.addDropListener(this::onDropped)
    }

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.DARK_GRAY)

        ui.begin(delta)
        selectType()
        editCurrentType()
        ui.end()

        return this
    }

    private fun selectType() {
        ui.sameLine {
            tileSet.tileTypes.forEach {
                ui.selectableImage(it.defaultTexture, 32.0f, 32.0f, selectedTileType == it) {
                    selectedTileType = it
                }
            }
            ui.plusButton(32.0f, 32.0f) {
                tileSet.add(TileType("New tile type", "default.png", false))
            }
        }
    }

    private fun editCurrentType() {
        selectedTileType?.let {
            ui.sameLine {
                ui.group {
                    ui.label("Name")
                    ui.label("Default path")
                    ui.label("Solid")
                }

                ui.group {
                    ui.textField(tileTypeNameTextData) {}
                    ui.textField(tileTypeDefaultPathTextData) {}
                    ui.checkBox(it.isSolid) {
                        selectedTileType?.isSolid = it
                    }
                }
            }

            ui.textButton("Delete") {
                tileSet.remove(it)
                selectedTileType = null
            }
        }
    }

    fun editRules() {
        //TODO: Implement
    }

    private fun onDropped(values: Array<String>) {
        //TODO: only allow images, only perform if tile type is selected and no rule is selected
        if (values.size == 1) {
            val path = values[0]
            selectedTileType?.defaultPath = path
            tileTypeDefaultPathTextData.setText(path)
        }
    }

    override fun onDestroy() {
        ui.dispose()

        Kore.removeDropListener(this::onDropped)
    }
}