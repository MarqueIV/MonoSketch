package mono.graphics.geo

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Rect(val position: Point, val size: Size) {
    val width: Int = size.width
    val height: Int = size.height
    val left: Int = position.left
    val right: Int = position.left + width - 1
    val top: Int = position.top
    val bottom: Int = position.top + height - 1

    private val validHorizontalRange = left..right
    private val validVerticalRange = top..bottom

    operator fun contains(point: Point): Boolean =
        point.left in validHorizontalRange && point.top in validVerticalRange

    fun getOverlappedRect(rect: Rect): Rect? {
        if (!isOverlapped(rect)) {
            return null
        }
        val offset = rect.position - position
        val top = max(offset.top, 0)
        val bottom = min(offset.top + rect.height, height) - 1
        val left = max(offset.left, 0)
        val right = min(offset.left + rect.width, width) - 1
        return byLTRB(
            left + position.left,
            top + position.top,
            right + position.left,
            bottom + position.top
        )
    }

    fun isOverlapped(rect: Rect): Boolean {
        val isHorizontalOverlap = left in rect.left..rect.right || rect.left in left..right
        val isVerticalOverlap = top in rect.top..rect.bottom || rect.top in top..bottom
        return isHorizontalOverlap && isVerticalOverlap
    }

    override fun toString(): String = "[$left, $top] - [$width x $height]"

    companion object {
        val ZERO = byLTWH(0, 0, 0, 0)

        fun byLTRB(left: Int, top: Int, right: Int, bottom: Int): Rect = Rect(
            Point(min(left, right), min(top, bottom)),
            Size(abs(right - left) + 1, abs(bottom - top) + 1)
        )

        fun byLTWH(left: Int, top: Int, width: Int, height: Int): Rect = Rect(
            Point(left, top),
            Size(width, height)
        )

        fun byHorizontalLine(left: Int, right: Int, top: Int) = byLTRB(left, top, right, top)

        fun byVerticalLine(left: Int, top: Int, bottom: Int) = byLTRB(left, top, left, bottom)
    }
}
