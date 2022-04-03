package game.levels

import com.gratedgames.utils.Disposable
import engine.Game
import engine.graphics.sprite.StaticSprite
import engine.physics.AxisAlignedRectangleShape
import engine.physics.Body
import engine.utils.Transform

abstract class Tile(x: Int, y: Int, textureName: String, isSolid: Boolean) : Disposable {
    val texture by Game.textures(textureName)
    val transform = Transform()
    val sprite = StaticSprite(texture, transform)
    val body: Body?

    init {
        transform.x = x * Level.TILE_SIZE
        transform.y = y * Level.TILE_SIZE
        transform.scaleX = Level.TILE_SIZE
        transform.scaleY = Level.TILE_SIZE

        if (isSolid) {
            body = Body(transform)
            body.setShape(AxisAlignedRectangleShape())
            body.setStatic()
            Game.physics.addBody(body)
        } else
            body = null

        Game.canvas.addComponent(sprite)
    }

    open fun update(delta: Float) {}

    override fun dispose() {
        Game.canvas.removeComponent(sprite)

        if (body != null)
            Game.physics.removeBody(body)
    }
}