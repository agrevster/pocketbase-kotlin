package servicies

import client
import coroutine
import loginBefore
import logoutAfter
import kotlin.test.*

class LogsServiceTests {

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
    }

    @AfterTest
    fun after(): Unit {
        logoutAfter()
    }


    @Test
    fun getAll() = coroutine {
        val logs = client.logs.getList(1, 5)
        assertEquals(5, logs.items.size)
        assertEquals(5, logs.perPage)
        assertEquals(1, logs.page)
    }

    @Test
    fun getOne() = coroutine {
        client.logs.getList(1, 1).items[0].id!!.let { id ->
            val response = client.logs.getOne(id)
            assertNotNull(response.id)
            assertNotNull(response.level)
            assertNotNull(response.message)
            assertNotNull(response.data)
            println(response)
        }
    }

    @Test
    fun getStats() = coroutine {
        val stats = client.logs.getStats()[0]
        println(stats)
    }


}