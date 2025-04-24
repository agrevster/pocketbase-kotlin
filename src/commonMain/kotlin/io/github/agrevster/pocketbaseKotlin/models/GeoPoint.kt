package io.github.agrevster.pocketbaseKotlin.models

import kotlinx.serialization.Serializable

@Serializable
/**
 * Used to represent a Pocketbase geo point record field.
 *
 * @param lon The longitude of the point.
 * @param lat The latitude of the point.
 */
public data class GeoPoint(val lon: Float? = null, val lat: Float? = null)