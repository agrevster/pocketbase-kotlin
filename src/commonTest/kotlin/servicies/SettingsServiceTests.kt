package servicies

import client
import coroutine
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import loginBefore
import logoutAfter
import kotlin.test.*

class SettingsServiceTests {

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
    }

    @AfterTest
    fun after(): Unit {
        logoutAfter()
    }

    @Test
    fun getAll(): Unit = coroutine {
        val settings = client.settings.getAll<String>()
        assertContains(settings, "logs")
    }

    @Test
    fun update(): Unit = coroutine {

        @Serializable
        data class AppSettings(val appName: String)

        @Serializable
        data class AppMeta(val meta: AppSettings)

        val originalSettings = client.settings.getAll<JsonElement>()


        assertNotNull(originalSettings.jsonObject.get("meta")?.jsonObject)
        assertNotNull(originalSettings.jsonObject.get("smtp")?.jsonObject)
        assertNotNull(originalSettings.jsonObject.get("backups")?.jsonObject)
        assertNotNull(originalSettings.jsonObject.get("s3")?.jsonObject)
        assertNotNull(originalSettings.jsonObject.get("rateLimits")?.jsonObject)
        assertNotNull(originalSettings.jsonObject.get("trustedProxy")?.jsonObject)
        assertNotNull(originalSettings.jsonObject.get("batch")?.jsonObject)
        assertNotNull(originalSettings.jsonObject.get("logs")?.jsonObject)

        val updatedSettings = buildJsonObject {
            for (jsonObj in originalSettings.jsonObject) {
                if (jsonObj.key != "meta") put(jsonObj.key, jsonObj.value)
            }
            putJsonObject("meta") {
                for (jsonObj in originalSettings.jsonObject.get("meta")!!.jsonObject) {
                    if (jsonObj.key != "appName") put(jsonObj.key, jsonObj.value)
                }
                put("appName", JsonPrimitive("test"))
            }
        }

        assertEquals("test", client.settings.update<AppMeta>(Json.encodeToString(updatedSettings), ShowFields("meta.appName")).meta.appName)

        assertEquals(
            originalSettings.jsonObject.get("meta")?.jsonObject?.get("appName")?.jsonPrimitive!!.content, client.settings.update<AppMeta>(
                Json.encodeToString(originalSettings), ShowFields(
                    "meta.appName"
                )
            ).meta.appName
        )


    }

}