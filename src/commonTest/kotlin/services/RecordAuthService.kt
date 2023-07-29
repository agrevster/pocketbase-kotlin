package services

import TestingUtils
import github.agrevster.pocketbaseKotlin.dsl.login
import github.agrevster.pocketbaseKotlin.models.Record
import github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*
import PocketbaseClient as TestClient


class RecordAuthService : TestingUtils() {

    private var mainRecordId: String? = null

    private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)
    private val testCollection = "auth_test"
    private val service = client.records


    private val recordLogin = ("main@test.com" to "auth_test_user") to "12345678"

    @Serializable
    class TestRecordCreate(
        val password: String,
        val emailVisibility: Boolean,
        @Transient val _bool: Boolean? = null,
        @Transient val _number: Int? = null,
        @Transient val _email: String? = null,
        @Transient val _username: String? = null,
        val passwordConfirm: String
    ) : TestRecord(_bool!!, _number!!, _username!!, _email!!)

    @Serializable
    open class TestRecord(val bool: Boolean, val int: Int, val username: String, val email: String) : Record()

    @BeforeTest
    fun before() = runBlocking {
        launch {
            client.login {
                val login = client.admins.authWithPassword(
                    TestClient.adminLogin.first,
                    TestClient.adminLogin.second
                )
                token = login.token
            }
            delay(500)
            client.collections.create<Collection>(Json.encodeToString(Collection(
                name = testCollection,
                type = Collection.CollectionType.AUTH,
                schema = listOf(
                    SchemaField("bool", type = SchemaField.SchemaFieldType.BOOL),
                    SchemaField("int", type = SchemaField.SchemaFieldType.NUMBER)
                    ),
                options = Collection.CollectionOptions(allowOAuth2Auth = true, allowEmailAuth = true, allowUsernameAuth = true, minPasswordLength = 8),
                createRule = "",
                updateRule = "",
                deleteRule = "",
                listRule = "",
                viewRule = "",
            )))

            val mainUser = client.records.create<TestRecord>(
                testCollection,
                Json.encodeToString(
                    TestRecordCreate(
                        recordLogin.second,
                        true,
                        true,
                        5,
                        recordLogin.first.first,
                        recordLogin.first.second,
                        recordLogin.second
                    )
                )
            )
            mainRecordId = mainUser.id
            client.login {
                val login = client.records.authWithPassword<TestRecord>(
                    testCollection,
                    recordLogin.first.first,
                    recordLogin.second
                )
                token = login.token
            }
        }
        println()
    }

    @AfterTest
    fun after(): Unit = runBlocking {
        launch {
            client.login {
                val login = client.admins.authWithPassword(
                    TestClient.adminLogin.first,
                    TestClient.adminLogin.second
                )
                token = login.token
            }
            delay(500)
            client.collections.delete(testCollection)
        }
        println()
    }


    private fun assertRecordValid(record: TestRecord) {
        assertNotNull(record)
        assertNotNull(record.id)
        assertNotNull(record.collectionId)
        assertNotNull(record.collectionName)
        assertNotNull(record.created)
        assertNotNull(record.updated)
        assertNotNull(record.username)
        assertNotNull(record.bool)
        assertNotNull(record.int)
        println(record)
    }

    @Test
    fun authWithPassword() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response =
                    service.authWithPassword<TestRecord>(testCollection, recordLogin.first.first, recordLogin.second)
                assertNotNull(response.token)
                assertRecordValid(response.record)
            }
            println()
        }
    }

    @Test
    fun authWithUsername() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response =
                    service.authWithUsername<TestRecord>(testCollection, recordLogin.first.first, recordLogin.second)
                assertNotNull(response.token)
                assertRecordValid(response.record)
            }
            println()
        }
    }


    @Test
    fun refresh() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response = service.refresh<TestRecord>(testCollection)
                assertNotNull(response.token)
                assertRecordValid(response.record)
            }
            println()
        }
    }


    @Test
    fun listAuthMethods() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response = service.listAuthMethods(testCollection)
                assertNotNull(response.emailPassword)
                assertNotNull(response.usernamePassword)
                assertNotNull(response.authProviders)
                response.authProviders.forEach { provider -> println(provider) }
            }
            println()
        }
    }


    @Test
    fun create() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = service.create<TestRecord>(
                    testCollection,
                    Json.encodeToString(
                        TestRecordCreate(
                            recordLogin.second, true, true, 5, "recordlogin@test.com",
                            "test_user", recordLogin.second
                        )
                    )
                )
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("username", "test_user", record.username)
                assertMatchesCreation<TestRecord>("email", "recordlogin@test.com", record.email)
                assertMatchesCreation<TestRecord>("bool", true, record.bool)
                assertMatchesCreation<TestRecord>("int", 5, record.int)
            }
            println()
        }
    }


    @Test
    fun update() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = service.update<TestRecord>(
                    testCollection, mainRecordId!!, Json.encodeToString(
                        TestRecord(
                            false, 1, recordLogin.first.second,
                            recordLogin.first.first
                        )
                    )
                )
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("username", recordLogin.first.second, record.username)
                assertMatchesCreation<TestRecord>("email", recordLogin.first.first, record.email)
                assertMatchesCreation<TestRecord>("bool", false, record.bool)
                assertMatchesCreation<TestRecord>("int", 1, record.int)
            }
            println()
        }
    }

    @Test
    fun getOne() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val record = service.getOne<TestRecord>(testCollection, mainRecordId!!)
                assertRecordValid(record)
                assertMatchesCreation<TestRecord>("username", recordLogin.first.second, record.username)
                assertMatchesCreation<TestRecord>("email", recordLogin.first.first, record.email)
                assertMatchesCreation<TestRecord>("bool", true, record.bool)
                assertMatchesCreation<TestRecord>("int", 5, record.int)
            }
            println()
        }
    }

    @Test
    fun getList() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                service.create<TestRecord>(
                    testCollection, Json.encodeToString(
                        TestRecordCreate(
                            recordLogin.second, true, true, 2, "2.auth_test@test.com", "auth_test2", recordLogin.second
                        )
                    )
                )
                service.create<TestRecord>(
                    testCollection, Json.encodeToString(
                        TestRecordCreate(
                            recordLogin.second,
                            true,
                            false,
                            10,
                            "3.auth_test@test.com",
                            "auth_test_3",
                            recordLogin.second
                        )
                    )
                )
                service.create<TestRecord>(
                    testCollection, Json.encodeToString(
                        TestRecordCreate(
                            recordLogin.second,
                            true,
                            false,
                            3,
                            "4.auth_test@test.com",
                            "auth_test_4",
                            recordLogin.second
                        )
                    )
                )
                service.create<TestRecord>(
                    testCollection, Json.encodeToString(
                        TestRecordCreate(
                            recordLogin.second,
                            true,
                            true,
                            100,
                            "5.auth_test@test.com",
                            "auth_test_5",
                            recordLogin.second
                        )
                    )
                )

                val list = service.getList<TestRecord>(testCollection, 1, 2)
                assertMatchesCreation<TestRecord>("page", 1, list.page)
                assertMatchesCreation<TestRecord>("perPage", 2, list.perPage)
                assertMatchesCreation<TestRecord>("totalItems", 5, list.totalItems)
                assertMatchesCreation<TestRecord>("totalPages", 3, list.totalPages)

                assertEquals(list.items.size, 2)
                list.items.forEach { record -> assertRecordValid(record) }
            }
            println()
        }
    }

    @Test
    fun getFullList() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val list = service.getFullList<TestRecord>(testCollection, 10)
                assertEquals(list.size, 1)
                list.forEach { record -> assertRecordValid(record) }
            }
            println()
        }
    }

    @Test
    fun delete() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val records = service.getFullList<TestRecord>(testCollection, 10)
                val ids = records.map { it.id }
                ids.forEach { if (it != mainRecordId) service.delete(testCollection, it!!) }

                val isClean = client.records.getFullList<TestRecord>(testCollection, 10).size == 1
                assertTrue(isClean, "The collection should only contain one test record!")
            }
            println()
        }
    }

}