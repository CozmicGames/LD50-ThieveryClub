package game.player

import com.cozmicgames.utils.Disposable
import engine.Game
import engine.graphics.sprite.StaticSprite
import engine.physics.AxisAlignedRectangleShape
import engine.physics.Body
import engine.physics.PlatformerController
import engine.utils.Transform
import game.levels.Level

class Player(x: Float, y: Float) : Disposable {
    private val texture by Game.textures("images/player.png")
    private val transform = Transform()
    private val sprite = StaticSprite(texture, transform)
    private val body = Body(transform)
    private val controller = PlatformerController(body)

    var isFacingRight = true
        private set

    init {
        transform.x = x
        transform.y = y
        transform.scaleX = Level.TILE_SIZE
        transform.scaleY = Level.TILE_SIZE

        body.setShape(AxisAlignedRectangleShape(), 100.0f)
        body.calculateMassAndInertia()
        body.restitution = 0.0f
        Game.physics.addBody(body)
        Game.canvas.addComponent(sprite)
    }

    fun update(delta: Float) {
        var movement = 0.0f
        movement += Game.controls.getValue("move_right")
        movement -= Game.controls.getValue("move_left")
        val jump = Game.controls.getTrigger("move_jump")
        val crouch = Game.controls.getState("move_crouch")

        if (movement != 0.0f)
            isFacingRight = movement > 0.0f

        sprite.isFlippedX = !isFacingRight

        controller.move(movement, crouch, jump, delta)
        controller.update()
    }

    override fun dispose() {
        Game.canvas.removeComponent(sprite)
        Game.physics.removeBody(body)
    }
}