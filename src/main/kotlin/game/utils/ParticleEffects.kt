package game.utils

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Vector2
import engine.graphics.particles.ParticleEffect
import engine.graphics.particles.generators.ColorGenerator
import engine.graphics.particles.generators.RandomVelocityGenerator
import engine.graphics.particles.generators.SizeGenerator
import engine.graphics.particles.generators.TimeGenerator
import engine.graphics.particles.spawners.DiskSpawner
import engine.graphics.particles.updaters.AttractorUpdater
import engine.graphics.particles.updaters.MovementUpdater
import engine.graphics.particles.updaters.SizeUpdater
import engine.graphics.particles.updaters.TimeUpdater

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
        e.addUpdater(AttractorUpdater(AttractorUpdater.Attractor(x, y + 25.0f, 1000.0f)))

        e.addSpawner(DiskSpawner(Vector2(x, y), size))

        return e
    }
}