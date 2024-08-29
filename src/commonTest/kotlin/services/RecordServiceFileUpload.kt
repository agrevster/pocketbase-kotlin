package services

import TestingUtils
import io.github.agrevster.pocketbaseKotlin.FileUpload
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import io.github.agrevster.pocketbaseKotlin.services.FilesService
import io.github.agrevster.pocketbaseKotlin.toJsonPrimitive
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
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
                            SchemaField("text", required = true, type = SchemaField.SchemaFieldType.TEXT),
                            SchemaField(
                                "file",
                                type = SchemaField.SchemaFieldType.FILE,
                                options = SchemaField.SchemaOptions(maxSelect = 1, maxSize = 5242880)
                            )
                        )
                    )
                )
            )
            val record = records.create<TestRecord>(
                testCollection,
                mapOf("text" to "HI".toJsonPrimitive()),
                listOf(FileUpload("file", getTestFile(1), "monkey.jpg"))
            )
            modifyRecordId = record.id
            imageId = record.file
        }
    }

    @AfterTest
    fun after(): Unit = runBlocking {
        launch {
            delay(delayAmount)
            client.collections.delete("123456789123478")
        }
    }


    private val service = client.files
    private val records = client.records

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
    fun create(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = records.create<TestRecord>(
                    testCollection,
                    mapOf("text" to "HELLO".toJsonPrimitive()),
                    listOf(FileUpload("file", getTestFile(1), "monkey.jpg"))
                )
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("text", "HELLO", record.text)
                assertTrue(record.file!!.contains("monkey"), "File name invalid!")
            }
        }
    }

    @Test
    fun update(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = records.update<TestRecord>(
                    testCollection,
                    modifyRecordId!!,
                    mapOf("text" to "BYE".toJsonPrimitive()),
                    listOf(FileUpload("file", getTestFile(2), "ape.jpg"))
                )
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("text", "BYE", record.text)
                assertTrue(record.file!!.contains("ape"), "File name invalid!")

            }
        }
    }

    @Test
    fun getFile(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = records.getOne<TestRecord>(testCollection, modifyRecordId!!)
                val image = client.httpClient.get(service.getFileURL(record, imageId!!))
                println(image.request.url)
                PocketbaseException.handle(image)
            }
        }
    }

    @Test
    fun getFileWithDownloadUrl(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = records.getOne<TestRecord>(testCollection, modifyRecordId!!)
                val image = client.httpClient.get(service.getFileURL(record, imageId!!, download = true))
                println(image.request.url)
                assertTrue { image.call.request.url.parameters.contains("download") }
                PocketbaseException.handle(image)
            }
        }
    }

    @Test
    fun getFileWithThumbs(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = records.getOne<TestRecord>(testCollection, modifyRecordId!!)
                val image = client.httpClient.get(
                    service.getFileURL(
                        record,
                        imageId!!,
                        thumbFormat = FilesService.ThumbFormat.WxH
                    )
                )
                println(image.request.url)
                assertTrue { image.call.request.url.parameters.contains("thumb") }
                PocketbaseException.handle(image)
            }
        }
    }

    @Test
    fun getFileWithThumbsAndDownload(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = records.getOne<TestRecord>(testCollection, modifyRecordId!!)
                val image = client.httpClient.get(
                    service.getFileURL(
                        record,
                        imageId!!,
                        download = true,
                        thumbFormat = FilesService.ThumbFormat.WxH
                    )
                )
                println(image.request.url)
                assertTrue { image.call.request.url.parameters.contains("download") }
                assertTrue { image.call.request.url.parameters.contains("thumb") }
                PocketbaseException.handle(image)
            }
        }
    }


    @Test
    fun removeFile(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = records.update<TestRecord>(
                    testCollection,
                    modifyRecordId!!,
                    mapOf("text" to "BYE".toJsonPrimitive()),
                    listOf(FileUpload("file", null, ""))
                )
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("text", "BYE", record.text)
                assertEquals("", record.file, "File should be empty!")
            }
        }
    }
}