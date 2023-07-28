package services

import TestingUtils
import github.agrevster.pocketbaseKotlin.PocketbaseClient
import github.agrevster.pocketbaseKotlin.PocketbaseException
import github.agrevster.pocketbaseKotlin.dsl.create
import github.agrevster.pocketbaseKotlin.dsl.login
import github.agrevster.pocketbaseKotlin.models.User
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds
import PocketbaseClient as TestClient
@Ignore
class BackupsService: TestingUtils() {

    /**
     * IMPORTANT NOTE ABOUT TESTING BACKUPS!
     * Before committing please remove the ignore annotation and run each test top to bottom.
     * If all of them pass, add the ignore annotation back and commit.
     *
     * There have been issues with testing backups automatically in the past.
     * This inconveniences removes any of these issues.
     */


    companion object {
        private val client = PocketbaseClient(TestClient.url)
    }

    private val service = client.backups

    @BeforeTest
    fun before(): Unit = runBlocking {
        launch {
            client.login {
                val login = client.admins.authWithPassword(
                    TestClient.adminLogin.first, TestClient.adminLogin.second
                )
                token = login.token
            }
        }
    }

    @Test
    fun createWithoutName(): Unit = runBlocking {
        assertDoesNotFail {
        launch {
                service.create()
            }
        }
    }

    @Test
    fun createWithName(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                service.create("testbackup.zip")
            }
        }
    }

    @Test
    fun listBackups(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val backups = service.getFullList()
                print(backups)
                assertTrue { backups.size == 2 }
            }
        }
    }

    @Test
    fun getBackupUrl(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val adminFileKey = client.files.generateProtectedFileToken()
                val backupUrl = service.getBackupUrl("testbackup.zip",adminFileKey)
                print(backupUrl)
                val backupFile = client.httpClient.get(backupUrl)
                PocketbaseException.handle(backupFile)
            }
        }
    }

    @Test
    fun restore(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                client.users.create {
                    username = "test_user_2"
                    password = "12345678910!"
                    passwordConfirm = "12345678910!"
                    email = "test_user@test.com"
                    emailVisibility = true
                }
                service.restore("testbackup.zip")
                delay(10.seconds)
                assertFails { client.users.getOne<User>("test_user_2") }
            }
        }
    }


    @Test
    fun delete(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val backups = service.getFullList().map { it.key }
                backups.forEach {backupKey ->
                    service.delete(backupKey)
                }
                assertTrue { service.getFullList().isEmpty() }
            }
        }
    }

}