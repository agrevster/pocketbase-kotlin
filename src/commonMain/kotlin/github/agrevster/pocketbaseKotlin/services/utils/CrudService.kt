@file:OptIn(PocketKtInternal::class)

package github.agrevster.pocketbaseKotlin.services.utils

import github.agrevster.pocketbaseKotlin.FileUpload
import github.agrevster.pocketbaseKotlin.PocketKtInternal
import github.agrevster.pocketbaseKotlin.PocketbaseClient
import github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import github.agrevster.pocketbaseKotlin.dsl.query.Filter
import github.agrevster.pocketbaseKotlin.dsl.query.SortFields
import github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import github.agrevster.pocketbaseKotlin.models.utils.ListResult

public abstract class CrudService<T : BaseModel>(client: PocketbaseClient) : BaseCrudService<T>(client) {

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
        expandRelations: ExpandRelations = ExpandRelations()
    ): List<T> {
        return _getFullList(baseCrudPath, batch, sortBy, filterBy, expandRelations)
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
        expandRelations: ExpandRelations = ExpandRelations()
    ): ListResult<T> {
        return _getList(baseCrudPath, page, perPage, sortBy, filterBy, expandRelations)
    }

    /**
     * Fetches a single record
     * @param [id] ID of the record you wish to view.
     */
    public suspend inline fun <reified T : BaseModel> getOne(
        id: String, expandRelations: ExpandRelations = ExpandRelations()
    ): T {
        return _getOne(baseCrudPath, id, expandRelations)
    }

    /**
     * Creates a new record and gets the record
     * @param [body] JSON data used to create the record in the form of a string
     */
    public suspend inline fun <reified T : BaseModel> create(
        body: String, expandRelations: ExpandRelations = ExpandRelations()
    ): T {
        return _create(baseCrudPath, body, expandRelations)
    }

    /**
     * Updates an existing records and gets it
     * @param [id] the id of the record to update
     * @param [body] JSON data used to update the record in the form of a string
     */
    public suspend inline fun <reified T : BaseModel> update(
        id: String, body: String, expandRelations: ExpandRelations = ExpandRelations()
    ): T {
        return _update(baseCrudPath, id, body, expandRelations)
    }

    /**
     * Creates a new record and gets the record
     * @param [body] the key value data used to create the record
     * @param [files] the files you wish to upload
     */
    public suspend inline fun <reified T : BaseModel> create(
        body: Map<String, Any>, files: List<FileUpload>, expandRelations: ExpandRelations = ExpandRelations()
    ): T {
        return _create(baseCrudPath, body, files, expandRelations)
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
        expandRelations: ExpandRelations = ExpandRelations()
    ): T {
        return _update(baseCrudPath, id, body, files, expandRelations)
    }

    /***
     * Deletes the specified record
     * @param [id] the id of the record you wish to delete
     */
    public suspend inline fun delete(id: String): Boolean {
        return _delete(baseCrudPath, id)
    }
}