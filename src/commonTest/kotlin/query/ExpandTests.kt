package query

import TestRecord
import client
import coroutine
import createTestCollection
import createTestRecord
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRecord
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRecordList
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import loginBefore
import logoutAfter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpandTests {

    @Serializable
    private data class ChildTestRecord(val name: String, val age: Int, val married: Boolean, val parent: String) : Record()

    @Serializable
    private data class ExpandedChildTestRecord(val name: String, val age: Int, val married: Boolean, val parent: String) : ExpandRecord<TestRecord>()

    var parentMap: Map<String, String>? = null

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        val parentCollection = createTestCollection("testParents")

        createTestCollection("testChildren", additionalFields = listOf(SchemaField("parent", SchemaField.SchemaFieldType.RELATION, true, cascadeDelete = false, maxSelect = 1, collectionId = parentCollection.id))) { collection ->
            collection.fields!!.first { it.name == "parent" }.let { field ->
                field.type == SchemaField.SchemaFieldType.RELATION && field.required!! && field.maxSelect == 1 && field.cascadeDelete == false
            }
        }

        parentMap = (0..3).associate {
            createTestRecord("testParents").let {
                it.name to it.id!!
            }
        }

        parentMap!!.values.forEach { parentID ->
            client.records.create<ChildTestRecord>("testChildren", Json.encodeToString(TestRecord.generateRandomRecord().let { record ->
                ChildTestRecord(record.name, record.age, record.married, parentID)
            }))
        }

    }

    @AfterTest
    fun after(): Unit = coroutine {

        client.collections.delete("testChildren")
        client.collections.delete("testParents")
        logoutAfter()
    }


    @Test
    fun expandOne() = coroutine {
        val expandedChild = client.records.getList<ExpandedChildTestRecord>("testChildren", 1, 1, expandRelations = ExpandRelations("parent")).items[0]
        assertEquals(parentMap!![expandedChild.expand!!["parent"]!!.name], expandedChild.parent)
    }

    @Test
    fun expandAll() = coroutine {
        client.records.getList<ExpandedChildTestRecord>("testChildren", 1, 5, expandRelations = ExpandRelations("parent")).items.forEach { record ->
            assertEquals(parentMap!![record.expand!!["parent"]!!.name], record.parent)
        }
    }


}

class MultiExpandTests {

    @Serializable
    private data class ChildTestRecord(val name: String, val age: Int, val married: Boolean, val parent: List<String>) : Record()

    @Serializable
    private data class ExpandedChildTestRecord(val name: String, val age: Int, val married: Boolean, val parent: List<String>) : ExpandRecordList<TestRecord>()

    var parentMap: Map<String, String>? = null

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        val parentCollection = createTestCollection("testParents")

        createTestCollection("testChildren", additionalFields = listOf(SchemaField("parent", SchemaField.SchemaFieldType.RELATION, true, cascadeDelete = false, maxSelect = 2, minSelect = 2, collectionId = parentCollection.id))) { collection ->
            collection.fields!!.first { it.name == "parent" }.let { field ->
                field.type == SchemaField.SchemaFieldType.RELATION && field.required!! && field.maxSelect == 2 && field.minSelect == 2 && field.cascadeDelete == false
            }
        }

        parentMap = (0..3).associate {
            createTestRecord("testParents").let {
                it.name to it.id!!
            }
        }

        parentMap!!.values.windowed(2).forEach { parents ->
            val (parent1, parent2) = parents

            client.records.create<ChildTestRecord>("testChildren", Json.encodeToString(TestRecord.generateRandomRecord().let { record ->
                ChildTestRecord(record.name, record.age, record.married, listOf(parent2, parent1))
            }))
        }
    }

    @AfterTest
    fun after(): Unit = coroutine {
        client.collections.delete("testChildren")
        client.collections.delete("testParents")
        logoutAfter()
    }


    @Test
    fun expandOne() = coroutine {
        val expandedChildren = client.records.getList<ExpandedChildTestRecord>("testChildren", 1, 1, expandRelations = ExpandRelations("parent")).items[0]
        assertEquals(2, expandedChildren.expand!!["parent"]!!.size)
        expandedChildren.expand!!["parent"]!!.forEach { parentRecord ->
            assertEquals(parentMap!![parentRecord.name], parentRecord.id)
        }
    }

    @Test
    fun expandAll() = coroutine {
        client.records.getList<ExpandedChildTestRecord>("testChildren", 1, 10, expandRelations = ExpandRelations("parent")).items.forEach { expandedChildren ->
            assertEquals(2, expandedChildren.expand!!["parent"]!!.size)
            expandedChildren.expand!!["parent"]!!.forEach { parentRecord ->
                assertEquals(parentMap!![parentRecord.name], parentRecord.id)
            }
        }
    }
}