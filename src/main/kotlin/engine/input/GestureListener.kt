package engine.input

import com.cozmicgames.utils.maths.Vector2


interface GestureListener {
	fun touchDown(x: Float, y: Float, pointer: Int, button: Int) {}

	fun tap(x: Float, y: Float, count: Int, button: Int) {}

	fun longPress(x: Float, y: Float) {}

	fun fling(velocityX: Float, velocityY: Float, button: Int) {}

	fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float) {}

	fun panStop(x: Float, y: Float, pointer: Int, button: Int) {}

	fun zoom(initialDistance: Float, distance: Float) {}

	fun pinch(initialPointerA: Vector2, initialPointerB: Vector2, pointerA: Vector2, pointerB: Vector2) {}

	fun pinchStop()
}


