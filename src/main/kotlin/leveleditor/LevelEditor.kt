package leveleditor

import com.cozmicgames.utils.collections.Array2D
import com.cozmicgames.utils.collections.FixedSizeStack
import common.levels.Level
import common.levels.TileType
import leveleditor.commands.SetTilesCommand

class LevelEditor(undoRedoStackSize: Int = 100) {
    interface Command {
        val isUndoable get() = false

        fun execute(): Boolean
        fun undo()
    }

    private val undoCommands = FixedSizeStack<Command>(undoRedoStackSize)
    private val redoCommands = FixedSizeStack<Command>(undoRedoStackSize)

    val hasUndoableCommand get() = !undoCommands.isEmpty
    val hasRedoableCommand get() = !redoCommands.isEmpty

    fun execute(command: Command) {
        if (command.isUndoable)
            undoCommands.push(command)

        if (command.execute())
            redoCommands.clear()
    }

    fun undo(): Boolean {
        if (undoCommands.isEmpty)
            return false

        val command = undoCommands.pop()
        redoCommands.push(command)
        command.undo()
        return true
    }

    fun redo(): Boolean {
        if (redoCommands.isEmpty)
            return false

        val command = redoCommands.pop()
        undoCommands.push(command)
        command.execute()
        return true
    }

    fun clear() {
        undoCommands.clear()
        redoCommands.clear()
    }
}

fun LevelEditor.setTiles(level: Level, x: Int, y: Int, width: Int, height: Int, tiles: Array2D<TileType?>) = execute(SetTilesCommand(level.getRegion(x, y, width, height), tiles))

fun LevelEditor.setTile(level: Level, x: Int, y: Int, tile: TileType?) = execute(SetTilesCommand(level.getRegion(x, y, 1, 1), Array2D(1, 1) { _, _ -> tile }))

fun LevelEditor.setTiles(region: LevelRegion, sourceRegion: LevelRegion) = execute(SetTilesCommand(region, sourceRegion.getTiles()))
