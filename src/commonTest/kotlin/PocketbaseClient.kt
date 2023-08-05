import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

class PocketbaseClient {

    @Test
    fun loginUser() = runBlocking {
        val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(url)
        launch {
            client.login {
                val login = client.users.authWithPassword(userLogin.first.first, userLogin.second)
                token = login.token
                println(login.token)
            }
            assertNotNull(client.authStore.token, "Auth store token should not be null")
        }
        println()
    }

    @Test
    fun loginAdmin() = runBlocking {
        val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(url)
        launch {
            client.login {
                val login = client.admins.authWithPassword(adminLogin.first, adminLogin.second)
                token = login.token
                println(login.token)
            }
            assertNotNull(client.authStore.token, "Auth store token should not be null")
        }
        println()
    }

    companion object {
        val url: URLBuilder.() -> Unit = {
            protocol = URLProtocol.HTTP
            host = "localhost"
            port = 8090
        }

        //                        EMAIL                USERNAME      PASSWORD
        val userLogin = ("user@test.com" to "test_user") to "test12345!"
        val testUserID = "mxnoq6vpgskqppj"

        //                         EMAIL               PASSWORD
        val adminLogin = "admin@test.com" to "test12345!"
        val adminId = "9pnyyihlffqpr1c"
    }

}