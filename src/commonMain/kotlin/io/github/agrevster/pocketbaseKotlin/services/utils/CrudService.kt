@file:OptIn(PocketKtInternal::class)


package io.github.agrevster.pocketbaseKotlin.services.utils

import io.github.agrevster.pocketbaseKotlin.FileUpload
import io.github.agrevster.pocketbaseKotlin.PocketKtInternal
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.dsl.query.SortFields
import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import io.github.agrevster.pocketbaseKotlin.models.utils.ListResult

@OptIn(PocketKtInternal::class)
public abstract class CrudService<T : BaseModel>(client: io.github.agrevster.pocketbaseKotlin.PocketbaseClient) :
    BaseCrudService<T>(client) {

    /**
     * The url path to the service's API
     */
    public abstract val baseCrudPath: String

    /**
     * Fetches all records in the collection at once
     * @param [batch] The amount of records you wish to fetch.
     */
    public suspend inline fun <reified T : BaseModel> getFullList(
        batch: Int,
        sortBy: SortFields = SortFields(),
        filterBy: Filter = Filter(),
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields(),
        skipTotal: Boolean = true
    ): List<T> {
        return _getFullList(baseCrudPath, batch, sortBy, filterBy, expandRelations, showFields, skipTotal)
    }

    /**
     * Fetches a paged list of records
     * @param [page] The page number you wish to fetch.
     * @param [perPage] The amount of records you wish to have per-single-page
     */
    public suspend inline fun <reified T : BaseModel> getList(
        page: Int,
        perPage: Int,
        sortBy: SortFields = SortFields(),
        filterBy: Filter = Filter(),
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields(), skipTotal: Boolean = false
    ): ListResult<T> {
        return _getList(baseCrudPath, page, perPage, sortBy, filterBy, expandRelations, showFields, skipTotal)
    }

    /**
     * Fetches a single record
     * @param [id] ID of the record you wish to view.
     */
    public suspend inline fun <reified T : BaseModel> getOne(
        id: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields()
    ): T {
        return _getOne(baseCrudPath, id, expandRelations, showFields)
    }

    /**
     * Creates a new record and gets the record
     * @param [body] JSON data used to create the record in the form of a string
     */
    public suspend inline fun <reified T : BaseModel> create(
        body: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields()
    ): T {
        return _create(baseCrudPath, body, expandRelations, showFields)
    }

    /**
     * Updates an existing records and gets it
     * @param [id] the id of the record to update
     * @param [body] JSON data used to update the record in the form of a string
     */
    public suspend inline fun <reified T : BaseModel> update(
        id: String, body: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields()
    ): T {
        return _update(baseCrudPath, id, body, expandRelations, showFields)
    }

    /**
     * Creates a new record and gets the record
     * @param [body] the key value data used to create the record
     * @param [files] the files you wish to upload
     */
    public suspend inline fun <reified T : BaseModel> create(
        body: Map<String, Any>,
        files: List<FileUpload>,
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields()
    ): T {
        return _create(baseCrudPath, body, files, expandRelations, showFields)
    }

    /**
     * Updates an existing records and gets it
     * @param [id] the id of the record to update
     * @param [body] the key value data used to create the record
     * @param [files] the files you wish to upload
     */
    public suspend inline fun <reified T : BaseModel> update(
        id: String,
        body: Map<String, Any>,
        files: List<FileUpload>,
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields()
    ): T {
        return _update(baseCrudPath, id, body, files, expandRelations, showFields)
    }

    /***
     * Deletes the specified record
     * @param [id] the id of the record you wish to delete
     */
    public suspend inline fun delete(id: String): Boolean {
        return _delete(baseCrudPath, id)
    }
}