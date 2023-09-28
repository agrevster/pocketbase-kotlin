package services

import TestingUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import PocketbaseClient as TestClient

class HealthService : TestingUtils() {

    companion object {
        private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)
    }

    @Test
    fun healthCheck(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                client.health.healthCheck()
            }
        }
    }

}