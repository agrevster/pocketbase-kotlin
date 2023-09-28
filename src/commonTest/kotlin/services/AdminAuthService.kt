package services

import CrudServiceTestSuite
import PocketbaseClient.Companion.adminId
import io.github.agrevster.pocketbaseKotlin.dsl.create
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.dsl.update
import io.github.agrevster.pocketbaseKotlin.models.Admin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import PocketbaseClient as TestClient

class AdminAuthService : CrudServiceTestSuite<Admin>(client.admins, "api/admins") {

    companion object {
        private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)
    }

    private val service = client.admins
    private var testAdminId: String? = null
    private var delete = true

    @BeforeTest
    fun before(): Unit = runBlocking {
        launch {
            client.login {
                val login = client.admins.authWithPassword(
                    TestClient.adminLogin.first, TestClient.adminLogin.second
                )
                token = login.token
            }

            val testAdmin = client.admins.create {
                email = "0.genadmin@test.com"
                avatar = 0
                password = "1234567891"
                passwordConfirm = "1234567891"
            }
            testAdminId = testAdmin.id
        }
    }


    @AfterTest
    fun after(): Unit = runBlocking {
        launch {
            delay(delayAmount)
            if (delete) {
                val admins = service.getFullList<Admin>(10)
                val ids = admins.map { it.id }
                ids.forEach { if (it != adminId) service.delete(it!!) }
                val isClean = service.getFullList<Admin>(10).size == 1
                assertTrue(isClean, "Admins should only contain the test admin!")
            }
        }
    }


    private fun assertAdminValid(admin: Admin) {
        assertNotNull(admin)
        assertNotNull(admin.id)
        assertNotNull(admin.created)
        assertNotNull(admin.updated)
        assertNotNull(admin.email)
        assertNotNull(admin.avatar)
        println(admin)
    }

    @Test
    override fun assertCrudPathValid() {
        super.assertCrudPathValid()
    }

    @Test
    fun assertSkipsTotal() {
        super.checkSkippedTotal<Admin>()
    }

    @Test
    fun authWithPassword(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response = service.authWithPassword(TestClient.adminLogin.first, TestClient.adminLogin.second)
                assertNotNull(response.token)
                assertAdminValid(response.record)
            }
        }
    }

    @Test
    fun refresh(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response = service.authRefresh()
                assertNotNull(response.token)
                assertAdminValid(response.record)
            }
        }
    }

    @Test
    fun create(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val admin = service.create {
                    id = "123456789123478"
                    email = "genadmin@test.com"
                    avatar = 0
                    password = "1234567891"
                    passwordConfirm = "1234567891"
                }
                assertAdminValid(admin)
                assertMatchesCreation<Admin>("email", "genadmin@test.com", admin.email)
                assertMatchesCreation<Admin>("avatar", 0, admin.avatar)
                assertMatchesCreation<Admin>("id", "123456789123478", admin.id)
            }
        }
    }

    @Test
    fun update(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val admin = service.update(testAdminId!!) {
                    email = "1.genadmin@test.com"
                    avatar = 9
                }
                assertAdminValid(admin)
                assertMatchesCreation<Admin>("email", "1.genadmin@test.com", admin.email)
                assertMatchesCreation<Admin>("avatar", 9, admin.avatar)
                assertMatchesCreation<Admin>("id", testAdminId!!, admin.id)
            }
        }
    }

    @Test
    fun getOne(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val admin = service.getOne<Admin>(testAdminId!!)
                assertAdminValid(admin)
                assertMatchesCreation<Admin>("email", "0.genadmin@test.com", admin.email)
                assertMatchesCreation<Admin>("avatar", 0, admin.avatar)
                assertMatchesCreation<Admin>("id", testAdminId, admin.id)
            }

        }
    }

    @Test
    fun getList(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                service.create {
                    password = "123456789123478"
                    passwordConfirm = "123456789123478"
                    email = "2.genadmin@test.com"
                    avatar = 1
                }
                service.create {
                    password = "123456789123478"
                    passwordConfirm = "123456789123478"
                    email = "3.genadmin@test.com"
                    avatar = 2
                }
                service.create {
                    password = "123456789123478"
                    passwordConfirm = "123456789123478"
                    email = "4.genadmin@test.com"
                    avatar = 3
                }
                val list = service.getList<Admin>(1, 2)
                assertMatchesCreation<Admin>("page", 1, list.page)
                assertMatchesCreation<Admin>("perPage", 2, list.perPage)
                assertMatchesCreation<Admin>("totalItems", 5, list.totalItems)
                assertMatchesCreation<Admin>("totalPages", 3, list.totalPages)

                assertEquals(list.items.size, 2)
                list.items.forEach { admin -> assertAdminValid(admin) }
            }

        }
    }

    @Test
    fun getFullList(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val list = service.getFullList<Admin>(10)
                assertEquals(2, list.size)
                println(list)
                list.forEach { admin -> assertAdminValid(admin) }
            }

        }
    }

    @Test
    fun delete(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                delete = false
                val admins = service.getFullList<Admin>(10)
                val ids = admins.map { it.id }
                ids.forEach { if (it != adminId) service.delete(it!!) }
                val isClean = service.getFullList<Admin>(10).size == 1
                assertTrue(isClean, "Admins should only contain the test admin!")
                testAdminId = null
            }

        }
    }
}