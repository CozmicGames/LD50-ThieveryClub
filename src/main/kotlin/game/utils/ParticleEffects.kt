package game.utils

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Vector2
import engine.graphics.particles.ParticleEffect
import engine.graphics.particles.generators.*
import engine.graphics.particles.spawners.DiskSpawner
import engine.graphics.particles.spawners.PointSpawner
import engine.graphics.particles.updaters.*
import kotlin.math.atan2
import kotlin.math.sqrt

object ParticleEffects {
    fun createFireEffect(x: Float, y: Float, size: Float, maxParticles: Int = 100): ParticleEffect {
        val e = ParticleEffect(maxParticles, maxParticles / 2.0f)
        e.addGenerator(TimeGenerator(0.3f, 1.5f))
        e.addGenerator(ColorGenerator(Color(0xF7DD4533.toInt()), Color(0xED9D4288.toInt()), Color(0xE05A0711.toInt()), Color(0xED1A0577.toInt())))
        e.addGenerator(RandomVelocityGenerator(0.1f, 0.5f))
        e.addGenerator(SizeGenerator(2.0f, 6.0f, 1.0f, 2.0f))

        e.addUpdater(TimeUpdater())
        e.addUpdater(MovementUpdater())
        e.addUpdater(SizeUpdater())
        e.addUpdater(ColorUpdater())
        e.addUpdater(AttractorUpdater(AttractorUpdater.Attractor(x, y + 25.0f, 1000.0f)))

        e.addSpawner(DiskSpawner(Vector2(x, y), size))

        return e
    }

    fun createBulletHitEffect(x: Float, y: Float, shooterX: Float, shooterY: Float, color: Color) {
        val dx = x - shooterX
        val dy = y - shooterY
        val distance = sqrt(dx * dx + dy * dy)
        val angle = atan2(dy, dx)

        val lighterColor = color.lighter(0.25f)
        val darkerColor = color.lighter(0.25f)

        val e = ParticleEffect(20, 20.0f)
        e.addGenerator(TimeGenerator(0.1f, 0.2f))
        e.addGenerator(ColorGenerator(lighterColor, darkerColor, lighterColor, darkerColor))
        e.addGenerator(AngledVelocityGenerator(angle - 0.2f, angle + 0.2f, 1.0f, 5.0f))
        e.addGenerator(SizeGenerator(1.0f, 3.0f, 1.0f, 3.0f))

        e.addUpdater(TimeUpdater())
        e.addUpdater(MovementUpdater())

        e.addSpawner(PointSpawner(Vector2(x, y)))

    }
}