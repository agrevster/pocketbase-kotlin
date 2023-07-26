package services

import CrudServiceTestSuite
import github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toInstant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import github.agrevster.pocketbaseKotlin.models.Collection
import github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import github.agrevster.pocketbaseKotlin.toJsonPrimitive
import kotlinx.serialization.encodeToString
import kotlin.test.*
import PocketbaseClient as TestClient

class CollectionService : CrudServiceTestSuite<Collection>(client.collections, "api/collections") {

    companion object {
        private val client = github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)
    }

    var delete = true

    private val service = client.collections

    @BeforeTest
    fun before() = runBlocking {
        launch {
            delete = true
            client.login {
                val login = client.admins.authWithPassword(
                    TestClient.adminLogin.first, TestClient.adminLogin.second
                )
                token = login.token
            }
            val json = Json.decodeFromString<List<Collection>>(
                """
                    [
{
        "id": "q324nkpuc3ocsgq",
        "name": "test_collection_0",
        "type": "base",
        "system": false,
        "schema": [
            {
                "id": "d3ccyr2s",
                "name": "text",
                "type": "text",
                "system": false,
                "required": false,
                "unique": false,
                "options": {
                    "min": null,
                    "max": null,
                    "pattern": ""
                }
            }
        ],
        "listRule": null,
        "viewRule": null,
        "createRule": null,
        "updateRule": null,
        "deleteRule": null,
        "options": {}
    }
    ]
                """.trimIndent()
            )
            client.collections.import(json)
        }
        println()
    }

    @AfterTest
    fun after() = runBlocking {
        launch {
            if (delete) {
                val usersCollectionId = service.getOne<Collection>("users").id
                val collections = service.getFullList<Collection>(10)
                val ids = collections.map { it.id }
                ids.forEach { if (it != usersCollectionId) service.delete(it!!) }
            }
        }
        println()
    }


    private fun assertCollectionValid(collection: Collection) {
        assertNotNull(collection)
        assertNotNull(collection.id)
        assertNotNull(collection.created)
        assertNotNull(collection.updated)
        assertNotNull(collection.name)
        assertNotNull(collection.schema)
        assertNotNull(collection.type)
        assertNotNull(collection.system)
        println(collection)
    }

    @Test
    override fun assertCrudPathValid() {
        super.assertCrudPathValid()
    }


    @Test
    fun import() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val json = Json.decodeFromString<List<Collection>>(
                    """
                    [
                    {
      "id": "y25869zt5494vb9",
      "created": "2023-01-20 02:41:00.141Z",
      "updated": "2023-01-20 02:41:00.141Z",
      "name": "test_collection_2",
      "type": "base",
      "system": false,
      "schema": [
        {
          "system": false,
          "id": "nug2ifoj",
          "name": "url",
          "type": "url",
          "required": true,
          "unique": true,
          "options": {
            "exceptDomains": [
              "google.com"
            ],
            "onlyDomains": null
          }
        }
      ],
      "listRule": null,
      "viewRule": null,
      "createRule": null,
      "updateRule": null,
      "deleteRule": null,
      "options": {}
    }
    ]
                """.trimIndent()
                )
                service.import(json, false)
                val collection = service.getOne<Collection>("y25869zt5494vb9")
                assertCollectionValid(collection)
                assertSchemaMatches(SchemaField(
                    name = "url",
                    required = true,
                    unique = true,
                    type = SchemaField.SchemaFieldType.URL,
                    options = SchemaField.SchemaOptions(exceptDomains = listOf("google.com"))
                ),collection.schema!![0])
            }
            println()
        }
    }

    @Test
    fun create() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val usersId = client.collections.getOne<Collection>("users").id
                val collection = service.create<Collection>(Json.encodeToString(Collection(collectionId ="123456789123478", name = "test_collection", type = Collection.CollectionType.BASE, schema = listOf(
                    SchemaField("string",type = SchemaField.SchemaFieldType.TEXT, required = true, options = SchemaField.SchemaOptions(min=4.toJsonPrimitive())),
                    SchemaField("int",type = SchemaField.SchemaFieldType.NUMBER, required = true),
                    SchemaField("bool",type = SchemaField.SchemaFieldType.BOOL),
                    SchemaField("email",type = SchemaField.SchemaFieldType.EMAIL, unique = true, options = SchemaField.SchemaOptions(onlyDomains = listOf("test.com"))),
                    SchemaField("url",type = SchemaField.SchemaFieldType.URL, unique = true, options = SchemaField.SchemaOptions(onlyDomains = listOf("facebook.com"))),
                    SchemaField("date",type = SchemaField.SchemaFieldType.DATE, required = true, options = SchemaField.SchemaOptions(
                        min="2022-08-19T02:22:00.00Z".toInstant().toJsonPrimitive(),
                        max="2023-08-19T02:22:00.00Z".toInstant().toJsonPrimitive()
                    )),
                    SchemaField("select", type = SchemaField.SchemaFieldType.SELECT, options = SchemaField.SchemaOptions(maxSelect = 1, values = listOf("VALUE1","VALUE2","VALUE3"))),
                    SchemaField("json",type=SchemaField.SchemaFieldType.JSON),
                    SchemaField("file",type=SchemaField.SchemaFieldType.FILE, options = SchemaField.SchemaOptions(maxSelect = 1, maxSize = 5242880)),
                    SchemaField("relation",SchemaField.SchemaFieldType.RELATION, options = SchemaField.SchemaOptions(
                        collectionId = usersId,
                        cascadeDelete = false,
                        maxSelect = 1,
                        minSelect = 1
                    )),
                    SchemaField("editer", type = SchemaField.SchemaFieldType.EDITOR)
                    ), createRule = "@request.auth.verified = true")))
                val schema = collection.schema
                assertCollectionValid(collection)

                assertSchemaMatches(SchemaField("string",type = SchemaField.SchemaFieldType.TEXT, required = true, options = SchemaField.SchemaOptions(min=4.toJsonPrimitive(), pattern = "")),schema!![0])
                assertSchemaMatches(SchemaField("int",type = SchemaField.SchemaFieldType.NUMBER, required = true),schema[1])
                assertSchemaMatches(SchemaField("bool",type = SchemaField.SchemaFieldType.BOOL),schema[2])
                assertSchemaMatches(SchemaField("email",type = SchemaField.SchemaFieldType.EMAIL, unique = true, options = SchemaField.SchemaOptions(onlyDomains = listOf("test.com"))),schema[3])
                assertSchemaMatches(SchemaField("url",type = SchemaField.SchemaFieldType.URL, unique = true, options = SchemaField.SchemaOptions(onlyDomains = listOf("facebook.com"))),schema[4])
                assertSchemaMatches(SchemaField("date",type = SchemaField.SchemaFieldType.DATE, required = true, options = SchemaField.SchemaOptions(
                    min="2022-08-19 02:22:00.000Z".toJsonPrimitive(),
                    max="2023-08-19 02:22:00.000Z".toJsonPrimitive()
                )),schema[5])
                assertSchemaMatches(SchemaField("select", type = SchemaField.SchemaFieldType.SELECT, options = SchemaField.SchemaOptions(maxSelect = 1, values = listOf("VALUE1","VALUE2","VALUE3"))),schema[6])
                assertSchemaMatches(SchemaField("json",type=SchemaField.SchemaFieldType.JSON),schema[7])
                assertSchemaMatches(SchemaField("file",type=SchemaField.SchemaFieldType.FILE, options = SchemaField.SchemaOptions(maxSelect = 1, maxSize = 5242880)),schema[8])
                assertSchemaMatches(SchemaField("relation",SchemaField.SchemaFieldType.RELATION, options = SchemaField.SchemaOptions(
                    collectionId = usersId,
                    cascadeDelete = false,
                    maxSelect = 1,
                    minSelect = 1
                )),schema[9])
                assertSchemaMatches(SchemaField("editer", type = SchemaField.SchemaFieldType.EDITOR),schema[10])
                assertMatchesCreation<Collection>("name", "test_collection", collection.name)
                assertMatchesCreation<Collection>("id", "123456789123478", collection.id)
                assertMatchesCreation<Collection>("type", "BASE", collection.type?.name)
                assertMatchesCreation<Collection>("createRule", "@request.auth.verified = true", collection.createRule)

            }
            println()
        }
    }

    @Test
    fun update() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val collection = service.update<Collection>("q324nkpuc3ocsgq",Json.encodeToString(
                    Collection("test_collection_1", schema = listOf(
                        SchemaField("bool", type = SchemaField.SchemaFieldType.BOOL, required = true)
                    ), createRule = null)
                ))
                assertCollectionValid(collection)
                assertSchemaMatches(SchemaField("bool", type = SchemaField.SchemaFieldType.BOOL, required = true),collection.schema!![0])
                assertMatchesCreation<Collection>("name", "test_collection_1", collection.name)
                assertMatchesCreation<Collection>("createRule", null, collection.createRule)
            }
            println()
        }
    }

    @Test
    fun getOne() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val collection = service.getOne<Collection>("q324nkpuc3ocsgq")
                assertCollectionValid(collection)
                assertMatchesCreation<Collection>("name", "test_collection_0", collection.name)
                assertMatchesCreation<Collection>("createRule", null, collection.createRule)
                assertSchemaMatches(SchemaField("text", type = SchemaField.SchemaFieldType.TEXT),collection.schema!![0])
            }
            println()
        }
    }

    @Test
    fun getList() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                service.create<Collection>(Json.encodeToString(Collection(
                    name = "test_collection_3",
                    type = Collection.CollectionType.BASE,
                    schema = listOf(
                        SchemaField("int", required = true, type = SchemaField.SchemaFieldType.NUMBER, options = SchemaField.SchemaOptions(min = 0.toJsonPrimitive()))
                    )
                )))
                service.create<Collection>(Json.encodeToString(Collection(
                    name = "test_collection_4",
                    type = Collection.CollectionType.BASE,
                    schema = listOf(
                        SchemaField("json",type = SchemaField.SchemaFieldType.JSON))
                    )
                ))

                val list = service.getList<Collection>(1, 2)
                assertMatchesCreation<Collection>("page", 1, list.page)
                assertMatchesCreation<Collection>("perPage", 2, list.perPage)
                assertEquals(list.items.size, 2)
                list.items.forEach { collection -> assertCollectionValid(collection) }
            }
            println()
        }
    }

    @Test
    fun getFullList() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val list = service.getFullList<Collection>(10)
                assertEquals(2, list.size)
                list.forEach { collection -> assertCollectionValid(collection) }
            }
            println()
        }
    }

    @Test
    fun delete() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                delete = false
                val usersCollectionId = service.getOne<Collection>("users").id
                val collections = service.getFullList<Collection>(10)
                val ids = collections.map { it.id }
                ids.forEach { if (it != usersCollectionId) service.delete(it!!) }
                val isClean = service.getFullList<Collection>(10).size == 1
                assertTrue(isClean, "Collections should only contain the user's collection!")
            }
            println()
        }
    }
}