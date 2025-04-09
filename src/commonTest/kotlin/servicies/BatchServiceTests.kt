package servicies

import TestRecord
import client
import coroutine
import createTestCollection
import createTestRecord
import deleteTestCollection
import io.github.agrevster.pocketbaseKotlin.FileUpload
import io.github.agrevster.pocketbaseKotlin.dsl.BatchRequest
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import loginBefore
import logoutAfter
import testImageFile
import kotlin.test.*

private suspend fun setBatchEnabled(enabled: Boolean) {

    @Serializable
    data class BatchSettings(val enabled: Boolean, val maxBodySize: Int, val maxRequests: Int, val timeout: Int)

    @Serializable
    data class BatchFromSettingsList(val batch: BatchSettings)

    val settings = client.settings.getAll<JsonObject>()

    val newSettings = buildJsonObject {
        for (jsonObj in settings) {
            if (jsonObj.key != "batch") put(jsonObj.key, jsonObj.value)
        }
        putJsonObject("batch") {
            for (jsonObj in settings.jsonObject["batch"]!!.jsonObject) {
                if (jsonObj.key != "enabled") put(jsonObj.key, jsonObj.value)
            }
            put("enabled", enabled)
        }
    }

    if (client.settings.update<BatchFromSettingsList>(Json.encodeToString(newSettings)).batch.enabled != enabled) error("Failed to update batch settings!")
}

class BatchServiceTests {

    private var collectionId: String? = null

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        setBatchEnabled(true)
        createTestCollection().let { collectionId = it.id!! }
    }

    @AfterTest
    fun after(): Unit = coroutine {
        setBatchEnabled(false)
        deleteTestCollection()
        logoutAfter()
    }

