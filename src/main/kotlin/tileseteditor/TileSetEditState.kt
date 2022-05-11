package tileseteditor

import com.cozmicgames.*
import com.cozmicgames.files.buildZip
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Rectangle
import com.cozmicgames.utils.maths.Vector2
import common.levels.TileSet
import common.levels.TileType
import common.utils.plusButton
import engine.Game
import engine.GameState
import engine.graphics.render
import engine.graphics.ui.GUI
import engine.graphics.ui.TextData
import engine.graphics.ui.widgets.*
import kotlin.math.min

class TileSetEditState : GameState {
    private lateinit var gui: GUI
    private lateinit var tileSet: TileSet
    private lateinit var tileTypeNameTextData: TextData
    private lateinit var tileTypeDefaultPathTextData: TextData
    private lateinit var rulePathTextData: TextData
    private lateinit var ruleLeftTextData: TextData
    private lateinit var ruleRightTextData: TextData
    private lateinit var ruleTopTextData: TextData
    private lateinit var ruleBottomTextData: TextData

    private var selectedTileType: TileType? = null
    private var selectedRule: TileType.Rule? = null
    private var draggedTileType: TileType? = null

    private val ruleListScroll = Vector2()

    private var currentTileTypeIndex = 0

    override fun onCreate() {
        gui = GUI()
        tileSet = TileSet()
        tileTypeNameTextData = TextData {
            if (tileSet.tileTypes.any { it.name == text })
                Kore.log.error(this::class, "Tile type name already exists. Tile types must have unique names.")
            else
                selectedTileType?.name = tileTypeNameTextData.text
        }

        tileTypeDefaultPathTextData = TextData { selectedTileType?.defaultPath = tileTypeDefaultPathTextData.text }
        rulePathTextData = TextData { selectedRule?.path = rulePathTextData.text }
        ruleLeftTextData = TextData { selectedRule?.left = ruleLeftTextData.text }
        ruleRightTextData = TextData { selectedRule?.right = ruleRightTextData.text }
        ruleTopTextData = TextData { selectedRule?.top = ruleTopTextData.text }
        ruleBottomTextData = TextData { selectedRule?.bottom = ruleBottomTextData.text }
    }

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.DARK_GRAY)

        drawVisualEditor()

        gui.begin()
        selectType(gui)
        editCurrentType(gui)
        selectRule(gui)
        editRule(gui)
        drawDraggedTileType(gui)
        gui.end()

        if (!Kore.input.isTouched)
            draggedTileType = null

        return this
    }

    private fun selectType(gui: GUI) {
        gui.sameLine {
            tileSet.tileTypes.forEach {
                val element = gui.selectableImage(it.defaultTexture, 32.0f, 32.0f, selectedTileType == it) {
                    if (selectedTileType != it) {
                        selectedTileType = it
                        tileTypeNameTextData.setText(it.name)
                        tileTypeDefaultPathTextData.setText(it.defaultPath)
                        selectedRule = null
                    }
                }

                val rectangle = Rectangle(element.x, element.y, element.width, element.height)
                if (gui.touchPosition in rectangle && Kore.input.isTouched)
                    draggedTileType = it
            }
            gui.tooltip(gui.plusButton(32.0f, 32.0f) {
                tileSet.add(TileType("New tile type ${++currentTileTypeIndex}", "default.png", false))
            }, "Adds a new tile type.")
        }
    }

    private fun editCurrentType(gui: GUI) {
        selectedTileType?.let {
            gui.group(Color.GRAY) {
                gui.sameLine {
                    gui.group {
                        gui.tooltip(gui.label("Name"), "The name of the tile type.")
                        gui.tooltip(gui.label("Default path"), "The path of the default image of the tile type.")
                        gui.tooltip(gui.label("Is solid"), "Whether the tile type should be collided with.")
                    }

                    gui.group {
                        gui.textField(tileTypeNameTextData) {}
                        gui.sameLine {
                            gui.textField(tileTypeDefaultPathTextData) {}
                            gui.textButton("Load") {
                                val result = Kore.dialogs.open(filters = Kore.graphics.supportedImageFormats.toList().toTypedArray())
                                if (result != null) {
                                    tileTypeDefaultPathTextData.setText(result)
                                    tileTypeDefaultPathTextData.onEnter(tileTypeDefaultPathTextData)
                                }
                            }
                        }
                        gui.checkBox(it.isSolid) {
                            selectedTileType?.isSolid = it
                        }
                    }
                }

                gui.tooltip(gui.textButton("Delete") {
                    tileSet.remove(it)
                    selectedTileType = null
                }, "Deletes the selected tile type.")
            }
        }
    }

    private fun selectRule(gui: GUI) {
        selectedTileType?.let { type ->
            gui.label("Rules")
            gui.scrollPane(maxHeight = Kore.graphics.height - (gui.lastElement?.nextY ?: 0.0f), scroll = ruleListScroll) {
                gui.tooltip(gui.plusButton(32.0f, 32.0f) {
                    type.rules += TileType.Rule(path = "default.png")
                }, "Adds a new rule.")
                type.rules.forEach {
                    gui.selectableImage(it.texture, 32.0f, 32.0f, selectedRule == it) {
                        if (selectedRule != it) {
                            selectedRule = it
                            rulePathTextData.setText(it.path)
                            ruleLeftTextData.setText(it.left ?: "")
                            ruleRightTextData.setText(it.right ?: "")
                            ruleTopTextData.setText(it.top ?: "")
                            ruleBottomTextData.setText(it.bottom ?: "")
                        }
                    }
                }
            }
        }
    }

    private fun editRule(gui: GUI) {
        selectedRule?.let {
            gui.sameLine {
                gui.group {
                    gui.tooltip(gui.label("Path"), "The path of the rules' image.")
                    gui.tooltip(gui.label("Left"), "What should be left of the tile type for this rule to apply.")
                    gui.tooltip(gui.label("Right"), "What should be right of the tile type for this rule to apply.")
                    gui.tooltip(gui.label("Top"), "What should be on top of the tile type for this rule to apply.")
                    gui.tooltip(gui.label("Bottom"), "What should be below of the tile type for this rule to apply.")
                }

                gui.group {
                    gui.sameLine {
                        gui.textField(rulePathTextData) {}
                        gui.textButton("Load") {
                            val result = Kore.dialogs.open(filters = Kore.graphics.supportedImageFormats.toList().toTypedArray())
                            if (result != null) {
                                rulePathTextData.setText(result)
                                rulePathTextData.onEnter(rulePathTextData)
                            }
                        }
                    }
                    gui.textField(ruleLeftTextData) {}
                    gui.textField(ruleRightTextData) {}
                    gui.textField(ruleTopTextData) {}
                    gui.textField(ruleBottomTextData) {}
                }
            }

            gui.tooltip(gui.textButton("Delete") {
                selectedTileType?.rules?.remove(it)
                selectedRule = null
            }, "Deletes the selected rule.")
        }
    }

    private fun drawVisualEditor() {
        Game.renderer.render {
            val tile = selectedTileType

            val imageSize = min(Kore.graphics.width, Kore.graphics.height) * 0.75f / 3.0f

            fun drawEmpty(x: Float, y: Float) {
                it.drawPathFilled(it.path {
                    rect(x, y, imageSize, imageSize)
                }, Color.GRAY)

                it.drawPathStroke(it.path {
                    rect(x, y, imageSize, imageSize)
                }, 2.0f, true, Color.LIGHT_GRAY, 0.0f)
            }

            val centerImageX = Kore.graphics.width * 0.5f - imageSize * 0.5f
            val centerImageY = Kore.graphics.height * 0.5f - imageSize * 0.5f

            if (tile == null) {
                drawEmpty(centerImageX, centerImageY)

                return@render
            }

            val rule = selectedRule

            if (rule == null) {
                it.draw(tile.defaultTexture, centerImageX, centerImageY, imageSize, imageSize)
            } else {
                it.draw(rule.texture, centerImageX, centerImageY, imageSize, imageSize)

                val leftImageX = centerImageX - imageSize
                val leftImageY = centerImageY

                val rightImageX = centerImageX + imageSize
                val rightImageY = centerImageY

                val topImageX = centerImageX
                val topImageY = centerImageY - imageSize

                val bottomImageX = centerImageX
                val bottomImageY = centerImageY + imageSize

                val leftTileType = if (rule.left == null) null else tileSet[requireNotNull(rule.left)]
                val rightTileType = if (rule.right == null) null else tileSet[requireNotNull(rule.right)]
                val topTileType = if (rule.top == null) null else tileSet[requireNotNull(rule.top)]
                val bottomTileType = if (rule.bottom == null) null else tileSet[requireNotNull(rule.bottom)]

                if (leftTileType == null)
                    drawEmpty(leftImageX, leftImageY)
                else
                    it.draw(leftTileType.getTexture(right = tile), leftImageX, leftImageY, imageSize, imageSize)

                if (rightTileType == null)
                    drawEmpty(rightImageX, rightImageY)
                else
                    it.draw(rightTileType.getTexture(left = tile), rightImageX, rightImageY, imageSize, imageSize)

                if (topTileType == null)
                    drawEmpty(topImageX, topImageY)
                else
                    it.draw(topTileType.getTexture(bottom = tile), topImageX, topImageY, imageSize, imageSize)

                if (bottomTileType == null)
                    drawEmpty(bottomImageX, bottomImageY)
                else
                    it.draw(bottomTileType.getTexture(top = tile), bottomImageX, bottomImageY, imageSize, imageSize)

                if (draggedTileType != null) {
                    val rectangle = Rectangle()
                    val touchPosition = Vector2(Kore.input.x.toFloat(), Kore.input.y.toFloat())
                    rectangle.width = imageSize
                    rectangle.height = imageSize

                    if (touchPosition in rectangle.apply {
                            x = leftImageX
                            y = leftImageY
                        }) {
                        it.drawPathStroke(it.path {
                            rect(rectangle)
                        }, 1.5f, true, Color.WHITE)

                        if (!Kore.input.isTouched) {
                            val name = requireNotNull(draggedTileType).name
                            rule.left = name
                            ruleLeftTextData.setText(name)
                        }
                    }

                    if (touchPosition in rectangle.apply {
                            x = rightImageX
                            y = rightImageY
                        }) {
                        it.drawPathStroke(it.path {
                            rect(rectangle)
                        }, 1.5f, true, Color.WHITE)

                        if (!Kore.input.isTouched) {
                            val name = requireNotNull(draggedTileType).name
                            rule.right = name
                            ruleRightTextData.setText(name)
                        }
                    }

                    if (touchPosition in rectangle.apply {
                            x = topImageX
                            y = topImageY
                        }) {
                        it.drawPathStroke(it.path {
                            rect(rectangle)
                        }, 1.5f, true, Color.WHITE)

                        if (!Kore.input.isTouched) {
                            val name = requireNotNull(draggedTileType).name
                            rule.top = name
                            ruleTopTextData.setText(name)
                        }
                    }

                    if (touchPosition in rectangle.apply {
                            x = bottomImageX
                            y = bottomImageY
                        }) {
                        it.drawPathStroke(it.path {
                            rect(rectangle)
                        }, 1.5f, true, Color.WHITE)

                        if (!Kore.input.isTouched) {
                            val name = requireNotNull(draggedTileType).name
                            rule.bottom = name
                            ruleBottomTextData.setText(name)
                        }
                    }
                }
            }
        }
    }

    private fun drawDraggedTileType(gui: GUI) {
        val tileType = draggedTileType ?: return
        val draggedTextureSize = 32.0f

        gui.transient {
            gui.setLastElement(gui.absolute(gui.touchPosition.x - draggedTextureSize * 0.5f, gui.touchPosition.y - draggedTextureSize * 0.5f))
            gui.image(tileType.defaultTexture, draggedTextureSize)
        }
    }

    override fun onDestroy() {
        gui.dispose()
    }
}

/**
 * TODO:
 * - Save/load
 * - On saving, copy used images to the tileset file --> Maybe support zip files?
 * - For this, also change the paths in tiletypes to the local ones
 * - Add automatic rule generation
 * - For example, 9-slice
 * - Make pretty
 */

