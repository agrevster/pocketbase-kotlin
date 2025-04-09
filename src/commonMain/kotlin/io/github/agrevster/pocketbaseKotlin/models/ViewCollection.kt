package io.github.agrevster.pocketbaseKotlin.models

import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
/**
 * Used to represent a Collection with the type of view. If you would like
 * access to view collection fields extend this class.
 *
 * @param viewQuery The SQL query used to determine what is shown in the
 *    view collection.
 */
public open class ViewCollection(public val viewQuery: String? = null, @Transient private val _name: String? = null, @Transient private val _system: Boolean? = null, @Transient private val _fields: List<SchemaField>? = null, @Transient private val _listRule: String? = null, @Transient private val _viewRule: String? = null, @Transient private val _createRule: String? = null, @Transient private val _updateRule: String? = null, @Transient private val _deleteRule: String? = null, @Transient private val _indexes: List<String>? = null, @Transient private val collectionId: String? = null) : Collection(_name, CollectionType.VIEW, _system, _fields, _listRule, _viewRule, _createRule, _updateRule, _deleteRule, _indexes, collectionId) {
    
}