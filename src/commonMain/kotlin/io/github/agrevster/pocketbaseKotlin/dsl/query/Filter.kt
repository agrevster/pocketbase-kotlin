package io.github.agrevster.pocketbaseKotlin.dsl.query

import io.ktor.http.*

/**
 * Used to filter the list of [Record]s returned by a request.
 * See the [Pocketbase docs](https://pocketbase.io/docs/api-rules-and-filters/) for a guide on how to format a filter request.
 * @param [expression] The filter request used to filter records.
 */
public data class Filter(val expression: String? = null) {
    public fun addTo(params: ParametersBuilder) {
        if (this.expression != null) params.append("filter", this.expression)
    }
}