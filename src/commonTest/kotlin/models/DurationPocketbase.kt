package models

import TestingUtils
import io.github.agrevster.pocketbaseKotlin.models.utils.DurationPocketbase
import io.github.agrevster.pocketbaseKotlin.models.utils.InstantPocketbase
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration

class DurationPocketbaseTest : TestingUtils() {
    @Test
    fun testSerializeDeserialize(): Unit {
        @Serializable
        data class TestSerializable(val duration: DurationPocketbase)

        val original = TestSerializable(Duration.parse("5m"))

        val encoded = Json.encodeToString(original)
        assertEquals(
            expected = "{\"duration\":\"${5 * 60}\"}",
            actual = encoded,
        )
        val decoded: TestSerializable = Json.decodeFromString(encoded)
        assertEquals(
            expected = original,
            actual = decoded,
        )
    }
}