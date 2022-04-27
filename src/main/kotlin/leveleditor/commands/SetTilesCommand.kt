package leveleditor.commands

import com.cozmicgames.utils.collections.Array2D
import common.levels.TileType
import leveleditor.LevelEditor
import leveleditor.LevelRegion

class SetTilesCommand(region: LevelRegion, private val tiles: Array2D<TileType?>) : LevelEditor.Command {
    override val isUndoable get() = true

    private val region = region.copy()
    private val previousTiles = region.getTiles()

    override fun execute(): Boolean {
        region.setTiles(tiles)
        return true
    }

    override fun undo() {
        region.setTiles(previousTiles)
    }
}
