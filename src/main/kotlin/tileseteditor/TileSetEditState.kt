package tileseteditor

import com.cozmicgames.*
import com.cozmicgames.files.*
import com.cozmicgames.graphics.safeHeight
import com.cozmicgames.graphics.safeWidth
import com.cozmicgames.utils.Charsets.UTF8
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Properties
import com.cozmicgames.utils.maths.Corners
import com.cozmicgames.utils.maths.Rectangle
import com.cozmicgames.utils.maths.Vector2
import common.levels.TileSet
import common.levels.TileType
import common.utils.plusButton
import engine.Game
import engine.GameState
import engine.graphics.asRegion
import engine.graphics.render
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.TextData
import engine.graphics.ui.drawRect
import engine.graphics.ui.widgets.*
import kotlin.math.floor
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
    private lateinit var imageFilterTextData: TextData

    private var selectedTileType: TileType? = null
    private var selectedRule: TileType.Rule? = null
    private var draggedTileType: TileType? = null

    private var draggedImageFileName: String? = null

    private val ruleListScroll = Vector2()
    private val imageListScroll = Vector2()

    private var currentTileTypeIndex = 0

    private val imageElementSize = 32.0f

    override fun onCreate() {
        gui = GUI()
        tileSet = TileSet(Kore.files.local(WORKING_DIRECTORY_NAME))
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
        imageFilterTextData = TextData {}
    }

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.DARK_GRAY)

        drawVisualEditor()

        gui.begin()
        selectType(gui)
        editCurrentType(gui)
        selectRule(gui)
        editRule(gui)
        drawImageList(gui)
        drawDraggedTileType(gui)
        drawDraggedImage(gui)
        gui.end()

        if (!Kore.input.isTouched) {
            draggedTileType = null
            draggedImageFileName = null
        }

        return this
    }

    private fun selectType(gui: GUI) {
        gui.sameLine {
            tileSet.tileTypes.forEach {
                val element = gui.selectableImage(it.defaultTexture, imageElementSize, imageElementSize, selectedTileType == it) {
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
            gui.tooltip(gui.plusButton(imageElementSize, imageElementSize) {
                tileSet.add("New tile type ${++currentTileTypeIndex}", "default.png", false)
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
                        val element = gui.textField(tileTypeDefaultPathTextData) {}
                        if (draggedImageFileName != null && isDroppedAtElement(gui, element)) {
                            val path = requireNotNull(draggedImageFileName)
                            tileTypeDefaultPathTextData.setText(path)
                            selectedTileType?.defaultPath = path
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
                gui.tooltip(gui.plusButton(imageElementSize, imageElementSize) {
                    type.addRule(path = "default.png")
                }, "Adds a new rule.")
                type.rules.forEach {
                    gui.selectableImage(it.texture, imageElementSize, imageElementSize, selectedRule == it) {
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
                    gui.tooltip(gui.label("Bottom"), "What should be below of the tile type for this rule to apply.")
                    gui.tooltip(gui.label("Top"), "What should be on top of the tile type for this rule to apply.")
                    gui.tooltip(gui.label("Left"), "What should be left of the tile type for this rule to apply.")
                    gui.tooltip(gui.label("Right"), "What should be right of the tile type for this rule to apply.")
                }

                gui.group {
                    var element = gui.textField(rulePathTextData) {}
                    if (draggedImageFileName != null && isDroppedAtElement(gui, element)) {
                        val path = requireNotNull(draggedImageFileName)
                        rulePathTextData.setText(path)
                        it.path = path
                    }

                    element = gui.textField(ruleBottomTextData) {}
                    if (draggedTileType != null && isDroppedAtElement(gui, element)) {
                        val name = requireNotNull(draggedTileType).name
                        it.bottom = name
                        ruleBottomTextData.setText(name)
                    }

                    element = gui.textField(ruleTopTextData) {}
                    if (draggedTileType != null && isDroppedAtElement(gui, element)) {
                        val name = requireNotNull(draggedTileType).name
                        it.top = name
                        ruleTopTextData.setText(name)
                    }

                    element = gui.textField(ruleLeftTextData) {}
                    if (draggedTileType != null && isDroppedAtElement(gui, element)) {
                        val name = requireNotNull(draggedTileType).name
                        it.left = name
                        ruleLeftTextData.setText(name)
                    }

                    element = gui.textField(ruleRightTextData) {}
                    if (draggedTileType != null && isDroppedAtElement(gui, element)) {
                        val name = requireNotNull(draggedTileType).name
                        it.right = name
                        ruleRightTextData.setText(name)
                    }
                }
            }

            gui.tooltip(gui.textButton("Delete") {
                selectedTileType?.removeRule(it)
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

            if (rule == null)
                it.draw(tile.defaultTexture, centerImageX, centerImageY, imageSize, imageSize)
            else {
                val rectangle = Rectangle()
                val touchPosition = Vector2(Kore.input.x.toFloat(), Kore.input.y.toFloat())
                rectangle.width = imageSize
                rectangle.height = imageSize

                it.draw(rule.texture, centerImageX, centerImageY, imageSize, imageSize)

                if (draggedImageFileName != null) {
                    if (touchPosition in rectangle.apply {
                            x = centerImageX
                            y = centerImageY
                        }) {
                        it.drawPathStroke(it.path {
                            rect(rectangle)
                        }, 1.5f, true, Color.WHITE)

                        if (!Kore.input.isTouched) {
                            val name = requireNotNull(draggedImageFileName)
                            rule.path = name
                            rulePathTextData.setText(name)
                        }
                    }
                }

                val bottomImageX = centerImageX
                val bottomImageY = centerImageY - imageSize

                val topImageX = centerImageX
                val topImageY = centerImageY + imageSize

                val leftImageX = centerImageX - imageSize
                val leftImageY = centerImageY

                val rightImageX = centerImageX + imageSize
                val rightImageY = centerImageY

                val bottomTileType = if (rule.bottom == null) null else tileSet[requireNotNull(rule.bottom)]
                val topTileType = if (rule.top == null) null else tileSet[requireNotNull(rule.top)]
                val leftTileType = if (rule.left == null) null else tileSet[requireNotNull(rule.left)]
                val rightTileType = if (rule.right == null) null else tileSet[requireNotNull(rule.right)]

                if (bottomTileType == null)
                    drawEmpty(bottomImageX, bottomImageY)
                else
                    it.draw(bottomTileType.getTexture(top = tile), bottomImageX, bottomImageY, imageSize, imageSize)

                if (topTileType == null)
                    drawEmpty(topImageX, topImageY)
                else
                    it.draw(topTileType.getTexture(bottom = tile), topImageX, topImageY, imageSize, imageSize)

                if (leftTileType == null)
                    drawEmpty(leftImageX, leftImageY)
                else
                    it.draw(leftTileType.getTexture(right = tile), leftImageX, leftImageY, imageSize, imageSize)

                if (rightTileType == null)
                    drawEmpty(rightImageX, rightImageY)
                else
                    it.draw(rightTileType.getTexture(left = tile), rightImageX, rightImageY, imageSize, imageSize)

                if (draggedTileType != null) {
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
                }
            }
        }
    }

    private fun drawDraggedTileType(gui: GUI) {
        val tileType = draggedTileType ?: return

        gui.transient {
            gui.setLastElement(gui.absolute(gui.touchPosition.x - imageElementSize * 0.5f, gui.touchPosition.y - imageElementSize * 0.5f))
            gui.image(tileType.defaultTexture, imageElementSize)
        }
    }

    private fun drawImageList(gui: GUI) {
        val padding = 4.0f
        val width = Kore.graphics.safeWidth * 0.75f
        val height = Kore.graphics.safeHeight * 0.2f
        val x = (Kore.graphics.safeWidth - width) * 0.5f
        val y = Kore.graphics.safeHeight - height

        gui.transient {
            gui.setLastElement(gui.absolute(x, y))
            gui.group {
                gui.sameLine {
                    gui.textButton("Import") {
                        val result = Kore.dialogs.open(filters = Kore.graphics.supportedImageFormats.toList().toTypedArray())
                        if (result != null)
                            importImage(Kore.files.absolute(result))
                    }

                    gui.textField(imageFilterTextData)
                }

                val files = arrayListOf<String>()
                val imagesPerRow = floor(width / imageElementSize).toInt()

                Kore.files.local(WORKING_DIRECTORY_NAME).list(files::add)

                gui.scrollPane(maxWidth = width, scroll = imageListScroll) {
                    files.filter {
                        if (imageFilterTextData.text.isNotEmpty())
                            it.contains(imageFilterTextData.text, true)
                        else
                            true
                    }.chunked(imagesPerRow) {
                        gui.sameLine {
                            it.forEach {
                                val element = gui.image(Game.textures[Kore.files.local(WORKING_DIRECTORY_NAME).child(it)] ?: Game.graphics2d.missingTexture.asRegion(), imageElementSize)
                                gui.tooltip(element, it)

                                val rectangle = Rectangle(element.x, element.y, element.width, element.height)
                                if (gui.touchPosition in rectangle && Kore.input.isTouched)
                                    draggedImageFileName = it

                                gui.spacing(padding)
                            }
                        }
                        gui.blankLine(padding)
                    }
                }
            }
        }
    }

    private fun isDroppedAtElement(gui: GUI, element: GUIElement): Boolean {
        val rectangle = Rectangle(element.x, element.y, element.width, element.height)
        if (gui.touchPosition in rectangle) {
            gui.currentCommandList.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, Corners.NONE, 0.0f, 1.5f, Color.WHITE)

            if (!Kore.input.isTouched)
                return true
        }

        return false
    }

    private fun drawDraggedImage(gui: GUI) {
        val imageFileName = draggedImageFileName ?: return
        val imageFile = Kore.files.local(WORKING_DIRECTORY_NAME).child(imageFileName)

        gui.transient {
            gui.setLastElement(gui.absolute(gui.touchPosition.x - imageElementSize * 0.5f, gui.touchPosition.y - imageElementSize * 0.5f))
            gui.image(Game.textures[imageFile] ?: Game.graphics2d.missingTexture.asRegion(), imageElementSize)
        }
    }

    private fun loadImage(fileName: String) {
        val file = Kore.files.local(WORKING_DIRECTORY_NAME).child(fileName)

        if (file in Game.textures)
            return

        Game.textures.add(file)
    }

    private fun open(fileName: String) {
        val tileSetFile = Kore.files.local("$TILESETS_DIRECTORY_NAME$fileName.zip")

        tileSetFile.list {
            if (it == TILESET_FILE_NAME) {
                val properties = Properties()
                properties.read(tileSetFile.child(it).readToString(UTF8))
                tileSet.read(properties)
            } else
                importImage(tileSetFile.child(it))
        }
    }

    private fun importImage(file: FileHandle) {
        val destFile = Kore.files.local(WORKING_DIRECTORY_NAME).child(file.nameWithExtension)
        file.copyTo(destFile)
        loadImage(destFile.nameWithExtension)
    }

    private fun save(fileName: String) {
        val workingDirectory = Kore.files.local(WORKING_DIRECTORY_NAME)
        val file = Kore.files.local("$TILESETS_DIRECTORY_NAME$fileName.zip")

        file.buildZip {
            workingDirectory.list {
                writeFile(workingDirectory.child(it), it)
            }
            addFile(TILESET_FILE_NAME, tileSet.write().write().toByteArray(Charsets.UTF_8))
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

