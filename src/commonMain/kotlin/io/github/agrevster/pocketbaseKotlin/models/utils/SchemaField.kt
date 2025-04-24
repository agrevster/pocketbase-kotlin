package io.github.agrevster.pocketbaseKotlin.models.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
/**
 * The schema for a [Collection]'s field.
 *
 * There are additional fields that depend on the [SchemaFieldType]. For a
 * list of them see
 * [this file's source](https://github.com/agrevster/pocketbase-kotlin/blob/master/src/commonMain/kotlin/io/github/agrevster/pocketbaseKotlin/models/Collection.kt)
 *
 * @param name the name given to the schema by the creator
 * @param type the [SchemaFieldType] assigned to the schema on creation
 * @param required weather or not, the schema is required to have a value.
 *    If this is false, the [Serializable] class should have an optional(?)
 *    type
 * @param system whether the schema is part of the pocketbase system
 * @param hidden whether the field is hidden from JSON API and filters
 * @param presentable whether the field should be prioritized in superuser
 *    relations
 * @param id the unique ID of the schema
 */
public class SchemaField(
    public val name: String? = null,
    public val type: SchemaFieldType? = null,
    public val required: Boolean? = null,
    public val system: Boolean? = null,
    public val hidden: Boolean? = null,
    public val presentable: Boolean? = null,
    public val id: String? = null,

//    Type specific fields
    // multiple:
    public val min: JsonPrimitive? = null,
    public val max: JsonPrimitive? = null,
    public val exceptDomains: List<String>? = null,
    public val onlyDomains: List<String>? = null,
    public val maxSelect: Int? = null,
    public val maxSize: Long? = null,

    // text:
    public val autogeneratePattern: String? = null,
    public val pattern: String? = null,
    public val primaryKey: Boolean? = null,
    // USES min max

    // editor:
    public val convertUrls: Boolean? = null,
    // USES maxSize

    //number:
    // USES min max
    public val onlyInt: Boolean? = null,

    //bool:
    //N/A

    //email:
    // USES exceptDomains onlyDomains

    //url:
    // USES exceptDomains onlyDomains

    //date:
    // USES min max

    //select:
    public val values: List<String>? = null,
    // USES maxSelect

    //file:
    public val mimeTypes: List<String>? = null,
    public val thumbs: List<String>? = null,
    public val protected: Boolean? = null,
    // USES maxSelect maxSize

    //relation:
    public val collectionId: String? = null,
    public val minSelect: Int? = null,
    public val cascadeDelete: Boolean? = null,
    // USES maxSelect

    //json
    // USES maxSize

    //autodate
    public val onCreate: Boolean? = null,
    public val onUpdate: Boolean? = null,

    ) {


    @Serializable
    /** The type of Schema Field selected by the creator. */
    public enum class SchemaFieldType {
        @SerialName("text")
        TEXT,

        @SerialName("number")
        NUMBER,

        @SerialName("bool")
        BOOL,

        @SerialName("email")
        EMAIL,

        @SerialName("url")
        URL,

        @SerialName("date")
        DATE,

        @SerialName("select")
        SELECT,

        @SerialName("json")
        JSON,

        @SerialName("file")
        FILE,

        @SerialName("relation")
        RELATION,

        @SerialName("editor")
        EDITOR,

        @SerialName("autodate")
        AUTO_DATE,

        @SerialName("password")
        PASSWORD,

        @SerialName("geoPoint")
        GEO_POINT
    }

    override fun toString(): String {
        return "SchemaField(name=$name, type=$type, required=$required, system=$system, hidden=$hidden, presentable=$presentable, id=$id)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SchemaField) return false

        if (required != other.required) return false
        if (system != other.system) return false
        if (hidden != other.hidden) return false
        if (presentable != other.presentable) return false
        if (maxSelect != other.maxSelect) return false
        if (maxSize != other.maxSize) return false
        if (primaryKey != other.primaryKey) return false
        if (convertUrls != other.convertUrls) return false
        if (onlyInt != other.onlyInt) return false
        if (protected != other.protected) return false
        if (minSelect != other.minSelect) return false
        if (cascadeDelete != other.cascadeDelete) return false
        if (onCreate != other.onCreate) return false
        if (onUpdate != other.onUpdate) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (id != other.id) return false
        if (min != other.min) return false
        if (max != other.max) return false
        if (exceptDomains != other.exceptDomains) return false
        if (onlyDomains != other.onlyDomains) return false
        if (autogeneratePattern != other.autogeneratePattern) return false
        if (pattern != other.pattern) return false
        if (values != other.values) return false
        if (mimeTypes != other.mimeTypes) return false
        if (thumbs != other.thumbs) return false
        if (collectionId != other.collectionId) return false

        return true
    }

}