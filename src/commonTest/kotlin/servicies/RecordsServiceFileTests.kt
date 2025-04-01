package servicies

import TestRecord
import client
import coroutine
import createTestCollection
import deleteTestCollection
import io.github.agrevster.pocketbaseKotlin.FileUpload
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import io.github.agrevster.pocketbaseKotlin.toJsonPrimitive
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import loginBefore
import logoutAfter
import testImageFile
import testImageFile2
import kotlin.test.*

private suspend fun createFileTestRecord(): FileTestRecord {
    val record = TestRecord.generateRandomRecord().let {
        mapOf("name" to it.name.toJsonPrimitive(), "age" to it.age.toJsonPrimitive(), "married" to it.married.toJsonPrimitive())
    }

    val testRecord = client.records.create<FileTestRecord>("test", record, listOf(FileUpload("file", testImageFile, "test-file.png")))

    fun validFile(fileName: String): Boolean = testRecord.file.split("_").take(2).joinToString(separator = "-") == fileName

    if (record["name"]?.content != testRecord.name || record["age"]?.int != testRecord.age || record["married"]?.boolean != testRecord.married || (!validFile("test-file") && !(validFile("test-file2")))) error("Error creating file upload test record: Records do not match")

    return testRecord
}

@Serializable
private data class FileTestRecord(val name: String, val age: Int, val married: Boolean, val file: String) : Record()

class RecordsServiceFileTests {

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        createTestCollection(additionalFields = listOf(SchemaField("file", type = SchemaField.SchemaFieldType.FILE, true)), checkAdditionalFields = { collection ->
            collection.fields?.first { it.name == "file" }.let { field ->
                field?.type == SchemaField.SchemaFieldType.FILE && field.required == true
            }
        })
    }

    @AfterTest
    fun after(): Unit = coroutine {
        deleteTestCollection()
        logoutAfter()
    }


    @Test
    fun create(): Unit = coroutine {
        val record = TestRecord.generateRandomRecord().let {
            mapOf("name" to it.name.toJsonPrimitive(), "age" to it.age.toJsonPrimitive(), "married" to it.married.toJsonPrimitive())
        }

        val createdRecord = client.records.create<FileTestRecord>("test", record, listOf(FileUpload("file", testImageFile, "test-file.png")))

        assertEquals(record["name"]!!.content, createdRecord.name)
        assertEquals(record["age"]!!.int, createdRecord.age)
        assertEquals(record["married"]!!.boolean, createdRecord.married)
        assertContains("test-file.png", createdRecord.file.split("_")[1])
    }

    @Test
    fun update(): Unit = coroutine {
        val testRecord = createFileTestRecord()

        val record = TestRecord.generateRandomRecord().let {
            mapOf("name" to it.name.toJsonPrimitive(), "age" to it.age.toJsonPrimitive(), "married" to it.married.toJsonPrimitive())
        }

        val updatedRecord = client.records.update<FileTestRecord>("test", testRecord.id!!, record, listOf(FileUpload("file", testImageFile2, "test-file2.png")))

        assertEquals(record["age"]!!.int, updatedRecord.age)
        assertEquals(record["married"]!!.boolean, updatedRecord.married)
        assertContains("test-file2.png", updatedRecord.file.split("_")[1])

    }

    @Test
    fun getFullList(): Unit = coroutine {
        val records = (0..5).map { createFileTestRecord() }
        assertEquals(records, client.records.getFullList<FileTestRecord>("test", 10))
    }

    @Test
    fun getList(): Unit = coroutine {
        val records = (0..5).map { createFileTestRecord() }

        val recordResponse = client.records.getList<FileTestRecord>("test", 1, 3)

        assertEquals(3, recordResponse.perPage)
        assertEquals(1, recordResponse.page)
        assertEquals(6, recordResponse.totalItems)
        assertEquals(2, recordResponse.totalPages)
        assertEquals(3, recordResponse.items.size)

        recordResponse.items.forEach { record ->
            assertContains(records, record)
        }

    }

    @Test
    fun getOne(): Unit = coroutine {
        val records = (0..2).map { createFileTestRecord() }

        val record = client.records.getOne<FileTestRecord>("test", records[1].id!!)

        assertEquals(record, records[1])
    }

    @Test
    fun getFileUrl(): Unit = coroutine {
        val record = createFileTestRecord()

        val response = client.httpClient.get(client.files.getFileURL(record, record.file))
        assertEquals(200, response.status.value)
        assertContains(response.body<String>(), "PNG")
    }

}

class RecordsServiceProtectedFileTests {
    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        createTestCollection(additionalFields = listOf(SchemaField("file", type = SchemaField.SchemaFieldType.FILE, true, protected = true)), checkAdditionalFields = { collection ->
            collection.fields?.first { it.name == "file" }.let { field ->
                field?.type == SchemaField.SchemaFieldType.FILE && field.required == true && field.protected == true
            }
        })
    }

    @AfterTest
    fun after(): Unit = coroutine {
        deleteTestCollection()
        logoutAfter()
    }

    @Test
    fun getFileUrlWithoutAuthentication(): Unit = coroutine {
        val record = createFileTestRecord()

        val response = client.httpClient.get(client.files.getFileURL(record, record.file))
        assertEquals(404, response.status.value)
    }


    @Test
    fun getFileUrlWithAuthentication(): Unit = coroutine {
        val record = createFileTestRecord()

        val response = client.httpClient.get(client.files.getFileURL(record, record.file, token = client.files.generateProtectedFileToken()))
        assertEquals(200, response.status.value)
        assertContains(response.body<String>(), "PNG")
    }
}