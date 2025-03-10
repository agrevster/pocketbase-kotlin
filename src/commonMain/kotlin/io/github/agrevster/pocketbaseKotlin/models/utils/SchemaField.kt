package io.github.agrevster.pocketbaseKotlin.models.utils

import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField.SchemaFieldType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
/**
 * The schema for a [Collection]'s field
 *
 * There are additional fields that depend on the [SchemaFieldType]. For a
 * list of them see
 * [this file's source](https://github.com/agrevster/pocketbase-kotlin/blob/master/src/commonMain/kotlin/io/github/agrevster/pocketbaseKotlin/models/Collection.kt)
 *
 * @param name the name given to the schema by the creator
 * @param type the [SchemaFieldType] assigned to the schema on creation
 * @param required weather or not the schema is required to have a value.
 *    If this is false the [Serializable] class should have an optional(?)
 *    type
 * @param system whether or not the schema is part of the pocketbase system
 * @param hidden whether or not the field is hidden from JSON API and
 *    filters
 * @param presentable whether or not the field should be prioritized in
 *    superuser relations
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
    public val primaryKey: String? = null,
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
    /** The type of Schema Field selected by the creator */
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
        AUTO_DATE
    }

    override fun toString(): String {
        return "SchemaField(name=$name, type=$type, required=$required, system=$system, hidden=$hidden, presentable=$presentable, id=$id)"
    }

}