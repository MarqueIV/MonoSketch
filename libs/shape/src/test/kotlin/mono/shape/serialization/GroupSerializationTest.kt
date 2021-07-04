package mono.shape.serialization

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mono.graphics.geo.Rect
import mono.shape.shape.Rectangle
import kotlin.test.Test

class GroupSerializationTest {
    @Test
    fun test() {
        val rectangle = SerializableRectangle(
            null,
            Rect.byLTRB(0, 0, 1, 1),
            Rectangle.Extra.DEFAULT
        )
        val group = SerializableGroup(
            null,
            listOf(rectangle)
        )
        val string = Json.encodeToString(group)
        println(string)
        val g = Json.decodeFromString<SerializableGroup>(string)
        println(g)
    }
}
