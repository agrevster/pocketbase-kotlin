package services

import TestingUtils
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import PocketbaseClient as TestClient

class LogService : TestingUtils() {

    private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)
    private val service = client.logs

    @BeforeTest
    fun before(): Unit = runBlocking {
        launch {
            client.login {
                val login = client.admins.authWithPassword(
                    TestClient.adminLogin.first,
                    TestClient.adminLogin.second
                )
                token = login.token
            }
        }
    }

    @Test
    fun getAll(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response = service.getList(1, 5)
                assertEquals(1, response.page)
                assertEquals(5, response.perPage)
                assertIs<Int>(response.totalItems)
                assertIs<Int>(response.totalPages)
                assertEquals(5, response.items.size)
                printJson(response)
            }
        }
    }

    @Test
    fun getOne(): Unit = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val id = service.getList(1, 1).items[0].id
                val response = service.getOne(id!!)
                printJson(response)
            }
        }
    }

    @Test
    fun getStats() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response = service.getStats()
                assertNotNull(response[0].total)
                assertNotNull(response[0].date)
                printJson(response)
            }
        }
    }

}