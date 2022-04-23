package engine.input

import com.cozmicgames.utils.extensions.average
import com.cozmicgames.utils.maths.Vector2

class InputDetector {
    val listener: GestureListener? = null
    var tapRectangleWidth = 20.0f
    var tapRectangleHeight = 20.0f
    var tapCountInterval = 0.4
    var longPressSeconds = 1.1
    var maxFlingDelay = Int.MAX_VALUE

    private var inTapRectangle = false
    private var tapCount = 0
    private var lastTapTime = 0.0
    private var lastTapX = 0.0f
    private var lastTapY = 0.0f
    private var lastTapButton = 0
    private var lastTapPointer = 0
    private var longPressFired = false
    private var pinching = false
    private var panning = false

    private val tracker = VelocityTracker()
    private var tapRectangleCenterX = 0.0f
    private var tapRectangleCenterY = 0.0f
    private var touchDownTime = 0.0
    private var pointerA = Vector2()
    private val pointerB = Vector2()
    private val initialPointerA = Vector2()
    private val initialPointerB = Vector2()

    /*
    private val longPressTask = Task { if (!longPressFired) longPressFired = listener!!.longPress(pointerA.x, pointerA.y) }

    fun touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean {
        return touchDown(x.toFloat(), y.toFloat(), pointer, button)
    }

    fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        if (pointer > 1) return false
        if (pointer == 0) {
            pointerA.set(x, y)
            touchDownTime = Time.current
            tracker.start(x, y, touchDownTime)
            if (Gdx.input.isTouched(1)) {
                // Start pinch.
                inTapRectangle = false
                pinching = true
                initialPointerA.set(pointerA)
                initialPointerB.set(pointerB)
                longPressTask.cancel()
            } else {
                // Normal touch down.
                inTapRectangle = true
                pinching = false
                longPressFired = false
                tapRectangleCenterX = x
                tapRectangleCenterY = y
                if (!longPressTask.isScheduled()) Timer.schedule(longPressTask, longPressSeconds)
            }
        } else {
            // Start pinch.
            pointerB.set(x, y)
            inTapRectangle = false
            pinching = true
            initialPointerA.set(pointerA)
            initialPointerB.set(pointerB)
            longPressTask.cancel()
        }
        return listener!!.touchDown(x, y, pointer, button)
    }

    fun touchDragged(x: Int, y: Int, pointer: Int): Boolean {
        return touchDragged(x.toFloat(), y.toFloat(), pointer)
    }

    fun touchDragged(x: Float, y: Float, pointer: Int): Boolean {
        if (pointer > 1) return false
        if (longPressFired) return false
        if (pointer == 0) pointerA.set(x, y) else pointerB.set(x, y)

        if (pinching) {
            if (listener != null) {
                val result = listener.pinch(initialPointerA, initialPointerB, pointerA, pointerB)
                return listener.zoom(initialPointerA.dst(initialPointerB), pointerA.dst(pointerB)) || result
            }
            return false
        }

        tracker.update(x, y, Time.current)

        if (inTapRectangle && !isWithinTapRectangle(x, y, tapRectangleCenterX, tapRectangleCenterY)) {
            longPressTask.cancel()
            inTapRectangle = false
        }

        if (!inTapRectangle) {
            panning = true
            return listener.pan(x, y, tracker.deltaX, tracker.deltaY)
        }
        return false
    }

    fun touchUp(x: Int, y: Int, pointer: Int, button: Int): Boolean {
        return touchUp(x.toFloat(), y.toFloat(), pointer, button)
    }

    fun touchUp(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        if (pointer > 1) return false

        if (inTapRectangle && !isWithinTapRectangle(x, y, tapRectangleCenterX, tapRectangleCenterY))
            inTapRectangle = false

        val wasPanning = panning
        panning = false
        longPressTask.cancel()

        if (longPressFired)
            return false

        if (inTapRectangle) {
            if (lastTapButton != button || lastTapPointer != pointer || Time.current - lastTapTime > tapCountInterval || !isWithinTapRectangle(x, y, lastTapX, lastTapY)) tapCount = 0
            tapCount++
            lastTapTime = Time.current
            lastTapX = x
            lastTapY = y
            lastTapButton = button
            lastTapPointer = pointer
            touchDownTime = 0.0
            return listener.tap(x, y, tapCount, button)
        }
        if (pinching) {
            pinching = false
            listener.pinchStop()
            panning = true

            if (pointer == 0)
                tracker.start(pointerB.x, pointerB.y, Gdx.input.getCurrentEventTime())
            else
                tracker.start(pointerA.x, pointerA.y, Gdx.input.getCurrentEventTime())

                return false
        }

        // handle no longer panning
        var handled = false
        if (wasPanning && !panning) handled = listener!!.panStop(x, y, pointer, button)

        // handle fling
        val time: Long = Gdx.input.getCurrentEventTime()
        if (time - touchDownTime <= maxFlingDelay) {
            tracker.update(x, y, time)
            handled = listener!!.fling(tracker.velocityX, tracker.velocityY, button) || handled
        }
        touchDownTime = 0
        return handled
    }

    fun cancel() {
        longPressTask.cancel()
        longPressFired = true
    }

    fun isLongPressed(): Boolean {
        return isLongPressed(longPressSeconds)
    }

    fun isLongPressed(duration: Float): Boolean {
        return if (touchDownTime == 0L) false else TimeUtils.nanoTime() - touchDownTime > (duration * 1000000000L).toLong()
    }

    fun isPanning(): Boolean {
        return panning
    }

    fun reset() {
        touchDownTime = 0
        panning = false
        inTapRectangle = false
        tracker.lastTime = 0
    }

    private fun isWithinTapRectangle(x: Float, y: Float, centerX: Float, centerY: Float): Boolean {
        return Math.abs(x - centerX) < tapRectangleWidth && Math.abs(y - centerY) < tapRectangleHeight
    }

    fun invalidateTapSquare() {
        inTapRectangle = false
    }
     */

    internal class VelocityTracker {
        var sampleSize = 10
        var lastX = 0.0f
        var lastY = 0.0f
        var deltaX = 0.0f
        var deltaY = 0.0f
        var lastTime = 0.0
        var numSamples = 0
        var meanX = Array(sampleSize) { 0.0f }
        var meanY = Array(sampleSize) { 0.0f }
        var meanTime = Array(sampleSize) { 0.0 }

        fun start(x: Float, y: Float, timeStamp: Double) {
            lastX = x
            lastY = y
            deltaX = 0f
            deltaY = 0f
            numSamples = 0

            repeat(sampleSize) {
                meanX[it] = 0f
                meanY[it] = 0f
                meanTime[it] = 0.0
            }

            lastTime = timeStamp
        }

        fun update(x: Float, y: Float, currTime: Double) {
            deltaX = x - lastX
            deltaY = y - lastY
            lastX = x
            lastY = y
            val deltaTime = currTime - lastTime
            lastTime = currTime
            val index = numSamples % sampleSize
            meanX[index] = deltaX
            meanY[index] = deltaY
            meanTime[index] = deltaTime
            numSamples++
        }

        val velocityX: Float
            get() {
                val meanX = meanX.average(numSamples)
                val meanTime = meanTime.average(numSamples)
                return if (meanTime == 0.0) 0.0f else (meanX / meanTime).toFloat()
            }

        val velocityY: Float
            get() {
                val meanY = meanY.average(numSamples)
                val meanTime = meanTime.average(numSamples)
                return if (meanTime == 0.0) 0.0f else (meanY / meanTime).toFloat()
            }
    }
}