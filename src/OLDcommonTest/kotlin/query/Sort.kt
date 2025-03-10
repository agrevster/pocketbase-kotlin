package query

import TestingUtils
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.dsl.query.SortFields
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
import kotlin.test.assertEquals
import PocketbaseClient as TestClient

class Sort : TestingUtils() {

    private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)
    private val testCollection = "sort_testing"
    private var collectionId: String? = null

    private fun randomName(): String {
        val range = CharRange('A', 'Z')
        val result = StringBuilder()
        (1..5).forEach { _ ->
            result.append(range.random())
        }
        return result.toString()
    }

    @Serializable
    data class TestRecord(val field1: String, val field2: Boolean) : BaseModel()


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
                            SchemaField("field1", required = true, type = SchemaField.SchemaFieldType.TEXT),
                            SchemaField("field2", type = SchemaField.SchemaFieldType.BOOL)
                        )
                    )
                )
            ).id

            for (i in 1..5) {
                client.records.create<TestRecord>(
                    testCollection, Json.encodeToString(TestRecord(randomName(), Random.nextBoolean()))
                )
            }
        }
    }

    @AfterTest
    fun after(): Unit = runBlocking {
        launch {
            client.collections.delete(collectionId!!)
            delay(delayAmount)
        }
    }


    @Test
    fun sortByName(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val sortedResponse = client.records.getList<TestRecord>(testCollection, 1, 5, SortFields("field1"))
                val expectedSort = sortedResponse.items.sortedBy { r -> r.field1 }
                assertEquals(expectedSort, sortedResponse.items, "Sorting does not match the expected sort!")
            }
        }
    }

    @Test
    fun sortByNameDescending(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val sortedResponse = client.records.getList<TestRecord>(testCollection, 1, 5, -SortFields("field1"))
                val expectedSort = sortedResponse.items.sortedByDescending { r -> r.field1 }
                assertEquals(expectedSort, sortedResponse.items, "Sorting does not match the expected sort!")
            }
        }
    }

    @Test
    fun sortByPlus(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val sortedResponse = client.records.getList<TestRecord>(testCollection, 1, 5, +SortFields("field1"))
                val expectedSort = sortedResponse.items.sortedBy { r -> r.field1 }
                assertEquals(expectedSort, sortedResponse.items, "Sorting does not match the expected sort!")
            }
        }
    }

    @Test
    fun sortByMultiple(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val sortedResponse =
                    client.records.getList<TestRecord>(testCollection, 1, 5, +SortFields("field1", "field2"))
                val expectedSort = sortedResponse.items.sortedWith(compareBy({ r -> r.field1 }, { r -> r.field2 }))
                assertEquals(expectedSort, sortedResponse.items, "Sorting does not match the expected sort!")
            }
        }
    }

    @Test
    fun sortByMultipleDescending(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val sortedResponse =
                    client.records.getList<TestRecord>(testCollection, 1, 5, -SortFields("field1", "field2"))
                val expectedSort =
                    sortedResponse.items.sortedWith(compareByDescending({ r -> r.field1 }, { r -> r.field2 }))
                assertEquals(expectedSort, sortedResponse.items, "Sorting does not match the expected sort!")
            }
        }
    }

}