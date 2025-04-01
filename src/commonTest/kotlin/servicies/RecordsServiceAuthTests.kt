package servicies

import client
import coroutine
import createTestCollection
import deleteTestCollection
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.services.utils.AuthService
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import loginBefore
import logoutAfter
import kotlin.random.Random
import kotlin.test.*

class RecordServiceAuthTests {

    @Serializable
    data class TestAuthRecord(val name: String, val age: Int, val married: Boolean, @Transient val _email: String? = null, @Transient val _emailVisibility: Boolean? = null, @Transient val _verified: Boolean? = null, val password: String? = null, val passwordConfirm: String? = null, val _authRecordId: String? = null) : AuthRecord(_email!!, _emailVisibility!!, _verified!!, _authRecordId) {
        companion object {
            fun generateRandomRecord(emailVisible: Boolean = true): TestAuthRecord {
                val chars = ('A'..'Z') + ('a'..'z') // Uppercase and lowercase letters
                val name = (1..3).map { chars.random() }.joinToString("")
                return TestAuthRecord(name, age = (1..99).random(), Random.nextBoolean(), "${name}@test.com", emailVisible, false, "password12345", "password12345")
            }
        }
    }

    private suspend fun createTestAuthRecord(): TestAuthRecord {
        val record = TestAuthRecord.generateRandomRecord()
        val createdRecord = client.records.create<TestAuthRecord>("test", Json.encodeToString(record))

        if (record.name != createdRecord.name || record.age != createdRecord.age || record.married != createdRecord.married) error("Error creating test record: data does not match!")
        if (record.email != createdRecord.email || record.emailVisibility != createdRecord.emailVisibility || record.verified != createdRecord.verified) error("Error creating test record: auth data does not match!")
        return createdRecord
    }

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        createTestCollection(type = Collection.CollectionType.AUTH)
    }

    @AfterTest
    fun after(): Unit = coroutine {
        deleteTestCollection()
        logoutAfter()
    }

    @Test
    fun create(): Unit = coroutine {
        val record = TestAuthRecord.generateRandomRecord()
        val createdRecord = client.records.create<TestAuthRecord>("test", Json.encodeToString(record))

        assertEquals(record.name, createdRecord.name)
        assertEquals(record.age, createdRecord.age)
        assertEquals(record.married, createdRecord.married)
        assertEquals(record.email, createdRecord.email)
        assertEquals(record.emailVisibility, createdRecord.emailVisibility)
        assertEquals(record.verified, createdRecord.verified)
    }

    @Test
    fun update(): Unit = coroutine {
        val record = createTestAuthRecord()

        val updatedTestRecord = TestAuthRecord(record.name, record.age, !record.married, record.email, record.emailVisibility, record.verified, _authRecordId = record.id!!)
        client.records.update<TestAuthRecord>("test", record.id!!, Json.encodeToString(updatedTestRecord))

        assertEquals(record.name, updatedTestRecord.name)
        assertEquals(record.age, updatedTestRecord.age)
        assertEquals(!record.married, updatedTestRecord.married)
        assertEquals(record.email, updatedTestRecord.email)
        assertEquals(record.emailVisibility, updatedTestRecord.emailVisibility)
        assertEquals(record.verified, updatedTestRecord.verified)
    }

    @Test
    fun delete(): Unit = coroutine {
        val record = createTestAuthRecord()

        client.records.delete("test", record.id!!)
        var found = true

        try {
            client.records.getOne<TestAuthRecord>("test", record.id!!)
        } catch (e: PocketbaseException) {
            if (e.reason.contains("404")) found = false
        }
        assertFalse(found)
    }


    @Test
    fun getFullList(): Unit = coroutine {
        val records = (0..5).map { createTestAuthRecord() }
        assertEquals(records, client.records.getFullList<TestAuthRecord>("test", 10))
    }

    @Test
    fun getList(): Unit = coroutine {
        val records = (0..5).map { createTestAuthRecord() }

        val recordResponse = client.records.getList<TestAuthRecord>("test", 1, 3)

        assertEquals(3, recordResponse.perPage)
        assertEquals(1, recordResponse.page)
        assertEquals(6, recordResponse.totalItems)
        assertEquals(2, recordResponse.totalPages)
        assertEquals(3, recordResponse.items.size)

        recordResponse.items.forEach { record ->
            assertContains(records, record)
        }

    }

    @Test
    fun getOne(): Unit = coroutine {
        val records = (0..2).map { createTestAuthRecord() }

        val record = client.records.getOne<TestAuthRecord>("test", records[1].id!!)

        assertEquals(record, records[1])
    }

    @Test
    fun authWithPassword(): Unit = coroutine {
        val record = createTestAuthRecord()

        val loginRecord = client.records.authWithPassword<TestAuthRecord>("test", record.email, password = "password12345")
        assertEquals(record.name, loginRecord.record.name)
        assertEquals(record.married, loginRecord.record.married)
        assertEquals(record.age, loginRecord.record.age)
        assertEquals(record.email, loginRecord.record.email)
        assertEquals(record.emailVisibility, loginRecord.record.emailVisibility)
        assertEquals(record.verified, loginRecord.record.verified)

        client.login(loginRecord.token)

        // Make sure we are logged in as this user, by default we can't do anything so this is good enough
        assertFailsWith<PocketbaseException> {
            client.records.getOne<TestAuthRecord>("test", loginRecord.record.id!!)
        }

        // We have to log back in as a superuser to that we can clean up
        loginBefore()

    }

    @Test
    fun authRefresh(): Unit = coroutine {
        val record = createTestAuthRecord()

        val loginRecord = client.records.authWithPassword<TestAuthRecord>("test", record.email, password = "password12345")
        assertEquals(record.name, loginRecord.record.name)
        assertEquals(record.married, loginRecord.record.married)
        assertEquals(record.age, loginRecord.record.age)
        assertEquals(record.email, loginRecord.record.email)
        assertEquals(record.emailVisibility, loginRecord.record.emailVisibility)
        assertEquals(record.verified, loginRecord.record.verified)

        client.login(loginRecord.token)

        // Make sure we are logged in as this user, by default we can't do anything so this is good enough
        assertFailsWith<PocketbaseException> {
            client.records.getOne<TestAuthRecord>("test", loginRecord.record.id!!)
        }

        assertEquals(client.records.refresh<TestAuthRecord>("test").token, loginRecord.token)

        // We have to log back in as a superuser to that we can clean up
        loginBefore()

    }

    @Test
    fun listAuthMethods(): Unit = coroutine {
        createTestAuthRecord()

        val authMethods = client.records.listAuthMethods("test")

        assertEquals(AuthService.PasswordAuthInfo(true, listOf("email")), authMethods.password)
        assertEquals(AuthService.OAuth2AuthInfo(false, listOf()), authMethods.oauth2)
        assertEquals(AuthService.GeneralAuthInfo(false, 0), authMethods.otp)
        assertEquals(AuthService.GeneralAuthInfo(false, 0), authMethods.mfa)
    }
}