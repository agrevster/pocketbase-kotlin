package servicies

import client
import coroutine
import loginBefore
import logoutAfter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class CronsServiceTests {

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
    }

    @AfterTest
    fun after(): Unit {
        logoutAfter()
    }


    @Test
    fun list() = coroutine {
        val jobs = client.crons.list()
        jobs.forEach { job ->
            println(job)
            assertNotNull(job.id)
        }
    }

    @Test
    fun run() = coroutine {
        //We just need to make sure this doesn't throw any exceptions
        client.crons.run("__pbLogsCleanup__")
    }

}