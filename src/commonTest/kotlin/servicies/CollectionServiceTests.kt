package servicies

import NameAndID
import TestRecord
import client
import coroutine
import createTestCollection
import createTestRecord
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import io.github.agrevster.pocketbaseKotlin.toJsonPrimitive
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import loginBefore
import logoutAfter
import kotlin.test.*

class CollectionServiceTests {


    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        repeat(3) {
            delay(500)
            createTestCollection(collectionName = "test$it")
        }
    }


    @AfterTest
    fun after(): Unit = coroutine {
        client.collections.getList<NameAndID>(1, 10, showFields = ShowFields("id", "name"), filterBy = Filter("name ~ 'test'")).items.forEach { collection ->
            client.collections.delete(collection.id!!)
        }
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
    fun import() = coroutine {
        val collectionToImport = Json.decodeFromString<Collection>("""
                {
  "id": "pbc_980333658",
  "listRule": "@request.auth.email = \"admin@test.com\"",
  "viewRule": "@request.auth.email = \"admin@test.com\"",
  "createRule": "@request.auth.email = \"admin@test.com\"",
  "updateRule": "@request.auth.email = \"admin@test.com\"",
  "deleteRule": "@request.auth.email = \"admin@test.com\"",
  "name": "testFromJson",
  "type": "base",
  "fields": [
    {
      "autogeneratePattern": "[a-z0-9]{15}",
      "hidden": false,
      "id": "text3208210256",
      "max": 15,
      "min": 15,
      "name": "id",
      "pattern": "^[a-z0-9]+$",
      "presentable": false,
      "primaryKey": true,
      "required": true,
      "system": true,
      "type": "text"
    },
    {
      "autogeneratePattern": "",
      "hidden": false,
      "id": "text3862414132",
      "max": 0,
      "min": 0,
      "name": "field1",
      "pattern": "",
      "presentable": true,
      "primaryKey": false,
      "required": true,
      "system": false,
      "type": "text"
    },
    {
      "hidden": false,
      "id": "autodate2990389176",
      "name": "created",
      "onCreate": true,
      "onUpdate": false,
      "presentable": false,
      "system": false,
      "type": "autodate"
    },
    {
      "hidden": false,
      "id": "autodate3332085495",
      "name": "updated",
      "onCreate": true,
      "onUpdate": true,
      "presentable": false,
      "system": false,
      "type": "autodate"
    }
  ],
  "indexes": [
    "CREATE UNIQUE INDEX `idx_JIvSlSYnRn` ON `fromJson` (`field1`)"
  ],
  "created": "2025-03-30 19:47:40.341Z",
  "updated": "2025-03-30 19:47:40.341Z",
  "system": false
}
            """.trimIndent())
        client.collections.import(listOf(collectionToImport))
        assertEquals(client.collections.getOne<Collection>("pbc_980333658"), collectionToImport)
    }

    @Test
    fun create() = coroutine { // @formatter:off Don't format so we can see everything...
        val fields = listOf<SchemaField>(
            SchemaField("id", SchemaField.SchemaFieldType.TEXT, true, true, hidden = false, min = 15.toJsonPrimitive(), max = 15.toJsonPrimitive(), pattern = "^[a-z0-9]+$", autogeneratePattern = "[a-z0-9]{15}"),
            SchemaField("created", SchemaField.SchemaFieldType.AUTO_DATE, onCreate = true, onUpdate = false),
            SchemaField("updated", SchemaField.SchemaFieldType.AUTO_DATE, onCreate = true, onUpdate = true),
            SchemaField("text", SchemaField.SchemaFieldType.TEXT, min = 2.toJsonPrimitive(), max = 10.toJsonPrimitive(), presentable = true),
            SchemaField("editor", SchemaField.SchemaFieldType.EDITOR, convertUrls = true, maxSize = 400),
            SchemaField("number", SchemaField.SchemaFieldType.NUMBER, min=0.toJsonPrimitive(), max=100.toJsonPrimitive()),
            SchemaField("bool", SchemaField.SchemaFieldType.BOOL),
            SchemaField("email", SchemaField.SchemaFieldType.EMAIL, onlyDomains = listOf("test.com")),
            SchemaField("url", SchemaField.SchemaFieldType.URL, exceptDomains = listOf("x.com")),
            SchemaField("datetime", SchemaField.SchemaFieldType.DATE, min="2025-03-20 12:00:00".toJsonPrimitive()),
            SchemaField("select", SchemaField.SchemaFieldType.SELECT, values = listOf("a", "b", "c"), maxSelect = 1),
            SchemaField("file", SchemaField.SchemaFieldType.FILE, mimeTypes = listOf("text/plain"), protected = true, maxSize = 5000, thumbs = listOf("50x50")),
            SchemaField("relation", SchemaField.SchemaFieldType.RELATION, collectionId = "_pb_users_auth_", cascadeDelete = false, presentable = true, maxSelect = 2, minSelect = 1),
            SchemaField("json", SchemaField.SchemaFieldType.JSON, maxSize = 50),
            SchemaField("location", SchemaField.SchemaFieldType.GEO_POINT, required = true),
        )
        // @formatter:on Alright... go back to doing your job!

        val everythingCollection = Collection("testAllFields", Collection.CollectionType.BASE, false, fields = fields, indexes = listOf())

        val createdCollection = client.collections.create<Collection>(Json.encodeToString(everythingCollection))
        assertTrue(everythingCollection.equalsWithoutId(createdCollection), "Collections do not match!\nExpected: $everythingCollection; Actual: $createdCollection")
    }

    @Test
    fun update() = coroutine {
        val existingCollection = client.collections.getOne<Collection>("test1")
        val modifiedCollection = Collection("test59", existingCollection.type, existingCollection.system, existingCollection.fields, existingCollection.listRule, existingCollection.viewRule, existingCollection.createRule, existingCollection.updateRule, existingCollection.deleteRule, existingCollection.indexes?.map { it.replace("test1", "test59") })
        val updatedCollection = client.collections.update<Collection>(existingCollection.id!!, Json.encodeToString(modifiedCollection))
        assertTrue(updatedCollection.equalsWithoutId(modifiedCollection), "Collections do not match!\nExpected: $modifiedCollection; Actual: $updatedCollection")

    }

    @Test
    fun delete(): Unit = coroutine {
        assertNotNull(client.collections.getOne<Collection>("test1"))
        client.collections.delete("test1")

        var found = true

        try {
            client.collections.getOne<Collection>("test1")
        } catch (e: PocketbaseException) {
            if (e.reason.contains("404")) found = false
        }
        assertFalse(found)

    }

    @Test
    fun getOne(): Unit = coroutine {
        val collection = client.collections.getOne<Collection>("test1")
        assertNotNull(collection)
    }


    @Test
    fun getList(): Unit = coroutine {
        val collections = client.collections.getList<Collection>(1, 2, filterBy = Filter("name ~ 'test'"))
        assertEquals(1, collections.page)
        assertEquals(2, collections.perPage)
        assertEquals(3, collections.totalItems)
        assertEquals(2, collections.totalPages)
        assertEquals(2, collections.items.size)
    }

    @Test
    fun getFullList(): Unit = coroutine {
        val collections = client.collections.getFullList<Collection>(10, filterBy = Filter("name ~ 'test'"))
        assertEquals(3, collections.size)
    }


    @Test
    fun scaffolds(): Unit = coroutine {
        client.collections.getScaffolds().keys.map { it.uppercase() }.forEach { type ->
            assertEquals(type, Collection.CollectionType.valueOf(type).name)
        }
    }


    @Test
    fun truncate(): Unit = coroutine {
        repeat(3) {
            createTestRecord("test1")
        }
        assertEquals(3, client.records.getFullList<TestRecord>("test1", 5).size)
        client.collections.truncate("test1")
        assertEquals(0, client.records.getFullList<TestRecord>("test1", 5).size)
    }

}