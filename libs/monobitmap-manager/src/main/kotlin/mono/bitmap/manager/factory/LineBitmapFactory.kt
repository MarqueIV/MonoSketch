package mono.bitmap.manager.factory

import mono.graphics.bitmap.MonoBitmap
import mono.graphics.geo.Point
import mono.shape.extra.manager.model.AnchorChar
import mono.shape.extra.manager.model.StraightStrokeStyle
import mono.shape.shape.extra.LineExtra
import kotlin.math.abs

/**
 * A drawable to draw Line shape to bitmap.
 */
object LineBitmapFactory {

    fun toBitmap(
        jointPoints: List<Point>,
        lineExtra: LineExtra
    ): MonoBitmap {
        val bitmapBuilder = BitmapBuilderDecoration.getInstance(jointPoints)

        val dashPattern = lineExtra.dashPattern
        createCharPoints(jointPoints, lineExtra.strokeStyle)
            .forEachIndexed { index, pointChar ->
                val char = if (dashPattern.isGap(index)) ' ' else pointChar.char
                bitmapBuilder.put(pointChar.top, pointChar.left, char)
            }

        val startAnchor = lineExtra.startAnchor
        if (startAnchor != null) {
            bitmapBuilder.putAnchorPoint(
                jointPoints[0],
                jointPoints[1],
                startAnchor
            )
        }
        val endAnchor = lineExtra.endAnchor
        if (endAnchor != null) {
            bitmapBuilder.putAnchorPoint(
                jointPoints[jointPoints.lastIndex],
                jointPoints[jointPoints.lastIndex - 1],
                endAnchor
            )
        }

        return bitmapBuilder.toBitmap()
    }

    private fun createCharPoints(
        jointPoints: List<Point>,
        strokeStyle: StraightStrokeStyle
    ): Sequence<PointChar> {
        val lines =
            jointPoints.zipWithNext().takeIf { it.isNotEmpty() } ?: return emptySequence()

        val firstPoint = lines.first().let { (p0, p1) ->
            val char = if (isHorizontal(p0, p1)) strokeStyle.horizontal else strokeStyle.vertical
            PointChar.point(p0.left, p0.top, char)
        }

        val charPoints = lines.asSequence()
            .zipWithNext { (p0, p1), (_, p2) -> Triple(p0, p1, p2) }
            .flatMap { (p0, p1, p2) ->
                val line = createLineChar(p0, p1, strokeStyle)

                val connectChar = strokeStyle.getRightAngleChar(p0, p1, p2)
                val connectPoint = PointChar.point(p1.left, p1.top, connectChar)
                line + connectPoint
            }

        val lastLine = lines.last()
        val lastLinePoint = createLineChar(lastLine.first, lastLine.second, strokeStyle)
        val lastPoint = lastLine.let { (p0, p1) ->
            val char = if (isHorizontal(p0, p1)) strokeStyle.horizontal else strokeStyle.vertical
            PointChar.point(p1.left, p1.top, char)
        }
        return firstPoint + charPoints + lastLinePoint + lastPoint
    }

    private fun createLineChar(
        p0: Point,
        p1: Point,
        strokeStyle: StraightStrokeStyle
    ): Sequence<PointChar> {
        val isHorizontal = isHorizontal(p0, p1)
        val isValid = if (isHorizontal) abs(p0.left - p1.left) > 1 else abs(p0.top - p1.top) > 1
        if (!isValid) {
            return emptySequence()
        }

        return if (isHorizontal) {
            val (begin, end) = adjustBeginEnd(p0.left, p1.left)
            PointChar.horizontalLine(begin, end, p0.top, strokeStyle.horizontal)
        } else {
            val (begin, end) = adjustBeginEnd(p0.top, p1.top)
            PointChar.verticalLine(p0.left, begin, end, strokeStyle.vertical)
        }
    }

    private fun adjustBeginEnd(begin: Int, end: Int): Pair<Int, Int> =
        if (begin < end) (begin + 1 to end - 1) else (begin - 1 to end + 1)

    private fun StraightStrokeStyle.getRightAngleChar(
        point0: Point,
        point1: Point,
        point2: Point
    ): Char {
        val isHorizontal0 = isHorizontal(point0, point1)
        val isHorizontal1 = isHorizontal(point1, point2)
        if (isHorizontal0 == isHorizontal1) {
            // Same line
            return if (isHorizontal0) horizontal else vertical
        }

        val isLeft = point0.left < point1.left || point2.left < point1.left
        val isUpper = point0.top < point1.top || point2.top < point1.top

        return if (isLeft) {
            if (isUpper) upLeft else downLeft
        } else {
            if (isUpper) downRight else upRight
        }
    }

    private fun BitmapBuilderDecoration.putAnchorPoint(
        anchor: Point,
        previousPoint: Point,
        anchorChar: AnchorChar
    ) {
        val char = if (isHorizontal(anchor, previousPoint)) {
            if (anchor.left < previousPoint.left) anchorChar.left else anchorChar.right
        } else {
            if (anchor.top < previousPoint.top) anchorChar.top else anchorChar.bottom
        }
        put(anchor.row, anchor.column, char)
    }

    private fun isHorizontal(point1: Point, point2: Point): Boolean = point1.top == point2.top

    private class BitmapBuilderDecoration(
        private val row0: Int,
        private val column0: Int,
        width: Int,
        height: Int
    ) {
        private val builder = MonoBitmap.Builder(width, height)

        fun put(row: Int, column: Int, char: Char) =
            builder.put(row - row0, column - column0, char)

        fun toBitmap(): MonoBitmap = builder.toBitmap()

        companion object {
            fun getInstance(jointPoints: List<Point>): BitmapBuilderDecoration {
                val boundLeft = jointPoints.minOf { it.left }
                val boundRight = jointPoints.maxOf { it.left }
                val boundTop = jointPoints.minOf { it.top }
                val boundBottom = jointPoints.maxOf { it.top }

                val boundWidth = boundRight - boundLeft + 1
                val boundHeight = boundBottom - boundTop + 1

                return BitmapBuilderDecoration(boundTop, boundLeft, boundWidth, boundHeight)
            }
        }
    }
}
