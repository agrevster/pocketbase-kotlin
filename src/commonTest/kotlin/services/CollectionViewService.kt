package services

import CrudServiceTestSuite
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*
import PocketbaseClient as TestClient

class CollectionViewService : CrudServiceTestSuite<Collection>(client.collections, "api/collections") {
    @Serializable
    class TestRecord(
        val username: String,
        val password: String? = null,
        val passwordConfirm: String? = null,
        val email: String
    ) : Record()

    private fun random(): String {
        val range = CharRange('A', 'Z')
        val result = StringBuilder()
        (1..9).forEach { _ ->
            result.append(range.random())
        }
        return result.toString()
    }

    var recordId: String? = null

    companion object {
        private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)
    }

    var delete = true

    private val service = client.collections

    @BeforeTest
    fun before(): Unit = runBlocking {
        launch {
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
        "id": "yc356em40o3y8u1",
        "name": "test_auth",
        "type": "auth",
        "system": false,
        "schema": [],
        "listRule": null,
        "viewRule": null,
        "createRule": null,
        "updateRule": null,
        "deleteRule": null,
        "options": {
            "allowEmailAuth": true,
            "allowOAuth2Auth": true,
            "allowUsernameAuth": true,
            "exceptEmailDomains": null,
            "manageRule": null,
            "minPasswordLength": 8,
            "onlyEmailDomains": null,
            "requireEmail": false
        }
    }
    ]
                """.trimIndent()
            )
            val json2 = Json.decodeFromString<List<Collection>>(
                """
                    [
{
        "id": "yzz7lwv6sqqyxbe",
        "name": "view_test",
        "type": "view",
        "system": false,
        "schema": [
            {
                "id": "o01krlxq",
                "name": "username",
                "type": "text",
                "system": false,
                "required": false,
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
        "options": {
            "query": "SELECT id,username FROM test_auth"
        }
    }
    ]
                """.trimIndent()
            )
            client.collections.import(json)
            client.collections.import(json2)


            repeat(4) {
                val password = random()
                val record = client.records.create<TestRecord>(
                    "test_auth", Json.encodeToString(
                        TestRecord(
                            random(), password, password,
                            random() + "@random.com"
                        )
                    )
                )
                if (it == 3) recordId = record.id
            }
        }
    }

    @AfterTest
    fun after(): Unit = runBlocking {
        launch {
            delay(delayAmount)
            if (delete) {
                val usersCollectionId = service.getOne<Collection>("users").id
                val collections = service.getFullList<Collection>(10)
                val ids = collections
                    .map { it.id }
                    .filter { (it != usersCollectionId && it != "yc356em40o3y8u1") }
                ids.forEach { service.delete(it!!) }
                //Removes test_collection last to prevent existing dependency errors
                service.delete("yc356em40o3y8u1")
            }
        }
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
    fun assertSkipsTotal() {
        super.checkSkippedTotal<Collection>()
    }

    @Test
    fun import(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val json = Json.decodeFromString<List<Collection>>(
                    """
                    [
{
        "id": "yzz7lwv6sqqyxbt",
        "name": "view_test2",
        "type": "view",
        "system": false,
        "schema": [
            {
                "id": "o01krlxe",
                "name": "username",
                "type": "text",
                "system": false,
                "required": false,
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
        "indexes": [],
        "options": {
            "query": "SELECT id,username FROM test_auth"
        }
    }
    ]
                """.trimIndent()
                )
                service.import(json, false)
                val collection = service.getOne<Collection>("yzz7lwv6sqqyxbt")
                assertCollectionValid(collection)
                assertSchemaMatches(
                    SchemaField("username", type = SchemaField.SchemaFieldType.TEXT),
                    collection.schema!![0]
                )
            }

        }
    }

    @Test
    fun create(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val collection = service.create<Collection>(
                    Json.encodeToString(
                        Collection(
                            collectionId = "123456789123478",
                            name = "test_collection",
                            type = Collection.CollectionType.VIEW,
                            options = Collection.CollectionOptions(query = "SELECT id,created,email from test_auth")
                        )
                    )
                )
                val schema = collection.schema
                assertCollectionValid(collection)
                assertSchemaMatches(SchemaField("email", type = SchemaField.SchemaFieldType.EMAIL), schema!![0])
                assertMatchesCreation<Collection>("name", "test_collection", collection.name)
                assertMatchesCreation<Collection>("id", "123456789123478", collection.id)
                assertMatchesCreation<Collection>("type", "VIEW", collection.type?.name)
                assertMatchesCreation<Collection>(
                    "query",
                    "SELECT id,created,email from test_auth",
                    collection.options?.query
                )

            }
        }
    }

    @Test
    fun update(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val collection = service.update<Collection>(
                    "view_test", Json.encodeToString(
                        Collection(
                            name = "query_test_2",
                            options = Collection.CollectionOptions(query = "SELECT id,username,email FROM test_auth")
                        )
                    )
                )

                val schema = collection.schema
                assertCollectionValid(collection)
                assertSchemaMatches(SchemaField("username", type = SchemaField.SchemaFieldType.TEXT), schema!![0])
                assertMatchesCreation<Collection>("name", "query_test_2", collection.name)
                assertMatchesCreation<Collection>("id", "yzz7lwv6sqqyxbe", collection.id)
                assertMatchesCreation<Collection>("type", "VIEW", collection.type?.name)
                assertMatchesCreation<Collection>(
                    "query",
                    "SELECT id,username,email FROM test_auth",
                    collection.options?.query
                )
            }
        }
    }

    @Test
    fun getOne(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val collection = service.getOne<Collection>("view_test")
                assertCollectionValid(collection)
                assertMatchesCreation<Collection>("name", "view_test", collection.name)
                assertMatchesCreation<Collection>("type", "VIEW", collection.type?.name)
                assertMatchesCreation<Collection>(
                    "query",
                    "SELECT id,username FROM test_auth",
                    collection.options?.query
                )
                assertSchemaMatches(
                    SchemaField("username", type = SchemaField.SchemaFieldType.TEXT),
                    collection.schema!![0]
                )
            }
        }
    }

    @Test
    fun getList(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                service.create<Collection>(
                    Json.encodeToString(
                        Collection(
                            name = "test_collection_3",
                            type = Collection.CollectionType.VIEW,
                            options = Collection.CollectionOptions(query = "select id,created FROM test_auth")
                        )
                    )
                )
                service.create<Collection>(
                    Json.encodeToString(
                        Collection(
                            name = "test_collection_2",
                            type = Collection.CollectionType.VIEW,
                            options = Collection.CollectionOptions(query = "select id,updated FROM test_auth")
                        )
                    )
                )
                val list = service.getList<Collection>(1, 2)
                assertMatchesCreation<Collection>("page", 1, list.page)
                assertMatchesCreation<Collection>("perPage", 2, list.perPage)
                assertEquals(list.items.size, 2)
                list.items.forEach { collection -> assertCollectionValid(collection) }
            }
        }
    }

    @Test
    fun getFullList(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val list = service.getFullList<Collection>(10)
                assertEquals(3, list.size)
                list.forEach { collection -> assertCollectionValid(collection) }
            }
        }
    }

    @Test
    fun delete(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                delete = false
                val usersCollectionId = service.getOne<Collection>("users").id
                val collections = service.getFullList<Collection>(10)
                val ids = collections
                    .map { it.id }
                    .filter { (it != usersCollectionId && it != "yc356em40o3y8u1") }
                ids.forEach { service.delete(it!!) }
                //Removes test_collection last to prevent existing dependency errors
                service.delete("yc356em40o3y8u1")

                val isClean = service.getFullList<Collection>(10).size == 1
                assertTrue(isClean, "Collections should only contain the user's collection!")
            }
        }
    }

    @Test
    fun recordGetOne(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = client.records.getOne<TestRecord>("test_auth", recordId!!)
                assertNotNull(record.id)
                assertNotNull(record.username)
            }
        }
    }

    @Test
    fun recordGetList(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val list = client.records.getList<TestRecord>("test_auth", 1, 2)
                assertMatchesCreation<Collection>("page", 1, list.page)
                assertMatchesCreation<Collection>("perPage", 2, list.perPage)
                assertMatchesCreation<Collection>("totalItems", 4, list.totalItems)
                assertMatchesCreation<Collection>("totalPages", 2, list.totalPages)

                assertEquals(list.items.size, 2)
                list.items.forEach { record ->
                    assertNotNull(record.id)
                    assertNotNull(record.username)
                }
            }
        }
    }

    @Test
    fun recordGetFullList(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val list = client.records.getFullList<TestRecord>("test_auth", 10)
                assertEquals(list.size, 4)
                list.forEach { record ->
                    assertNotNull(record.id)
                    assertNotNull(record.username)
                }
            }
        }
    }

}