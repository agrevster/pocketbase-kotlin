package servicies

import client
import coroutine
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import loginBefore
import logoutAfter
import kotlin.test.*

/**
 * IMPORTANT NOTE ABOUT TESTING BACKUPS! Before committing please remove
 * the ignore annotation and run each test top to bottom. If all of them
 * pass, add the ignore annotation back and commit.
 *
 * There have been issues with testing backups automatically in the past.
 * This inconveniences removes any of these issues.
 */
@Ignore
class BackupServiceTests {

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
    }

    @AfterTest
    fun after(): Unit {
        logoutAfter()
    }


    @Test
    fun createWithoutName() = coroutine {
        client.backups.create()
    }

    @Test
    fun createWithName() = coroutine {
        client.backups.create("testbackup.zip")
    }

    @Test
    fun listBackups() = coroutine {
        client.backups.getFullList().let { backupList ->
            assertEquals(2, backupList.size)
        }
    }

    @Test
    fun getBackupUrl() = coroutine {
        val adminFileKey = client.files.generateProtectedFileToken()
        val backupUrl = client.backups.getBackupUrl("testbackup.zip", adminFileKey)
        print(backupUrl)
        val backupFile = client.httpClient.get(backupUrl)
        PocketbaseException.handle(backupFile)
    }

    @Test
    fun restoreBackup() = coroutine {
        @Serializable
        data class CreateUser(val password: String, val passwordConfirm: String, @Transient val _email: String? = null, @Transient val _emailVisibility: Boolean? = null, @Transient val _verified: Boolean? = null) : AuthRecord(_email!!, _emailVisibility!!, _verified!!)
        client.records.create<AuthRecord>("users", Json.encodeToString(CreateUser("password12345", "password12345", "user@test.com", false, false)))
        delay(2000)
        client.backups.restore("testbackup.zip")
        delay(10000)
        assertEquals(0, client.records.getFullList<AuthRecord>("users", 3).size)
    }

    @Test
    fun delete() = coroutine {
        val backups = client.backups.getFullList().map { it.key }
        backups.forEach { backupKey ->
            client.backups.delete(backupKey)
        }
        assertTrue { client.backups.getFullList().isEmpty() }
    }

}