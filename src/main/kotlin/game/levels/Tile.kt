package game.levels

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.Properties
import engine.Game
import engine.graphics.sprite.StaticSprite
import engine.physics.AxisAlignedRectangleShape
import engine.physics.Body
import engine.utils.Transform

abstract class Tile(textureName: String, val isSolid: Boolean) : Disposable {
    private val texture by Game.textures(Kore.files.asset(textureName))
    private val transform = Transform()
    private val sprite = StaticSprite(texture, transform)
    private var body: Body? = null

    lateinit var level: Level

    var x = 0
        private set

    var y = 0
        private set

    val left: Tile? get() = level.getTile(x - 1, y)
    val right: Tile? get() = level.getTile(x + 1, y)
    val top: Tile? get() = level.getTile(x, y + 1)
    val bottom: Tile? get() = level.getTile(x, y - 1)

    internal fun initialize(x: Int, y: Int, level: Level) {
        this.level = level
        this.x = x
        this.y = y

        transform.x = x * Level.TILE_SIZE + Level.TILE_SIZE * 0.5f
        transform.y = y * Level.TILE_SIZE + Level.TILE_SIZE * 0.5f
        transform.scaleX = Level.TILE_SIZE
        transform.scaleY = Level.TILE_SIZE

        if (isSolid) {
            val body = Body(transform)
            body.setShape(AxisAlignedRectangleShape())
            body.setStatic()
            Game.physics.addBody(body)
            this.body = body
        }

        Game.canvas.addComponent(sprite)
    }

    open fun update(delta: Float) {}

    open fun write(properties: Properties) {}

    open fun read(properties: Properties) {}

    override fun dispose() {
        Game.canvas.removeComponent(sprite)

        body?.let {
            Game.physics.removeBody(it)
        }
    }
}