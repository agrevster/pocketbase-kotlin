package services

import TestingUtils
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.*
import PocketbaseClient as TestClient

class RealtimeService : TestingUtils() {

    private val testCollection = "realtime_test"
    private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)

    private val testRecords = mutableMapOf<String, Boolean>()

    @Serializable
    private data class TestRecord(val text: String, val bool: Boolean) : Record()

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
                            SchemaField("text", type = SchemaField.SchemaFieldType.TEXT),
                            SchemaField(name = "bool", type = SchemaField.SchemaFieldType.BOOL)
                        )
                    )
                )
            )
        }
    }

    @AfterTest
    fun after(): Unit = runBlocking {
        launch {
            delay(2000)
            client.collections.delete("123456789123478")
        }
    }


    private fun randomName(): String {
        val range = CharRange('A', 'Z')
        val result = StringBuilder()
        repeat(4) {
            result.append(range.random())
        }
        return result.toString()
    }

    private suspend fun clean() {
        val records = client.records.getFullList<Record>(testCollection, 7)
        val ids = records.map { it.id!! }
        ids.forEach { record -> client.records.delete(testCollection, record) }
        val isClean = client.records.getList<Record>(testCollection, 1, 1).items.isEmpty()
        assertTrue(isClean, "Records should be empty!")
    }

    private val service = client.realtime

    private fun assertRecordValid(record: TestRecord) {
        assertNotNull(record)
        assertNotNull(record.id)
        assertNotNull(record.created)
        assertNotNull(record.updated)
        assertNotNull(record.collectionId)
        assertNotNull(record.collectionName)
        assertNotNull(record.bool)
        assertNotNull(record.text)
        assertNotNull(record)
        println(record)
    }

    @Test
    fun handleCreate(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            val receivedRecords = mutableMapOf<String, Boolean>()

            launch(CoroutineName("Connect")) {
                service.connect()
            }
            launch {
                delay(4000)
                repeat(5) {
                    val record = client.records.create<TestRecord>(
                        testCollection, Json.encodeToString(
                            TestRecord(
                                randomName(), Random.nextBoolean()
                            )
                        )
                    )
                    testRecords[record.text] = record.bool
                }
            }
            launch(CoroutineName("Sub and listen")) {
                service.subscribe(testCollection)
                service.listen {
                    if (action.isBodyEvent()) {
                        println("Found")
                        val record = parseRecord<TestRecord>()
                        assertRecordValid(record)
                        receivedRecords[record.text] = record.bool
                    }
                }
            }

            launch(CoroutineName("Disconnect")) {
                delay(8000)
                service.disconnect()
                clean()
                assertEquals(receivedRecords, testRecords, "Test records are missing from recived records")
                testRecords.clear()
            }
        }
    }


    @Test
    fun handleUpdate(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            val receivedRecords = mutableMapOf<String, Boolean>()

            launch(CoroutineName("Connect")) {
                service.connect()
            }
            launch {
                val ids = mutableSetOf<String>()
                repeat(5) {
                    val record = client.records.create<TestRecord>(
                        testCollection, Json.encodeToString(
                            TestRecord(
                                randomName(), Random.nextBoolean()
                            )
                        )
                    )
                    ids.add(record.id!!)
                }
                delay(4000)
                ids.forEach { id ->
                    val record = client.records.update<TestRecord>(
                        testCollection, id, Json.encodeToString(
                            TestRecord(
                                randomName(), Random.nextBoolean()
                            )
                        )
                    )
                    testRecords[record.text] = record.bool
                }

            }
            launch(CoroutineName("Sub and listen")) {
                service.subscribe(testCollection)
                service.listen {
                    if (action.isBodyEvent()) {
                        println("Found")
                        val record = parseRecord<TestRecord>()
                        assertRecordValid(record)
                        receivedRecords[record.text] = record.bool
                    }
                }
            }

            launch(CoroutineName("Disconnect")) {
                delay(8000)
                service.disconnect()
                clean()
                assertEquals(receivedRecords, testRecords, "Test records are missing from recived records")
                testRecords.clear()
            }
        }
    }

    @Test
    fun handleDelete(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            val ids = mutableSetOf<String>()
            val removedIds = mutableSetOf<String>()
            launch(CoroutineName("Connect")) {
                service.connect()
            }
            launch {
                repeat(5) {
                    val record = client.records.create<TestRecord>(
                        testCollection, Json.encodeToString(
                            TestRecord(
                                randomName(), Random.nextBoolean()
                            )
                        )
                    )
                    ids.add(record.id!!)
                }
                delay(4000)
                ids.forEach { id ->
                    client.records.delete(testCollection, id)
                }

            }
            launch(CoroutineName("Sub and listen")) {
                service.subscribe(testCollection)
                service.listen {
                    if (action.isBodyEvent()) {
                        println("Found")
                        val record = parseRecord<TestRecord>()
                        assertRecordValid(record)
                        removedIds.add(record.id!!)
                    }
                }
            }

            launch(CoroutineName("Disconnect")) {
                delay(8000)
                service.disconnect()
                assertEquals(removedIds, ids, "Test records are missing from recived records")
                testRecords.clear()
            }
        }
    }
}