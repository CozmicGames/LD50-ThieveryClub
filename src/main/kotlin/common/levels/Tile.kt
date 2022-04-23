package common.levels

import com.cozmicgames.utils.Disposable
import engine.Game
import engine.graphics.sprite.StaticSprite
import engine.physics.AxisAlignedRectangleShape
import engine.physics.Body
import engine.utils.Transform

class Tile(val type: TileType) : Disposable {
    private val transform = Transform()
    private val sprite = StaticSprite(type.defaultTexture, transform)
    private var body: Body? = null

    lateinit var level: Level

    val isActive get() = level.isActive

    var x = 0
        private set

    var y = 0
        private set

    val left get() = level.getTile(x - 1, y)
    val right get() = level.getTile(x + 1, y)
    val top get() = level.getTile(x, y + 1)
    val bottom get() = level.getTile(x, y - 1)

    private val isReachable get() = left != null && right != null && top != null && bottom != null

    internal fun initialize(x: Int, y: Int, level: Level) {
        this.level = level
        this.x = x
        this.y = y

        transform.x = x * Level.TILE_SIZE + Level.TILE_SIZE * 0.5f
        transform.y = y * Level.TILE_SIZE + Level.TILE_SIZE * 0.5f
        transform.scaleX = Level.TILE_SIZE
        transform.scaleY = Level.TILE_SIZE

        level.canvas.addComponent(sprite)
    }

    internal fun updateBody() {
        if (type.isSolid && isReachable) {
            val body = Body(transform)
            body.setShape(AxisAlignedRectangleShape())
            body.setStatic()
            this.body = body

            if (isActive)
                Game.physics.addBody(body)
        } else body?.let {
            if (isActive)
                Game.physics.removeBody(it)

            body = null
        }
    }

    internal fun updateSprite() {
        sprite.texture = type.getTexture(this)
    }

    fun activate() {
        level.canvas.addComponent(sprite)
        body?.let {
            Game.physics.addBody(it)
        }
    }

    fun deactivate() {
        level.canvas.removeComponent(sprite)
        body?.let {
            Game.physics.removeBody(it)
        }
    }

    override fun dispose() {
        level.canvas.removeComponent(sprite)
        body?.let {
            Game.physics.removeBody(it)
        }
    }
}