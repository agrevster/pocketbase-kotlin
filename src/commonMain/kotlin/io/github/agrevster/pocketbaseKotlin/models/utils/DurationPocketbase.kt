package io.github.agrevster.pocketbaseKotlin.models.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration

public typealias DurationPocketbase = @Serializable(DurationPocketbaseSerializer::class) Duration

public object DurationPocketbaseSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("duration", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Duration {
        val durationValue = decoder.decodeLong()
        return Duration.parse("${durationValue}s")
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeLong(value.inWholeSeconds)
    }

}