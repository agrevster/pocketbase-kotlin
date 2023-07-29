package services

import CrudServiceTestSuite
import PocketbaseClient.Companion.testUserID
import github.agrevster.pocketbaseKotlin.dsl.create
import github.agrevster.pocketbaseKotlin.dsl.login
import github.agrevster.pocketbaseKotlin.dsl.update
import github.agrevster.pocketbaseKotlin.models.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import PocketbaseClient as TestClient


class UserAuthService : CrudServiceTestSuite<User>(client.users, "api/collections/users/records") {
    companion object {
        private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)
    }

    private val service = client.users
    private var recordId: String? = null
    private var delete = true

    @BeforeTest
    fun before() = runBlocking {
        launch {
            client.login {
                val login = client.users.authWithUsername(
                    TestClient.userLogin.first.first, TestClient.userLogin.second
                )
                token = login.token
            }
            val user = client.users.create {
                password = "12345678"
                passwordConfirm = "12345678"
                username = "pocket_kt_test_gen"
                email = "0.genuser@test.com"
                emailVisibility = false
                id = "123456789123478"
            }
            recordId = user.id
            delete = true
        }
        println()
    }

    @AfterTest
    fun after() = runBlocking {
        launch {
            if (delete) {
                val users = service.getFullList<User>(10)
                val ids = users.map { it.id }
                ids.forEach { if (it != testUserID) service.delete(it!!) }

                val isClean = service.getFullList<User>(10).size == 1
                assertTrue(isClean, "Users should only contain the test user!")
            }
        }
        println()
    }


    private fun assertUserValid(user: User) {
        assertNotNull(user)
        assertNotNull(user.id)
        assertNotNull(user.collectionId)
        assertNotNull(user.collectionName)
        assertNotNull(user.created)
        assertNotNull(user.updated)
        assertNotNull(user.username)
        assertNotNull(user.verified)
        assertNotNull(user.emailVisibility)
        println(user)
    }

    @Test
    override fun assertCrudPathValid() {
        super.assertCrudPathValid()
    }

    @Test
    fun authWithPassword() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response = service.authWithPassword(TestClient.userLogin.first.first, TestClient.userLogin.second)
                assertNotNull(response.token)
                assertUserValid(response.record)
            }
            println()
        }
    }

    @Test
    fun authWithUsername() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response = service.authWithUsername(TestClient.userLogin.first.second, TestClient.userLogin.second)
                assertNotNull(response.token)
                assertUserValid(response.record)
            }
            println()
        }
    }


    @Test
    fun refresh() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response = service.refresh()
                assertNotNull(response.token)
                assertUserValid(response.record)
            }
            println()
        }
    }


    @Test
    fun listAuthMethods() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response = service.listAuthMethods("users")
                assertNotNull(response.emailPassword)
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
                val user = service.create {
                    id = "123456789123498"
                    password = "12345678"
                    passwordConfirm = "12345678"
                    username = "pocket_kt_test"
                    email = "1.genuser@test.com"
                    emailVisibility = false
                }
                assertUserValid(user)
                assertMatchesCreation<User>("username", "pocket_kt_test", user.username)
                assertMatchesCreation<User>("emailVisibility", false, user.emailVisibility)
                assertMatchesCreation<User>("id", "123456789123498", user.id)
            }
        }
        println()
    }


    @Test
    fun update() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val user = service.update(recordId!!) {
                    username = "pocket_kt_test1"
                    emailVisibility = true
                }
                assertUserValid(user)
                assertMatchesCreation<User>("username", "pocket_kt_test1", user.username)
                assertMatchesCreation<User>("emailVisibility", true, user.emailVisibility)
                assertMatchesCreation<User>("id", "123456789123478", user.id)
                assertMatchesCreation<User>("email", "0.genuser@test.com", user.email)
            }
            println()
        }
    }

    @Test
    fun getOne() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val user = service.getOne<User>(recordId!!)
                assertUserValid(user)
                assertMatchesCreation<User>("username", "pocket_kt_test_gen", user.username)
                assertMatchesCreation<User>("emailVisibility", false, user.emailVisibility)
                assertMatchesCreation<User>("id", "123456789123478", user.id)
            }
            println()
        }
    }

    @Test
    fun getList() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                service.create {
                    password = "12345678"
                    passwordConfirm = "12345678"
                    username = "pocket_kt_test2"
                    email = "2.genuser@test.com"
                    emailVisibility = true
                }
                service.create {
                    password = "12345678"
                    passwordConfirm = "12345678"
                    username = "pocket_kt_test3"
                    email = "3.genuser@test.com"
                    emailVisibility = true
                }
                service.create {
                    password = "12345678"
                    passwordConfirm = "12345678"
                    username = "pocket_kt_test4"
                    email = "4.genuser@test.com"
                    emailVisibility = true
                }
                val list = service.getList<User>(1, 2)
                assertMatchesCreation<User>("page", 1, list.page)
                assertMatchesCreation<User>("perPage", 2, list.perPage)
                assertMatchesCreation<User>("totalItems", 5, list.totalItems)
                assertMatchesCreation<User>("totalPages", 3, list.totalPages)

                assertEquals(list.items.size, 2)
                list.items.forEach { user -> assertUserValid(user) }
            }
            println()
        }
    }

    @Test
    fun getFullList() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val list = service.getFullList<User>(10)
                assertEquals(list.size, 2)
                list.forEach { user -> assertUserValid(user) }
            }
            println()
        }
    }

    @Test
    fun delete() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                delete = false
                val users = service.getFullList<User>(10)
                val ids = users.map { it.id }
                ids.forEach { if (it != testUserID) service.delete(it!!) }

                val isClean = service.getFullList<User>(10).size == 1
                assertTrue(isClean, "Users should only contain the test user!")
            }
            println()
        }
    }

}