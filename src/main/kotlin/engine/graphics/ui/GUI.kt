package engine.graphics.ui

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.safeHeight
import com.cozmicgames.graphics.safeWidth
import com.cozmicgames.input
import com.cozmicgames.input.CharListener
import com.cozmicgames.input.KeyListener
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.maths.Matrix4x4
import com.cozmicgames.utils.maths.Rectangle
import com.cozmicgames.utils.maths.Vector2
import engine.graphics.font.BitmapFont
import engine.graphics.render
import kotlin.math.max

class GUI(val context: GUIContext = GUIContext(), val style: GUIStyle = GUIStyle()) : Disposable {
    /**
     * The state an element can have.
     * HOVERED: The mouse is hovering over the element.
     * ACTIVE: The element is being interacted with.
     * ENTERED: The element has been entered.
     * LEFT: The element has been left.
     *
     * States can be combined to a bitfield using the combine function.
     * The resulting bitfield can be used to check if a state is active by the isSet function.
     */
    enum class State {
        HOVERED,
        ACTIVE,
        ENTERED,
        LEFT;

        companion object {
            /**
             * Combines the given states into a bitfield.
             * @param states The states to combine.
             * @param base The base state.
             *
             * @return The resulting bitfield.
             */
            fun combine(vararg states: State, base: Int = 0): Int {
                var flags = base
                states.forEach {
                    flags = flags or (1 shl it.ordinal)
                }
                return flags
            }

            /**
             * Checks if the given state is set in the bitfield.
             * @param flags The bitfield to check.
             * @param state The state to check for.
             *
             * @return True if the state is set, false otherwise.
             */
            fun isSet(flags: Int, state: State) = (flags and (1 shl state.ordinal)) != 0
        }
    }

    /**
     * Used for state bitfield generation.
     * @param state The state to add to the bitfield.
     *
     * @return The resulting bitfield.
     */
    operator fun Int.plus(state: State) = State.combine(state, base = this)

    /**
     * Used for checking if a state bitfield contains the specified state.
     * @param state The state to check for.
     *
     * @return True if the state is set, false otherwise.
     */
    operator fun Int.contains(state: State) = State.isSet(this, state)

    /**
     * Describes which behaviour an element has.
     * NONE: The element has no behaviour.
     * DEFAULT: The element is active once on interaction.
     * REPEATED: The element is active as long as it is interacted with.
     */
    enum class ButtonBehaviour {
        NONE,
        DEFAULT,
        REPEATED
    }

    /**
     * Describes the supported plot types.
     * POINTS: The plot is a series of points.
     * BARS: The plot is a series of bars.
     * LINES: The plot is a series of lines.
     */
    enum class PlotType {
        POINTS,
        BARS,
        LINES
    }

    private val commandList = GUICommandList()
    private val transform = Matrix4x4()
    private var isSameLine = false
    private var lineHeight = 0.0f

    private val keyListener: KeyListener = { key, down ->
        if (down)
            currentTextData?.onKeyAction(key)
    }

    private val charListener: CharListener = {
        currentTextData?.onCharAction(it)
    }

    /**
     * The last element that was added to the GUI.
     */
    var lastElement: GUIElement? = null
        private set

    /**
     * The current command list.
     */
    var currentCommandList = commandList
        private set

    /**
     * The current group, if one is present.
     * @see GUIGroup
     */
    var currentGroup: GUIGroup? = null
        private set

    /**
     * The current scissor rectangle, if one is present.
     */
    var currentScissorRectangle: Rectangle? = null
        private set

    /**
     * The current text data, if one is present.
     */
    var currentTextData: TextData? = null

    /**
     * The current touch position.
     */
    val touchPosition = Vector2()
        get() = field.set(Kore.input.x.toFloat(), (Kore.graphics.height - Kore.input.y).toFloat())

    /**
     * The previous frames' touch position.
     */
    val lastTouchPosition = Vector2()
        get() = field.set(Kore.input.lastX.toFloat(), (Kore.graphics.height - Kore.input.lastY).toFloat())

    /**
     * The font used for text rendering.
     */
    val drawableFont = BitmapFont(style.font, scale = style.fontSize / style.font.size)

    /**
     * The time counter, in seconds.
     */
    var time = 0.0
        private set

    init {
        Kore.input.addKeyListener(keyListener)
        Kore.input.addCharListener(charListener)
    }

    /**
     * Records the commands added during execution of [block] and returns them as a command list.
     *
     * @param block The block to execute.
     *
     * @return The command list.
     */
    fun recordCommands(block: () -> Unit): GUICommandList {
        val list = GUICommandList()
        val previousList = currentCommandList
        currentCommandList = list
        block()
        currentCommandList = previousList
        return list
    }

