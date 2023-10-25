package io.github.agrevster.pocketbaseKotlin.services.utils

import io.github.agrevster.pocketbaseKotlin.FileUpload
import io.github.agrevster.pocketbaseKotlin.PocketKtInternal
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.dsl.query.SortFields
import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import io.github.agrevster.pocketbaseKotlin.models.utils.ListResult
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

public abstract class BaseCrudService<T : BaseModel>(client: io.github.agrevster.pocketbaseKotlin.PocketbaseClient) :
    BaseService(client) {
    @PocketKtInternal
    public suspend inline fun <reified T : BaseModel> _getFullList(
        path: String,
        batch: Int,
        sortBy: SortFields = SortFields(),
        filterBy: Filter = Filter(),
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields(), skipTotal: Boolean = true
    ): List<T> {
        val result = mutableListOf<T>()
        var page = 1
        while (true) {
            val list = _getList<T>(path, page, batch, sortBy, filterBy, expandRelations, showFields, skipTotal)
            val items = list.items.toMutableList()
            result.addAll(items)
            if (list.perPage != items.size) return result
            page += 1
        }
    }

    @PocketKtInternal
    public suspend inline fun <reified T : BaseModel> _getList(
        path: String,
        page: Int,
        perPage: Int,
        sortBy: SortFields = SortFields(),
        filterBy: Filter = Filter(),
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields(),
        skipTotal: Boolean = false
    ): ListResult<T> {
        val response = client.httpClient.get {
            url {
                path(path)
                sortBy.addTo(parameters)
                filterBy.addTo(parameters)
                expandRelations.addTo(parameters)
                showFields.addTo(parameters)
                parameters.append("page", page.toString())
                parameters.append("perPage", perPage.toString())
                if (skipTotal) parameters.append("skipTotal", "1")
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    @PocketKtInternal
    public suspend inline fun <reified T : BaseModel> _getOne(
        path: String,
        id: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields()
    ): T {
        val response = client.httpClient.get {
            url {
                path(path, id)
                contentType(ContentType.Application.Json)
                expandRelations.addTo(parameters)
                showFields.addTo(parameters)
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    @PocketKtInternal
    public suspend inline fun <reified T : BaseModel> _create(
        path: String,
        body: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields()
    ): T {
        val response = client.httpClient.post {
            url {
                path(path)
                contentType(ContentType.Application.Json)
                expandRelations.addTo(parameters)
                showFields.addTo(parameters)
            }
            setBody(body)
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    @PocketKtInternal
    public suspend inline fun <reified T : BaseModel> _update(
        path: String,
        id: String,
        body: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields()
    ): T {
        val response = client.httpClient.patch {
            url {
                path(path, id)
                contentType(ContentType.Application.Json)
                expandRelations.addTo(parameters)
                showFields.addTo(parameters)
            }
            setBody(body)
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    @PocketKtInternal
    public suspend inline fun _delete(path: String, id: String): Boolean {
        val response = client.httpClient.delete {
            url {
                path(path, id)
            }
        }
        PocketbaseException.handle(response)
        return true
    }

    @PocketKtInternal
    public suspend inline fun <reified T : BaseModel> _create(
        path: String,
        body: Map<String, Any>,
        files: List<FileUpload>,
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields()
    ): T {
        val response = client.httpClient.post {
            url {
                path(path)
                expandRelations.addTo(parameters)
                showFields.addTo(parameters)
            }

            setBody(MultiPartFormDataContent(
                formData {
                    for (file in files) {
                        append(
                            file.field,
                            file.file ?: ByteArray(0),
                            headers = if (file.file != null) headersOf(
                                HttpHeaders.ContentDisposition,
                                "filename=\"${file.fileName}\""
                            ) else headersOf()
                        )
                    }
                    for (element in body) {
                        append(element.key, element.value.toString())
                    }
                }
            ))
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    @PocketKtInternal
    public suspend inline fun <reified T : BaseModel> _update(
        path: String,
        id: String,
        body: Map<String, Any>,
        files: List<FileUpload>,
        expandRelations: ExpandRelations = ExpandRelations(),
        showFields: ShowFields = ShowFields()
    ): T {
        val response = client.httpClient.patch {
            url {
                path(path, id)
                expandRelations.addTo(parameters)
                showFields.addTo(parameters)
            }

            setBody(MultiPartFormDataContent(
                formData {
                    for (file in files) {
                        append(
                            file.field,
                            file.file ?: ByteArray(0),
                            headers = if (file.file != null) headersOf(
                                HttpHeaders.ContentDisposition,
                                "filename=\"${file.fileName}\""
                            ) else headersOf()
                        )
                    }
                    for (element in body) {
                        append(element.key, element.value.toString())
                    }
                }
            ))
        }
        PocketbaseException.handle(response)
        return response.body()
    }
}