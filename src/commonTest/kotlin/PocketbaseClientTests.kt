package tests

import ADMIN_CREDS
import client
import coroutine
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.dsl.logout
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PocketbaseClientTests {
    @Test
    fun init(): Unit = coroutine {
        val testClient = PocketbaseClient({
            host = "localhost"
            port = 8090
            protocol = URLProtocol.HTTP
        })
        assertEquals("{\"message\":\"API is healthy.\",\"code\":200,\"data\":{}}\n", testClient.httpClient.get("api/health").bodyAsText())
    }

    @Test
    fun authentication(): Unit = coroutine {
        val token = client.records.authWithPassword<AuthRecord>("_superusers", ADMIN_CREDS.email, ADMIN_CREDS.password).token
        client.login(token)
        assertEquals(token, client.authStore.token, "Token is not saved in auth store!")
    }

    @Test
    fun logout(): Unit = coroutine {
        val token = client.records.authWithPassword<AuthRecord>("_superusers", ADMIN_CREDS.email, ADMIN_CREDS.password).token
        client.login(token)
        assertEquals(token, client.authStore.token, "Token is not saved in auth store!")
        client.logout()
        assertNull(client.authStore.token, "Token was not removed from auth store on logout!")
    }
}