    /**
     * Sets the last element.
     * This is used to determine the position of the next element.
     * This is also used to determine the size of the panel.
     *
     * All GUI functions must end with this and should return the element.
     *
     * @param x The x position of the element.
     * @param y The y position of the element.
     * @param width The width of the element.
     * @param height The height of the element.
     * @param applyOffsets Whether to apply offsets from the GUI's style to the element.
     *
     * @return The element.
     */
    fun setLastElement(x: Float, y: Float, width: Float, height: Float, applyOffsets: Boolean = true): GUIElement {
        return setLastElement(if (isSameLine)
            object : GUIElement(x, y, width, height) {
                override val nextX get() = x + width + if (applyOffsets) style.offsetToNextX else 0.0f
                override val nextY get() = y
            }
        else
            object : GUIElement(x, y, width, height) {
                override val nextX get() = x
                override val nextY get() = y + height + if (applyOffsets) style.offsetToNextY else 0.0f
            }, applyOffsets
        )
    }

    /**
     * Sets the last element.
     * This is used to determine the position of the next element.
     * This is also used to determine the size of the panel.
     *
     * All GUI functions must end with this and should return the element.
     *
     * @param element The element.
     * @param applyOffsets Whether to apply offsets from the GUI's style to the element.
     *
     * @return The element.
     */
    fun setLastElement(element: GUIElement, applyOffsets: Boolean = true): GUIElement {
        lineHeight = if (isSameLine)
            max(lineHeight, if (applyOffsets) element.height + style.offsetToNextY else element.height)
        else
            if (applyOffsets) element.height + style.offsetToNextY else element.height

        lastElement = element

        currentGroup?.let {
            it.width = max(it.width, element.x + element.width - it.x + if (applyOffsets) style.elementPadding else 0.0f)

            if (!isSameLine)
                it.height += lineHeight
        }

        return element
    }

    /**
     * Gets the last element.
     * This is used to determine the position of the current element.
     *
     * @param defaultX The default x position of the element if no last element is set. Defaults to 0.0f.
     * @param defaultY The default y position of the element if no last element is set. Defaults to 0.0f.
     *
     * @return The last element.
     */
    fun getLastElement(defaultX: Float = 0.0f, defaultY: Float = 0.0f): GUIElement {
        return lastElement ?: absolute(defaultX, defaultY)
    }

    /**
     * Sets the last element to an absolute position.
     *
     * @param point The position.
     *
     * @return The created element.
     */
    fun absolute(point: Vector2) = absolute(point.x, point.y)

    /**
     * Sets the last element to an absolute position.
     *
     * @param x The x position.
     * @param y The y position.
     *
     * @return The created element.
     */
    fun absolute(x: Float, y: Float) = object : GUIElement(x, y, 0.0f, 0.0f) {
        override val nextX get() = x
        override val nextY get() = y
    }

    /**
     * Sets the last element to a relative position.
     * It's calculated by x = factorX * srcX and y = factorY * srcY.
     *
     * @param factorX The x factor, should be between 0 and 1.
     * @param factorY The y factor, should be between 0 and 1.
     * @param srcX The source size in the x dimension. Defaults to the surface width.
     * @param srcY The source size in the y dimension. Defaults to the surface height.
     *
     * @return The created element.
     */
    fun relative(factorX: Float, factorY: Float, srcX: Float = Kore.graphics.width.toFloat(), srcY: Float = Kore.graphics.height.toFloat()) = object : GUIElement(factorX * srcX, factorY * srcY, 0.0f, 0.0f) {
        override val nextX get() = x
        override val nextY get() = y
    }

    /**
     * Sets the last element to an offset from [src].
     * It's calculated by x = srcX + offsetX and y = srcY + offsetY.
     *
     * @param offset The offset.
     * @param src The source element. Defaults to the last element.
     * @param resetX Whether to reset the x position afterwards. Defaults to false.
     * @param resetY Whether to reset the y position afterwards. Defaults to false.
     * @param block The block to execute.
     */
    fun offset(offset: GUIElement, src: GUIElement = getLastElement(), resetX: Boolean = false, resetY: Boolean = false, block: () -> Unit) = offset(offset.nextX, offset.nextY, src, resetX, resetY, block)

    /**
     * Sets the last element to an offset from [src].
     * It's calculated by x = srcX + offsetX and y = srcY + offsetY.
     *
     * @param offsetX The x offset.
     * @param offsetY The y offset.
     * @param src The source element. Defaults to the last element.
     * @param resetX Whether to reset the x position afterwards. Defaults to false.
     * @param resetY Whether to reset the y position afterwards. Defaults to false.
     * @param block The block to execute.
     */
    fun offset(offsetX: Float, offsetY: Float, src: GUIElement = getLastElement(), resetX: Boolean = false, resetY: Boolean = false, block: () -> Unit) = offset(offsetX, offsetY, src.nextX, src.nextY, resetX, resetY, block)

