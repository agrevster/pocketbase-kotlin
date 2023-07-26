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
import kotlinx.serialization.json.JsonElement

public abstract class SubCrudService<T : BaseModel>(client: PocketbaseClient) : BaseCrudService<T>(client) {

    /**
     * The url path to the service's API
     */
    public abstract fun baseCrudPath(collectionId: String): String

    /**
     * Fetches all records in the collection at once
     * @param [sub] The collection you wish to preform this action on
     * @param [batch] The amount of records you wish to fetch.
     */
    public suspend inline fun <reified T : BaseModel> getFullList(
        sub: String,
        batch: Int,
        sortBy: SortFields = SortFields(),
        filterBy: Filter = Filter(),
        expandRelations: ExpandRelations = ExpandRelations()
    ): List<T> {
        return _getFullList(baseCrudPath(sub), batch, sortBy, filterBy, expandRelations)
    }

    /**
     * Fetches a paged list of records
     * @param [sub] The collection you wish to preform this action on
     * @param [page] The page number you wish to fetch.
     * @param [perPage] The amount of records you wish to have per-single-page
     */
    public suspend inline fun <reified T : BaseModel> getList(
        sub: String,
        page: Int,
        perPage: Int,
        sortBy: SortFields = SortFields(),
        filterBy: Filter = Filter(),
        expandRelations: ExpandRelations = ExpandRelations()
    ): ListResult<T> {
        return _getList(baseCrudPath(sub), page, perPage, sortBy, filterBy, expandRelations)
    }

    /**
     * Fetches a single record
     * @param [sub] The collection you wish to preform this action on
     * @param [id] ID of the record you wish to view.
     */
    public suspend inline fun <reified T : BaseModel> getOne(
        sub: String, id: String, expandRelations: ExpandRelations = ExpandRelations()
    ): T {
        return _getOne(baseCrudPath(sub), id, expandRelations)
    }

    /**
     * Creates a new record and gets the record
     * @param [sub] The collection you wish to preform this action on
     * @param [body] JSON data used to create the record in the form of a string
     */
    public suspend inline fun <reified T : BaseModel> create(
        sub: String,
        body: String,
        expandRelations: ExpandRelations = ExpandRelations()
    ): T {
        return _create(baseCrudPath(sub), body, expandRelations)
    }

    /**
     * Updates an existing records and gets it
     * @param [sub] The collection you wish to preform this action on
     * @param [id] the id of the record to update
     * @param [body] JSON data used to update the record in the form of a string
     */
    public suspend inline fun <reified T : BaseModel> update(
        sub: String,
        id: String,
        body: String,
        expandRelations: ExpandRelations = ExpandRelations()
    ): T {
        return _update(baseCrudPath(sub), id, body, expandRelations)
    }

    /**
     * Creates a new record and gets the record
     * @param [sub] The collection you wish to preform this action on
     * @param [body] the key value data used to create the record
     * @param [files] the files you wish to upload
     */
    public suspend inline fun <reified T : BaseModel> create(
        sub: String,
        body: Map<String, JsonElement>,
        files: List<FileUpload>,
        expandRelations: ExpandRelations = ExpandRelations()
    ): T {
        return _create(baseCrudPath(sub), body, files, expandRelations)
    }

    /**
     * Updates an existing records and gets it
     * @param [sub] The collection you wish to preform this action on
     * @param [id] the id of the record to update
     * @param [body] the key value data used to create the record
     * @param [files] the files you wish to upload
     */
    public suspend inline fun <reified T : BaseModel> update(
        sub: String,
        id: String,
        body: Map<String, JsonElement>,
        files: List<FileUpload>,
        expandRelations: ExpandRelations = ExpandRelations()
    ): T {
        return _update(baseCrudPath(sub), id, body, files, expandRelations)
    }

    /***
     * Deletes the specified record
     * @param [sub] The collection you wish to preform this action on
     * @param [id] the id of the record you wish to delete
     */
    public suspend inline fun delete(sub: String, id: String): Boolean {
        return _delete(baseCrudPath(sub), id)
    }
}