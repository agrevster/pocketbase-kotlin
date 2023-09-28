package services

import TestingUtils
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

class RecordService : TestingUtils() {

    private var modifyRecordId: String? = null

    private val testCollection = "records_test"
    private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)

    @BeforeTest
    fun before(): Unit = runBlocking {
        launch {
            client.login {
                val login = client.admins.authWithPassword(TestClient.adminLogin.first, TestClient.adminLogin.second)
                token = login.token
            }
            client.collections.create<Collection>(
                Json.encodeToString(
                    Collection(
                        name = testCollection,
                        type = Collection.CollectionType.BASE,
                        collectionId = "123456789123478",
                        schema = listOf(
                            SchemaField("name", required = true, type = SchemaField.SchemaFieldType.TEXT),
                            SchemaField("age", required = true, type = SchemaField.SchemaFieldType.NUMBER),
                            SchemaField("happy", type = SchemaField.SchemaFieldType.BOOL)
                        )
                    )
                )
            )
            modifyRecordId =
                service.create<TestRecord>(testCollection, Json.encodeToString(TestRecord("Jim", 70, true))).id
        }
    }

    @AfterTest
    fun after(): Unit = runBlocking {
        launch {
            delay(delayAmount)
            client.collections.delete("123456789123478")
        }
    }


    private val service = client.records

    private fun assertRecordValid(record: TestRecord) {
        assertNotNull(record)
        assertNotNull(record.id)
        assertNotNull(record.created)
        assertNotNull(record.updated)
        assertNotNull(record.collectionId)
        assertNotNull(record.collectionName)
        println(record)
    }


    @Test
    fun assertCrudPathValid() {
        assertEquals("api/collections/$testCollection/records", service.baseCrudPath(testCollection))
    }

    @Serializable
    class TestRecord(val name: String, val age: Int, val happy: Boolean? = null) : Record()

    @Test
    fun create(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = service.create<TestRecord>(
                    testCollection, Json.encodeToString(TestRecord("Bob", 32))
                )
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("name", "Bob", record.name)
                assertMatchesCreation<TestRecord>("age", 32, record.age)
                assertMatchesCreation<TestRecord>("happy", false, record.happy)
            }
        }
    }

    @Test
    fun update(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = service.update<TestRecord>(
                    testCollection, modifyRecordId!!, Json.encodeToString(TestRecord("Bob", 33, false))
                )
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("name", "Bob", record.name)
                assertMatchesCreation<TestRecord>("age", 33, record.age)
                assertMatchesCreation<TestRecord>("happy", false, record.happy)
            }
        }
    }

    @Test
    fun getOne(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = service.getOne<TestRecord>(testCollection, modifyRecordId!!)
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("name", "Jim", record.name)
                assertMatchesCreation<TestRecord>("age", 70, record.age)
                assertMatchesCreation<TestRecord>("happy", true, record.happy)
            }
        }
    }

    @Test
    fun getList(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                service.create<TestRecord>(testCollection, Json.encodeToString(TestRecord("Tim", 23, true)))
                service.create<TestRecord>(testCollection, Json.encodeToString(TestRecord("Kim", 2, false)))
                service.create<TestRecord>(testCollection, Json.encodeToString(TestRecord("Vim", 10, false)))

                val list = service.getList<TestRecord>(testCollection, 1, 2)
                assertMatchesCreation<Collection>("page", 1, list.page)
                assertMatchesCreation<Collection>("perPage", 2, list.perPage)
                assertMatchesCreation<Collection>("totalItems", 4, list.totalItems)
                assertMatchesCreation<Collection>("totalPages", 2, list.totalPages)

                assertEquals(list.items.size, 2)
                list.items.forEach { record -> assertRecordValid(record) }
            }
        }
    }

    @Test
    fun getFullList(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val list = service.getFullList<TestRecord>(testCollection, 10)
                assertEquals(list.size, 1)
                list.forEach { record -> assertRecordValid(record) }
            }
        }
    }

    @Test
    fun delete(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val records = service.getFullList<Record>(testCollection, 6)
                val ids = records.map { it.id!! }
                ids.forEach { record -> service.delete(testCollection, record) }
                val isClean = service.getList<Record>(testCollection, 1, 1).items.isEmpty()
                assertTrue(isClean, "Collections should only contain the user's collection!")
            }
        }
    }
}