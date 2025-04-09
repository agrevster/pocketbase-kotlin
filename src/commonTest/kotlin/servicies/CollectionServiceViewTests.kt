package servicies

import NameAndID
import TestRecord
import client
import coroutine
import createTestCollection
import createTestRecord
import deleteTestCollection
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.models.ViewCollection
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import kotlinx.serialization.json.Json
import loginBefore
import logoutAfter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionServiceViewTests {


    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        createTestCollection()
    }


    @AfterTest
    fun after(): Unit = coroutine {
        deleteTestCollection()
        logoutAfter()
    }

    private fun SchemaField.compareSchemaFieldsWithoutID(other: Any?): Boolean { //Make required,system,hidden and presentable all default to false
        if (this === other) return true
        if (other !is SchemaField) return false

        if ((required == true) != other.required) return false
        if ((system == true) != other.system) return false
        if ((hidden == true) != other.hidden) return false
        if ((presentable == true) != other.presentable) return false
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

    private fun Collection.equalsWithoutId(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Collection) return false

        if (system != other.system) return false
        if (name != other.name) return false
        if (type != other.type) return false
        fields?.let { fields ->
            for (i in fields.indices) fields[i].compareSchemaFieldsWithoutID(other.fields?.get(i))
        }
        if (listRule != other.listRule) return false
        if (viewRule != other.viewRule) return false
        if (createRule != other.createRule) return false
        if (updateRule != other.updateRule) return false
        if (deleteRule != other.deleteRule) return false
        if (indexes != other.indexes) return false

        return true
    }

    @Test
    fun createViewCollection(): Unit = coroutine {
        repeat(4) { createTestRecord() }
        val query = """
            SELECT
              test.name,
              test.age,
              (ROW_NUMBER() OVER()) as id
            FROM test 
        """.trimIndent()
        val collection = client.collections.create<ViewCollection>(Json.encodeToString(ViewCollection(query, "testView")))
        assertEquals("testView", collection.name)
        assertEquals(Collection.CollectionType.VIEW, collection.type)
        assertEquals(3, collection.fields?.size)
        assertEquals(query, collection.viewQuery)

        assertEquals(4, client.records.getList<NameAndID>("testView", 1, 10).totalItems)

        client.collections.delete(collection.id!!)

    }

}