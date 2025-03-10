package query

import TestingUtils
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import PocketbaseClient as TestClient

class Filter : TestingUtils() {

    private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)
    private val testCollection = "filter_testing"

    private var collectionId: String? = null

    private fun randomNum() = (1..10).random()

    @Serializable
    data class TestRecord(val field1: Int, val field2: Boolean) : BaseModel()

    @BeforeTest
    fun before(): Unit = runBlocking {
        launch {
            client.login {
                val login = client.admins.authWithPassword(TestClient.adminLogin.first, TestClient.adminLogin.second)
                token = login.token
            }
            collectionId = client.collections.create<Collection>(
                Json.encodeToString(
                    Collection(
                        name = testCollection, type = Collection.CollectionType.BASE, schema = listOf(
                            SchemaField("field1", required = true, type = SchemaField.SchemaFieldType.NUMBER),
                            SchemaField("field2", type = SchemaField.SchemaFieldType.BOOL)
                        )
                    )
                )
            ).id

            for (i in 1..8) {
                client.records.create<TestRecord>(
                    testCollection, Json.encodeToString(TestRecord(randomNum(), Random.nextBoolean()))
                )
            }
        }
    }

    @AfterTest
    fun after(): Unit = runBlocking {
        launch {
            delay(delayAmount)
            client.collections.delete(collectionId!!)
        }
    }

    @Test
    fun filterByField1(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val sortedResponse =
                    client.records.getList<TestRecord>(testCollection, 1, 10, filterBy = Filter("3 > field1"))
                sortedResponse.items.forEach { item -> println(item.field1);assertTrue("Item's retried from the request should have a field1 less than than three!") { item.field1 < 3 } }
            }
        }
    }

    @Test
    fun filterByField2(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val sortedResponse =
                    client.records.getList<TestRecord>(testCollection, 1, 10, filterBy = Filter("field2 = true"))
                sortedResponse.items.forEach { item ->
                    println(item.field1);assertTrue(
                    item.field2, "Item's retried from the request should have field2 = true"
                )
                }
            }
        }
    }

    @Test
    fun filterByBoth(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val sortedResponse = client.records.getList<TestRecord>(
                    testCollection, 1, 10, filterBy = Filter("3 < field1 && field2 = true")
                )
                sortedResponse.items.forEach { item ->
                    println(item.field1);assertTrue(
                    item.field1 > 3 && item.field2,
                    "Item's retried from the request should have field2 = true and field1 greater than 3"
                )
                }
            }
        }
    }


}