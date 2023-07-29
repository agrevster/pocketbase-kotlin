package io.github.agrevster.pocketbaseKotlin.models.utils

import kotlinx.serialization.Serializable

@Serializable
/**
 * The base class used for list queries
 * @property [page] the page number
 * @property [perPage] the amount of [items] are on the page
 * @property [totalItems] the total amount of items in the collection
 * @property [totalPages] the total amount of pages with [perPage] [items] on each page
 */
public data class ListResult<T>(
    val page: Int = 1,
    val perPage: Int = 0,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val items: List<T>
) {
    override fun toString(): String {
        return "ListResult(page=$page, perPage=$perPage, totalItems=$totalItems, totalPages=$totalPages, items=$items)"
    }
}