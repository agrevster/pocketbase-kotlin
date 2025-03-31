package servicies

import client
import coroutine
import loginBefore
import logoutAfter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthServiceTests {

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
    }

    @AfterTest
    fun after(): Unit {
        logoutAfter()
    }


    @Test
    fun checkHealth() = coroutine {
        val health = client.health.healthCheck()
        assertEquals(200, health.code)
        assertEquals("API is healthy.", health.message)
    }


}