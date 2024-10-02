package query

import TestingUtils
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRecord
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRecordList
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import PocketbaseClient as TestClient

class ExpandList : TestingUtils() {

    private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)

    private val dataCollection = "expand_list_testing_data"
    private val testCollection = "expand_list_testing"
    private var testingCollectionId: String? = null
    private var dataCollectionId: String? = null
    private val recordRelations = mutableMapOf<String, List<String>>()

    private fun randomName(): String {
        val range = CharRange('A', 'Z')
        val result = StringBuilder()
        (1..3).forEach { _ ->
            result.append(range.random())
        }
        return result.toString()
    }

    @Serializable
    data class DataRecord(val field1: String) : Record()


    @Serializable
    data class TestRecord(val rel: List<String>) : ExpandRecordList<DataRecord>()

    @BeforeTest
    fun before(): Unit = runBlocking {
        launch {
            client.login {
                val login = client.admins.authWithPassword(TestClient.adminLogin.first, TestClient.adminLogin.second)
                token = login.token
            }
            val data = client.collections.create<Collection>(
                Json.encodeToString<Collection>(
                    Collection(
                        name = dataCollection,
                        type = Collection.CollectionType.BASE,
                        schema = listOf(
                            SchemaField(
                                name = "field1",
                                required = true,
                                type = SchemaField.SchemaFieldType.TEXT
                            )
                        )
                    )
                )
            )
            dataCollectionId = data.id

            val testing = client.collections.create<Collection>(
                Json.encodeToString<Collection>(
                    Collection(
                        name = testCollection,
                        type = Collection.CollectionType.BASE,
                        schema = listOf(
                            SchemaField(
                                name = "rel",
                                required = true,
                                type = SchemaField.SchemaFieldType.RELATION,
                                options = SchemaField.SchemaOptions(
                                    collectionId = dataCollectionId!!,
                                    maxSelect = 3,
                                    cascadeDelete = false
                                )
                            )
                        )
                    )
                )
            )
            testingCollectionId = testing.id

            for (i in 1..3) {
                val relationList = mutableListOf<String>()
                for (j in 1..3){
                    val d = client.records.create<DataRecord>(
                        dataCollection, Json.encodeToString(DataRecord(randomName()))
                    )
                    relationList.add(d.id!!)
                }
                val r = client.records.create<TestRecord>(
                    testCollection, Json.encodeToString(TestRecord(relationList))
                )
                recordRelations[r.id!!] = relationList
            }
        }
    }

    @AfterTest
    fun after(): Unit = runBlocking {
        launch {
            delay(delayAmount)
            client.collections.delete(testingCollectionId!!)
            client.collections.delete(dataCollectionId!!)
        }
    }

    @Test
    fun getOneRelation(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val relatedResponse = client.records.getList<TestRecord>(
                    testCollection, 1, 1, expandRelations = ExpandRelations("rel")
                ).items.first()
                val relations = relatedResponse.expand!!["rel"]!!.map { it.id!! }
                assertEquals(recordRelations[relatedResponse.id], relations)
            }
        }
    }

    @Test
    fun getAllRelations(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val relatedResponse =
                    client.records.getList<TestRecord>(testCollection, 1, 5, expandRelations = ExpandRelations("rel"))
                relatedResponse.items.forEach { testRecord ->
                    val relations = testRecord.expand!!["rel"]!!.map { it.id!! }
                    assertEquals(recordRelations[testRecord.id], relations)
                }
            }
        }
    }
}