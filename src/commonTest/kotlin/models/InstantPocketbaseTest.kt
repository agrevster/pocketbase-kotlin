package tests.models

import io.github.agrevster.pocketbaseKotlin.models.utils.InstantPocketbase
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class InstantPocketbaseTest {
    @Test
    fun testSerializeDeserialize(): Unit {
        @Serializable
        data class TestSerializable(val instant: InstantPocketbase)

        val original = TestSerializable(Instant.parse("2023-10-24T01:02:03.123Z"))

        val encoded = Json.encodeToString(original)
        assertEquals(expected = "{\"instant\":\"2023-10-24 01:02:03.123Z\"}", actual = encoded)
        val decoded: TestSerializable = Json.decodeFromString(encoded)
        assertEquals(expected = original, actual = decoded)
    }
}