// Debugging function
//            builder.createBatchBody().forEach { part ->
//                when (part) {
//                    is PartData.FormItem -> {
//                        println("${part.name}: ${part.value}")
//                    }
//
//                    is PartData.FileItem -> {
//                        println("${part.name}: ${part.originalFileName}")
//                    }
//
//                    else -> {}
//                }
//            }

    @Test
    fun batchCreate() = coroutine {
        val records = (0..4).map { TestRecord.generateRandomRecord() }
        client.batch.send {
            records.forEach { record ->
                create(collectionId!!, Json.encodeToJsonElement(record).jsonObject)
            }
        }.forEachIndexed { index, recordResponse ->
            assertEquals(200, recordResponse.status)
            assertEquals(Json.decodeFromJsonElement<TestRecord>(recordResponse.body!!), records[index])
        }
    }

    @Test
    fun batchUpdate() = coroutine {
        val records = (0..4).map { createTestRecord() }
        client.batch.send {
            records.forEach { record ->
                val changedRecord = TestRecord(record.name, record.age, !record.married, record.id!!)
                update(collectionId!!, record.id!!, Json.encodeToJsonElement(changedRecord).jsonObject)
            }
        }.forEachIndexed { index, recordResponse ->
            assertEquals(200, recordResponse.status)

            Json.decodeFromJsonElement<TestRecord>(recordResponse.body!!).let { record ->
                assertEquals(record.name, records[index].name)
                assertEquals(record.age, records[index].age)
                assertEquals(record.married, !records[index].married)
            }
        }
    }

    @Test
    fun batchDelete() = coroutine {
        val records = (0..4).map { createTestRecord() }
        client.batch.send {
            records.forEach { record ->
                delete(collectionId!!, record.id!!)
            }
        }.forEachIndexed { index, recordResponse ->
            assertEquals(204, recordResponse.status)
            assertNull(recordResponse.body)
        }
    }


    @Test
    fun batchUpsert() = coroutine {
        val records = buildList<TestRecord> {
            repeat(2) { add(createTestRecord()) }
            repeat(2) { add(TestRecord.generateRandomRecord()) }
        }.map { record -> TestRecord(record.name, record.age, !record.married, record.id) }

        client.batch.send {
            records.forEach { record ->
                val changedRecord = TestRecord(record.name, record.age, !record.married, record.id)
                upsert(collectionId!!, Json.encodeToJsonElement(changedRecord).jsonObject)
            }
        }.forEachIndexed { index, recordResponse ->
            assertEquals(200, recordResponse.status)

            Json.decodeFromJsonElement<TestRecord>(recordResponse.body!!).let { record ->
                assertEquals(record.name, records[index].name)
                assertEquals(record.age, records[index].age)
                assertEquals(record.married, !records[index].married)
            }
        }
    }

    @Test
    fun batchMultipleOperations() = coroutine {
        val records = buildList<Pair<BatchRequest.BatchRequestMethod, TestRecord>> {
            repeat(2) { add(BatchRequest.BatchRequestMethod.POST to TestRecord.generateRandomRecord()) }
            repeat(2) { add(BatchRequest.BatchRequestMethod.DELETE to createTestRecord()) }
            repeat(2) { add(BatchRequest.BatchRequestMethod.PATCH to createTestRecord()) }
        }

        val batchResponse = client.batch.send {
            records.forEach { (type, record) ->
                when (type) {
                    BatchRequest.BatchRequestMethod.POST -> {
                        create(collectionId!!, Json.encodeToJsonElement(record).jsonObject)
                    }

                    BatchRequest.BatchRequestMethod.PATCH -> {
                        val newRecord = TestRecord(record.name, record.age, !record.married, record.id!!)
                        update(collectionId!!, record.id!!, Json.encodeToJsonElement(newRecord).jsonObject)
                    }

                    BatchRequest.BatchRequestMethod.DELETE -> {
                        delete(collectionId!!, record.id!!)
                    }

                    else -> error("type not implemented: $type")
                }
            }
        }
        listOf(BatchRequest.BatchRequestMethod.POST, BatchRequest.BatchRequestMethod.DELETE, BatchRequest.BatchRequestMethod.PATCH).forEachIndexed { index, method ->
            val batch = batchResponse.windowed(2, 2, false)[index]
            val generated = records.windowed(2, 2, false)[index]
            repeat(2) { recordIndex ->
                val batchForRecord = batch[recordIndex]
                val record = generated[recordIndex].second
                when (method) {
                    BatchRequest.BatchRequestMethod.POST -> {
                        assertEquals(200, batchForRecord.status)
                        assertEquals(record, Json.decodeFromJsonElement(batchForRecord.body!!))
                    }

                    BatchRequest.BatchRequestMethod.PATCH -> {
                        assertEquals(200, batchForRecord.status)
                        assertEquals(TestRecord(record.name, record.age, !record.married), Json.decodeFromJsonElement(batchForRecord.body!!))
                    }

                    BatchRequest.BatchRequestMethod.DELETE -> {
                        assertEquals(204, batchForRecord.status)
                        assertNull(batchForRecord.body)
                    }

                    else -> error("method not implemented: $method")
                }

            }
        }
    }
}

class BatchServiceFileTests {

    @Serializable
    private data class FileTestRecord(val name: String, val age: Int, val married: Boolean, val file: String) : Record()

    private var collectionId: String? = null

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        setBatchEnabled(true)
        createTestCollection(additionalFields = listOf(SchemaField("file", type = SchemaField.SchemaFieldType.FILE, true, maxSelect = 1))) { collection ->
            collection.fields!!.first { it.name == "file" }.let { it.required == true && it.type == SchemaField.SchemaFieldType.FILE && it.maxSelect == 1 }
        }.let { collectionId = it.id!! }
    }

    @AfterTest
    fun after(): Unit = coroutine {
        setBatchEnabled(false)
        deleteTestCollection()
        logoutAfter()
    }

    @Test
    fun batchCreateWithFile() = coroutine {
        val records = (0..4).map { TestRecord.generateRandomRecord() }
        client.batch.send {
            records.forEach { record ->
                create(collectionId!!, Json.encodeToJsonElement(record).jsonObject, files = listOf(FileUpload("file", testImageFile, "test-file.png")))
            }
        }.forEachIndexed { index, recordResponse ->
            assertEquals(200, recordResponse.status)

            Json.decodeFromJsonElement<FileTestRecord>(recordResponse.body!!).let { decodedRecord ->
                assertEquals(records[index].name, decodedRecord.name)
                assertEquals(records[index].age, decodedRecord.age)
                assertEquals(records[index].married, decodedRecord.married)
                assertContains("test-file.png", decodedRecord.file.split("_")[1])
            }
        }
    }
}