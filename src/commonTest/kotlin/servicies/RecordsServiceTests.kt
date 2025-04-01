package servicies

import TestRecord
import client
import coroutine
import createTestCollection
import createTestRecord
import deleteTestCollection
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import loginBefore
import logoutAfter
import kotlin.test.*

class RecordsServiceTests {

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        createTestCollection()
    }

    @AfterTest
    fun after(): Unit = coroutine {
        deleteTestCollection()
        logoutAfter()
    }

    @Test
    fun create(): Unit = coroutine {
        val record = TestRecord.generateRandomRecord()

        val createdRecord = client.records.create<TestRecord>("test", Json.encodeToString(record))

        assertEquals(record.name, createdRecord.name)
        assertEquals(record.age, createdRecord.age)
        assertEquals(record.married, createdRecord.married)
    }

    @Test
    fun update(): Unit = coroutine {
        val testRecord = createTestRecord()

        val updatedTestRecord = TestRecord(testRecord.name, testRecord.age, !testRecord.married, testRecordId = testRecord.id!!)
        client.records.update<TestRecord>("test", testRecord.id!!, Json.encodeToString(updatedTestRecord))

        assertEquals(testRecord.name, updatedTestRecord.name)
        assertEquals(testRecord.age, updatedTestRecord.age)
        assertEquals(!testRecord.married, updatedTestRecord.married)

    }

    @Test
    fun delete(): Unit = coroutine {
        val testRecord = createTestRecord()

        client.records.delete("test", testRecord.id!!)

        var found = true

        try {
            client.records.getOne<TestRecord>("test", testRecord.id!!)
        } catch (e: PocketbaseException) {
            if (e.reason.contains("404")) found = false
        }
        assertFalse(found)

    }

    @Test
    fun getFullList(): Unit = coroutine {
        val records = (0..5).map { createTestRecord() }
        assertEquals(records, client.records.getFullList<TestRecord>("test", 10))
    }

    @Test
    fun getList(): Unit = coroutine {
        val records = (0..5).map { createTestRecord() }

        val recordResponse = client.records.getList<TestRecord>("test", 1, 3)

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
        val records = (0..2).map { createTestRecord() }

        val record = client.records.getOne<TestRecord>("test", records[1].id!!)

        assertEquals(record, records[1])
    }
}