    /**
     * Sets the last element to an offset from [src].
     * It's calculated by x = srcX + offsetX and y = srcY + offsetY.
     *
     * @param offsetX The x offset.
     * @param offsetY The y offset.
     * @param srcX The source x position.
     * @param srcY The source y position.
     * @param resetX Whether to reset the x position afterwards. Defaults to false.
     * @param resetY Whether to reset the y position afterwards. Defaults to false.
     * @param block The block to execute.
     */
    fun offset(offsetX: Float, offsetY: Float, srcX: Float, srcY: Float, resetX: Boolean = false, resetY: Boolean = false, block: () -> Unit): GUIElement {
        val lastElement = getLastElement()
        setLastElement(srcX + offsetX, srcY + offsetY, 0.0f, 0.0f)
        block()
        return setLastElement(if (resetX) lastElement.nextX else getLastElement().nextX, if (resetY) lastElement.nextY else getLastElement().nextY, 0.0f, 0.0f)
    }

    /**
     * Performs a block of code and resetting the last element afterwards.
     *
     * @param block The block to execute.
     */
    fun transient(block: () -> Unit): GUIElement {
        val lastElement = getLastElement()
        block()
        return setLastElement(lastElement)
    }

    /**
     * Adds a blank line element.
     *
     * @param size The size of the blank line.
     */
    fun blankLine(size: Float = style.elementSize, element: GUIElement = getLastElement()): GUIElement {
        val (x, y) = element
        return setLastElement(x, y, 0.0f, size)
    }

    /**
     * Adds a spacing element.
     * The spacing is a horizontal gap of the given [width].
     *
     * @param width The width of the spacing. Defaults to [style.elementSize].
     */
    fun spacing(width: Float = style.elementSize, element: GUIElement = getLastElement()): GUIElement {
        val (x, y) = element
        return setLastElement(x, y, width, 0.0f)
    }

    /**
     * Gets the state of the area covered by [rectangle] with the given [behaviour].
     *
     * @param rectangle The rectangle.
     * @param behaviour The behaviour. Defaults to [ButtonBehaviour.NONE].
     *
     * @return The state of the area as a bitfield.
     */
    fun getState(rectangle: Rectangle, behaviour: ButtonBehaviour = ButtonBehaviour.NONE): Int {
        var state = 0

        currentScissorRectangle?.let {
            if (touchPosition !in it || !(it intersects rectangle))
                return state
        }

        if (touchPosition in rectangle) {
            state += State.HOVERED

            when (behaviour) {
                ButtonBehaviour.DEFAULT -> if (Kore.input.justTouched) state += State.ACTIVE
                ButtonBehaviour.REPEATED -> if (Kore.input.isTouched) state += State.ACTIVE
                else -> {}
            }

            if (lastTouchPosition !in rectangle)
                state += State.ENTERED
        } else if (lastTouchPosition in rectangle)
            state += State.LEFT

        return state
    }

    /**
     * Marks the elements added during the execution of [block] to be laid out horizontally.
     * The elements will be laid out from left to right.
     *
     * @param block The block to execute.
     */
    fun sameLine(block: () -> Unit): GUIElement {
        if (isSameLine) {
            block()
            return getLastElement()
        }

        val lastElement = getLastElement()
        isSameLine = true
        lineHeight = 0.0f

        block()

        isSameLine = false

        return setLastElement(lastElement.nextX, lastElement.nextY, 0.0f, lineHeight)
    }

    /**
     * Groups the elements added during the execution of [block] and sets the last element as a union of those.
     * This is useful for grouping elements that are not laid out horizontally.
     *
     * @param block The block to execute.
     */
    fun group(backgroundColor: Color? = null, element: GUIElement = getLastElement(), block: () -> Unit): GUIElement {
        val previousGroup = currentGroup
        val previousSameLine = isSameLine
        val previousLineHeight = lineHeight

        val group = GUIGroup(element.nextX, element.nextY, 0.0f, 0.0f)
        currentGroup = group
        isSameLine = false
        lineHeight = 0.0f

        val commands = recordCommands(block)

        backgroundColor?.let {
            currentCommandList.drawRectFilled(group.x, group.y, group.width, group.height, style.roundedCorners, style.cornerRounding, it)
        }
        currentCommandList.addCommandList(commands)

        currentGroup = previousGroup
        isSameLine = previousSameLine
        lineHeight = previousLineHeight

        return setLastElement(group.x, group.y, group.width, group.height)
    }

    fun begin(delta: Float) {
        time += delta
        lastElement = null
    }

    fun end() {
        transform.setToOrtho2D(Kore.graphics.safeInsetLeft.toFloat(), Kore.graphics.safeWidth.toFloat(), Kore.graphics.safeHeight.toFloat(), Kore.graphics.safeInsetTop.toFloat())
        context.renderer.render(transform) {
            it.withFlippedY(true) {
                commandList.process(context)
            }
        }
    }

    override fun dispose() {
        Kore.input.removeKeyListener(keyListener)
        Kore.input.removeCharListener(charListener)
        drawableFont.dispose()
        context.dispose()
    }
}