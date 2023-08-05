package query

import TestingUtils
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds
import PocketbaseClient as TestClient

class ShowFields : TestingUtils() {

    private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)
    private val testCollection = "showfields_testing"

    private var collectionId: String? = null

    private fun randomNum() = (1..10).random()

    @Serializable
    // Added default values to allow them to still be serialised without errors
    data class TestRecord(val field1: Int? = null, val field2: Boolean? = null) : Record()

    @BeforeTest
    fun before() = runBlocking {
        launch {
            client.login {
                val login =
                    client.admins.authWithPassword(TestClient.adminLogin.first, TestClient.adminLogin.second)
                token = login.token
            }
            collectionId = client.collections.create<Collection>(Json.encodeToString(Collection(name = testCollection, type = Collection.CollectionType.BASE, schema = listOf(
                SchemaField("field1", required = true, type = SchemaField.SchemaFieldType.NUMBER),
                SchemaField("field2", type = SchemaField.SchemaFieldType.BOOL))))).id

            for (i in 1..8) {
                client.records.create<TestRecord>(
                    testCollection,
                    Json.encodeToString(TestRecord(randomNum(), Random.nextBoolean()))
                )
            }
        }
        println()
    }

    @AfterTest
    fun after() = runBlocking {
        launch {
            client.collections.delete(collectionId!!)
            delay(delayAmount)
        }
        println()
    }

    @Test
    fun showField1() = runBlocking {
        assertDoesNotFail {
            launch {
                val field1Only =
                    client.records.getList<TestRecord>(testCollection, 1, 10, showFields = ShowFields("field1"))
                field1Only.items.forEach { item ->
                    assertNotNull(item.field1)
                    assertNull(item.field2)
                    printJson(item)
                }
            }
            println()
        }
    }

    @Test
    fun showField2() = runBlocking {
        assertDoesNotFail {
            launch {
                val field1Only =
                    client.records.getList<TestRecord>(testCollection, 1, 10, showFields = ShowFields("field2"))
                field1Only.items.forEach { item ->
                    assertNull(item.field1)
                    assertNotNull(item.field2)
                    printJson(item)
                }
            }
            println()
        }
    }

    @Test
    fun showMultipleFields() = runBlocking {
        assertDoesNotFail {
            launch {
                val field1Only =
                    client.records.getList<TestRecord>(testCollection, 1, 10, showFields = ShowFields("field1","field2","id"))
                field1Only.items.forEach { item ->
                    printJson(item)
                    assertNotNull(item.field1)
                    assertNotNull(item.field2)
                    assertNotNull(item.id)
                    assertNull(item.collectionId)
                }
            }
            println()
        }
    }



}