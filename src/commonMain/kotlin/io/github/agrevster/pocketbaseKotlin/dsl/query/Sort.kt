package io.github.agrevster.pocketbaseKotlin.dsl.query

import io.github.agrevster.pocketbaseKotlin.toFieldList
import io.ktor.http.*

/**
 * An idiomatic way to handle Pocketbase sorting.
 * Add + before your [SortFields] object to sort with the records you included on top, or add - if you want those items to come last.
 *
 * @param [fields] the list of fields you want to sort by.
 */
public data class SortFields(val fields: String) {
    public constructor(vararg fields: String) : this(fields.toFieldList())

    /**
     * Adds the current [SortFields] to a request's parameters
     */
    public fun addTo(params: ParametersBuilder) {
        params.append("sort", this.fields)
    }

    /**
     * Add this in front of a [SortFields] object to sort the record fields specified in the field list first.
     */
    public operator fun unaryPlus(): SortFields = SortFields("+${this.removeModifiers().fields}")

    /**
     * Add this in front of a [SortFields] object to sort the record fields specified in the field list last.
     */
    public operator fun unaryMinus(): SortFields = SortFields("-${this.removeModifiers().fields}")
}

private fun SortFields.removeModifiers() = SortFields(this.fields.removePrefix("+").removePrefix("-"))