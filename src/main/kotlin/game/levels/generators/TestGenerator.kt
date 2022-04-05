package game.levels.generators

import game.levels.Level
import game.levels.LevelGenerator
import game.levels.tiles.GroundTile

class TestGenerator : LevelGenerator {
    override fun generate(level: Level) {
        for (x in 0 until level.width) {
            repeat(4) {
                level.setTile(x, it, GroundTile())
            }
        }

        repeat(5) {
            level.setTile(it + 5, 5, GroundTile())
            level.setTile(it + 5, 6, GroundTile())
        }

        repeat(5) {
            level.setTile(it + 20, 5, GroundTile())
        }
    }
}