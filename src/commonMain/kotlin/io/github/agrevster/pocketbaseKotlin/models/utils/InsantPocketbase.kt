package io.github.agrevster.pocketbaseKotlin.models.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

public typealias InstantPocketbase = @Serializable(InstantPocketbaseSerializer::class) Instant

internal fun Int.padded(length: Int): String {
    return this.toString().padStart(length, '0')
}

public fun Instant.toStringPocketbase(): String {
    val dt = this.toLocalDateTime(TimeZone.UTC)

    return "${dt.year}-${dt.month.number.padded(2)}-${dt.day.padded(2)} ${dt.hour.padded(2)}:${dt.minute.padded(2)}:${
        dt.second.padded(
            2
        )
    }.${dt.nanosecond / 1_000_000}Z"
}

public fun Instant.Companion.parsePocketbase(string: String): InstantPocketbase {
    return parse(string.replace(' ', 'T'))
}

public object InstantPocketbaseSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val dateStr = decoder.decodeString()
        return Instant.parsePocketbase(dateStr)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toStringPocketbase())
    }

}