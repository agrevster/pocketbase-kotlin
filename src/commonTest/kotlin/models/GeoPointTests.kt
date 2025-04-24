package models

import TestRecord
import client
import coroutine
import createTestCollection
import deleteTestCollection
import io.github.agrevster.pocketbaseKotlin.models.GeoPoint
import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import loginBefore
import logoutAfter
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GeoPointTests {

    @Serializable
    data class GeoPointTestRecord(val name: String, val age: Int, val married: Boolean, val location: GeoPoint, @Transient val _id: String? = null) : BaseModel(_id)

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        createTestCollection(additionalFields = listOf(SchemaField("location", SchemaField.SchemaFieldType.GEO_POINT, required = true))) { collection ->
            collection.fields?.first { it.name == "location" }?.required == true && collection.fields.first { it.name == "location" }.type == SchemaField.SchemaFieldType.GEO_POINT
        }
    }

    @AfterTest
    fun after(): Unit = coroutine {
        deleteTestCollection()
        logoutAfter()
    }

    @Test
    fun create(): Unit = coroutine {
        val record = TestRecord.generateRandomRecord().let { record ->
            val (lat, lon) = (0..2).map { Random.nextFloat() }
            GeoPointTestRecord(record.name, record.age, record.married, GeoPoint(lat, lon))
        }
        val createdRecord = client.records.create<GeoPointTestRecord>("test", Json.encodeToString(record))

        assertEquals(record.name, createdRecord.name)
        assertEquals(record.age, createdRecord.age)
        assertEquals(record.married, createdRecord.married)
        assertEquals(record.location, createdRecord.location)
    }

    @Test
    fun update(): Unit = coroutine {
        val record = TestRecord.generateRandomRecord().let { record ->
            val (lat, lon) = (0..2).map { Random.nextFloat() }
            GeoPointTestRecord(record.name, record.age, record.married, GeoPoint(lat, lon))
        }
        val createdRecord = client.records.create<GeoPointTestRecord>("test", Json.encodeToString(record))

        val updatedRecord = GeoPointTestRecord(createdRecord.name, createdRecord.age, createdRecord.married, GeoPoint(.52f, -53f), createdRecord.id!!)
        client.records.update<GeoPointTestRecord>("test", createdRecord.id!!, Json.encodeToString(updatedRecord))

        assertEquals(record.name, updatedRecord.name)
        assertEquals(record.age, updatedRecord.age)
        assertEquals(record.married, updatedRecord.married)
        assertEquals(GeoPoint(.52f, -53f), updatedRecord.location)
    }

    @Test
    fun get(): Unit = coroutine {
        val record = TestRecord.generateRandomRecord().let { record ->
            val (lat, lon) = (0..2).map { Random.nextFloat() }
            GeoPointTestRecord(record.name, record.age, record.married, GeoPoint(lat, lon))
        }
        client.records.create<GeoPointTestRecord>("test", Json.encodeToString(record))


        val recordList = client.records.getList<GeoPointTestRecord>("test", 1, 1)


        assertEquals(recordList.totalItems, 1)
        assertEquals(recordList.items.first().name, record.name)
        assertEquals(recordList.items.first().age, record.age)
        assertEquals(recordList.items.first().married, record.married)
        assertEquals(recordList.items.first().location, GeoPoint(lat = record.location.lat, lon = record.location.lon))
    }
}