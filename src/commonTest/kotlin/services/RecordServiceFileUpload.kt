package services

import TestingUtils
import github.agrevster.pocketbaseKotlin.FileUpload
import github.agrevster.pocketbaseKotlin.PocketbaseException
import github.agrevster.pocketbaseKotlin.models.Collection
import github.agrevster.pocketbaseKotlin.dsl.login
import github.agrevster.pocketbaseKotlin.models.Record
import github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import github.agrevster.pocketbaseKotlin.toJsonPrimitive
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*
import PocketbaseClient as TestClient

class RecordServiceFileUpload : TestingUtils() {

    private var modifyRecordId: String? = null
    private var imageId: String? = null
    private val testCollection = "fileupload_test"

    private val client = github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)

    @BeforeTest
    fun before() = runBlocking {
        launch {
            client.login {
                val login =
                    client.admins.authWithPassword(TestClient.adminLogin.first, TestClient.adminLogin.second)
                token = login.token
            }
            client.collections.create<Collection>(Json.encodeToString(Collection(
                name = testCollection,
                type = Collection.CollectionType.BASE,
                collectionId = "123456789123478",
                schema = listOf(
                    SchemaField("text", required = true, type = SchemaField.SchemaFieldType.TEXT),
                    SchemaField("file",type = SchemaField.SchemaFieldType.FILE, options = SchemaField.SchemaOptions(maxSelect = 1, maxSize = 5242880))
                )
            )))
            val record = service.create<TestRecord>(
                testCollection, mapOf("text" to "HI".toJsonPrimitive()),
                listOf(FileUpload("file", getTestFile(1), "monkey.jpg"))
            )
            modifyRecordId = record.id
            imageId = record.file
        }
        println()
    }

    @AfterTest
    fun after() = runBlocking {
        launch {
            client.collections.delete("123456789123478")
        }
        println()
    }


    private val service = client.records

    private fun assertRecordValid(record: TestRecord) {
        assertNotNull(record)
        assertNotNull(record.id)
        assertNotNull(record.created)
        assertNotNull(record.updated)
        assertNotNull(record.collectionId)
        assertNotNull(record.collectionName)
        assertNotNull(record.text)
        assertNotNull(record.file)
        assertNotNull(record)

        println(record)
    }


    @Serializable
    class TestRecord(val text: String, val file: String?) : Record()

    @Test
    fun create() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = service.create<TestRecord>(
                    testCollection, mapOf("text" to "HELLO".toJsonPrimitive()),
                    listOf(FileUpload("file", getTestFile(1), "monkey.jpg"))
                )
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("text", "\"HELLO\"", record.text)
                assertTrue(record.file!!.contains("monkey"), "File name invalid!")
            }
            println()
        }
    }

    @Test
    fun update() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = service.update<TestRecord>(
                    testCollection, modifyRecordId!!, mapOf("text" to "BYE".toJsonPrimitive()),
                    listOf(FileUpload("file", getTestFile(2), "ape.jpg"))
                )
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("text", "\"BYE\"", record.text)
                assertTrue(record.file!!.contains("ape"), "File name invalid!")

            }
            println()
        }
    }

    @Test
    fun getFile() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = service.getOne<TestRecord>(testCollection, modifyRecordId!!)
                val image = client.httpClient.get(service.getFileURL(record, imageId!!))
                PocketbaseException.handle(image)
            }
            println()
        }
    }


    @Test
    fun removeFile() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = service.update<TestRecord>(
                    testCollection, modifyRecordId!!, mapOf("text" to "BYE".toJsonPrimitive()),
                    listOf(FileUpload("file", null, ""))
                )
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("text", "\"BYE\"", record.text)
                assertEquals("", record.file, "File should be empty!")
            }
            println()
        }
    }
}