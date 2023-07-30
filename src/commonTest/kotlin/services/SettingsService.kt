package services

import TestingUtils
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import PocketbaseClient as TestClient

class SettingsService : TestingUtils() {

    private val client = io.github.agrevster.pocketbaseKotlin.PocketbaseClient(TestClient.url)
    private val service = client.settings
    private var gotSettings: BaseSettings? = null

    @BeforeTest
    fun before() = runBlocking {
        launch {
            client.login {
                val login = client.admins.authWithPassword(
                    TestClient.adminLogin.first, TestClient.adminLogin.second
                )
                token = login.token
            }
            val body = Json.encodeToString(BaseSettings(Meta("testApe", "http://localhost:8090", true)))
            service.update<BaseSettings>(body)
        }
        println()
    }


    @Serializable
    private data class Meta(val appName: String, val appUrl: String, val hideControls: Boolean)

    @Serializable
    private data class BaseSettings(val meta: Meta)

    @Test
    fun getAll() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                val response = service.getAll<BaseSettings>()
                gotSettings = response
                printJson(response)
            }
            println()
        }
    }

    @Test
    fun modify() = runBlocking {
        assertDoesNotFail("No exceptions should be thrown") {
            launch {
                var body = Json.encodeToString(BaseSettings(Meta("testApp", "http://localhost:8090", false)))
                println(body)
                val response = service.update<BaseSettings>(body)
                assertMatchesCreation<BaseSettings>("appName", "testApp", response.meta.appName)
                assertMatchesCreation<BaseSettings>("appUrl", "http://localhost:8090", response.meta.appUrl)
                assertMatchesCreation<BaseSettings>("hideControls", false, response.meta.hideControls)
                body = Json.encodeToString(gotSettings)
                service.update<BaseSettings>(body)
                printJson(response)
            }
            println()
        }
    }
}